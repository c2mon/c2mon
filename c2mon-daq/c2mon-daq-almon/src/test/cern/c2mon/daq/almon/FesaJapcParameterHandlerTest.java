/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import static cern.c2mon.daq.almon.FesaJapcParameterHandler.CYCLE_NAME_FIELD;
import static cern.c2mon.daq.almon.FesaJapcParameterHandler.FESA_PLS_LINE_USER_PROPERTY;
import static cern.c2mon.daq.almon.FesaJapcParameterHandler.NAMES_ARRAY_FIELD;
import static cern.c2mon.daq.almon.FesaJapcParameterHandler.PREFIXES_ARRAY_FIELD;
import static cern.c2mon.daq.almon.FesaJapcParameterHandler.SUFFIXES_ARRAY_FIELD;
import static cern.c2mon.daq.almon.FesaJapcParameterHandler.TIMESTAMPS_ARRAY_FIELD;
import static cern.c2mon.daq.almon.JapcParameterHandler.ALARM_MON_FAULT_FAMILY;
import static cern.c2mon.daq.almon.JapcParameterHandler.ASI_PREFIX_PROPERTY;
import static cern.c2mon.daq.almon.JapcParameterHandler.ASI_SUFFIX_PROPERTY;
import static cern.japc.ext.mockito.JapcMock.acqVal;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.newSuperCycle;
import static cern.japc.ext.mockito.JapcMock.pe;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.daq.almon.address.AlarmTriplet;
import cern.c2mon.daq.almon.address.AlarmType;
import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.impl.AlmonHardwareAddressImpl;
import cern.c2mon.daq.almon.plsline.PlsLineResolver;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.almon.sender.TestAlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.japc.Parameter;
import cern.japc.Selector;
import cern.japc.ext.mockito.Cycle;
import cern.japc.ext.mockito.JapcMock;
import cern.japc.ext.mockito.SuperCycle;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;

/**
 * The <code>GmJapcParameterHandlerTest</code> tests FESA alarm activation/termination algorithm.
 * 
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/dmn-almon-config.xml")
@ActiveProfiles("TEST")
public class FesaJapcParameterHandlerTest {

    public static final String EX_SERVER_DOWN = "Server is down";
    public static final String EX_DEVICE_UNKNOWN = "Device is not known by naming server";

    public static final Selector SELECTOR1 = sel(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR);

    JapcParameterHandler handler1, handler2, handler3, handler4;

    @Resource
    TestAlmonSender mockSender;

    @Resource(name = "almonSenderProxy")
    AlmonSender senderProxy;

    @Resource
    PlsLineResolver plsLineResolver;

    ISourceDataTag sdt;

    IEquipmentMessageSender ems;

    @Before
    public void before() {
        resetJapcMock();
        JapcMock.init();

        sdt = Mockito.mock(ISourceDataTag.class);
        when(sdt.getId()).thenReturn(1000L);
        
        ems = mock(IEquipmentMessageSender.class);        
    }

    @After
    public void after() {
        try {
            if (handler1 != null)
                handler1.stopMonitoring();
        } catch (Exception ex) {
            // ignore, that's just test clean-up
        }
        try {
            if (handler2 != null)
                handler2.stopMonitoring();
        } catch (Exception ex) {
            // ignore, that's just test clean-up
        }
        try {
            if (handler3 != null)
                handler3.stopMonitoring();
        } catch (Exception ex) {
            // ignore, that's just test clean-up
        }
        try {
            if (handler4 != null)
                handler4.stopMonitoring();
        } catch (Exception ex) {
            // ignore, that's just test clean-up
        }
    }

    @Test(timeout = 10000)
    @DirtiesContext
    public void testAlarmsActivationAndTermination() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("PR.TFB-AMP/Alarm");

        String[] fields = { NAMES_ARRAY_FIELD, TIMESTAMPS_ARRAY_FIELD, CYCLE_NAME_FIELD, PREFIXES_ARRAY_FIELD,
                SUFFIXES_ARRAY_FIELD };

        String[] namesArray1 = { "DAMPERPS_100010" };
        String[] namesArray2 = { "DAMPERPS_100020" };

        long[] timestampsArray1 = { 1389175345532888003L };
        long[] timestampsArray2 = { 1389175345532987834L };

        String[] prefixesArray = { "" };
        String[] suffixesArray = { "" };

        Object[] values1 = { namesArray1, timestampsArray1, "user1", prefixesArray, suffixesArray };
        Object[] values2 = { namesArray2, timestampsArray2, "", prefixesArray, suffixesArray };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        AlarmTriplet alarmTriplet = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 1000);
        AlmonHardwareAddress address1 = new AlmonHardwareAddressImpl(AlarmType.FESA, "PR.TFB-AMP", "Alarm",
                "DAMPERPS_100010", alarmTriplet);

        AlarmTriplet alarmTriplet2 = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 3000);
        AlmonHardwareAddress address2 = new AlmonHardwareAddressImpl(AlarmType.FESA, "PR.TFB-AMP", "Alarm",
                "DAMPERPS_100020", alarmTriplet2);

        handler1 = new FesaJapcParameterHandler(sdt, address1, ems, senderProxy, plsLineResolver);
        handler1.startMonitoring();

        handler2 = new FesaJapcParameterHandler(sdt, address2, ems, senderProxy, plsLineResolver);
        handler2.startMonitoring();

        Thread.sleep(200);

        p1.setValue(null, mpv(fields, values2));

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 2) {
            Thread.sleep(50);
        }

        List<AlarmRecord> records = mockSender.getAlarmsSequence(alarmTriplet);

        assertEquals(2, records.size());
        AlarmRecord r1 = records.get(0);
        AlarmRecord r2 = records.get(1);

        assertEquals(AlarmState.ACTIVE, r1.getAlarmState());
        assertEquals(1389175345532888003L / 1000000, r1.getUserTimestamp());

        Properties p = r1.getUserProperties();
        assertNotNull(p);
        assertEquals("user1", p.get(FesaJapcParameterHandler.FESA_CYCLE_NAME_USER_PROPERTY));
        assertEquals("1", p.get(FesaJapcParameterHandler.FESA_PLS_LINE_USER_PROPERTY));

        assertEquals(AlarmState.TERMINATED, r2.getAlarmState());

        while (mockSender.getAlarmsSequence(alarmTriplet2).size() < 1) {
            Thread.sleep(50);
        }

        records = mockSender.getAlarmsSequence(alarmTriplet2);

        assertEquals(1, records.size());
        r1 = records.get(0);
        assertEquals(AlarmState.ACTIVE, r1.getAlarmState());
        assertEquals(1389175345532987834L / 1000000, r1.getUserTimestamp());
        p = r1.getUserProperties();
        assertNotNull(p);
        assertNull(p.get(FesaJapcParameterHandler.FESA_CYCLE_NAME_USER_PROPERTY));
        assertNull(p.get(FesaJapcParameterHandler.FESA_PLS_LINE_USER_PROPERTY));

    }

    // @Test(timeout = 10000)
    @DirtiesContext
    public void testParameterExceptionHandling() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("PR.TFB-AMP/Alarm");
        // Parameter p2 = mockParameter("PR.TFB-AMP/Alarm");

        String[] fields = { NAMES_ARRAY_FIELD, TIMESTAMPS_ARRAY_FIELD, CYCLE_NAME_FIELD, PREFIXES_ARRAY_FIELD,
                SUFFIXES_ARRAY_FIELD };

        String[] namesArray1 = { "DAMPERPS_100010", "DAMPERPS_100020" };

        long[] timestampsArray1 = { 1389175345532888003L, 1389175345532987834L };

        String[] prefixesArray = { "" };
        String[] suffixesArray = { "" };

        Object[] values1 = { namesArray1, timestampsArray1, "user1", prefixesArray, suffixesArray };

        // register behavior - by default throw exception simulating the server is down, followed by an active alarm
        when(p1.getValue(null)).thenThrow(pe(EX_SERVER_DOWN)).thenThrow(pe(EX_SERVER_DOWN))
                .thenReturn(acqVal("PR.TFB-AMP/Alarm", mpv(fields, values1)));

        // FesaParameter fesaParameter = new FesaParameter("PR.TFB-AMP", "Alarm", "DAMPERPS_100010", "NONE",
        // "ADTPSMODULE_210", "PR.TFB-AMP", 1000);
        //
        // FesaParameter fesaParameter2 = new FesaParameter("PR.TFB-AMP", "Alarm", "DAMPERPS_100020", "NONE",
        // "ADTPSMODULE_210", "PR.TFB-AMP", 2000);

        AlarmTriplet alarmTriplet = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 1000);
        AlmonHardwareAddress address1 = new AlmonHardwareAddressImpl(AlarmType.FESA, "PR.TFB-AMP", "Alarm",
                "DAMPERPS_100010", alarmTriplet);

        AlarmTriplet alarmTriplet2 = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 2000);
        AlmonHardwareAddress address2 = new AlmonHardwareAddressImpl(AlarmType.FESA, "PR.TFB-AMP", "Alarm",
                "DAMPERPS_100020", alarmTriplet);

        handler1 = new FesaJapcParameterHandler(sdt, address1, ems, senderProxy, plsLineResolver);
        handler2 = new FesaJapcParameterHandler(sdt, address2, ems, senderProxy, plsLineResolver);
        handler1.startMonitoring();
        handler2.startMonitoring();

        SuperCycle superCycle = newSuperCycle(new Cycle("", 200));
        superCycle.start();

        AlarmTriplet deviceAccessFaultTriplet = new AlarmTriplet(ALARM_MON_FAULT_FAMILY, "PR.TFB-AMP", 2);

        while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 2) {
            Thread.sleep(50);
        }

        List<AlarmRecord> records = mockSender.getAlarmsSequence(deviceAccessFaultTriplet);
        assertEquals(2, records.size());
        AlarmRecord r1 = records.get(0);
        AlarmRecord r2 = records.get(1);
        assertEquals(AlarmState.ACTIVE, r1.getAlarmState());
        assertEquals(AlarmState.TERMINATED, r2.getAlarmState());

        // AlarmTriplet alarmTriplet = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 1000);

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 1) {
            Thread.sleep(50);
        }

        records = mockSender.getAlarmsSequence(alarmTriplet);
        assertEquals(1, records.size());
        r1 = records.get(0);
        assertEquals(AlarmState.ACTIVE, r1.getAlarmState());
    }

    /**
     * The goal of this tests it to verify alarm monitor correctly handles activations/terminations of the device access
     * fault alarms.
     * 
     * @throws Exception
     */
    // @Test(timeout = 4000)
    @DirtiesContext
    public void testParameterExceptionHandling2() throws Exception {

        // Expected algorithm is following: - if more than 1 parameters are registered for the same device (common in
        // FESA), then:
        // - if device access alarm has been activated (e.g. RDA equipment is down) and a call to stop monitoring of one
        // of the parameters is done (usually when one of the alarms is unregistered from the db and that change is
        // detected by the alarm monitor's periodic db polling job) then the device fault alarm should STILL stay
        // active, because there are still some parameters registered, linked to that device.
        // ONLY after the last parameter belonging to the device is unregistered (in practice it means - the whole alarm
        // sets for that device is removed) should the alarm monitor terminate the device access fault alarm.
        //

        /*
         * // create mock parameter Parameter p1 = mockParameter("PR.TFB-AMP/Alarm");
         * 
         * // register behavior - by default throw exception simulating the server is down
         * when(p1.getValue(null)).thenThrow(pe(EX_SERVER_DOWN));
         * 
         * // 4 FESA parameters, pointing to the same device FesaParameter fesaParameter1 = new
         * FesaParameter("PR.TFB-AMP", "Alarm", "DAMPERPS_100010", "NONE", "ADTPSMODULE_210", "PR.TFB-AMP", 1000);
         * FesaParameter fesaParameter2 = new FesaParameter("PR.TFB-AMP", "Alarm", "DAMPERPS_100020", "NONE",
         * "ADTPSMODULE_210", "PR.TFB-AMP", 2000); FesaParameter fesaParameter3 = new FesaParameter("PR.TFB-AMP",
         * "Alarm", "DAMPERPS_100030", "NONE", "ADTPSMODULE_210", "PR.TFB-AMP", 3000); FesaParameter fesaParameter4 =
         * new FesaParameter("PR.TFB-AMP", "Alarm", "DAMPERPS_100040", "NONE", "ADTPSMODULE_210", "PR.TFB-AMP", 4000);
         * 
         * handler1 = new FesaJapcParameterHandler(fesaParameter1, senderProxy, plsLineResolver); handler2 = new
         * FesaJapcParameterHandler(fesaParameter2, senderProxy, plsLineResolver); handler3 = new
         * FesaJapcParameterHandler(fesaParameter3, senderProxy, plsLineResolver); handler4 = new
         * FesaJapcParameterHandler(fesaParameter4, senderProxy, plsLineResolver);
         * 
         * handler1.startMonitoring(); handler2.startMonitoring(); handler3.startMonitoring();
         * handler4.startMonitoring();
         * 
         * SuperCycle superCycle = newSuperCycle(new Cycle("", 200)); superCycle.start();
         * 
         * AlarmTriplet deviceAccessFaultTriplet = new AlarmTriplet(ALARM_MON_FAULT_FAMILY, "PR.TFB-AMP", 2);
         * 
         * while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 1) { Thread.sleep(50); }
         * 
         * List<AlarmRecord> records = mockSender.getAlarmsSequence(deviceAccessFaultTriplet); assertEquals(1,
         * records.size()); AlarmRecord r1 = records.get(0); assertEquals(AlarmState.ACTIVE, r1.getAlarmState());
         * assertEquals(ERROR_SERVER_UNREACHABLE, r1.getUserProperties().get(ASI_PREFIX_PROPERTY));
         * assertEquals(EX_SERVER_DOWN, r1.getUserProperties().get(ALMON_FAULT_PROPERTY_TAG));
         * 
         * // now stop monitoring ONLY of the one of the FESA parameters handler1.stopMonitoring(); Thread.sleep(100);
         * handler2.stopMonitoring(); Thread.sleep(300); handler3.stopMonitoring(); Thread.sleep(300);
         * 
         * // still - just one activation record is expected records =
         * mockSender.getAlarmsSequence(deviceAccessFaultTriplet); assertEquals(1, records.size());
         * 
         * // stop monitoring of the remaining parameter handler4.stopMonitoring();
         * 
         * while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 2) { Thread.sleep(50); }
         * 
         * records = mockSender.getAlarmsSequence(deviceAccessFaultTriplet); assertEquals(2, records.size());
         * AlarmRecord r2 = records.get(1); assertEquals(AlarmState.TERMINATED, r2.getAlarmState());
         * 
         * // OK, again, subscribe just 1 parameter - back to the same device handler1 = new
         * FesaJapcParameterHandler(fesaParameter1, senderProxy, plsLineResolver); handler1.startMonitoring();
         * 
         * while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 3) { Thread.sleep(50); }
         * 
         * // we should have back activation of the device fault records =
         * mockSender.getAlarmsSequence(deviceAccessFaultTriplet); assertEquals(3, records.size()); AlarmRecord r3 =
         * records.get(2); assertEquals(AlarmState.ACTIVE, r3.getAlarmState()); assertEquals(ERROR_SERVER_UNREACHABLE,
         * r3.getUserProperties().get(ASI_PREFIX_PROPERTY)); assertEquals(EX_SERVER_DOWN,
         * r3.getUserProperties().get(ALMON_FAULT_PROPERTY_TAG));
         * 
         * // change the exception thrown by the RDA server
         * doThrow(pe(EX_DEVICE_UNKNOWN)).when(p1).getValue(anySelector());
         * 
         * // wait for alarm update while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 4) {
         * Thread.sleep(50); } assertEquals(4, records.size()); AlarmRecord r4 = records.get(3);
         * assertEquals(AlarmState.ACTIVE, r4.getAlarmState()); assertEquals(ERROR_UNKNOWN_DEVICE,
         * r4.getUserProperties().get(ASI_PREFIX_PROPERTY)); assertEquals(EX_DEVICE_UNKNOWN,
         * r4.getUserProperties().get(ALMON_FAULT_PROPERTY_TAG));
         * 
         * // stop parameter's monitoring handler1.stopMonitoring();
         * 
         * // wait for alarm termination while (mockSender.getAlarmsSequence(deviceAccessFaultTriplet).size() < 5) {
         * Thread.sleep(50); } assertEquals(5, records.size()); AlarmRecord r5 = records.get(4);
         * assertEquals(AlarmState.TERMINATED, r5.getAlarmState());
         */
    }

    @Test(timeout = 10000)
    @DirtiesContext
    public void testAlarmUpdate() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("PR.TFB-AMP/Alarm");

        String[] fields = { NAMES_ARRAY_FIELD, TIMESTAMPS_ARRAY_FIELD, CYCLE_NAME_FIELD, PREFIXES_ARRAY_FIELD,
                SUFFIXES_ARRAY_FIELD };

        String[] namesArray1 = { "DAMPERPS_100010", "DAMPERPS_100020" };

        long[] timestampsArray1 = { 1389175345532888003L, 1389175345532987834L };

        String[] prefixesArray = { "" };
        String[] suffixesArray = { "" };

        String[] suffixesArray2 = { "suffix1", "suffix2" };

        Object[] values1 = { namesArray1, timestampsArray1, "user1", prefixesArray, suffixesArray };
        Object[] values2 = { namesArray1, timestampsArray1, "user1", prefixesArray, suffixesArray2 };

        // register behavior - by default throw exception simulating the server is down, followed by an active alarm
        when(p1.getValue(null)).thenReturn(acqVal("PR.TFB-AMP/Alarm", mpv(fields, values1))).thenReturn(
                acqVal("PR.TFB-AMP/Alarm", mpv(fields, values2)));

        AlarmTriplet alarmTriplet = new AlarmTriplet("ADTPSMODULE_210", "PR.TFB-AMP", 1000);
        AlmonHardwareAddress address1 = new AlmonHardwareAddressImpl(AlarmType.FESA, "PR.TFB-AMP", "Alarm",
                "DAMPERPS_100010", alarmTriplet);

        handler1 = new FesaJapcParameterHandler(sdt, address1, ems, senderProxy, plsLineResolver);
        handler1.startMonitoring();

        SuperCycle superCycle = newSuperCycle(new Cycle("", 200));
        superCycle.start();
        Thread.sleep(600);

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 2) {
            Thread.sleep(50);
        }

        List<AlarmRecord> records = mockSender.getAlarmsSequence(alarmTriplet);
        assertEquals(2, records.size());

        AlarmRecord r1 = records.get(0);
        AlarmRecord r2 = records.get(1);

        // 1st - expect alarm activation
        assertEquals(AlarmState.ACTIVE, r1.getAlarmState());

        // no prefixes nor suffixes in the first record
        assertNull(r1.getUserProperties().get(ASI_PREFIX_PROPERTY));
        assertNull(r1.getUserProperties().get(ASI_SUFFIX_PROPERTY));
        assertEquals("1", r2.getUserProperties().get(FESA_PLS_LINE_USER_PROPERTY));

        // 2nd - still the alarm should stay active - is should be just updated
        assertEquals(AlarmState.ACTIVE, r2.getAlarmState());
        assertNull(r2.getUserProperties().get(ASI_PREFIX_PROPERTY));
        assertNotNull(r2.getUserProperties().get(ASI_SUFFIX_PROPERTY));
        assertEquals("suffix1", r2.getUserProperties().get(ASI_SUFFIX_PROPERTY));
        assertEquals("1", r2.getUserProperties().get(FESA_PLS_LINE_USER_PROPERTY));
        
    }

}