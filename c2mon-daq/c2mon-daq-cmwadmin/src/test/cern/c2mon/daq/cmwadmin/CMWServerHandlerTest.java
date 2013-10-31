/*
 * Copyright CERN 2012, All Rights Reserved.
 */
package cern.c2mon.daq.cmwadmin;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.BeforeClass;
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
 * @author mbuttner
 */
@UseHandler(CMWServerHandler.class)
public class CMWServerHandlerTest extends GenericMessageHandlerTst {

    static Logger log = Logger.getLogger(CMWServerHandlerTest.class);

    CMWServerHandler handler;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("cmw.directory.list", "cs-ccr-ctb07:5021");
    }

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

        Capture<Long> id = new Capture<Long>();
        Capture<Boolean> val = new Capture<Boolean>();
        Capture<String> msg = new Capture<String>();

        messageSender.sendCommfaultTag(EasyMock.capture(id), EasyMock.capture(val), EasyMock.capture(msg));
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        try {
            handler.connectToDataSource();
        } catch (EqIOException ex) {
            fail("EqIOException was  NOT expected");
        }

        Thread.sleep(5000);

        verify(messageSender);

        assertEquals(107211L, id.getValue().longValue());
        assertEquals(true, val.getValue());
        assertEquals("initial connection state", msg.getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(52501L).getQuality().getQualityCode());
        assertEquals(true, sdtv.getFirstValue(52501L).getValue());
        assertEquals("", sdtv.getFirstValue(52501L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(52502L).getQuality().getQualityCode());
        assertEquals(0, sdtv.getFirstValue(52502L).getValue());
        assertEquals("", sdtv.getFirstValue(52502L).getValueDescription());

    }

    @Test
    @UseConf("e_cmwadmin_test_conf_3.xml")
    public void tetEqAddressNull() throws Exception {
        try {
            handler.connectToDataSource();
            fail("EqIOException was expected");
        } catch (EqIOException ex) {
            assertEquals("equipment address must NOT be null. Check DAQ configuration!", ex.getMessage());
        }

    }

}
