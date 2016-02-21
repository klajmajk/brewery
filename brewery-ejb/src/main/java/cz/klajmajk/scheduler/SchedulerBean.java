/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.scheduler;

import cz.klajmajk.ejbs.DeviceFacade;
import cz.klajmajk.ejbs.NetworkBean;
import cz.klajmajk.ejbs.RestConsumerBean;
import cz.klajmajk.ejbs.SessionFacade;
import cz.klajmajk.entities.Device;
import cz.klajmajk.entities.Session;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

/**
 *
 * @author Adam
 */
@Startup
@Singleton
public class SchedulerBean {

    @Resource
    private TimerService timerService;
    @Inject
    private RestConsumerBean restConsumerBean;
    @Inject
    private DeviceFacade deviceFacade;
    @Inject
    private SessionFacade sessionFacade;
    @Inject
    private NetworkBean networkBean;

    @PostConstruct
    public void init() {
        List<Device> devices = deviceFacade.findAll();
        for (Timer timer : timerService.getTimers()) {
            timer.cancel();
        }
        for (Device device : devices) {
            TimerIdentificator ti = new TimerIdentificator(IdentyficatorTypeEnum.REFRESH, device.getId());
            System.out.println("Initing timer: "+ti+" refresh: "+device.getRefreshRate());
            timerService.createTimer(1000, device.getRefreshRate() * 1000, ti);
        }
        for (Session session : sessionFacade.findAll()) {            
            TimerIdentificator ti = new TimerIdentificator(IdentyficatorTypeEnum.PERSIT, session.getDevice().getId());
            System.out.println("Initing timer: "+ti+" refresh: "+session.getDevice().getPersistRate());
            timerService.createTimer(session.getDevice().getPersistRate() * 1000, session.getDevice().getPersistRate() * 1000, ti);
        }

    }

    @Timeout
    public void execute(Timer timer) {
        TimerIdentificator ti = (TimerIdentificator) timer.getInfo();
        if (ti.getType() == IdentyficatorTypeEnum.REFRESH) {
            try {
                restConsumerBean.handleRefresh(ti.getDeviceId());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (ti.getType() == IdentyficatorTypeEnum.PERSIT) {
            try {
                System.out.println("Persisting: "+ti.toString());
                networkBean.handlePersist(ti.getDeviceId());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

}
