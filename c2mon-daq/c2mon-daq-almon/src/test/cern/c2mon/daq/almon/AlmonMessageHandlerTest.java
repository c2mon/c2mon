/*
 * Copyright CERN 2014, All Rights Reserved.
 */
package cern.c2mon.daq.almon;

import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.japc.Parameter;
import cern.japc.Selector;
import cern.japc.ext.mockito.JapcMock;
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
public class AlmonMessageHandlerTest extends /* GenericMessageHandlerTst */AlmonDynReconfMessageHandlerTest {

    static Logger LOG = Logger.getLogger(AlmonMessageHandlerTest.class);

    static final Selector GM_DEVICE_SELECTOR = sel(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR);

    @Override
    protected void beforeTest() throws Exception {
        LOG.info("entering beforeTest()..");
        emh = (AlmonMessageHandler) msgHandler;
        JapcMock.init();
        resetJapcMock();

        LOG.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        LOG.info("entering afterTest()..");

        LOG.info("leaving afterTest()");
    }

    @Test
    @UseConf("conf-gm-one-metric.xml")
    public void testGmAlarmActivation() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 1 };
        Object[] values2 = { 0 };

        setAnswer(p1, GM_DEVICE_SELECTOR, new DefaultParameterAnswer(mpv(fields, values1)));

        Thread.sleep(300);

        emh.connectToDataSource();

        Thread.sleep(1000);

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values1));
        Thread.sleep(300);
        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values2));

        Thread.sleep(500);

        verify(messageSender);

        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, sdtv.getFirstValue(54675L).getValue());
        assertNotNull(sdtv.getFirstValue(54675L).getValueDescription());
        UserProperties uprops = UserProperties.fromJson(sdtv.getFirstValue(54675L).getValueDescription());
        assertNotNull(uprops);
        assertTrue(uprops.isEmpty());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, sdtv.getLastValue(54675L).getValue());
        assertNotNull(sdtv.getLastValue(54675L).getValueDescription());
        uprops = UserProperties.fromJson(sdtv.getLastValue(54675L).getValueDescription());
        assertNull(uprops);
    }

}
