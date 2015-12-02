/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.mobicall;

import java.text.SimpleDateFormat;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 */
public final class MobicallAlarmsPublisher implements AlarmListener {

    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarmsPublisher.class);
    @SuppressWarnings("unused")
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY HH:MM:SS");

    private C2monConnectionIntf c2mon;
    private MobicallConfigLoaderIntf loader;
    private SenderIntf sender;
    
 
    private int eventCounter = 0;
    
    private static Thread configuratorThread;
    private static MobicallConfigurator configurator;

    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public MobicallAlarmsPublisher(C2monConnectionIntf c2mon, MobicallConfigLoaderIntf loader, SenderIntf sender) throws Exception {
        LOG.info("Building publisher instance ...");
        this.loader = loader;
        this.c2mon = c2mon;
        this.sender = sender;
        LOG.info("Publisher ready.");
    }
    
    //
    // --- PUBLIC METHODS ---------------------------------------------------------------
            
    public static String getAlarmId(AlarmValue av) {
        return av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
    }
    
    public void connect() {
        try {
            LOG.info("Starting Mobicall publisher");
            sender.setup();
            c2mon.start();
            
            configurator = new MobicallConfigurator(loader);
            configuratorThread = new Thread(configurator);
            configuratorThread.start();

            Collection<AlarmValue> activeAlarms = c2mon.getActiveAlarms();
            LOG.info("... now listening to incoming alarms.");

            c2mon.setListener(this);
            c2mon.connectListener();
            for (AlarmValue av : activeAlarms) {
                this.onAlarmUpdate(av);
            }
            LOG.info("Started with initial selection of " + activeAlarms.size() + " alarms.");
        } catch (Exception e) {
            LOG.error("A major problem occured while running the Publisher. Stopping now!", e);
            throw new RuntimeException(e);
        }
    }

    public void close() {        
        LOG.info("Stopping C2MON ...");
        c2mon.stop();
        LOG.info("Stopping configurator ...");
        configurator.stop();
        try {
            configuratorThread.join(MobicallConfigurator.LATENCY * 1000);
        } catch (Exception ie) {
            // exit anyway!
        }
        synchronized (this) {
            this.notifyAll();
        }
        LOG.info("Publisher stopped.");
    }

    //
    // --- Implements AlarmListener -----------------------------------------------------------
    //
    @Override
    public void onAlarmUpdate(AlarmValue av) {
        String alarmId = getAlarmId(av);
        LOG.debug(" RECEIVED    > " + alarmId + " is active:" + av.isActive());        
        MobicallAlarm ma = loader.find(alarmId);
        if (ma != null) {
            if (c2mon.isTagValid(av.getTagId())) {
                sender.send(ma.getMobicallId(), composeTrapMessage(ma, av));
            } else {
                LOG.warn("Quality of alarm " + alarmId + " is not valid & existing, no notification sent");                
            }
        }
        LOG.debug(" PROCESSED    > " + alarmId);
        eventCounter++;
        if (eventCounter >= 1000) {
            LOG.info("Processed 1000 alarm events since last message.");
            eventCounter = 0;
        }
    }

    
    //
    // --- PRIVATE -------------------------------------------------------------------------------
    //

  /**
   * Create the value of the message field in the SNMP trap
   * 
   * @param alarm <code>Alarm</code> objet used to fill the message placeholders
   * @param av <code>AlarmValue</code>
   * @return <code>String</code> the value to be assigned to the "message" OID in the SNMP trap
   */
  public static String composeTrapMessage(MobicallAlarm alarm, AlarmValue av) {
      StringBuffer message = new StringBuffer();
      message.append(alarm.getSystemName());
      message.append(" ");
      message.append(alarm.getIdentifier());
      message.append(" ");
      message.append(alarm.getFaultCode() + " [" + alarm.getMobicallId() + "] ");
      message.append(" ");
      message.append(alarm.getProblemDescription());
      message.append(" ");
      message.append(av.isActive() ? "ACTIVE" : "TERMINATE");
      return message.toString();
  }

}
    

