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
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.diamon.alarms.client.ClientAlarmEvent;

@UseHandler(LaserNativeMessageHandler.class)
public class LaserNativeMessageHandlerTest extends GenericMessageHandlerTst {

    LaserNativeMessageHandler laserMessage;

    @Override
    protected void beforeTest() throws Exception {
        // TODO Auto-generated method stub
        laserMessage = (LaserNativeMessageHandler) msgHandler;
        // laserMessage.connectToDataSource();
    }

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub
        laserMessage.disconnectFromDataSource();
    }

    /**
     * Here the alarm is terminated, we are testing that nothing happened to the corresponding dataTag which should be
     * already false
     */
    @Test
    @UseConf("f_laser_test1.xml")
    public void testAlarm1() throws Exception {

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        laserMessage.connectToDataSource();

        AlarmListener t = new AlarmListener();
        ClientAlarmEvent alarm = new ClientAlarmEvent("LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
        t.setEquipment(laserMessage.getEquipmentConfiguration());
        t.setEquipmentMessage(laserMessage.getEquipmentMessageSender());
        t.onAlarm(alarm);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(124149).getQuality().getQualityCode());
        assertEquals(false, sdtv.getLastValue(124149).getValue());
        assertEquals("", sdtv.getFirstValue(124149).getValueDescription());

    }

    /**
     * In this case, the alarm is terminated. We test that the corresponding dataTag which is True at the beginning
     * turns to false.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("f_laser_test1.xml")
    public void testDataTagTurnsToFalse() throws Exception {

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        laserMessage.connectToDataSource();

        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124149);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        Thread.sleep(1000);

        AlarmListener t = new AlarmListener();
        ClientAlarmEvent alarm = new ClientAlarmEvent("LHCCOLLIMATOR", "TCSG.B5R7.B2", 22000);
        t.setEquipment(laserMessage.getEquipmentConfiguration());
        t.setEquipmentMessage(laserMessage.getEquipmentMessageSender());
        t.onAlarm(alarm);

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(124149).getQuality().getQualityCode());

        assertEquals(false, sdtv.getValueAt(0, 124149).getValue());
        assertEquals(true, sdtv.getValueAt(1, 124149).getValue());
        assertEquals(false, sdtv.getValueAt(2, 124149).getValue());

        assertEquals("", sdtv.getFirstValue(124149).getValueDescription());

    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testNoCorrespondingDataTag() throws Exception {

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        laserMessage.connectToDataSource();

        AlarmListener t = new AlarmListener();
        ClientAlarmEvent alarm = new ClientAlarmEvent("NewAlarm", "blabla", 22000);
        t.setEquipment(laserMessage.getEquipmentConfiguration());
        t.setEquipmentMessage(laserMessage.getEquipmentMessageSender());
        t.onAlarm(alarm);

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(1, sdtv.getNumberOfCapturedValues(124150));
        assertEquals(false, sdtv.getLastValue(124149).getValue());
        assertEquals(false, sdtv.getLastValue(124150).getValue());

    }

}
