/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.common.vcm;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.EasyMock.expect;

import static cern.tim.shared.common.datatag.ValueChangeMonitor.OPERATOR;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ValueChangeMonitorEngine;
import cern.c2mon.daq.common.vcm.testhandler.EspMessageHandler;
import cern.c2mon.daq.common.vcm.testhandler.GenericMessageHandlerTst;
import cern.c2mon.daq.common.vcm.testhandler.SourceDataTagValueCapture;
import cern.c2mon.daq.common.vcm.testhandler.UseConf;
import cern.c2mon.daq.common.vcm.testhandler.UseHandler;
import cern.tim.shared.common.datatag.ValueChangeMonitor;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements a set of JUnit tests for <code>JMXMessageHandler</code>. All tests that require
 * JMXMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.esp.EspMessageHandler
 * @author wbuczak
 */
@UseHandler(EspMessageHandler.class)
public class ValueChangeMonitorEngineTest extends GenericMessageHandlerTst {

    DriverKernel kernel;

    static Logger log = Logger.getLogger(ValueChangeMonitorEngineTest.class);

    static {
        System.setProperty("dmn2.daq.esp.interval", "1");
    }

    EspMessageHandler espHandler;

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        espHandler = (EspMessageHandler) msgHandler;

        kernel = createMock(DriverKernel.class);

        Map<Long, EquipmentMessageHandler> handlersMap = new HashMap<Long, EquipmentMessageHandler>();
        handlersMap.put(5250L, espHandler);

        expect(kernel.getEquipmentMessageHandlersTable()).andReturn(handlersMap).atLeastOnce();
        replay(kernel);

        // normally kernel will be autowired by spring
        ValueChangeMonitorEngine.getInstance().setDriverKernel(kernel);

        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("entering afterTest()..");

        espHandler.disconnectFromDataSource();

        ValueChangeMonitorEngine.stop();

        log.info("leaving afterTest()");
        Thread.sleep(500);
    }

    @Test
    @UseConf("conf-vcm-two-metrics-with-time-win-and-step.xml")
    public void checkValueCheckMonitorFactors() throws Exception {

        espHandler.skip(54675L, true);
        espHandler.skip(54676L, true);

        ISourceDataTag tag1 = espHandler.getEquipmentConfiguration().getSourceDataTag(54675L);
        ISourceDataTag tag2 = espHandler.getEquipmentConfiguration().getSourceDataTag(54676L);

        espHandler.connectToDataSource();

        Thread.sleep(1000);

        assertTrue(tag1.hasValueCheckMonitor());
        assertNotNull(tag1.getValueCheckMonitor());

        ValueChangeMonitor m1 = tag1.getValueCheckMonitor();
        assertEquals(OPERATOR.MORE, m1.getOperator());
        assertTrue(m1.hasStep());
        assertEquals(0.99f, m1.getStep().floatValue(), 0.0);
        assertTrue(m1.hasTimeWindow());
        assertEquals(2000, m1.getTimeWindow().intValue());

        assertTrue(tag2.hasValueCheckMonitor());
        assertNotNull(tag2.getValueCheckMonitor());

        ValueChangeMonitor m2 = tag2.getValueCheckMonitor();
        assertEquals(OPERATOR.MORE, m2.getOperator());
        assertTrue(m2.hasStep());
        assertEquals(0.99f, m2.getStep().floatValue(), 0.0);
        assertTrue(m2.hasTimeWindow());
        assertEquals(2000, m2.getTimeWindow().intValue());

    }

    @Test
    @UseConf("conf-vcm-single-metric-with-time-win-and-step.xml")
    public void test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        espHandler.skip(54675L, true);

        espHandler.connectToDataSource();

        Thread.sleep(2200);

        espHandler.skip(54675L, false);

        Thread.sleep(2200);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getLastValue(54675L).getValue());
    }

    @Test
    @UseConf("conf-vcm-two-metrics-with-time-win-and-step.xml")
    public void test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        espHandler.skip(54675L, true);
        espHandler.skip(54676L, true);

        espHandler.connectToDataSource();

        Thread.sleep(2200);

        espHandler.skip(54676L, false);

        Thread.sleep(2100);

        verify(messageSender);

        assertEquals(1, sdtv.getNumberOfCapturedValues(54675L));
        assertEquals(2, sdtv.getNumberOfCapturedValues(54676L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54676L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getLastValue(54676L).getValue());
    }

    @Test
    @UseConf("conf-vcm-two-metrics-with-time-win-and-step.xml")
    public void test3() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        espHandler.skip(54675L, true);
        espHandler.skip(54676L, true);

        espHandler.connectToDataSource();

        Thread.sleep(1500);

        espHandler.skip(54676L, false);

        Thread.sleep(2500);

        espHandler.skip(54675L, false);

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));
        assertEquals(2, sdtv.getNumberOfCapturedValues(54676L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getLastValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54676L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getLastValue(54676L).getValue());
    }

    @Test
    @UseConf("conf-vcm-single-metric-with-time-win-only.xml")
    public void test4() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // do not increase the value in iterations
        espHandler.setStep(54675L, 0);
        espHandler.skip(54675L, true);

        espHandler.connectToDataSource();
        Thread.sleep(2400);

        espHandler.skip(54675L, false);

        Thread.sleep(3500);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getLastValue(54675L).getValue());
    }

    @Test
    @UseConf("conf-vcm-single-metric-with-step-only.xml")
    public void test5() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // do not increase the value in iterations
        espHandler.setStep(54675L, 2);
        espHandler.skip(54675L, false);

        espHandler.connectToDataSource();
        Thread.sleep(2200);

        espHandler.setStep(54675L, 1);

        Thread.sleep(2000);

        espHandler.skip(54675L, true);

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getLastValue(54675L).getValue());

    }

}
