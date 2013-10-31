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
import static cern.japc.ext.mockito.JapcMock.pe;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.spv;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import java.util.Calendar;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;
import org.mockito.Matchers;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.SubscriptionHandle;
import cern.japc.ext.mockito.JapcMock;
import cern.japc.ext.mockito.SuperCycle;
import cern.japc.factory.ParameterValueFactory;
import cern.c2mon.driver.common.conf.core.ConfigurationController;
import cern.c2mon.driver.test.GenericMessageHandlerTst;
import cern.c2mon.driver.test.UseConf;
import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.datatag.SourceDataTagValue;

/**
 * This class implements a common parent class for JUnit testing framework for JAPC EquipmentMessageHandlers.
 * 
 * @author wbuczak
 */
public abstract class AbstractGenericJapcMessageHandlerTst extends GenericMessageHandlerTst {

    protected GenericJapcMessageHandler japcHandler;

    protected SuperCycle spsSupercycle;
    protected SuperCycle lhcSuperCycle;

    protected final long getStartMonitoringTimeout() throws Exception {
        // reduce the timeout to 100ms only for tests
        return 100;
    }

    // if set, JAPC mockito framework will be initialized
    static boolean initMockito = true;

    @Override
    protected void beforeTest() throws Exception {
        japcHandler = (GenericJapcMessageHandler) msgHandler;

        if (initMockito) {
            JapcMock.init();
            resetJapcMock();
        }

        // GenericJapcMessageHandler.MSTART_RETRY_TIMOUT = getStartMonitoringTimeout();

        configurationController = new ConfigurationController(null, null);

        configurationController.setProcessConfiguration(pconf);

        pconf.getEquipmentConfigurations().put(equipmentConfiguration.getId(), equipmentConfiguration);

        configurationController.putImplementationCommandTagChanger(equipmentConfiguration.getId(), japcHandler);
        configurationController.putImplementationDataTagChanger(equipmentConfiguration.getId(), japcHandler);

        // configurationController.putImplementationEquipmentConfigurationChanger(TEST_EQUIPMENT_ID,
        // equipmentConfigurationChanger);
    }

    @Override
    protected void afterTest() {
        if (initMockito) {
            stopMockSupercycles();
            JapcMock.resetJapcMock();
        }
    }

    @Override
    @After
    public void cleanUp() throws Exception {
        if (japcHandler != null)
            japcHandler.disconnectFromDataSource();

        if (initMockito) {
            stopMockSupercycles();
            JapcMock.resetJapcMock();
        }
    }

    // @Test
    @UseConf("e_japc_test1.xml")
    public void testStartMonitoringExceptionThrown() throws Exception {

        JapcMock.setSubscriptionAnswer(new JapcMock.SubscriptionAnswer() {

            @Override
            protected SubscriptionHandle createSubscription(Parameter mock, Selector selector,
                    ParameterValueListener parameterValueListener) {

                if ("D1/P1".equals(mock.getName())) {
                    SubscriptionHandle sh = createMock(SubscriptionHandle.class);
                    try {
                        sh.startMonitoring();
                        expectLastCall().andThrow(new ParameterException("Simulated1"));
                        sh.startMonitoring();
                        expectLastCall().andThrow(new ParameterException("Simulated2"));
                        sh.startMonitoring();
                        expectLastCall().once();

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

        Thread.sleep(1000);

        verify(messageSender);
    }

    @Test()
    @UseConf("e_japc_test7.xml")
    public void commandExecutionTest1() throws Exception {
        
        if (!initMockito) return;

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

    @Test
    public void testConvertSourceTimestampToMs() {
        
        if (!initMockito) return;
        
        Calendar cal = Calendar.getInstance();
        cal.set(1970, 1, 15);
        assertEquals(cal.getTimeInMillis() * 1000,
                GenericJapcMessageHandler.convertSourceTimestampToMs(cal.getTimeInMillis()));

        cal.set(2010, 8, 17);

        assertEquals(cal.getTimeInMillis(), GenericJapcMessageHandler.convertSourceTimestampToMs(cal.getTimeInMillis()));
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