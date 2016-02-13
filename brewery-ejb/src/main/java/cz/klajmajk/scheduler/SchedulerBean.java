/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.klajmajk.scheduler;

import cz.klajmajk.ejbs.RestConsumerBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 *
 * @author Adam
 */
@Singleton
public class SchedulerBean {

    @Inject
    private RestConsumerBean restConsumerBean;

    //@Schedule(second = "*/30", minute = "*", hour = "*")

    @Schedule(minute = "*/1", hour = "*")
    public void atSchedule() throws InterruptedException {
        try {

            System.out.println("Scheduler called");
            restConsumerBean.handleSchedulerEvent();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
