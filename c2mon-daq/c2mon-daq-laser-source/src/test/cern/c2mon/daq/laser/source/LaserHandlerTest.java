/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.laser.source.testutil.AlarmMessageTestData;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

/**
 * This serie of unit tests is based on the C2MON DAQ framework mock.
 * 
 * @author mbuttner
 */
@UseHandler(LaserNativeMessageHandler.class)
public class LaserHandlerTest extends GenericMessageHandlerTst {

    LaserNativeMessageHandler laserMessage;
    AlarmListener listener = AlarmListener.getAlarmListener();

    //
    // --- SETUP --------------------------------------------------------------------------
    // 
    @Override
    protected void beforeTest() throws Exception {
        laserMessage = (LaserNativeMessageHandler) msgHandler;
        laserMessage.setAlarmListener(listener);
    }

    @Override
    protected void afterTest() throws Exception {
        // NOTHING
    }

    //
    // --- TESTS ---------------------------------------------------------------------------
    //
    @Test
    @UseConf("f_laser_test1.xml")
    public void testAlarmTurnsTrue() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        laserMessage.connectToDataSource();
        laserMessage.onMessage(AlarmMessageTestData.createUpdateMessage(true, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(true, sdtv.getValueAt(0, 124149).getValue());
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testAlarmTurnsFalse() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        laserMessage.connectToDataSource();
        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124149);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        laserMessage.onMessage(AlarmMessageTestData.createUpdateMessage(false, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(true, sdtv.getValueAt(0, 124149).getValue());
        assertEquals(false, sdtv.getValueAt(1, 124149).getValue());
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testNoCorrespondingAlarms() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124149);
        ISourceDataTag dataTag1 = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124150);
        
        laserMessage.connectToDataSource();
        laserMessage.onMessage(AlarmMessageTestData.createUnknownAlarm(true, MessageType.UPDATE, "LHC"));

        Thread.sleep(1000);
        
        assertNull(dataTag.getCurrentValue());
        assertNull(dataTag1.getCurrentValue());
        

    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testBackupMessage() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        laserMessage.connectToDataSource();
        
        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124150);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        laserMessage.onMessage(AlarmMessageTestData.createBackupMessage(MessageType.BACKUP, "LHC", false));

        Thread.sleep(3000);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(2, sdtv.getNumberOfCapturedValues(124150));

        assertEquals(true, sdtv.getValueAt(0, 124149).getValue());

        assertEquals(true, sdtv.getValueAt(0, 124150).getValue());
        assertEquals(false, sdtv.getValueAt(1, 124150).getValue());

    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testActivateByBackup() throws Exception {
        messageSender.sendCommfaultTag(107211, true);

        expectLastCall().once();      
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        laserMessage.connectToDataSource();
        laserMessage.onMessage(AlarmMessageTestData.createBackupMessage(MessageType.BACKUP, "LHC", false));

        Thread.sleep(3000);

        verify(messageSender);
        assertEquals(1, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(true, sdtv.getValueAt(0, 124149).getValue());
    }

    @Test
    @UseConf("f_laser_test1.xml")
    public void testTerminateByBackup() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        laserMessage.connectToDataSource();
        
        ISourceDataTag dataTag = laserMessage.getEquipmentConfiguration().getSourceDataTag((long) 124149);
        laserMessage.getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

        laserMessage.onMessage(AlarmMessageTestData.createBackupMessage(MessageType.BACKUP, "LHC", true));

        Thread.sleep(3000);

        verify(messageSender);
        assertEquals(2, sdtv.getNumberOfCapturedValues(124149));
        assertEquals(false, sdtv.getValueAt(1, 124149).getValue());
    }

}
