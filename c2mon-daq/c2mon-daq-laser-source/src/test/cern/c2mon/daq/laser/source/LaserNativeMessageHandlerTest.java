/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

@UseHandler(LaserNativeMessageHandler.class)
public class LaserNativeMessageHandlerTest extends GenericMessageHandlerTst {

    LaserNativeMessageHandler laserMessage;

    AlarmListener listener;

    public LaserNativeMessageHandlerTest() {
        listener = new AlarmListener();
        laserMessage = new LaserNativeMessageHandler(listener);
    }

    @Override
    protected void beforeTest() throws Exception {
        // TODO Auto-generated method stub
        laserMessage = (LaserNativeMessageHandler) msgHandler;
        // laserMessage.connectToDataSource();
    }

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testAlarmTurnsTrue() throws Exception {
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        laserMessage.connectToDataSource();

        listener.onMessage(MyAlarmMessageData.createUpdateMessage(true, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(false, sdtv.getValueAt(0, 124149).getValue());
        assertEquals(true, sdtv.getValueAt(1, 124149).getValue());
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testAlarmTurnsFalse() throws Exception {
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        laserMessage.connectToDataSource();

        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124149);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        listener.onMessage(MyAlarmMessageData.createUpdateMessage(false, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(3, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(false, sdtv.getValueAt(0, 124149).getValue());
        assertEquals(true, sdtv.getValueAt(1, 124149).getValue());
        assertEquals(false, sdtv.getValueAt(2, 124149).getValue());
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testNoCorrespondingAlarms() throws Exception {
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        laserMessage.connectToDataSource();

        listener.onMessage(MyAlarmMessageData.createUnknownAlarm(true, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(1, sdtv.getNumberOfCapturedValues(124150));
        assertEquals(false, sdtv.getLastValue(124149).getValue());
        assertEquals(false, sdtv.getLastValue(124150).getValue());

    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testBackupMessage() throws Exception {
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(6);

        replay(messageSender);

        laserMessage.connectToDataSource();

        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124150);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        listener.onMessage(MyAlarmMessageData.createBackupMessage(MessageType.BACKUP, "LHC"));

        Thread.sleep(3000);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(3, sdtv.getNumberOfCapturedValues(124150));

        assertEquals(false, sdtv.getValueAt(0, 124149).getValue());
        assertEquals(true, sdtv.getValueAt(1, 124149).getValue());

        assertEquals(false, sdtv.getValueAt(0, 124150).getValue());
        assertEquals(true, sdtv.getValueAt(1, 124150).getValue());
        assertEquals(false, sdtv.getValueAt(2, 124150).getValue());

    }

    private static class MyAlarmMessageData extends AlarmMessageData {

        public MyAlarmMessageData() {

        }

        static AlarmMessageData createUpdateMessage(boolean active, MessageType messageType, String sourceId) {
            MyAlarmMessageData result = new MyAlarmMessageData();
            result.setSourceHost(sourceId);
            result.setSourceTs(System.currentTimeMillis());
            result.setMessageType(messageType);
            result.setSourceId(sourceId);

            ClientAlarmEvent alarm = MyClientAlarmEvent.createAlarm(active, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);

            result.addFault(alarm);

            return result;
        }

        static AlarmMessageData createUnknownAlarm(boolean active, MessageType messageType, String sourceId) {
            MyAlarmMessageData result = new MyAlarmMessageData();
            result.setSourceHost(sourceId);
            result.setSourceTs(System.currentTimeMillis());
            result.setMessageType(messageType);
            result.setSourceId(sourceId);

            ClientAlarmEvent alarm = MyClientAlarmEvent.createAlarm(active, "Unknown", "ABCD.EFGH.IJKL", 21000);

            result.addFault(alarm);

            return result;
        }

        static AlarmMessageData createBackupMessage(MessageType messageType, String sourceId) {
            MyAlarmMessageData result = new MyAlarmMessageData();
            result.setSourceHost(sourceId);
            result.setSourceTs(System.currentTimeMillis());
            result.setMessageType(messageType);
            result.setSourceId(sourceId);

            try {
                ClientAlarmEvent alarm = MyClientAlarmEvent.createAlarm(true, "LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
                Thread.sleep(1000);
                ClientAlarmEvent alarm2 = MyClientAlarmEvent.createAlarm(true, "LHC", "test", 1);
                Thread.sleep(1000);
                ClientAlarmEvent alarm3 = MyClientAlarmEvent.createAlarm(true, "LHCCOLL", "TCSG", 2);

                result.addFault(alarm);
                result.addFault(alarm2);
                result.addFault(alarm3);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            return result;
        }

    }

    private static class MyClientAlarmEvent extends ClientAlarmEvent {

        public MyClientAlarmEvent(String deviceClass, String deviceName, int faultCode) {
            super(deviceClass, deviceName, faultCode);
        }

        static ClientAlarmEvent createAlarm(boolean active, String deviceClass, String deviceName, int faultCode) {
            MyClientAlarmEvent alarm = new MyClientAlarmEvent(deviceClass, deviceName, faultCode);
            alarm.setActive(active);
            alarm.setUserTs(System.currentTimeMillis());

            return alarm;

        }
    }

}
