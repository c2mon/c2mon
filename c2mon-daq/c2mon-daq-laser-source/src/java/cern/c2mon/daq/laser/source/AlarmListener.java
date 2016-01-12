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
package cern.c2mon.daq.laser.source;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.diamon.alarms.client.AlarmConnector;
import cern.diamon.alarms.client.AlarmConnectorFactory;
import cern.diamon.alarms.client.AlarmConsumerInterface;
import cern.diamon.alarms.client.AlarmMessageData;

/**
 * The listener connects to the JMS brokers (and topics) specified in AlarmsJms.properties
 * in the root of the classpath. I fthe file can not be found, default values are used.
 * 
 * For all alarm messages received, a forward to the C2MON-DAQ equipment message handler 
 * corresponding to the alarms source id done.
 * 
 * @author mbuttner
 */
public class AlarmListener implements AlarmConsumerInterface {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmListener.class);
    private static AlarmListener listener;

    private AlarmConnector connector;
    boolean isStarted = false;
    private final ConcurrentHashMap<String, LaserNativeMessageHandler> handlers = new ConcurrentHashMap<>();

    private String jmsUrl = "failover:(tcp://jms-laser-pro1:61670,tcp://jms-laser-pro2:61670)";
    private String jmsRootTopic = "CERN.DIAMON.ALARM.INCOMING.OLD.";

    
    //
    // --- CONSTRUCTION -----------------------------------------------------------------------------------
    //
    private AlarmListener() {
        Properties config = new Properties();
        try {
            config.load(this.getClass().getResourceAsStream("/AlarmsJms.properties"));
            jmsUrl = config.getProperty("diamon.alarms.jms.brokers");
            jmsRootTopic = config.getProperty("diamon.alarms.jms.root_topic");
        } catch (IOException e) {
            LOG.warn("Failed to load JMS configuration parameters, using defaults");
        }
    }

    public synchronized static AlarmListener getAlarmListener() {
        if (listener == null) {
            listener = new AlarmListener();
        }
        return listener;
    }

    //
    // --- PUBLIC METHODS ----------------------------------------------------------------------------------
    //
    
    public synchronized void addHandler(LaserNativeMessageHandler messageHandler) {
        handlers.put(messageHandler.getEquipmentConfiguration().getName(), messageHandler);
    }

    public synchronized void removeHandler(LaserNativeMessageHandler messageHandler) {
        handlers.remove(messageHandler.getEquipmentConfiguration().getName());

        if (handlers.isEmpty()) {
            disconnectFromLaser();
        }
        
    }

    public synchronized void connectToLaser() throws JMSException {
        connector = AlarmConnectorFactory.getConnector(jmsUrl);
        connector.addListener(this);
        connector.setTopicRoot(jmsRootTopic);
        connector.connect();
        isStarted = true;
    }

    
    public void startListeningToSource(String sourceName) throws JMSException {
        if (!isStarted) {
            connectToLaser();
        }
        LOG.info("Start listening to source {}", sourceName);
        connector.addSource(sourceName);
    }

    public void removeListeningToSource(String sourceName) throws JMSException {
        LOG.info("Remove listing from source {}", sourceName);
        if (connector != null) {
            connector.removeSource(sourceName);
        }
    }

    public synchronized void disconnectFromLaser() {
        if (this.connector != null) {
            connector.disconnect();
        }
        isStarted = false;
    }

    //
    // --- Implements AlarmListener ----------------------------------------------------------------------
    //
    /*
     * Forward the alarm message to the equipment handler corresponding to the alarm source.
     */
    @Override
    public void onMessage(AlarmMessageData alarmMessage) {
        LaserNativeMessageHandler result = handlers.get(alarmMessage.getSourceId());
        if (result != null) {
            result.onMessage(alarmMessage);
        }               
    }

    @Override
    public void onException(JMSException e) {
        LOG.warn("JMS exception due to communication problem: " + e.getMessage());
    }

    @Override
    public void reset() {
        LOG.info("The connection was reset: you should adapt your internal data accordingly");
    }

    @Override
    public String getName() {
        return "LASER_DAQ";
    }



}
