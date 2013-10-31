/*
 * Copyright CERN 2012, All Rights Reserved.
 */
package cern.c2mon.daq.cmwadmin;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.cmwadmin.CMWServerHandler;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This class implements a set of JUnit tests for <code>CMWServerHandler</code>. All tests that require
 * CMWServerHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 * 
 * @see
 * @see cern.c2mon.daq.cmwadmin.CMWServerHandler
 * @author wbuczak
 */
@UseHandler(CMWServerHandler.class)
public class CMWServerHandlerTest2 extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(CMWServerHandlerTest2.class);

    CMWServerHandler handler;

    @Override
    protected void beforeTest() throws Exception {
        handler = (CMWServerHandler) msgHandler;
    }

    @Override
    protected void afterTest() throws Exception {
        handler.disconnectFromDataSource();
    }

    @Test
    @UseConf("e_cmwadmin_test_conf_1.xml")
    public void testStartStopHandler() throws Exception {

        Capture<Long> id1 = new Capture<Long>();
        Capture<Boolean> val1 = new Capture<Boolean>();
        Capture<String> msg1 = new Capture<String>();

        Capture<Long> id2 = new Capture<Long>();
        Capture<Boolean> val2 = new Capture<Boolean>();
        Capture<String> msg2 = new Capture<String>();

        messageSender.sendCommfaultTag(EasyMock.capture(id1), EasyMock.capture(val1), EasyMock.capture(msg1));
        messageSender.sendCommfaultTag(EasyMock.capture(id2), EasyMock.capture(val2), EasyMock.capture(msg2));

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        // expectLastCall().times(2);
        expectLastCall().atLeastOnce();

        replay(messageSender);

        try {
            handler.connectToDataSource();
        } catch (EqIOException ex) {
            fail("EqIOException was  NOT expected");
        }

        Thread.sleep(5000);

        verify(messageSender);

        assertEquals(107211L, id1.getValue().longValue());
        assertEquals(true, val1.getValue());
        assertEquals("initial connection state", msg1.getValue());

        assertEquals(107211L, id2.getValue().longValue());
        assertEquals(false, val2.getValue());
        assertEquals("connection timed out", msg2.getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(52501L).getQuality().getQualityCode());
        assertEquals(false, sdtv.getFirstValue(52501L).getValue());

        System.out.println(sdtv.getFirstValue(52501L).getValueDescription());

        assertTrue(sdtv.getFirstValue(52501L).getValueDescription().contains("down or unreachable"));
    }

}
