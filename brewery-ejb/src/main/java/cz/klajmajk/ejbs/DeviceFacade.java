/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Device;
import cz.klajmajk.scheduler.SchedulerBean;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Adam
 */
@Stateless
@LocalBean
public class DeviceFacade extends AbstractFacade<Device> {
    @PersistenceContext(unitName = "cz.klajmajk_brewery")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DeviceFacade() {
        super(Device.class);
    }

    @Override
    public void remove(Device entity) {
        super.remove(entity); 
    }

    @Override
    public void edit(Device entity) {
        super.edit(entity);    
    }

    @Override
    public void create(Device entity) {
        super.create(entity);
    }
    
    

    
    
}
