/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.ejbs;

import cz.klajmajk.entities.Device;
import cz.klajmajk.entities.Record;
import cz.klajmajk.entities.RecordTypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Adam
 */
@Singleton
public class RestConsumerBean {  

    @Inject
    private DeviceFacade deviceFacade;   
    @Inject
    private NetworkBean networkBean;

    private void handleJsonPair(String key, JsonValue get, Device device) {
        RecordTypeEnum type;
        if (key.contains("temp")) {
            type = RecordTypeEnum.TEMPERATURE;
        } else if (key.contains("On")) {
            type = RecordTypeEnum.STATE;
        } else {
            type = RecordTypeEnum.UNKNOWN;
        }
        Record record = new Record(type, new Double(get.toString()), key, null);
        System.out.println(record);
        handleRecord(record, device);

    }

    private void handleRecord(Record record, Device device) {
        Record recToDelete = null;
        List<Record> lastRecords = networkBean.getRecordsByDevice().get(device);
        if (lastRecords == null) {
            lastRecords = new ArrayList<>();
            networkBean.getRecordsByDevice().put(device, lastRecords);
        }
        
        for (Record lastRecord : lastRecords) {
            if (lastRecord.getName().equals(record.getName())) {
                recToDelete = lastRecord;
            }
        }
        if (recToDelete != null) {
            lastRecords.remove(recToDelete);
        }
        lastRecords.add(record);
    }

    

    @Asynchronous
    public void handleRefresh(Long deviceId) {
        Device device = deviceFacade.find(deviceId);
        try {
            List<String> records = new ArrayList<>();
            Client client = ClientBuilder.newClient();
            client.property("jersey.config.client.connectTimeout", 1000);
            client.property("jersey.config.client.readTimeout", 100);
            WebTarget target = client.target(device.getAddress());
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) { // 200
                JsonObject jsonObj = response.readEntity(JsonObject.class);
                System.out.println("Received:");
                Set<String> keys = jsonObj.keySet();

                for (String key : keys) {
                    handleJsonPair(key, jsonObj.get(key), device);
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
