/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.clic;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.dmn2.agentlib.AgentClientConstants;
import cern.tim.driver.test.GenericMessageHandlerTst;
import cern.tim.driver.test.SourceDataTagValueCapture;
import cern.tim.driver.test.UseConf;
import cern.tim.driver.test.UseHandler;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.util.parser.SimpleXMLParser;

/**
 * @author wbuczak
 */

@UseHandler(ClicMessageHandler.class)
public class ClicMessageHandlerTest extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(ClicMessageHandlerTest.class);

    ClicMessageHandler clicMsgHandler;

    BrokerService broker;
    DummyClicAgent clicAgent;

    public static final String ACTIVEMQ_URL = "tcp://localhost:9999";
    public static final String ACTIVEMQ_FAILOVER_URL = "failover:(" + ACTIVEMQ_URL
            + ")?jms.prefetchPolicy.all=100&startupMaxReconnectAttempts=2";

    @Override
    protected void beforeTest() throws Exception {
        System.setProperty(AgentClientConstants.DMN2_AGENTLIB_BROKER_PROPERTY, ACTIVEMQ_FAILOVER_URL);
        System.setProperty("app.name", "ClicMessageHandlerTest");

        broker = new BrokerService();
        broker.setUseJmx(true);
        broker.setPersistent(false);
        broker.addConnector(ACTIVEMQ_URL);
        broker.start();

        log.info("entering beforeTest()..");
        clicMsgHandler = (ClicMessageHandler) msgHandler;

        clicAgent = new DummyClicAgent(ACTIVEMQ_FAILOVER_URL);
        clicAgent.start();
        clicAgent.startHeartbeat();
        clicAgent.startAcquisition();

        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {

        clicMsgHandler.disconnectFromDataSource();

        clicAgent.stopHeartbeat();
        clicAgent.stopAcquisition();
        clicAgent.stop();

        broker.stop();
    }

    @Test
    @UseConf("conf-clic-single-metric.xml")
    public void subscriptionTest1() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(1);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        Thread.sleep(2000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100909).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(100909).getValue());
        assertEquals("", sdtv.getFirstValue(100909).getValueDescription());
    }

    @Test
    @UseConf("conf-clic-three-metrics.xml")
    public void subscriptionTest2() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        Thread.sleep(2000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100909).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(100909).getValue());
        assertEquals("", sdtv.getFirstValue(100909).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100910).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(100910).getValue());
        assertEquals("", sdtv.getFirstValue(100910).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100911).getQuality().getQualityCode());
        assertEquals(3, sdtv.getFirstValue(100911).getValue());
        assertEquals("", sdtv.getFirstValue(100911).getValueDescription());
    }

    @Test
    @UseConf("conf-clic-two-metrics.xml")
    public void subscriptionWithClicReconfigurationTest1() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100909).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(100909).getValue());
        assertEquals("", sdtv.getFirstValue(100909).getValueDescription());

        // first time the expected metric is not configured on the CLIC agent's side
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getFirstValue(100912).getQuality()
                .getQualityCode());
        assertEquals("Field: test.property.10 missing in the map. Please check your configuration.", sdtv
                .getFirstValue(100912).getQuality().getDescription());

        // after CLIC is reconfigured, with the next acquisition the expected metric should now be available
        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(1, 100912).getQuality().getQualityCode());
        assertEquals(10, sdtv.getValueAt(1, 100912).getValue());
        assertEquals("", sdtv.getValueAt(1, 100912).getValueDescription());
        
        // the CLIC agent is supposed to receive reconfiguration request once only
        assertEquals(1,clicAgent.getReconfigurationCounter());
    }

    @Test
    @UseConf("conf-clic-two-metrics.xml")
    public void subscriptionWithClicReconfigurationTest2() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(5);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        Thread.sleep(2500);

        // force the CLIC to unregister one of its metrics
        clicAgent.removeMetric("test.property.1");

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(100909).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(100909).getValue());
        assertEquals("", sdtv.getFirstValue(100909).getValueDescription());

        // first time the expected metric is not configured on the CLIC agent's side
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getFirstValue(100912).getQuality()
                .getQualityCode());
        assertEquals("Field: test.property.10 missing in the map. Please check your configuration.", sdtv
                .getFirstValue(100912).getQuality().getDescription());

        // after CLIC is reconfigured, with the next acquisition the expected metric should now be available
        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(1, 100912).getQuality().getQualityCode());
        assertEquals(10, sdtv.getValueAt(1, 100912).getValue());
        assertEquals("", sdtv.getValueAt(1, 100912).getValueDescription());

        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValueAt(1, 100909).getQuality()
                .getQualityCode());
        assertEquals("Field: test.property.1 missing in the map. Please check your configuration.",
                sdtv.getValueAt(1, 100909).getQuality().getDescription());

        // with next iteration the CLIC should be back reconfigured, hence the tag 100909 is again correctly received
        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(100909).getQuality().getQualityCode());
        assertEquals(1, sdtv.getLastValue(100909).getValue());
        assertEquals("", sdtv.getLastValue(100909).getValueDescription());
        
        // the CLIC agent is supposed to receive reconfiguration request twice!
        assertEquals(2,clicAgent.getReconfigurationCounter());        
    }

    @Test
    @UseConf("conf-clic-commands.xml")
    public void commandExecutionGetTest1() throws Exception {

        clicMsgHandler.connectToDataSource();

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100850L, "TEST:TESTCMD4", 5250L, (short) 0,
                    "testProcess", "String");

            String result = clicMsgHandler.runCommand(sctv);

            assertEquals("process testProcess restarted", result);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }
    }

    @Test
    @UseConf("conf-clic-commands.xml")
    public void commandExecutionGetTest2() throws Exception {

        clicMsgHandler.connectToDataSource();

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100852L, "TEST:TESTCMD6", 5250L, (short) 0,
                    "GetFipDetails;{1}", "String");

            String result = clicMsgHandler.runCommand(sctv);

            assertEquals("received command: GetFipDetails with arguments: 1", result);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

    }

    /**
     * The goal of this test is to verify the CLIC message handler's behavior when a request to add a new DataTag is
     * received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("conf-clic-single-metric.xml")
    public void reconfigure_AddDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        Thread.sleep(1000);

        StringBuilder str = new StringBuilder();

        str.append("<DataTag id=\"100910\" name=\"TEST-TAG01\" control=\"false\">");
        str.append("  <data-type>Integer</data-type>");
        str.append("  <DataTagAddress>");
        str.append("    <HardwareAddress class=\"ch.cern.tim.shared.datatag.address.impl.JAPCHardwareAddressImpl\">");
        str.append("       <device-name>DMN.CLIC.TEST</device-name>\n");
        str.append("       <property-name>Acquisition</property-name>\n");
        str.append("       <data-field-name>test.property.2</data-field-name>\n");
        str.append("    </HardwareAddress>");
        str.append("    <time-to-live>3600000</time-to-live>");
        str.append("    <priority>2</priority>");
        str.append("    <guaranteed-delivery>false</guaranteed-delivery>");
        str.append("  </DataTagAddress>");
        str.append("</DataTag>");

        SimpleXMLParser parser = new SimpleXMLParser();

        // trigger adding new DataTag
        SourceDataTag newTag = SourceDataTag.fromConfigXML(parser.parse(str.toString()).getDocumentElement());

        ChangeReport report = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(1000);

        // try adding once again the same tag
        ChangeReport report2 = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(1500);

        verify(messageSender);

        SourceDataTagValue fistValueTag1 = sdtv.getFirstValue(100909L);
        SourceDataTagValue firstValueTag2 = sdtv.getFirstValue(100910L);

        assertEquals(SourceDataQuality.OK, fistValueTag1.getQuality().getQualityCode());
        assertEquals(1, fistValueTag1.getValue());

        assertEquals(SourceDataQuality.OK, firstValueTag2.getQuality().getQualityCode());
        assertEquals(2, firstValueTag2.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());
        // the second should fail, since the tag is already registered
        assertEquals(CHANGE_STATE.FAIL, report2.getState());
        assertEquals("DataTag 100910 is already in equipment 5250", report2.getErrorMessage());
    }

    /**
     * The goal of this test is to verify the CLIC message handler's behavior when a request to remove an existing
     * DataTag is received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("conf-clic-single-heartbeat-metric.xml")
    public void reconfigure_RemoveDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(1, 2);

        replay(messageSender);

        clicMsgHandler.connectToDataSource();

        assertEquals(1, clicMsgHandler.getEquipmentConfiguration().getSourceDataTags().size());

        Thread.sleep(2000);

        // emulate receiving request to remove a tag
        ChangeReport report = configurationController.onDataTagRemove(new DataTagRemove(1L, 100909L,
                equipmentConfiguration.getId()));
        Thread.sleep(2000);

        // emulate receiving request to remove a tag - again the same one
        ChangeReport report2 = configurationController.onDataTagRemove(new DataTagRemove(1L, 100909L,
                equipmentConfiguration.getId()));

        Thread.sleep(200);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(100909L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(100909L);

        // there should be only one update received for that tag
        assertEquals(2, sdtv.getNumberOfCapturedValues(100909L));

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(2, firstValue1.getValue());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(3, lastValue1.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());

        assertEquals(0, clicMsgHandler.getEquipmentConfiguration().getSourceDataTags().size());

        // the other one should give some warning, but the status should still be SUCCESS
        assertEquals(CHANGE_STATE.SUCCESS, report2.getState());
        assertEquals("The data tag with id 100909 to remove was not found in equipment with id 5250",
                report2.getWarnMessage());

    }

}
