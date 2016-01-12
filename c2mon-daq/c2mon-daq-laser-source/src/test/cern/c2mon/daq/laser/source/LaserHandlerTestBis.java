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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.c2mon.daq.laser.source.testutil.AlarmMessageTestData;
import cern.c2mon.daq.laser.source.testutil.EquipmentSenderMock;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

/**
 * The unit tests hereafter use the EquipmentSenderMock to collect more directly
 * the data issued by the LaserMessageHandler. They cover approximately the same
 * aspects than LaserMessageHandlerTest, but with slightly different checkpoints
 * 
 * @author mbuttner
 */
@UseHandler(LaserNativeMessageHandler.class)
public class LaserHandlerTestBis extends GenericMessageHandlerTst {
    
    LaserNativeMessageHandler laserMessage;
    AlarmListener listener = AlarmListener.getAlarmListener();
    EquipmentSenderMock sender;
    
    //
    // --- SETUP ---------------------------------------------------------------
    //
    @Override
    protected void beforeTest() throws Exception {
        laserMessage = (LaserNativeMessageHandler) msgHandler;
        laserMessage.setAlarmListener(listener);
        sender = new EquipmentSenderMock(laserMessage.getName());
        laserMessage.connectToDataSource(sender);
    }

    @Override
    protected void afterTest() throws Exception {
        laserMessage.disconnectFromDataSource();
    }

    //
    // --- TESTS ---------------------------------------------------------------
    //
    // simple connection and config should not trigger any value update!
    @Test
    @UseConf("f_laser_test1.xml")
    public void testConnection() throws Exception {        
        Thread.sleep(3000);
        assertEquals(0, sender.getActivations());
        assertEquals(0, sender.getTerminations());
    }

    // check that the first update results in one activation (because the test message contains
    // one valid alarm) and a termination (the one configured for the source, but not in the mesage)
    @Test
    @UseConf("f_laser_test1.xml")
    public void testInitialBackup() throws Exception {
        laserMessage.onMessage(AlarmMessageTestData.createBackupMessage(MessageType.BACKUP, "LHC", false));
        Thread.sleep(3000);
        assertEquals(1, sender.getActivations());
        assertEquals(1, sender.getTerminations());        
    }
    
    //
    // check basic activation of alarms in an update message
    @Test
    @UseConf("f_laser_test1.xml")
    public void testActivation() throws Exception {
        laserMessage.onMessage(AlarmMessageTestData.createUpdateMessage(true, MessageType.UPDATE, "LHC"));        
        Thread.sleep(3000);
        assertEquals(2, sender.getActivations());
        assertEquals(0, sender.getTerminations());        

    }

    //
    // check basic termination of alarms in an update message
    @Test
    @UseConf("f_laser_test1.xml")
    public void testTermination() throws Exception {
        laserMessage.onMessage(AlarmMessageTestData.createUpdateMessage(false, MessageType.UPDATE, "LHC"));        
        Thread.sleep(3000);
        assertEquals(0, sender.getActivations());
        assertEquals(2, sender.getTerminations());        

    }

    //
    // check reconfiguration
    @Test
    @UseConf("f_laser_test1.xml")
    public void testReconfig() throws Exception {
        laserMessage.onMessage(AlarmMessageTestData.createUpdateMessage(true, MessageType.UPDATE, "LHC"));        
        Thread.sleep(3000);
        assertEquals(2, sender.getActivations());
        assertEquals(0, sender.getTerminations());        

        // reconfiguration should nullify the tag value
        ISourceDataTag tag = laserMessage.getTag(124149L);
        laserMessage.onRemoveDataTag(tag , new ChangeReport(1L));
        laserMessage.onAddDataTag(tag, new ChangeReport(2L));
        
        // now backup should reactivate the alarm
        laserMessage.onMessage(AlarmMessageTestData.createBackupMessage(MessageType.BACKUP, "LHC", false));
        Thread.sleep(3000);
        assertEquals(3, sender.getActivations());
        assertEquals(1, sender.getTerminations());        
    }

}
