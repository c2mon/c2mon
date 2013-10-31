/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.jmx;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.jmx.JMXMessageHandler;
import cern.c2mon.daq.jmx.mbeans.AttributeHolder;
import cern.c2mon.daq.jmx.mbeans.AttributeHolderMBean;
import cern.c2mon.daq.jmx.mbeans.Cache;
import cern.c2mon.daq.jmx.mbeans.CacheMBean;
import cern.c2mon.daq.jmx.mbeans.QueueSampler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.util.parser.SimpleXMLParser;

/**
 * This class implements a set of JUnit tests for <code>JMXMessageHandler</code>. All tests that require
 * JMXMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.jmx.JMXMessageHandler
 * @author wbuczak
 */
@UseHandler(JMXMessageHandler.class)
public class JMXMessageHandlerTest extends GenericMessageHandlerTst {

    /**
     * -Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.port=9999
     * -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=true
     * -Dcom.sun.management.jmxremote.password.file=src/test/ch/cern/tim/driver/jmx/testsrv.password
     * -Dcom.sun.management.jmxremote.access.file=src/test/ch/cern/tim/driver/jmx/testsrv.access
     * 
     * -Djmx.daq.passwd.file="src/test/ch/cern/tim/driver/jmx/pcache-passwords.txt"
     */

    static Logger log = Logger.getLogger(JMXMessageHandlerTest.class);

    JMXMessageHandler jmxHandler;

    MBeanServer mbs;
    // ObjectName oname;

    JMXConnectorServer connector;

    private static final String CACHE_BEAN_OBJ_NAME = "cern.example.mbeans:type=Cache";
    private static final String TEST_BEAN_OBJ_NAME = "cern.example.mbeans:type=TestBean";
    private static final String QUEUE_SAMPLER_BEAN_OBJ_NAME = "cern.example.mbeans:type=QueueSampler";

    @SuppressWarnings("static-access")
    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        jmxHandler = (JMXMessageHandler) msgHandler;

        // change connection timeout for test
        jmxHandler.MBEAN_CONNECTION_RETRY_TIMOUT = 100L;

        JMXServiceURL surl = new JMXServiceURL("rmi", null, 9999);

        mbs = ManagementFactory.getPlatformMBeanServer();

        // register our testing mbean
        CacheMBean mbean = new Cache();
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.registerMBean(mbean, oname);

        Queue<String> queue = new ArrayBlockingQueue<String>(10);
        queue.add("Request-1");
        queue.add("Request-2");
        queue.add("Request-3");

        QueueSampler mxbean = new QueueSampler(queue);
        ObjectName mxbeanName = new ObjectName(QUEUE_SAMPLER_BEAN_OBJ_NAME);
        mbs.registerMBean(mxbean, mxbeanName);

        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("");
        log.info("entering afterTest()..");
        jmxHandler.disconnectFromDataSource();

        ObjectName oname = null;
        try {
            oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
            mbs.unregisterMBean(oname);
        } catch (Exception ex) {
        }

        try {
            oname = new ObjectName(TEST_BEAN_OBJ_NAME);
            mbs.unregisterMBean(oname);
        } catch (Exception ex) {
        }

        try {
            oname = new ObjectName(QUEUE_SAMPLER_BEAN_OBJ_NAME);
            mbs.unregisterMBean(oname);
        } catch (Exception ex) {
        }

        if (null != connector)
            connector.stop();
        log.info("leaving afterTest()");
    }

    @Test
    @UseConf("e_jmx_test1.xml")
    public void testInvalidCredentials() throws Exception {

        Capture<Long> id = new Capture<Long>();
        Capture<Boolean> val = new Capture<Boolean>();
        Capture<String> msg = new Capture<String>();

        messageSender.sendCommfaultTag(EasyMock.capture(id), EasyMock.capture(val), EasyMock.capture(msg));
        expectLastCall().once();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(2000);
               
        verify(messageSender);

        assertEquals(
                "failed to connect to MBean service: service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi Exception caught: "
                        + "Authentication failed! Invalid username or password", msg.getValue());

        assertEquals(107211L, id.getValue().longValue());
        assertEquals(false, val.getValue());
    }

    @Test
    @UseConf("e_jmx_test2.xml")
    public void testConnectionAndInitialization() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(200, sdtv.getLastValue(54675L).getValue());
        assertEquals("", sdtv.getLastValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals("test string 1", sdtv.getLastValue(54676L).getValue());
        assertEquals("", sdtv.getLastValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getLastValue(54677L).getQuality()
                .getQualityCode());
        assertEquals("The specified MBean does not exist in the repository: cern.example.mbeans:type=WrongMBean", sdtv
                .getLastValue(54677L).getQuality().getDescription());
    }

    @Test
    @UseConf("e_jmx_test3.xml")
    public void tetEqAddressNull() throws Exception {
        try {
            jmxHandler.connectToDataSource();
            fail("EqIOException was expected");
        } catch (EqIOException ex) {
            assertEquals("equipment address must NOT be null. Check DAQ configuration!", ex.getMessage());
        }

    }

    @Test
    @UseConf("e_jmx_test4.xml")
    public void testPolling() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1500);

        // change the cache value
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.setAttribute(oname, new Attribute("CacheSize", 500));

        Thread.sleep(500);

        mbs.setAttribute(oname, new Attribute("CacheSize", 600));

        Thread.sleep(500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(200, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(1, 54675L).getQuality().getQualityCode());
        assertEquals(500, sdtv.getValueAt(1, 54675L).getValue());
        assertEquals("", sdtv.getValueAt(1, 54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(600, sdtv.getLastValue(54675L).getValue());
        assertEquals("", sdtv.getLastValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test9.xml")
    public void testPolling2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54675L).getValue());
        assertEquals("[a, b]", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test10.xml")
    public void testPolling3() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(10, sdtv.getFirstValue(54675L).getValue());
        assertEquals("[0,0,0,0,0,0,0,0,0,0]", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test5.xml")
    public void testNotifications() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(8);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1500);

        // change the cache value
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.setAttribute(oname, new Attribute("CacheSize", 500));
        Thread.sleep(200);
        mbs.setAttribute(oname, new Attribute("CachedObjects", 50));
        Thread.sleep(100);

        mbs.setAttribute(oname, new Attribute("CacheSize", 400));
        Thread.sleep(200);
        mbs.setAttribute(oname, new Attribute("CachedObjects", 100));
        Thread.sleep(500);

        mbs.setAttribute(oname, new Attribute("CacheSize", 300));
        Thread.sleep(200);
        mbs.setAttribute(oname, new Attribute("CachedObjects", 100));

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(200, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());

        // for tag #54676 we expect only the initial value, since no notifications are sent
        // for that tag by the server
        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(0, sdtv.getFirstValue(54676L).getValue());
        assertEquals("", sdtv.getFirstValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(1, 54675L).getQuality().getQualityCode());
        assertEquals(500, sdtv.getValueAt(1, 54675L).getValue());
        // assertEquals("200", sdtv.getValueAt(1, 54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(2, 54675L).getQuality().getQualityCode());
        assertEquals(400, sdtv.getValueAt(2, 54675L).getValue());
        // assertEquals("500", sdtv.getValueAt(2, 54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(300, sdtv.getLastValue(54675L).getValue());
        // assertEquals("400", sdtv.getLastValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test6.xml")
    public void testNotifications2() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().once();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1500);

        // change the cache value
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.setAttribute(oname, new Attribute("CachedObjects", 50));
        Thread.sleep(1000);

        mbs.setAttribute(oname, new Attribute("CachedObjects", 100));
        Thread.sleep(1000);

        mbs.setAttribute(oname, new Attribute("CachedObjects", 200));

        Thread.sleep(100);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(0, sdtv.getFirstValue(54676L).getValue());
        assertEquals("", sdtv.getFirstValue(54676L).getValueDescription());
    }

    @SuppressWarnings("static-access")
    @Ignore
    @Test
    @UseConf("e_jmx_test5.xml")
    public void testConnectionBreakAndRecovery() throws Exception {

        JMXMessageHandler.CONNECTION_TEST_INTERVAL = 400L;

        messageSender.sendCommfaultTag(107211, true);
        // messageSender.sendCommfaultTag(107211, false,isA(String.class));
        messageSender.sendCommfaultTag(107211, false, "Looks like connection with the JMX server has been dropped");
        messageSender.sendCommfaultTag(107211, true);

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        // modify the JVM uptime attribute, to simulate connection problem
        jmxHandler.JVM_UPTIME_OBJECT_ATTRIBUTE = "WrongAttribute";

        Thread.sleep(1000);

        // modify the JVM uptime attribute back to its original value
        jmxHandler.JVM_UPTIME_OBJECT_ATTRIBUTE = "Uptime";

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(200, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals(0, sdtv.getFirstValue(54676L).getValue());
        assertEquals("", sdtv.getFirstValue(54676L).getValueDescription());

        // again initialization is expected, since the connection is reopened

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(200, sdtv.getLastValue(54675L).getValue());
        assertEquals("", sdtv.getLastValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals(0, sdtv.getLastValue(54676L).getValue());
        assertEquals("", sdtv.getLastValue(54676L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test7.xml")
    public void testCommandsExecution() throws Exception {
        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1200);

        verify(messageSender);

        SourceCommandTagValue sctv = new SourceCommandTagValue(100847L, "TEST:TESTCMD1", 5250L, (short) 0, null,
                "Integer");
        try {

            Object ret = jmxHandler.runCommand(sctv);
            Thread.sleep(500);
            assertNotNull(ret);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        sctv = new SourceCommandTagValue(100848L, "TEST:TESTCMD2", 5250L, (short) 0, true, "Boolean");
        try {

            Object ret = jmxHandler.runCommand(sctv);
            Thread.sleep(500);
            assertNotNull(ret);

            fail("EqCommandTagException was expected at this point");
        } catch (EqCommandTagException ex) {
        }

        sctv = new SourceCommandTagValue(100848L, "TEST:TESTCMD2", 5250L, (short) 0, null, "Integer");
        try {

            Object ret = jmxHandler.runCommand(sctv);
            Thread.sleep(500);
            assertNotNull(ret);

            fail("EqCommandTagException was expected at this point");
        } catch (EqCommandTagException ex) {
        }

        sctv = new SourceCommandTagValue(100848L, "TEST:TESTCMD2", 5250L, (short) 0, 0, "Integer");
        try {
            Object ret = jmxHandler.runCommand(sctv);
            Thread.sleep(500);
            assertNotNull(ret);
            assertEquals("A", ret);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        sctv = new SourceCommandTagValue(100848L, "TEST:TESTCMD2", 5250L, (short) 0, 1, "Integer");
        try {
            Object ret = jmxHandler.runCommand(sctv);
            Thread.sleep(500);
            assertNotNull(ret);
            assertEquals("B", ret);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

    }

    /**
     * The goal of this test is to verify the JMX message handler's behavior when a request to add a new DataTag is
     * received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_jmx_test6.xml")
    public void reconfigure_AddDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        // change the cache value
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.setAttribute(oname, new Attribute("CachedObjects", 10));

        // register new testing mbean
        AttributeHolderMBean mbean = new AttributeHolder();
        ObjectName oname2 = new ObjectName(TEST_BEAN_OBJ_NAME);
        mbs.registerMBean(mbean, oname2);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        StringBuilder str = new StringBuilder();

        str.append("<DataTag id=\"1000001\" name=\"JAPC-TEST-TAG02\" control=\"false\">");
        str.append("  <data-type>Integer</data-type>");
        str.append("  <DataTagAddress>");
        str.append("    <HardwareAddress class=\"ch.cern.tim.shared.datatag.address.impl.JMXHardwareAddressImpl\">");
        str.append("       <object-name>cern.example.mbeans:type=TestBean</object-name>\n");
        str.append("       <attribute>TestAttribute</attribute>\n");
        str.append("       <receive-method>poll</receive-method>\n");
        str.append("    </HardwareAddress>");
        str.append("    <time-to-live>3600000</time-to-live>");
        str.append("    <priority>2</priority>");
        str.append("    <guaranteed-delivery>false</guaranteed-delivery>");
        str.append("  </DataTagAddress>");
        str.append("</DataTag>");

        SimpleXMLParser parser = new SimpleXMLParser();

        // Thread.sleep(500);

        // trigger adding new DataTag
        SourceDataTag newTag = SourceDataTag.fromConfigXML(parser.parse(str.toString()).getDocumentElement());

        ChangeReport report = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(1000);

        // try adding once again the same tag
        ChangeReport report2 = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(500);

        verify(messageSender);

        SourceDataTagValue fistValueTag1 = sdtv.getFirstValue(54676L);
        SourceDataTagValue firstValueTag2 = sdtv.getFirstValue(1000001L);

        assertEquals(SourceDataQuality.OK, fistValueTag1.getQuality().getQualityCode());
        assertEquals(10, fistValueTag1.getValue());

        assertEquals(SourceDataQuality.OK, firstValueTag2.getQuality().getQualityCode());
        assertEquals(5, firstValueTag2.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());
        // the second should fail, since the tag is already registered
        assertEquals(CHANGE_STATE.FAIL, report2.getState());
        assertEquals("DataTag 1000001 is already in equipment 5250", report2.getErrorMessage());

        try {
            mbs.unregisterMBean(oname2);
        } catch (Exception ex) {
        }

    }

    /**
     * The goal of this test is to verify the JMX message handler's behavior when a request to remove an existing
     * DataTag is received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_jmx_test11.xml")
    public void reconfigure_RemoveDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        System.out.println(jmxHandler.getEquipmentConfiguration().getSourceDataTags().size());

        Thread.sleep(1000);

        // set the new value
        ObjectName oname = new ObjectName(CACHE_BEAN_OBJ_NAME);
        mbs.setAttribute(oname, new Attribute("CacheSize", 250));
        Thread.sleep(1000);

        // emulate receiving request to remove a tag
        ChangeReport report = configurationController.onDataTagRemove(new DataTagRemove(1L, 54675L,
                equipmentConfiguration.getId()));
        Thread.sleep(1000);

        // set some new values, they should no longer be received, sine a tag should
        // be unregistered by now
        mbs.setAttribute(oname, new Attribute("CacheSize", 260));
        Thread.sleep(200);
        mbs.setAttribute(oname, new Attribute("CacheSize", 270));
        Thread.sleep(200);
        mbs.setAttribute(oname, new Attribute("CacheSize", 280));
        Thread.sleep(200);

        // emulate receiving request to remove a tag - again the same one
        ChangeReport report2 = configurationController.onDataTagRemove(new DataTagRemove(1L, 54676L,
                equipmentConfiguration.getId()));

        Thread.sleep(200);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(54675L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(54675L);

        // there should be only one update received for that tag
        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(200, firstValue1.getValue());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(250, lastValue1.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());

        // the other one should give some warning, but the status should still be SUCCESS
        assertEquals(CHANGE_STATE.SUCCESS, report2.getState());
        assertEquals("The data tag with id 54676 to remove was not found in equipment with id 5250",
                report2.getWarnMessage());

    }

    @Test
    @UseConf("e_jmx_test12.xml")
    public void testPollingForMapElement() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54675L).getValue());
        assertEquals("{CATEGORY1/process1/metric1=123, CATEGORY1/process2/metric1=128}", sdtv.getFirstValue(54675L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(123, sdtv.getFirstValue(54676L).getValue());
        assertEquals("", sdtv.getFirstValue(54676L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test13.xml")
    public void testGetArraySize() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        // the array has 10 elements
        assertEquals(10, sdtv.getFirstValue(54675L).getValue());
        assertEquals("[0,0,0,0,0,0,0,0,0,0]", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test14.xml")
    public void testMapValueFromComposedAttribute() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(2000, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test15.xml")
    public void testMapValueFromComposedAttribute2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        ObjectName oname = new ObjectName(QUEUE_SAMPLER_BEAN_OBJ_NAME);

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(500);

        Object[] params = new Object[] { new Integer(120) };
        String[] signatures = new String[] { "java.lang.Integer" };
        mbs.invoke(oname, "updateMetricInsideMap", params, signatures);

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(2000, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(120, sdtv.getLastValue(54675L).getValue());
        assertEquals("", sdtv.getLastValue(54675L).getValueDescription());

    }

    /**
     * This is a logging project integration test. Normally it should be commented out (@Ignore annotation) Uncomment if
     * you need to run it from your development environment, but do not commit!
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    @UseConf("e_jmx_test16.xml")
    public void testMapAtrributeLoggingProject1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(100000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(3, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());
    }

    // @Test
    @UseConf("e_jmx_test17.xml")
    public void testGetHeapSizeFromJVM() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        // assertEquals(3, sdtv.getFirstValue(54675L).getValue());
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("e_jmx_test-tabular-data-bean.xml")
    public void testTabularDataGetSize() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        // there should be quite a lot of properties
        assertTrue((Integer) sdtv.getFirstValue(54675L).getValue() > 0);

        // the description contains the whole map
        assertTrue(sdtv.getFirstValue(54675L).getValueDescription().length() > 0);
    }

    @Test
    @UseConf("e_jmx_test-tabular-data-bean2.xml")
    public void testTabularDataGetValueFromSpecifiedKey() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        Thread.sleep(500);

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        jmxHandler.connectToDataSource();

        Thread.sleep(1000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        // there should be quite a lot of properties
        assertTrue(((String) sdtv.getFirstValue(54675L).getValue()).length() > 0);

        System.out.println(sdtv.getFirstValue(54675L).getValue());
        // the description contains the whole map
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());
    }

}
