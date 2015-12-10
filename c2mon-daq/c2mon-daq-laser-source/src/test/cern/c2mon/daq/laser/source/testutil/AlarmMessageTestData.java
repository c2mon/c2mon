/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source.testutil;

import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

/**
 * Creates various alarm messages for unit testing of the LASER DAQ. The messages are built using
 * the diamon.alarms.commons package, so that the result can be used as parameter for the alarm
 * listener in the DAQ.
 * 
 * @author mbuttner
 */
public class AlarmMessageTestData extends AlarmMessageData {

    private AlarmMessageTestData() {

    }

    public static AlarmMessageData createUpdateMessage(boolean active, MessageType messageType, String sourceId) throws InterruptedException {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        ClientAlarmEvent alarm1 = AlarmTestEvent.createAlarm(active, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
        Thread.sleep(1000);
        ClientAlarmEvent alarm2 = AlarmTestEvent.createAlarm(active, "DMNALMON", "MKBV.UA63.SCSS.AB2", 2);

        result.addFault(alarm1);
        result.addFault(alarm2);

        return result;
    }

    public static AlarmMessageData createUnknownAlarm(boolean active, MessageType messageType, String sourceId) {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        ClientAlarmEvent alarm = AlarmTestEvent.createAlarm(active, "Unknown", "ABCD.EFGH.IJKL", 21000);

        result.addFault(alarm);

        return result;
    }

    
    public static AlarmMessageData createBackupMessage(MessageType messageType, String sourceId, boolean empty) {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        try {
            if (!empty) {
                ClientAlarmEvent alarm1 = AlarmTestEvent.createAlarm(true, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
                Thread.sleep(1000);
                ClientAlarmEvent alarm2 = AlarmTestEvent.createAlarm(true, "LHC", "test", 1);
                Thread.sleep(1000);
                ClientAlarmEvent alarm3 = AlarmTestEvent.createAlarm(true, "LHCCOLL", "TCSG", 2);

                result.addFault(alarm1);
                result.addFault(alarm2);
                result.addFault(alarm3);
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return result;
    }

}