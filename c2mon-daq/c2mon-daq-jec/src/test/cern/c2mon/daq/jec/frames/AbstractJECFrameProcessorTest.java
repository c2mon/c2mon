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
package cern.c2mon.daq.jec.frames;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.easymock.ConstructorArgs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.PLCObjectFactory;
import cern.c2mon.daq.jec.config.PLCConfiguration;
import cern.c2mon.daq.jec.plc.JECPFrames;

public class AbstractJECFrameProcessorTest extends ThreadCatchHelper {
    private AbstractJECPFrameProcessor abstractJECPFrameProcessor;
    private PLCObjectFactory plcObjectFactory;
    @Before
    public void setUp() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        EquipmentLogger equipmentLogger = new EquipmentLogger("asd", "asd", "asd");
        PLCConfiguration plcConfiguration = new PLCConfiguration();
        plcConfiguration.setProtocol("TestPLCDriver");
        plcObjectFactory = new PLCObjectFactory(plcConfiguration);
        abstractJECPFrameProcessor = createMock(AbstractJECPFrameProcessor.class, new ConstructorArgs(AbstractJECPFrameProcessor.class.getConstructor(Integer.TYPE, PLCObjectFactory.class,
                EquipmentLogger.class), 0, plcObjectFactory, equipmentLogger));
        prepareForAsynchronousFailureHandling(Thread.currentThread());
    }

    @Test
    public void testAcknowledge() {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 1);
        // abstractJECPFrameProcessor.
    }

    @Test
    public void testPush() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);

        // methods which should be called
        abstractJECPFrameProcessor.processJECPFrame(frame);

        replay(abstractJECPFrameProcessor);
        abstractJECPFrameProcessor.start();
        Thread.sleep(100L);
        verify(abstractJECPFrameProcessor);
    }

    @Test
    public void testPushWrongMsgId() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetMessageIdentifier((byte) 2);
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);

        // methods which should be called
        abstractJECPFrameProcessor.acknowledgeReceivedMessage(frame);

        replay(abstractJECPFrameProcessor);
        abstractJECPFrameProcessor.start();
        Thread.sleep(100L);
        verify(abstractJECPFrameProcessor);
    }

    @Test
    public void testPushWrongSeqId() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 0);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);

        // methods which should be called
        abstractJECPFrameProcessor.acknowledgeReceivedMessage(frame);

        replay(abstractJECPFrameProcessor);
        abstractJECPFrameProcessor.start();
        Thread.sleep(100L);
        verify(abstractJECPFrameProcessor);
    }
    
    @Test
    public void testClearDataQueue() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 0);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);
        frame.SetSequenceNumber((byte) 2);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);
        abstractJECPFrameProcessor.clearDataQueue();
        assertFalse(abstractJECPFrameProcessor.processNextJECPFrame());
    }
    
    @Test
    public void testPause() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame, false);
        abstractJECPFrameProcessor.setPause(true);

        // methods which should be called
        abstractJECPFrameProcessor.acknowledgeReceivedMessage(frame);
        
        replay(abstractJECPFrameProcessor);
        abstractJECPFrameProcessor.start();
        Thread.sleep(100L);
        verify(abstractJECPFrameProcessor);
        assertTrue(abstractJECPFrameProcessor.isPause());
        
        reset(abstractJECPFrameProcessor);
        
        // methods which should be called
        abstractJECPFrameProcessor.processJECPFrame(frame);
        
        replay(abstractJECPFrameProcessor);
        abstractJECPFrameProcessor.setPause(false);
        Thread.sleep(100L);
        verify(abstractJECPFrameProcessor);
        assertFalse(abstractJECPFrameProcessor.isPause());
    }
    
    @Test
    public void testStartStopThread() throws InterruptedException {
        abstractJECPFrameProcessor.start();
        Thread.sleep(100L);
        assertTrue(abstractJECPFrameProcessor.isAlive());
        abstractJECPFrameProcessor.stopThread();
        Thread.sleep(100L);
        assertFalse(abstractJECPFrameProcessor.isAlive());
    }
    
    @After
    public void tearDown() {
        reportPossibleProblemsInListenerWithMethod();
    }

}
