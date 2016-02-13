/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Record;
import cz.klajmajk.entities.Session;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
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

    public List<Record> findForChart(Session session, String paramName) {
        Object result = getEntityManager().createNativeQuery("SELECT reltuples::bigint AS estimate FROM pg_class where relname='record'").getSingleResult();
        int countEstimate = Math.toIntExact((Long) result);
        result = getEntityManager().createNativeQuery("select count (*) from (select distinct name from record) as alias").getSingleResult();
        int namesCount = Math.toIntExact((Long) result);
        countEstimate = countEstimate/namesCount;
        
        Query q = getEntityManager().createNativeQuery("select alias.id, alias.datetime, alias. type, alias.val, alias.name \n"
                + "from (\n"
                + "	select (row_number() over ()) as rn, r.id, r.datetime, r. type, r.val, r.name \n"
                + "	from record r\n"
                + "	where r.name = '"+paramName+"'\n"
                + "	)as alias \n"
                + "where mod(alias.rn,"+countEstimate/100 +")=0",Record.class);
        List<Record> records = q.getResultList();
        return records;

    }

}