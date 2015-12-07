/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

class AlarmMessageTestData extends AlarmMessageData {

    private AlarmMessageTestData() {

    }

    static AlarmMessageData createUpdateMessage(boolean active, MessageType messageType, String sourceId) throws InterruptedException {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        ClientAlarmEvent alarm = ClientAlarmTestEvent.createAlarm(active, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
        Thread.sleep(1000);
        ClientAlarmEvent alarm1 = ClientAlarmTestEvent.createAlarm(active, "DMNALMON", "MKBV.UA63.SCSS.AB2", 2);

        result.addFault(alarm);
        result.addFault(alarm1);

        return result;
    }

    static AlarmMessageData createUnknownAlarm(boolean active, MessageType messageType, String sourceId) {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        ClientAlarmEvent alarm = ClientAlarmTestEvent.createAlarm(active, "Unknown", "ABCD.EFGH.IJKL", 21000);

        result.addFault(alarm);

        return result;
    }

    
    static AlarmMessageData createBackupMessage(MessageType messageType, String sourceId, boolean empty) {
        AlarmMessageTestData result = new AlarmMessageTestData();
        result.setSourceHost(sourceId);
        result.setSourceTs(System.currentTimeMillis());
        result.setMessageType(messageType);
        result.setSourceId(sourceId);

        try {
            if (!empty) {
                ClientAlarmEvent alarm = ClientAlarmTestEvent.createAlarm(true, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
                Thread.sleep(1000);
                ClientAlarmEvent alarm2 = ClientAlarmTestEvent.createAlarm(true, "LHC", "test", 1);
                Thread.sleep(1000);
                ClientAlarmEvent alarm3 = ClientAlarmTestEvent.createAlarm(true, "LHCCOLL", "TCSG", 2);

                result.addFault(alarm);
                result.addFault(alarm2);
                result.addFault(alarm3);
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return result;
    }

}