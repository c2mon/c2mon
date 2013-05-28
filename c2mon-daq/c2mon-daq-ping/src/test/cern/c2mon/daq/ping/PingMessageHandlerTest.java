/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.ping;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.tim.driver.test.GenericMessageHandlerTst;
import cern.tim.driver.test.SourceDataTagValueCapture;
import cern.tim.driver.test.UseConf;
import cern.tim.driver.test.UseHandler;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements a set of JUnit tests for <code>JMXMessageHandler</code>. All tests that require
 * JMXMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
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

    PingMessageHandler pingHandler;

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        pingHandler = (PingMessageHandler) msgHandler;

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

    /**
     * NOTE: this test is an integration test, run agains real devices. It should be commented out before commiting to
     * SVN because if the machines availability changes, this would cause that test to fail. It should only be
     * uncommented to run from local development machine, by a developer.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("integration-test-ten-metrics.xml")
    public void testPing4() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(5, 9);

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
