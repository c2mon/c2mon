/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.fesa;

import static cern.japc.ext.mockito.JapcMatchers.anySelector;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.whenGetValueThen;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.japc.AbstractGenericJapcMessageHandlerTst;
import cern.c2mon.daq.japc.fesa.FesaAlarmJapcMessageHandler;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SubscriptionHandle;
import cern.japc.ext.mockito.JapcMock;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

/**
 * This class implements a set of JUnit tests for FesaJapcMessageHandler. THe class uses mockito for JAPC simulation.
 * All tests that requiring FesaJapcMessageHandler's pre-configuration with XML based configuration shall be annotated
 * with UseConf annotation, specifying the XML file to be used, and the handler class
 * 
 * @author wbuczak
 */
@UseHandler(FesaAlarmJapcMessageHandler.class)
@Ignore("test temporarly disabled")
public class FesaAlarmJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    static Logger log = Logger.getLogger(FesaAlarmJapcMessageHandlerTest.class);

    @Test
    @UseConf("e_japc_fesa_test1.xml")
    public void test01() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        replay(messageSender);

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(2, ((FesaAlarmJapcMessageHandler) japcHandler).groupedTags.size());
        assertEquals(3, ((FesaAlarmJapcMessageHandler) japcHandler).groupedTags.get("CE.SCOPE07.CH01:Alarm").size());
        assertEquals(1, ((FesaAlarmJapcMessageHandler) japcHandler).groupedTags.get("MTV0253:Alarm").size());
    }

    @Test
    @UseConf("e_japc_fesa_test1.xml")
    public void test02() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv2 = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv3 = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv2), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv3), isA(SourceDataTagValue.class)));

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("CE.SCOPE07.CH01/Alarm");

        String[] fieldNames = { "names", "timestamps", "prefixes", "suffixes" };

        String[] names = { "noHwScopeAlarm", "timingUnsyncTStampsAlarm" };
        long currentTime = System.currentTimeMillis();
        long[] timestamps = new long[] { currentTime, currentTime - 100 };

        String[] prefixes = new String[] { "pref1", "pref2" };
        String[] suffixes = new String[] { "suff1", "suff2" };

        Object[] values = { names, timestamps, prefixes, suffixes };

        // by default the value defined below will be delivered right after the client subscribes
        whenGetValueThen(p1, anySelector(), mpv(fieldNames, values));

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.OK, sdtv2.getValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.OK, sdtv3.getValue().getQuality().getQualityCode());

        assertEquals(true, sdtv.getValue().getValue());
        assertEquals(false, sdtv2.getValue().getValue());
        assertEquals(true, sdtv3.getValue().getValue());

        assertEquals(currentTime - 100, sdtv3.getValue().getTimestamp().getTime());
        assertEquals(currentTime, sdtv.getValue().getTimestamp().getTime());

        assertEquals("terminating alarm", sdtv2.getValue().getValueDescription());
        assertEquals("ASI_SUFFIX=suff2$$ASI_PREFIX=pref2", sdtv3.getValue().getValueDescription());
        assertEquals("ASI_SUFFIX=suff1$$ASI_PREFIX=pref1", sdtv.getValue().getValueDescription());
    }

    @Test
    @UseConf("e_japc_fesa_test1.xml")
    public void test03() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("MTV0253/Alarm");

        String[] fieldNames = { "names", "timestamps", "prefixes", "suffixes" };

        String[] names = { "id1", "id2" };
        long currentTime = System.currentTimeMillis();
        long[] timestamps = new long[] { currentTime, currentTime - 100 };

        String[] prefixes = new String[] { "pref1", "pref2" };
        String[] suffixes = new String[] { "suff1", "suff2" };

        Object[] values = { names, timestamps, prefixes, suffixes };

        // by default the value defined below will be delivered right after the client subscribes
        whenGetValueThen(p1, anySelector(), mpv(fieldNames, values));

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
        assertEquals(false, sdtv.getValue().getValue());
        assertTrue(sdtv.getValue().getTimestamp().getTime() > currentTime);
        assertEquals("terminating alarm", sdtv.getValue().getValueDescription());
    }

    @Test
    @UseConf("e_japc_fesa_test1.xml")
    public void testStartMonitoringExceptionThrownFesa() throws Exception {

        JapcMock.setSubscriptionAnswer(new JapcMock.SubscriptionAnswer() {

            @Override
            protected SubscriptionHandle createSubscription(Parameter mock, Selector selector,
                    ParameterValueListener parameterValueListener) {
                if ("CE.SCOPE07.CH01/Alarm".equals(mock.getName())) {
                    SubscriptionHandle sh = createMock(SubscriptionHandle.class);
                    try {
                        sh.startMonitoring();
                        expectLastCall().andThrow(new ParameterException("Simulated1"));
                        sh.startMonitoring();
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
        Capture<SourceDataTagValue> sdtv2 = new Capture<SourceDataTagValue>();
        Capture<SourceDataTagValue> sdtv3 = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv2), isA(SourceDataTagValue.class)));
        messageSender.addValue(and(EasyMock.capture(sdtv3), isA(SourceDataTagValue.class)));

        replay(messageSender);

        japcHandler.connectToDataSource();

        Thread.sleep(150);

        verify(messageSender);
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv2.getValue().getQuality().getQualityCode());
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv3.getValue().getQuality().getQualityCode());
    }
}
