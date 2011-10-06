package ch.cern.tim.driver.jec.frames;

import java.io.IOException;


import org.easymock.classextension.ConstructorArgs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentLoggerFactory;
import ch.cern.tim.driver.jec.PLCObjectFactory;
import ch.cern.tim.driver.jec.config.PLCConfiguration;
import ch.cern.tim.jec.JECPFrames;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

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
        abstractJECPFrameProcessor.pushJECPFrame(frame);

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
        abstractJECPFrameProcessor.pushJECPFrame(frame);

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
        abstractJECPFrameProcessor.pushJECPFrame(frame);

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
        abstractJECPFrameProcessor.pushJECPFrame(frame);
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame);
        frame.SetSequenceNumber((byte) 2);
        abstractJECPFrameProcessor.pushJECPFrame(frame);
        abstractJECPFrameProcessor.clearDataQueue();
        assertFalse(abstractJECPFrameProcessor.processNextJECPFrame());
    }
    
    @Test
    public void testPause() throws InterruptedException, IOException {
        JECPFrames frame = plcObjectFactory.getRawRecvFrame();
        frame.SetSequenceNumber((byte) 1);
        abstractJECPFrameProcessor.pushJECPFrame(frame);
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
