package cz.klajmajk;

import cz.klajmajk.ejbs.NetworkBean;
import cz.klajmajk.ejbs.RecordFacade;
import cz.klajmajk.ejbs.RestConsumerBean;
import cz.klajmajk.entities.Session;
import cz.klajmajk.ejbs.SessionFacade;
import cz.klajmajk.entities.Record;
import cz.klajmajk.util.JsfUtil;
import cz.klajmajk.util.PaginationHelper;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.units.JustNow;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

@Named("sessionController")
@SessionScoped
public class SessionController implements Serializable {

    private LineChartModel model = null;

    private Session current;
    private DataModel items = null;
    @Inject
    private cz.klajmajk.ejbs.SessionFacade ejbFacade;
    @Inject
    private RecordFacade recordFacade;
    @Inject
    private RestConsumerBean restBean;
    @Inject
    private NetworkBean networkBean;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public SessionController() {
    }

    public String timeAgo(Date date) {
        if (date != null) {
            PrettyTime p = new PrettyTime();
            for (TimeUnit t : p.getUnits()) {
                if (t instanceof JustNow) {
                    ((JustNow) t).setMaxQuantity(1000L);
                }
            }
            String toReturn = p.format(date);
            if (toReturn.equals("před chvílí")) {
                return "teď";
            }
            return p.format(date);
        } else {
            return "";
        }
    }

    public Record getCurrentMeasuredRecord(long id) {
        Record record = networkBean.getCurrentByName("tempMeasured", id);
        return record;

    }

    public List<Session> getActiveSessions() {
        List<Session> arrayList = new ArrayList<>();
        for (Session s : ejbFacade.findAll()) {
            if (s.isActive()) {
                arrayList.add(s);
            }
        }
        return arrayList;

    }

    public Session getSelected() {
        if (current == null) {
            current = new Session();
            selectedItemIndex = -1;
        }
        return current;
    }

    private SessionFacade getFacade() {
        return (SessionFacade) ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Session) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Session();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current);
            JsfUtil.addSuccessMessage("SessionCreated");
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "PersistenceErrorOccured");
            return null;
        }
    }

    public String prepareEdit() {
        current = (Session) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("SessionUpdated");
            recreateModel();
            return "List";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "PersistenceErrorOccured");
            return null;
        }
    }

    public String destroy() {
        current = (Session) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("SessionDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Session getSession(java.lang.Long id) {
        return ejbFacade.find(id);
    }

    private LineChartModel createDateModel(Session s) {

        LineChartModel dateModel = new LineChartModel();
        dateModel.setExtender("extender");

        LineChartSeries tempSetSerie = new LineChartSeries();
        tempSetSerie.setLabel("tempSet");
        LineChartSeries tempMeasuredSerie = new LineChartSeries();
        tempMeasuredSerie.setLabel("tempMeasured");
        LineChartSeries systemOnSerie = new LineChartSeries();
        systemOnSerie.setLabel("systemOn");

        List<Record> records = recordFacade.findForChart(s, "tempSet");

        DateFormat chartFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        for (Record record : records) {
            tempSetSerie.set(chartFormatter.format(record.getDatetime()), record.getVal());
        }
        records = recordFacade.findForChart(s, "tempMeasured");
        for (Record record : records) {
            if (record.getVal() != -127) {
                tempMeasuredSerie.set(chartFormatter.format(record.getDatetime()), record.getVal());
            }
        }

        dateModel.addSeries(tempSetSerie);
        dateModel.addSeries(tempMeasuredSerie);
        dateModel.addSeries(systemOnSerie);
        dateModel.getAxis(AxisType.Y).setLabel("Temp");
        DateAxis axis = new DateAxis("Time");
        axis.setTickAngle(-50);
        axis.setTickFormat("%a %d. %m. %y %H:%#M:%S");
        dateModel.getAxes().put(AxisType.X, axis);
        return dateModel;
    }

    public LineChartModel getDateModel() {
        return dateModel(current);

    }

    public LineChartModel dateModel(Session s) {
        if (s != null) {
            model = createDateModel(s);
            return model;
        }
        return null;
    }

    @FacesConverter(forClass = Session.class)
    public static class SessionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            SessionController controller = (SessionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "sessionController");
            return controller.getSession(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Session) {
                Session o = (Session) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Session.class.getName());
            }
        }

    }

}
