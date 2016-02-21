/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Adam
 */
@Entity
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String address;
    private int refreshRate;
    private int persistRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device() {
    }

    public Device(String name, String address, int refreshRate, int persistRate) {
        this.name = name;
        this.address = address;
        this.refreshRate = refreshRate;
        this.persistRate = persistRate;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public int getPersistRate() {
        return persistRate;
    }

    public void setPersistRate(int persistRate) {
        this.persistRate = persistRate;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.id);
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + Objects.hashCode(this.address);
        hash = 73 * hash + this.refreshRate;
        hash = 73 * hash + this.persistRate;
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
        final Device other = (Device) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        if (this.refreshRate != other.refreshRate) {
            return false;
        }
        if (this.persistRate != other.persistRate) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Device{" + "id=" + id + ", name=" + name + ", address=" + address + ", refreshRate=" + refreshRate + ", persistRate=" + persistRate + '}';
    }
    
    
}
