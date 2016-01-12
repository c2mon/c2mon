/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.publisher.mobicall;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 */
public final class MobicallAlarmsPublisher implements AlarmListener {

    public static final int DEFAULT_RESEND_ON_START = 60;
    
    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarmsPublisher.class);

    private C2monConnectionIntf c2mon;
    private MobicallConfigLoaderIntf loader;
    private SenderIntf sender;
    
 
    private int eventCounter = 0;
    
    private static Thread configuratorThread;
    private static MobicallConfigurator configurator;

    private int resendDelayOnStart = DEFAULT_RESEND_ON_START; // in seconds
    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public MobicallAlarmsPublisher(C2monConnectionIntf c2mon, MobicallConfigLoaderIntf loader, SenderIntf sender) throws Exception {
        LOG.info("Building publisher instance ...");
        this.loader = loader;
        this.c2mon = c2mon;
        this.sender = sender;
        
        if (System.getProperty("mobicall.resend.delay") != null) {
            this.resendDelayOnStart = Integer.parseInt(System.getProperty("mobicall.resend.delay"));            
        }
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
            long resendFromTs = System.currentTimeMillis() - (resendDelayOnStart * 1000);
            for (AlarmValue av : activeAlarms) {
                this.onAlarmUpdate(av, resendFromTs);
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
    /**
     * The alarm event callback as defined by the C2MON interface. Here the data is only 
     * forwarded to the version of the method able to process both startup and runtime events.
     */
    @Override
    public void onAlarmUpdate(AlarmValue av) {
        onAlarmUpdate(av, 0);
    }
    
    /**
     * If the second parameter is non-0, it is used as timestamp to discard alarms older than
     * this value. This mechanism is used to prevent redundant notification at startup.
     * 
     * @param av <code>AlarmValue</code> describing the alarm event sent by C2MON
     * @param resendFromTs <code>long</code> 0 at runtime, otherwise timestamp for inclusion of events
     */
    public void onAlarmUpdate(AlarmValue av, long resendFromTs) {
        String alarmId = getAlarmId(av);
        LOG.debug(" RECEIVED    > " + alarmId + " is active:" + av.isActive());        
        MobicallAlarm ma = loader.find(alarmId);
        if (ma != null) {
            if (c2mon.isTagValid(av.getTagId())) {
                if (resendFromTs == 0 || av.getTimestamp().getTime() > resendFromTs) {
                    sender.send(ma.getMobicallId(), composeTrapMessage(ma, av));
                } else {
                    LOG.info("Do not re-notify " + getAlarmId(av) + " (too old)");                    
                }
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
    

