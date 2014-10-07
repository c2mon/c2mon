/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.ping;

import static cern.c2mon.daq.ping.Target.PingStatus.Reachable;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * This class implements a set of JUnit tests for <code>PingMessageHandler</code>. All tests that require
 * PingMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.ping.PingMessageHandler
 * @author wbuczak
 */
@UseHandler(PingMessageHandler.class)
public class PingMessageHandlerTest extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(PingMessageHandlerTest.class);

    static {
        System.setProperty("dmn2.daq.ping.interval", "4");
        System.setProperty("dmn2.daq.ping.dns_refresh_address", "5");
        System.setProperty("dmn2.daq.ping.timeout", "500");
    }

    static enum EVENT {
        REGISTER,
        UNREGISTER;
    };

    PingMessageHandler pingHandler;

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        pingHandler = (PingMessageHandler) msgHandler;

        configurationController = new ConfigurationController(null, null);
        configurationController.setProcessConfiguration(pconf);
        pconf.getEquipmentConfigurations().put(equipmentConfiguration.getId(), equipmentConfiguration);
        configurationController.putImplementationDataTagChanger(equipmentConfiguration.getId(), pingHandler);

        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("entering afterTest()..");
        pingHandler.disconnectFromDataSource();
        log.info("leaving afterTest()");
    }

    @Test
    @UseConf("conf-ping-single-metric.xml")
    public void testPing1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(1);

        replay(messageSender);

        pingHandler.connectToDataSource();

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54675L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54675L).getValueDescription());
    }

    @Test
    @UseConf("conf-ping-three-metrics.xml")
    public void testPing2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        pingHandler.connectToDataSource();

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54676L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54677L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54677L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54677L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54678L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54678L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54678L).getValueDescription());
    }

    @Test
    @UseConf("conf-ping-two-correct-one-incorrect-metrics.xml")
    public void testPing3() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        pingHandler.connectToDataSource();

        Thread.sleep(2500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54676L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54677L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54677L).getValue());
        assertEquals("Target host: localhost replied within timeout", sdtv.getFirstValue(54677L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54678L).getQuality().getQualityCode());
        assertEquals(3, sdtv.getFirstValue(54678L).getValue());
        assertEquals("Unknown host: unexisting-host", sdtv.getFirstValue(54678L).getValueDescription());
    }

    @Test
    @UseConf("conf-ping-single-metric.xml")
    public void testReconfiguration() throws Exception {

        Thread.sleep(2500);

        final ArrayList<ChangeReport> reports1 = new ArrayList<>();
        final ArrayList<ChangeReport> reports2 = new ArrayList<>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        pingHandler.connectToDataSource();

        // give the chance to the first ping update to be delivered
        Thread.sleep(2500);

        StringBuilder str = new StringBuilder();

        str.append("<DataTag id=\"54675\" name=\"BE.TEST:TEST1\" control=\"false\">");
        str.append("  <data-type>Integer</data-type>");
        str.append("  <DataTagAddress>");
        str.append("    <HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl\">");
        str.append("       <address>localhost</address>");
        str.append("    </HardwareAddress>");
        str.append("    <time-to-live>3600000</time-to-live>");
        str.append("    <priority>2</priority>");
        str.append("    <guaranteed-delivery>false</guaranteed-delivery>");
        str.append("  </DataTagAddress>");
        str.append("</DataTag>");

        // trigger adding new DataTag
        final SourceDataTag newTag = SourceDataTag.fromConfigXML(new SimpleXMLParser().parse(str.toString())
                .getDocumentElement());

        final int EVENTS_NUMBER = 6;
        final BlockingQueue<EVENT> eventsQueue = new ArrayBlockingQueue<>(EVENTS_NUMBER);

        for (int i = 0; i < EVENTS_NUMBER; ++i) {
            if (i % 2 == 0) {
                eventsQueue.add(EVENT.UNREGISTER);
            } else {
                eventsQueue.add(EVENT.REGISTER);
            }
        }

        final class Job implements Runnable {

            @Override
            public void run() {
                try {

                    EVENT e = eventsQueue.take();

                    switch (e) {
                    case REGISTER:

                        // no need of synchronized list as there's only 1 thread operating on it
                        reports1.add(configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration
                                .getId(), newTag)));
                        break;

                    case UNREGISTER:

                        // no need of synchronized list as there's only 1 thread operating on it
                        reports2.add(configurationController.onDataTagRemove(new DataTagRemove(1L, newTag.getId(),
                                equipmentConfiguration.getId())));
                        break;
                    }

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }

        ExecutorService pool = Executors.newSingleThreadExecutor();
        for (int i = 0; i < EVENTS_NUMBER; i++) {
            pool.submit(new Job());
        }

        pool.awaitTermination(3000, TimeUnit.MILLISECONDS);
        pool.shutdown();

        verify(messageSender);

        for (ChangeReport cr : reports1) {
            assertEquals(CHANGE_STATE.SUCCESS, cr.getState());
        }
        for (ChangeReport cr : reports2) {
            assertEquals(CHANGE_STATE.SUCCESS, cr.getState());
        }

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));
        SourceDataTagValue value1 = sdtv.getFirstValue(54675L);
        SourceDataTagValue value2 = sdtv.getValueAt(1, 54675L);

        assertEquals(SourceDataQuality.OK, value1.getQuality().getQualityCode());
        assertEquals(SourceDataQuality.OK, value2.getQuality().getQualityCode());

        assertEquals(Reachable.getCode(), value1.getValue());
        assertEquals(Reachable.getCode(), value2.getValue());
    }

    /**
     * NOTE: this test is an integration test, run agains real devices. It should be commented out before commiting to
     * SVN because if the machines availability changes, this would cause that test to fail. It should only be
     * uncommented to run from local development machine, by a developer.
     * 
     * @throws Exception
     */
    // @Test
    @UseConf("integration-test-ten-metrics.xml")
    public void testPing4() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(9);

        replay(messageSender);

        pingHandler.connectToDataSource();

        Thread.sleep(3000);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54670L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54670L).getValue());
        assertEquals("Target host: cs-ccr-diam1 replied within timeout", sdtv.getFirstValue(54670L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54671L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54671L).getValue());
        assertEquals("Target host: cs-ccr-diam2 replied within timeout", sdtv.getFirstValue(54671L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54672L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54672L).getValue());
        assertEquals("Target host: cs-ccr-diam3 replied within timeout", sdtv.getFirstValue(54672L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54673L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54673L).getValue());
        assertEquals("Target host: cs-ccr-dmnt1 replied within timeout", sdtv.getFirstValue(54673L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54674L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54674L).getValue());
        assertEquals("Target host: cs-ccr-dmnp1 replied within timeout", sdtv.getFirstValue(54674L)
                .getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54675L).getValue());
        assertEquals("Destination host: dleirf2 unreachable", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54676L).getValue());
        assertEquals("Destination host: v3n351 unreachable", sdtv.getFirstValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54677L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54677L).getValue());
        assertEquals("Destination host: v3n201 unreachable", sdtv.getFirstValue(54677L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54678L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getFirstValue(54678L).getValue());
        assertEquals("Destination host: v3n117 unreachable", sdtv.getFirstValue(54678L).getValueDescription());

    }
}