/*
 * Copyright CERN 2014, All Rights Reserved.
 */
package cern.c2mon.daq.almon;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMatchers.anySelector;
import static cern.japc.ext.mockito.JapcMock.acqVal;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.setAnswer;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.japc.ext.mockito.JapcMock;

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.japc.Parameter;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;

/**
 * This class implements a set of JUnit tests for <code>AlmonMessageHandler</code>. All tests that require
 * AlmonMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see cern.c2mon.daq.almon.AlmonMessageHandler
 * @author wbuczak
 */
@UseHandler(AlmonMessageHandler.class)
public class AlmonMessageHandlerTest extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(AlmonMessageHandlerTest.class);

    static {
        // activate the TEST spring profile
        System.setProperty("spring.profiles.active", "TEST");
    }

    AlmonMessageHandler emh;

    @Override
    protected void beforeTest() throws Exception {
        log.info("entering beforeTest()..");
        emh = (AlmonMessageHandler) msgHandler;

        JapcMock.init();
        resetJapcMock();
        
        log.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        log.info("entering afterTest()..");

        emh.disconnectFromDataSource();

        log.info("leaving afterTest()");
    }

    @Test
    @UseConf("conf-gm-one-metric.xml")
    public void test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(1);

        replay(messageSender);        
                
        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 1 };
        //Object[] values2 = { 0 };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        emh.connectToDataSource();

        Thread.sleep(2500);

        // set the new value
        p1.setValue(null, mpv(fields, values1));

        
        Thread.sleep(1500);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getFirstValue(54675L).getValue());      
    }

    // @Test
    // @UseConf("conf-ping-three-metrics.xml")
    public void test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        emh.connectToDataSource();

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

    // @Test
    // @UseConf("conf-ping-two-correct-one-incorrect-metrics.xml")
    public void test3() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        emh.connectToDataSource();

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
    // @Test
    // @UseConf("integration-test-ten-metrics.xml")
    public void test4() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(9);

        replay(messageSender);

        emh.connectToDataSource();

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
