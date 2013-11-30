/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.laserproxy;

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

import cern.c2mon.daq.japc.AbstractGenericJapcMessageHandlerTst;
import cern.c2mon.daq.japc.laserproxy.LaserProxyJapcMessageHandler;
import cern.japc.Parameter;
import cern.japc.Selector;
import cern.japc.ext.mockito.Cycle;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;

@UseHandler(LaserProxyJapcMessageHandler.class)
public class LaserProxyAlarmJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    static Logger log = Logger.getLogger(LaserProxyAlarmJapcMessageHandlerTest.class);

    // @Test
    // @UseConf("e_japc_test1.xml")
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

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(54675L, (Object) sdtv.getValue().getId());
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getValue().getQuality().getQualityCode());
        assertEquals("handleJAPCValue() : Type \"Simple\" is not supported", sdtv.getValue().getQuality()
                .getDescription());
    }

    // @Test
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
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv1.getValue().getQuality().getQualityCode());
    }

    /**
     * this method tests mapped subscription, but the configuration does not include the field that the DAQ expects to
     * be present.
     */
    // @Test
    @UseConf("e_japc_laserproxy_test1.xml")
    public void subscription_Test8() throws Exception {

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("LaserProxy/Metrics");

        String[] fieldNames = { "names", "timestamps", "properties",
                "fault_member_test1:fault_family_test1:fault_code_test1" };

        String[] names = { "field1", "field2", "fault_member_test1:fault_family_test1:fault_code_test1", "field4" };
        long currentTime = System.currentTimeMillis();
        long[] timestamps = new long[] { currentTime, currentTime, currentTime, currentTime };

        String[] properties = new String[] { "", "", "", "" };

        Object[] values = { names, timestamps, properties, true };
        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fieldNames, values)));

        japcHandler.connectToDataSource();

        // set the new value
        p1.setValue(null, mpv(fieldNames, values));

        Thread.sleep(150);

        verify(messageSender);

        assertEquals(SourceDataQuality.OK, sdtv.getValue().getQuality().getQualityCode());
        assertEquals(true, sdtv.getValue().getValue());
        assertEquals(currentTime, sdtv.getValue().getTimestamp().getTime());
        assertEquals("", sdtv.getValue().getValueDescription());
    }
}
