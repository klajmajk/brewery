/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Device;
import cz.klajmajk.entities.Record;
import cz.klajmajk.entities.Session;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.Root;

/**
 *
 * @author Adam
 */
@Stateless
@LocalBean
public class RecordFacade extends AbstractFacade<Record> {

    @PersistenceContext(unitName = "cz.klajmajk_brewery")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public RecordFacade() {
        super(Record.class);
    }

    @Override
    public List<Record> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root c = cq.from(entityClass);
        cq.select(c);
        cq.orderBy(getEntityManager().getCriteriaBuilder().desc(c.get("datetime")));

        return getEntityManager().createQuery(cq).getResultList();
    }

    @Override
    public List<Record> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root c = cq.from(entityClass);
        cq.select(c);
        cq.orderBy(getEntityManager().getCriteriaBuilder().desc(c.get("datetime")));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    
    public List<String> getAllRecordNamesPerSession(Session session){
        Query q = getEntityManager().createNativeQuery("select name from (select distinct r.name from record r where r.session_id = "+session.getId()+") as alias");
        return q.getResultList();
    }

    public List<Record> findForChart(Session session, String paramName) {
        //Object result = getEntityManager().createNativeQuery("SELECT reltuples::bigint AS estimate FROM pg_class where relname='record'").getSingleResult();
        
        Object result = getEntityManager().createNativeQuery("select count(r.id) from record r where r.session_id= "+session.getId()+"").getSingleResult();
        int countEstimate = Math.toIntExact((Long) result);
        result = getEntityManager().createNativeQuery("select count (*) from (select distinct r.name from record r where r.session_id = "+session.getId()+") as alias").getSingleResult();
        int namesCount = Math.toIntExact((Long) result);
        if(namesCount == 0) countEstimate = 0;
        else countEstimate = countEstimate / namesCount;
        int modBase = countEstimate / 200;
        if (modBase == 0) modBase = 1;

        Query q = getEntityManager().createNativeQuery("select alias.id, alias.datetime, alias.type, alias.val, alias.name \n"
                + "from (\n"
                + "	select (row_number() over ()) as rn, r.id, r.datetime, r. type, r.val, r.name \n"
                + "	from record r\n"
                + "	where r.name = '" + paramName + "' and r.session_id = "+ session.getId()+"\n"
                + "	)as alias \n"
                + "where mod(alias.rn," + modBase + ")=0", Record.class);
        List<Record> records = q.getResultList();
        return records;

    }

    public Record getLastRecord(String name, Device device) {
        try {
            Object result = getEntityManager().createNativeQuery("SELECT record.* FROM record INNER JOIN session ON record.session_id = session.id WHERE record.name = '" + name + "' AND session.device_id = " + device.getId() + " ORDER BY record.datetime DESC LIMIT 1", Record.class).getSingleResult();
            Record record = (Record) result;
            return record;
        } catch (NoResultException e) {
            return null;
        }

    }

}
