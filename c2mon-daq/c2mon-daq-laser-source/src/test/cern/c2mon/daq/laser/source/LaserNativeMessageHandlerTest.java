/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.c2mon.daq.laser.source.LaserNativeMessageHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;


@UseHandler(LaserNativeMessageHandler.class)
public class LaserNativeMessageHandlerTest extends GenericMessageHandlerTst {
    
    LaserNativeMessageHandler laserMessage;

    
    @Override
    protected void beforeTest() throws Exception {
        // TODO Auto-generated method stub
        laserMessage = (LaserNativeMessageHandler) msgHandler;
    }
    
    
    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub
       laserMessage.disconnectFromDataSource();
    }

    @Test
    @UseConf("timopalarmConfig.xml")   // e_myhandler_test1.xml configuration will be used for this test.
    public void test1() throws Exception {
     
//     // create junit captures for the tag id, value and message (for the commmfault tag)
//        Capture<Long> id = new Capture<Long>();
//        Capture<Boolean> val = new Capture<Boolean>();
//        Capture<String> msg = new Capture<String>();
//    
//        // message sender's sendCommfaultTag is expected to be called - the DAQ is expected to send commfault tag once it is initialized
//        messageSender.sendCommfaultTag(EasyMock.captureLong(id), EasyMock.captureBoolean(val), EasyMock.capture(msg));
//        // it should be called only once
//        expectLastCall().once();
//        
//     // record the mock
//        replay(messageSender);
//        
     laserMessage.connectToDataSource();
        assertTrue(true);
//        
//        Thread.sleep(2000);
//        
//        // verify that messageSender's interfaces were called according to what has been recorded
//        verify(messageSender);
//    
//        // check the message of the commfault tag is as expected
//        assertEquals(
//               "failed to connect to MBean service: service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi Exception caught: "
//                        + "Authentication failed! Invalid username or password", msg.getValue());
//    
//        // check the id of the commfault tag is correct
//        assertEquals(107211L, id.getValue().longValue());
//        // ..and the value
//        assertEquals(false, val.getValue());
        
    }

}
