package cern.c2mon.daq.laser.source;

import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.diamon.alarms.client.AlarmConnector;
import cern.diamon.alarms.client.AlarmConnectorFactory;
import cern.diamon.alarms.client.AlarmConsumerInterface;
import cern.diamon.alarms.client.AlarmMessageData;

public class AlarmListener implements AlarmConsumerInterface {

    private static final Logger log = LoggerFactory.getLogger(AlarmListener.class);

    private AlarmConnector connector;

    boolean isStarted = false;

    private final ConcurrentHashMap<String, LaserNativeMessageHandler> listHandler = new ConcurrentHashMap<>();

    public static AlarmListener INSTANCE;

    private AlarmListener() {

    }

    public synchronized static AlarmListener getAlarmListener() {
        if (INSTANCE == null) {
            INSTANCE = new AlarmListener();
        }
        return INSTANCE;
    }

    private synchronized LaserNativeMessageHandler getHandler(String sourceName) {
        return listHandler.get(sourceName);
    }

    public synchronized void addHandler(LaserNativeMessageHandler messageHandler) {
        listHandler.put(messageHandler.getEquipmentConfiguration().getName(), messageHandler);
    }

    public synchronized void removeHandler(LaserNativeMessageHandler messageHandler) {
        listHandler.remove(messageHandler.getEquipmentConfiguration().getName());

        if (listHandler.isEmpty()) {
            disconnectFromLaser();
        }
        
    }

    public synchronized void connectToLaser() throws JMSException {
        System.setProperty("diamon.alarms.jmsdriver", "cern.diamon.alarms.sonic.SonicConnection");

        connector = AlarmConnectorFactory.getConnector("tcp://sljas2:2506,tcp://sljas3:2506");
        connector.addListener(this);
        connector.setTopicRoot("CMW.ALARM_SYSTEM.ALARMS.SOURCES.");
        connector.connect();
        isStarted = true;
    }

    public void startListingToSource(String sourceName) throws JMSException {
        if (!isStarted) {
            connectToLaser();
        }
        log.info("Start listening to source {}", sourceName);
        connector.addSource(sourceName);
    }

    public void removeListingToSource(String sourceName) throws JMSException {
        log.info("Remove listing from source {}", sourceName);
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

    @Override
    public void onMessage(AlarmMessageData alarmMessage) {

        LaserNativeMessageHandler result = getHandler(alarmMessage.getSourceId());
        
        if (result != null) {
            result.onMessage(alarmMessage);
        }
        

        
    }

    
    @Override
    public void onException(JMSException e) {
        log.warn("JMS exception due to communication problem: " + e.getMessage());
    }

    @Override
    public void reset() {
        log.info("The connection was reset: you should adapt your internal data accordingly");
    }

    @Override
    public String getName() {
        return "MyTestConsumer";
    }



}
