package cern.c2mon.daq.jmx;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.jmx.JMXMessageHandler;
import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.vcm.ValueChangeMonitorEngine;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements integration tests for VCMs using real JMX publication. This tests should normally 
 * be annotated with @Ignore, in order to avoid its execution by by the continuous-integration environment.
 * Comment out the @Ignore annotation if you wish to execute this test in your local development environment.
 * 
 * @author mmitev
 */
//@Ignore
@UseHandler(JMXMessageHandler.class)
public class JMXVCMIntegrationTest extends GenericMessageHandlerTst {

    
    DriverKernel kernel;
    JMXMessageHandler jmxHandler;
    static Logger log = Logger.getLogger(JMXVCMIntegrationTest.class);
    
    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        jmxHandler = (JMXMessageHandler) msgHandler;

        // change connection timeout for test
        jmxHandler.MBEAN_CONNECTION_RETRY_TIMOUT = 1000L;

        JMXServiceURL surl = new JMXServiceURL("rmi", null, 9999);
        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("");
        log.info("entering afterTest()..");
        jmxHandler.disconnectFromDataSource();

        log.info("leaving afterTest()");
    }
    
    void setUpKernel() {
        kernel = createMock(DriverKernel.class);

        Map<Long, EquipmentMessageHandler> handlersMap = new HashMap<Long, EquipmentMessageHandler>();
        handlersMap.put(5250L, jmxHandler);

        expect(kernel.getEquipmentMessageHandlersTable()).andReturn(handlersMap).atLeastOnce();
        replay(kernel);
        // normally kernel will be autowired by spring
        ValueChangeMonitorEngine.getInstance().setDriverKernel(kernel);
    }
    
    @Test
    @Ignore
    @UseConf("e_jmx_integration_test_vcm1.xml")
    public void test_IntegrationTestVCM1() throws Exception {
  
        setUpKernel();
        
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        // 5 minutes
        Thread.sleep(16000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, ((Boolean) sdtv.getFirstValue(54676L).getValue()).booleanValue());
    }
}
