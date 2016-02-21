/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Device;
import cz.klajmajk.entities.Record;
import cz.klajmajk.entities.Session;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Adam
 */
@Singleton
@Stateful
public class NetworkBean {
    
    @PersistenceContext(unitName = "cz.klajmajk_brewery")
    private EntityManager em;
    
     @Inject
    private SessionFacade sessionFacade;
    @Inject
    private RecordFacade recordFacade;
    @Inject
    private DeviceFacade deviceFacade;

    private final HashMap<Device, List<Record>> recordsByDevice = new HashMap<>();
    
    public Record getCurrentByName(String name, long deviceId) {
        Device d =deviceFacade.find(deviceId);
        List<Record> lastRecords = recordsByDevice.get(d);
        if (lastRecords != null) {
            for (Record lastRecord : lastRecords) {
                if (lastRecord.getName().equals(name)) {
                    return lastRecord;
                }
            }
        }
        Record lastFormDB = recordFacade.getLastRecord(name, d);
        return lastFormDB;
    }

    public HashMap<Device, List<Record>> getRecordsByDevice() {
        return recordsByDevice;
    }
    
    
    @Asynchronous
    public void handlePersist(Long deviceId) {
        for (Session session : sessionFacade.findAll()) {
            if ((session.isActive()) && (session.getDevice().getId().equals(deviceId))) {

                List<Record> lastRecords = getRecordsByDevice().get(session.getDevice());
                if (lastRecords != null) {
                    for (Record lastRecord : lastRecords) {
                        System.out.println("Persisting: " + lastRecord);
                        lastRecord.setSession(session);
                        em.persist(lastRecord);
                    }
                    em.flush();
                    getRecordsByDevice().put(session.getDevice(), null);
                }
            }
        }
    }
    
    
    
}
