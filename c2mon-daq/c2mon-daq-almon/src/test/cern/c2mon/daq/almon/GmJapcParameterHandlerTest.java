/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import static cern.c2mon.daq.almon.JapcParameterHandler.ALARM_MON_FAULT_FAMILY;
import static cern.japc.ext.mockito.JapcMock.acqVal;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.newSuperCycle;
import static cern.japc.ext.mockito.JapcMock.pe;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.annotation.Resource;

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
 * The <code>GmJapcParameterHandlerTest</code> tests GM alarm activation/termination algorithm.
 * 
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/dmn-almon-config.xml")
@ActiveProfiles("TEST")
public class GmJapcParameterHandlerTest {

    public static final String ERROR_SERVER_DOWN = "Server is down";
    public static final Selector SELECTOR1 = sel(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR);

    JapcParameterHandler handler;

    @Resource
    TestAlmonSender mockSender;

    @Resource(name = "almonSenderProxy")
    AlmonSender senderProxy;

    ISourceDataTag sdt;

    IEquipmentMessageSender ems;

    @Before
    public void before() {
        JapcMock.init();
        resetJapcMock();

        sdt = Mockito.mock(ISourceDataTag.class);
        when(sdt.getId()).thenReturn(1000L);

        ems = Mockito.mock(IEquipmentMessageSender.class);
        ems = mock(IEquipmentMessageSender.class);        

    }

    @Test(timeout = 4000)
    @DirtiesContext
    public void testAlarmsActivationAndTermination() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("D1/ALARM");

        String[] fields = { "value" };
        Object[] value_0 = { 0 };
        Object[] value_1 = { 1 };
        Object[] value_2 = { 2 };

        setAnswer(p1, SELECTOR1, new DefaultParameterAnswer(mpv(fields, value_0)));

        AlarmTriplet alarmTriplet = new AlarmTriplet("CLASS1", "D1", 1);

        AlmonHardwareAddress address = new AlmonHardwareAddressImpl(AlarmType.GM, "D1", "ALARM", "value", alarmTriplet);

        handler = new GmJapcParameterHandler(sdt, address, ems, senderProxy);
        handler.startMonitoring();

        Thread.sleep(200);

        p1.setValue(SELECTOR1, mpv(fields, value_1));
        Thread.sleep(200);

        p1.setValue(SELECTOR1, mpv(fields, value_2));
        Thread.sleep(200);

        p1.setValue(SELECTOR1, mpv(fields, value_0));
        Thread.sleep(200);

        p1.setValue(SELECTOR1, mpv(fields, value_2));
        Thread.sleep(200);

        p1.setValue(SELECTOR1, mpv(fields, value_1));
        Thread.sleep(200);

        AlarmTriplet alarmTriplet2 = new AlarmTriplet("CLASS1", "D1", 2);

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 2) {
            Thread.sleep(50);
        }

        List<AlarmRecord> alarmStates = mockSender.getAlarmsSequence(alarmTriplet);
        List<AlarmRecord> alarmStates2 = mockSender.getAlarmsSequence(alarmTriplet2);

        assertEquals(3, alarmStates.size());
        assertEquals(AlarmState.ACTIVE, alarmStates.get(0).getAlarmState());
        assertEquals(AlarmState.TERMINATED, alarmStates.get(1).getAlarmState());
        assertEquals(AlarmState.ACTIVE, alarmStates.get(2).getAlarmState());

        assertEquals(0, alarmStates2.size());
    }

    // @Test(timeout = 4000)
    @DirtiesContext
    public void testParameterExceptionHandling() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("D1/ALARM");

        // register behavior - by default throw exception simulating the server is down, followed by an active alarm,
        // finally followed by alarm termination
        when(p1.getValue(SELECTOR1)).thenThrow(pe(ERROR_SERVER_DOWN)).thenReturn(acqVal("D1/P1", 1))
                .thenReturn(acqVal("D1/P1", 0));

        AlarmTriplet alarmTriplet2 = new AlarmTriplet("CLASS1", "D1", 1);
        AlmonHardwareAddress address = new AlmonHardwareAddressImpl(AlarmType.GM, "D1", "ALARM", "value",
                alarmTriplet2);

        handler = new GmJapcParameterHandler(sdt, address, ems, senderProxy);
        handler.startMonitoring();

        SuperCycle superCycle = newSuperCycle(new Cycle(SELECTOR1.getId(), 200));
        superCycle.start();

        AlarmTriplet alarmTriplet = new AlarmTriplet(ALARM_MON_FAULT_FAMILY, "D1", 2);

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 2) {
            Thread.sleep(50);
        }
        while (mockSender.getAlarmsSequence(alarmTriplet2).size() < 2) {
            Thread.sleep(50);
        }

        List<AlarmRecord> alarmStates = mockSender.getAlarmsSequence(alarmTriplet);
        List<AlarmRecord> alarmStates2 = mockSender.getAlarmsSequence(alarmTriplet2);

        assertEquals(2, alarmStates.size());
        assertEquals(AlarmState.ACTIVE, alarmStates.get(0).getAlarmState());
        assertEquals(AlarmState.TERMINATED, alarmStates.get(1).getAlarmState());

        assertEquals(2, alarmStates2.size());
        assertEquals(AlarmState.ACTIVE, alarmStates2.get(0).getAlarmState());
        assertEquals(AlarmState.TERMINATED, alarmStates2.get(1).getAlarmState());

    }

    // @Test(timeout = 4000)
    @DirtiesContext
    public void testParameterExceptionHandling2() throws Exception {

        // create mock parameter
        Parameter p1 = mockParameter("D1/ALARM");

        // register behavior - a sequence of values and followed by server-down exception, followed by another value
        when(p1.getValue(sel(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR)))
                .thenReturn(acqVal("D1/P1", 1), acqVal("D1/P1", 2)).thenThrow(pe(ERROR_SERVER_DOWN))
                .thenReturn(acqVal("D1/P1", 1));

        AlarmTriplet alarmTriplet2 = new AlarmTriplet("CLASS1", "D1", 1);
        AlmonHardwareAddress address = new AlmonHardwareAddressImpl(AlarmType.GM, "D1", "ALARM", "value",
                alarmTriplet2);

        handler = new GmJapcParameterHandler(sdt, address, ems, senderProxy);
        handler.startMonitoring();

        SuperCycle superCycle = newSuperCycle(new Cycle(SELECTOR1.getId(), 200));
        superCycle.start();

        AlarmTriplet alarmTriplet = new AlarmTriplet(ALARM_MON_FAULT_FAMILY, "D1", 2);
        AlarmTriplet alarmTriplet3 = new AlarmTriplet("CLASS1", "D1", 2);

        while (mockSender.getAlarmsSequence(alarmTriplet).size() < 1) {
            Thread.sleep(50);
        }
        while (mockSender.getAlarmsSequence(alarmTriplet2).size() < 2) {
            Thread.sleep(50);
        }
        while (mockSender.getAlarmsSequence(alarmTriplet3).size() < 1) {
            Thread.sleep(50);
        }

        List<AlarmRecord> alarmStates = mockSender.getAlarmsSequence(alarmTriplet);
        List<AlarmRecord> alarmStates2 = mockSender.getAlarmsSequence(alarmTriplet2);
        List<AlarmRecord> alarmStates3 = mockSender.getAlarmsSequence(alarmTriplet3);

        assertEquals(1, alarmStates.size());
        assertEquals(AlarmState.ACTIVE, alarmStates.get(0).getAlarmState());

        assertEquals(2, alarmStates2.size());
        assertEquals(AlarmState.ACTIVE, alarmStates2.get(0).getAlarmState());
        assertEquals(AlarmState.TERMINATED, alarmStates2.get(1).getAlarmState());

        assertEquals(1, alarmStates3.size());
        assertEquals(AlarmState.ACTIVE, alarmStates3.get(0).getAlarmState());
    }

}