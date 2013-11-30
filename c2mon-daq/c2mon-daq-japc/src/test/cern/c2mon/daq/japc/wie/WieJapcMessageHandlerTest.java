package cern.c2mon.daq.japc.wie;

import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.japc.AbstractGenericJapcMessageHandlerTst;
import cern.c2mon.daq.japc.wie.WieJapcMessageHandler;
import cern.japc.Parameter;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

@UseHandler(WieJapcMessageHandler.class)
public class WieJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    /**
     * This tests verifies the WieJapcMessageHandler's subscription mechanism. The generic handler by
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_wie1.xml")
    public void subscription_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));

        expectLastCall().times(6);

        replay(messageSender);

        // Create Mock parameters

        Parameter p1 = mockParameter("BFCT10T-FAN/Acquisition");

        String[] fields = { "agent.snmp.wiener.fanAirTemperatureOid", "agent.snmp.wiener.outputVoltageOid",
                "agent.snmp.wiener.outputVoltageOid.names", "agent.snmp.wiener.fanRotationSpeedOid" };

        // Simple array

        int[] valueFanArray1 = { 100, 1, 2, 5 };
        int[] valueFanArray2 = { 999, 99, 2, 5 };

        // Array with names array

        String[] fieldNames1 = { "field1", "+5V0", "totals", "field2" };
        String[] fieldNames2 = { "+5V0", "field1", "totals", "field2" };

        float[] valueArray1 = { 0, 1, 2, 5 };
        float[] valueArray2 = { 99, 0, 2, 5 };

        Object[] values1 = { 20, valueArray1, fieldNames1, valueFanArray1 };
        Object[] values2 = { 30, valueArray2, fieldNames2, valueFanArray2 };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        Thread.sleep(1200);

        // set the new value

        p1.setValue(null, mpv(fields, values1));

        Thread.sleep(1000);

        // set the new value
        p1.setValue(null, mpv(fields, values2));

        Thread.sleep(1000);

        verify(messageSender);

        // Scalar

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(20, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(30, sdtv.getLastValue(54675L).getValue());

        // Simple array with fixed index

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54679L).getQuality().getQualityCode());
        assertEquals(100, sdtv.getFirstValue(54679L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54679L).getQuality().getQualityCode());
        assertEquals(999, sdtv.getLastValue(54679L).getValue());

        // Array with names array

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54678L).getQuality().getQualityCode());
        assertEquals(1.0F, sdtv.getFirstValue(54678L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54678L).getQuality().getQualityCode());
        assertEquals(99.0F, sdtv.getLastValue(54678L).getValue());
    }

}
