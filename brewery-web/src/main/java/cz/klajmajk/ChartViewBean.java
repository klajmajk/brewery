/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk;

import cz.klajmajk.entities.Session;
import java.io.Serializable;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.LineChartModel;

/**
 *
 * @author Adam
 */
@Named("chartViewBean")
@ViewScoped
public class ChartViewBean implements Serializable {
    @Inject
    private SessionController sessionBean;
    private HashMap<Session, LineChartModel> map;
    
    
    @PostConstruct
    public void init(){
        map = new HashMap<>();
    }
    
    public LineChartModel getChartModel(){
        return chartModel(sessionBean.getSelected());
    }
    
    public LineChartModel chartModel(Session s){
        if(map.get(s) == null){
            map.put(s, sessionBean.dateModel(s));
        }
        return map.get(s);
    }

}
