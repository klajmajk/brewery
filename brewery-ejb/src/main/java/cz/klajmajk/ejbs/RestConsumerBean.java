/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Record;
import cz.klajmajk.entities.RecordTypeEnum;
import cz.klajmajk.entities.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Adam
 */
@Stateless
public class RestConsumerBean {

    @PersistenceContext(unitName = "cz.klajmajk_brewery")
    private EntityManager em;

    @Inject
    private cz.klajmajk.ejbs.SessionFacade sessionFacade;

    private final List<Record> lastRecords = new ArrayList<>();

    public void handleSchedulerEvent() {
        for (Session session : sessionFacade.findAll()) {
            if (session.isActive()) {
                try {
                    List<String> records = new ArrayList<>();
                    Client client = ClientBuilder.newClient();
                    WebTarget target = client.target(session.getDevice().getAddress());
                    Response response = target.request(MediaType.APPLICATION_JSON).get();
                    if (response.getStatus() == Response.Status.OK.getStatusCode()) { // 200
                        JsonObject jsonObj = response.readEntity(JsonObject.class);
                        System.out.println("Received:");
                        Set<String> keys = jsonObj.keySet();

                        for (String key : keys) {
                            handleJsonPair(key, jsonObj.get(key), session);
                            //System.out.println("Key: " + key + " value: " + jsonObj.get(key));
                        }
                        // do stuff
                    } else if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) { // 500
                        System.out.println("receive exception");
                        // do stuff
                    } // 
                } catch (Exception e) {
                    System.out.println("Connection error");
                }

            }
        }
    }

    private void handleJsonPair(String key, JsonValue get, Session session) {
        RecordTypeEnum type;
        if (key.contains("temp")) {
            type = RecordTypeEnum.TEMPERATURE;
        } else if (key.contains("On")) {
            type = RecordTypeEnum.STATE;
        } else {
            type = RecordTypeEnum.UNKNOWN;
        }
        Record record = new Record(type, new Double(get.toString()), key, session);
        handleRecord(record);
        System.out.println("adding record: " + record);
        em.persist(record);
        em.flush();

    }

    private void handleRecord(Record record) {
        Record recToDelete = null;
        for (Record lastRecord : lastRecords) {
            if ((lastRecord.getName().equals(record.getName())) && (lastRecord.getSession().equals(record.getSession()))) {
                recToDelete = lastRecord;
            }
        }
        if (recToDelete != null) {
            lastRecords.remove(recToDelete);
        }

        lastRecords.add(record);
    }

    public Record getCurrentByName(String name) {
        for (Record lastRecord : lastRecords) {
            if (lastRecord.getName().equals(name)) {
                return lastRecord;
            }
        }
        return null;
    }

}
