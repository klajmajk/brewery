/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.scheduler;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Adam
 */
public class TimerIdentificator implements Serializable{
    private final IdentyficatorTypeEnum type;
    private final Long deviceId;

    public TimerIdentificator(IdentyficatorTypeEnum type, Long deviceId) {
        this.type = type;
        this.deviceId = deviceId;
    }

    public IdentyficatorTypeEnum getType() {
        return type;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return "TimerIdentificator{" + "type=" + type + ", deviceId=" + deviceId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.deviceId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimerIdentificator other = (TimerIdentificator) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.deviceId, other.deviceId)) {
            return false;
        }
        return true;
    }
    
    
       
    
    
    
}
