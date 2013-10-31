package cern.c2mon.daq.japc;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
//import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ValueChangeMonitorEngine;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements integration tests for VCMs using real JAPC-RDA publication. This tests should normally 
 * be annotated with @Ignore, in order to avoid its execution by by the continuous-integration environment.
 * Comment out the @Ignore annotation if you wish to execute this test in your local development environment.
 * 
 * @author wbuczak
 */
@Ignore
@UseHandler(GenericJapcMessageHandler.class)
public class JapcVCMIntegrationTest extends AbstractGenericJapcMessageHandlerTst {

    
    DriverKernel kernel;
    
    static {
        // don't use JAPC mockito-  we want to subscribe to real JAPC-RDA device
        initMockito = false;
    }
   
    
    void setUpKernel() {
        kernel = createMock(DriverKernel.class);

        Map<Long, EquipmentMessageHandler> handlersMap = new HashMap<Long, EquipmentMessageHandler>();
        handlersMap.put(5250L, japcHandler);

        expect(kernel.getEquipmentMessageHandlersTable()).andReturn(handlersMap).atLeastOnce();
        replay(kernel);
        // normally kernel will be autowired by spring
        ValueChangeMonitorEngine.getInstance().setDriverKernel(kernel);
    }
    
    @Test
    @UseConf("e_japc_test_vcm1.xml")
    public void test_IntegrationTestVCM1() throws Exception {
  
        setUpKernel();
        
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().once();

        replay(messageSender);

        japcHandler.connectToDataSource();

        // 5 minutes
        Thread.sleep(16000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100909L).getQuality().getQualityCode());
        assertTrue(((Boolean) sdtv.getFirstValue(100909L).getValue()).booleanValue());
    }

}
