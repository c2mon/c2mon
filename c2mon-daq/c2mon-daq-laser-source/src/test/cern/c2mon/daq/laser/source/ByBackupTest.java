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
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

@UseHandler(LaserNativeMessageHandler.class)
public class ByBackupTest extends GenericMessageHandlerTst {

    LaserNativeMessageHandler laserMessage;
    AlarmListener listener = AlarmListener.getAlarmListener();

    public ByBackupTest() {
        
    }

    @Override
    protected void beforeTest() throws Exception {
        laserMessage = (LaserNativeMessageHandler) msgHandler;
        laserMessage.setAlarmListener(listener);
    }

    @Override
    protected void afterTest() throws Exception {
        // NOTHING
    }


    @Test
    @UseConf("f_laser_test1.xml")
    public void testActivateByBackup() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();
        
        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

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
        expectLastCall().times(3);

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
