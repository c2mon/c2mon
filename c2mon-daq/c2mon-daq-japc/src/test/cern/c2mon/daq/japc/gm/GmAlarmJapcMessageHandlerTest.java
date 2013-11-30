/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.gm;

import static cern.japc.ext.mockito.JapcMatchers.anySelector;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.newSuperCycle;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static cern.japc.ext.mockito.JapcMock.spv;
import static cern.japc.ext.mockito.JapcMock.whenGetValueThen;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.japc.AbstractGenericJapcMessageHandlerTst;
import cern.c2mon.daq.japc.gm.GmJapcMessageHandler;
import cern.japc.Parameter;
import cern.japc.Selector;
import cern.japc.ext.mockito.Cycle;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * This class implements a set of JUnit tests for GmJapcMessageHandler. THe class uses mockito for JAPC simulation. All
 * tests that requiring GmJapcMessageHandler's pre-configuration with XML based configuration shall be annotated with
 * UseConf annotation, specifying the XML file to be used, and the handler class
 * 
 * @author wbuczak
 */
@UseHandler(GmJapcMessageHandler.class)
@Ignore("test temporarly disabled")
public class GmAlarmJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    static Logger log = Logger.getLogger(GmAlarmJapcMessageHandlerTest.class);

    @Test
    @UseConf("e_japc_test2.xml")
    public void subscription_Test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv1 = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv1), isA(SourceDataTagValue.class)));
        // exect that call only once - Note: one tag will not be invalidated twice in a sequence, with the same quality
        // code!
        expectLastCall().once();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P2");
        Selector s1 = sel("SPS.USER.SFTPRO");
        Selector s2 = sel("SPS.USER.CNGS");

        whenGetValueThen(p1, s1, spv(333), spv(333), spv(345), spv(345), spv(345), spv(345), spv(345));
        whenGetValueThen(p1, s2, spv(500), spv(520));

        // Simulation of SPS super cycle
        spsSupercycle = newSuperCycle(new Cycle("SPS.USER.SFTPRO", 200), new Cycle("SPS.USER.CNGS", 170));
        spsSupercycle.start();

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(54676L, (Object) sdtv1.getValue().getId());
        assertEquals(SourceDataQuality.OK, sdtv1.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscriptions
     */
    @Test
    @UseConf("e_japc_test4.xml")
    public void subscription_Test4() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().once();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("GTPMGRP.PSB/AlarmGroups");

        String[] fieldNames = { "field1", "groups", "totals", "field2" };
        String[] indexArray = { "index0", "index1", "PSB:EJ+TR-KICKERS", "index3" };
        int[] valueArray1 = { 0, 1, 2, 5, 4 };
        int[] valueArray2 = { 0, 1, 2, 3, 4 };
        int someSimpeValue = 100;

        Object[] values1 = { valueArray1, indexArray, valueArray2, someSimpeValue };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values1)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values1));

        Thread.sleep(150);

        verify(messageSender);
        assertEquals(54677L, (Object) sdtv.getValue().getId());
        assertEquals(null, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscription, which does not contain arrays, but fields of simple type. If the field
     * name is not specified in the configuration, the default one shall be taken.
     */
    @Test
    @UseConf("e_japc_gm_test1.xml")
    public void subscription_Test7() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("PR80.VPI71H/ALARM");

        whenGetValueThen(p1, anySelector(), spv(100));

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);
        assertEquals(100909L, (Object) sdtv.getValue().getId());
        assertEquals(100, sdtv.getValue().getValue());
        assertEquals("", sdtv.getValue().getValueDescription());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscription, but the configuration does not include the field that the DAQ expects to
     * be present.
     */
    @Test
    @UseConf("e_japc_clic_test1.xml")
    public void subscription_Test8() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("DIAMON.CLIC.CS-CCR-LASERTEST1.13999/Acquisition");

        String[] fieldNames = { "entries", "details" };

        String[] entries = { "field1", "sys.net.iowait", "field3" };
        String[] details = new String[] { "", "", "" };

        Object[] values = { entries, details };
        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values));

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
    }

}
