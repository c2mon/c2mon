/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc;

import static cern.japc.ext.mockito.JapcMatchers.anyParameterValue;
import static cern.japc.ext.mockito.JapcMatchers.anySelector;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.newSuperCycle;
import static cern.japc.ext.mockito.JapcMock.pe;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static cern.japc.ext.mockito.JapcMock.spv;
import static cern.japc.ext.mockito.JapcMock.whenGetValueThen;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.mockito.Matchers;

import cern.c2mon.daq.japc.JAPCMessageHandler;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.SubscriptionHandle;
import cern.japc.ext.mockito.Cycle;
import cern.japc.ext.mockito.JapcMock;
import cern.japc.ext.mockito.SuperCycle;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;
import cern.japc.factory.ParameterValueFactory;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * This class implements a set of JUnit tests for JAPCMessageHandler. THe class uses mockito for JAPC simulation. All
 * the tests that require JAPCMessageHandler's pre-configuration with XML based configuration shall be annotated with
 * UseConf annotation, specifying the XML file to be used.
 * 
 * @author wbuczak
 */
@UseHandler(value = JAPCMessageHandler.class)
public class JAPCMessageHandlerTest extends GenericMessageHandlerTst {

    JAPCMessageHandler japcHandler;

    static Logger log = Logger.getLogger(JAPCMessageHandlerTest.class);

    SuperCycle spsSupercycle;
    SuperCycle lhcSuperCycle;

    @Override
    protected void beforeTest() throws Exception {
        japcHandler = (JAPCMessageHandler) msgHandler;
        JapcMock.init();
        resetJapcMock();
    }

    @Override
    protected void afterTest() {
        stopMockSupercycles();
        JapcMock.resetJapcMock();
    }

    @Test
    @UseConf("e_japc_test1.xml")
    public void subscription_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().once();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        // by default the value defined below will be delivered right after the client subscribes
        whenGetValueThen(p1, anySelector(), spv(330));

        japcHandler.connectToDataSource();

        Thread.sleep(100);

        verify(messageSender);

        assertEquals(54675L, (Object) sdtv.getValue().getId());
        assertEquals(330, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());

    }

    @Test
    @UseConf("e_japc_test2.xml")
    public void subscription_Test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv1 = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv2 = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv1), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv2), isA(SourceDataTagValue.class)));

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

        Thread.sleep(800);

        verify(messageSender);

        assertEquals(54676L, (Object) sdtv1.getValue().getId());
        assertEquals(333, sdtv1.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv1.getValue().getQuality().getQualityCode());

        assertEquals(54676L, (Object) sdtv2.getValue().getId());
        assertEquals(345, sdtv2.getValue().getValue());

        assertEquals(345, sdtv2.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv2.getValue().getQuality().getQualityCode());

    }

    @Test
    @UseConf("e_japc_test3.xml")
    public void testStartMonitoringExceptionHandling() throws Exception {

        JapcMock.setSubscriptionAnswer(new JapcMock.SubscriptionAnswer() {

            @Override
            protected SubscriptionHandle createSubscription(Parameter mock, Selector selector,
                    ParameterValueListener parameterValueListener) {
                if ("D1/P2".equals(mock.getName()) & "SPS.USER.SFTPRO".equals(selector.getId())) {
                    SubscriptionHandle sh = createMock(SubscriptionHandle.class);
                    try {
                        sh.startMonitoring();
                        expectLastCall().andThrow(new ParameterException("Simulated1"));
                        expect(sh.getParameter()).andReturn(mock);
                        sh.stopMonitoring();
                        replay(sh);
                    } catch (Throwable t) { // should never happen
                    }

                    return sh;

                } else {
                    return super.createSubscription(mock, selector, parameterValueListener);
                }
            }
        });

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));

        expectLastCall().once();

        replay(messageSender);

        japcHandler.connectToDataSource();

        Thread.sleep(100);

        verify(messageSender);
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
        assertEquals("Unable to create subscription for tag: 54676. Problem description: Simulated1", sdtv.getValue()
                .getQuality().getDescription());
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

        Thread.sleep(100);

        verify(messageSender);
        assertEquals(54677L, (Object) sdtv.getValue().getId());
        assertEquals(2, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
    }

    @Test
    @UseConf("e_japc_test5.xml")
    public void subscription_Test5() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D4/P4");
        Selector s1 = sel("SPS.USER.SFTPRO");
        setAnswer(p1, s1, new DefaultParameterAnswer(new float[] { 10.2f, 8.3f, 33.4f }));

        japcHandler.connectToDataSource();

        p1.setValue(s1, spv(new float[] { 10.2f, 8.3f, 33.4f }));

        Thread.sleep(100);

        verify(messageSender);

        assertEquals(54678L, (Object) sdtv.getValue().getId());
        assertEquals(8.3f, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
    }

    @Test
    @UseConf("e_japc_test6.xml")
    public void subscription_Test6() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv2 = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv2), isA(SourceDataTagValue.class)));

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D4/P5");
        Selector s1 = sel("SPS.USER.SFTPRO");

        // prepare a 2d array
        SimpleParameterValue sval = ParameterValueFactory.newParameterValue(new float[] { 10.2f, 8.3f, 33.4f, 10.2f,
                8.5f, 33.4f }, new int[] { 2, 3 });

        setAnswer(p1, s1, new DefaultParameterAnswer(sval));

        japcHandler.connectToDataSource();

        p1.setValue(s1, sval);

        // prepare another 2d array - this time with wrong dimensions
        SimpleParameterValue sval2 = ParameterValueFactory.newParameterValue(new float[] { 10.2f, 8.3f, 33.4f, 10.2f,
                8.5f, 33.4f }, new int[] { 5, 6 });

        Thread.sleep(100);

        p1.setValue(s1, sval2);

        Thread.sleep(100);

        verify(messageSender);

        assertEquals(54679L, (Object) sdtv.getValue().getId());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());

        assertEquals(54679L, (Object) sdtv2.getValue().getId());
        assertEquals(SourceDataQuality.UNKNOWN, sdtv2.getValue().getQuality().getQualityCode());
    }

    @Test
    @UseConf("e_japc_test7.xml")
    public void commands_Test7() throws Exception {

        Parameter p1 = mockParameter("D7/P7");
        Parameter p2 = mockParameter("D8/P8");
        Parameter p3 = mockParameter("D9/P9");

        Selector s1 = sel("SPS.USER.TESTCYCLE");

        // Register behavior for setValue(..)
        doThrow(pe("Server is down")).when(p3).setValue(anySelector(), anyParameterValue());

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100847L, "TEST:TESTCMD1", 5250L, (short) 0, 100,
                    "Integer");

            japcHandler.sendCommand(sctv);

            MapParameterValue mpv = ParameterValueFactory.newParameterValue(new String[] { "field1" },
                    new SimpleParameterValue[] { spv(100) });

            org.mockito.Mockito.verify(p1).setValue(Matchers.eq(s1), Matchers.eq(mpv));

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100848L, "TEST:TESTCMD2", 5250L, (short) 0, 12.43f,
                    "Float");

            japcHandler.sendCommand(sctv);

            org.mockito.Mockito.verify(p2).setValue(anySelector(), Matchers.eq(spv(12.43f)));

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100849L, "TEST:TESTCMD1", 5250L, (short) 0, 100,
                    "Integer");

            japcHandler.sendCommand(sctv);

            fail("EqCommandTagException was expected at this point!");

        } catch (EqCommandTagException ex) {

        }

    }

    /**
     * this method tests mapped subscriptions (with fields each containing simple value obj)
     */
    @Test
    @UseConf("e_japc_test8.xml")
    public void subscription_Test8() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().once();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D8/P8");

        String[] fieldNames = { "field1", "field2", "field3" };

        Object[] values = { 10, 20, 30 };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values));

        Thread.sleep(150);

        verify(messageSender);
        assertEquals(54677L, (Object) sdtv.getValue().getId());
        assertEquals(10, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscription, which does not contain arrays, but fields of simple type. If the field
     * name is not specified in the configuration, the default one shall be taken.
     */
    @Test
    @UseConf("e_japc_test9.xml")
    public void subscription_Test9() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("cryo.S34.CryoStart/AMR3");

        String[] fieldNames = { "field1", "value", "timestamp" };

        Object[] values = { 10, 1, System.currentTimeMillis() };
        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values));

        Thread.sleep(100);

        verify(messageSender);
        assertEquals(100909L, (Object) sdtv.getValue().getId());
        assertEquals(true, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscription, which does not contain arrays, but fields of simple type. If the field
     * name is not specified in the configuration, the default one shall be taken. This time we test the behavior, when
     * not existing field name is given in the configuration
     */
    @Test
    @UseConf("e_japc_test10.xml")
    public void subscription_Test10() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("cryo.S34.CryoStart/AMR3");

        String[] fieldNames = { "field1", "value", "timestamp" };

        Object[] values = { 10, 1, System.currentTimeMillis() };
        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values));

        Thread.sleep(150);

        verify(messageSender);
        assertEquals(100909L, (Object) sdtv.getValue().getId());
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscriptions with arrays. The first update contains the whole structure, but the
     * following would only contain the things that have changes in the structure.
     */
    @Test
    @UseConf("e_japc_test11.xml")
    public void subscription_Test11() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv2 = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv2), isA(SourceDataTagValue.class)));
        // expectLastCall().once();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("GTPMGRP.PSB/AlarmGroups");

        String[] fieldNames = { "groups", "totals", "field2" };

        String[] indexArray = { "index0", "index1", "PSB:EJ+TR-KICKERS", "index3" };
        int[] valueArray1 = { 0, 1, 2, 5 };
        int someSimpeValue = 100;

        Object[] values1 = { indexArray, valueArray1, someSimpeValue };

        String[] indexArray2 = { "index0", "index1" }; // the next time only 2 elements in the array
        int[] valueArray2 = { 0, 1 };
        int someSimpeValue2 = 101;

        Object[] values2 = { indexArray2, valueArray2, someSimpeValue2 };

        String[] indexArray3 = { "PSB:EJ+TR-KICKERS" };
        int[] valueArray3 = { 6 };
        int someSimpeValue3 = 102;

        Object[] values3 = { indexArray3, valueArray3, someSimpeValue3 };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values1)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values1));

        Thread.sleep(100);

        // set the new value
        p1.setValue(null, mpv(fieldNames, values2));

        Thread.sleep(100);
        // set the new value
        p1.setValue(null, mpv(fieldNames, values3));

        Thread.sleep(100);

        verify(messageSender);
        assertEquals(54677L, (Object) sdtv.getValue().getId());
        assertEquals(2, sdtv.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());

        assertEquals(54677L, (Object) sdtv2.getValue().getId());
        assertEquals(6, sdtv2.getValue().getValue());
        assertEquals(SourceDataQuality.OK, sdtv2.getValue().getQuality().getQualityCode());
    }

    @Test
    public void testConvertSourceTimestampToMs() {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, 1, 15);
        assertEquals(cal.getTimeInMillis() * 1000, JAPCMessageHandler.convertSourceTimestampToMs(cal.getTimeInMillis()));

        cal.set(2010, 8, 17);

        assertEquals(cal.getTimeInMillis(), JAPCMessageHandler.convertSourceTimestampToMs(cal.getTimeInMillis()));
    }

    public void stopMockSupercycles() {

        if (spsSupercycle != null) {
            spsSupercycle.stop();
            spsSupercycle = null;
        }

        if (lhcSuperCycle != null) {
            lhcSuperCycle.stop();
            lhcSuperCycle = null;
        }
    }

}
