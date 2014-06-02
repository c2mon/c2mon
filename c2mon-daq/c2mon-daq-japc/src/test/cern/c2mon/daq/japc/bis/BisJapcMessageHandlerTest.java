package cern.c2mon.daq.japc.bis;

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
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.japc.Parameter;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;

@UseHandler(BisJapcMessageHandler.class)
public class BisJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    /**
     * This tests verifies the BisJapcMessageHandler's subscription mechanism.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_bis1.xml")
    public void subscription_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));

        expectLastCall().times(3);

        replay(messageSender);

        // Create Mock parameters

        Parameter p1 = mockParameter("CIBX.400.LN4.RF/BoardRegisters");

        String[] fields = { "registerNames", "registerValues" };


        // Array with names array

        String[] fieldNames1 = { "FIELD1", "STATUS", "FIELD2"};
        String[] fieldNames2 = { "FIELD1", "FIELD2", "STATUS"};
        
        float[] valueArray1 = { 0, 1, 2};
        float[] valueArray2 = { 99, 0, 2};

        Object[] values1 = { fieldNames1, valueArray1 };
        Object[] values2 = { fieldNames2, valueArray2 };

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

        
        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54675L).getValue());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getLastValue(54675L).getValue());
    }

    
    @Test
    @UseConf("e_japc_bis1.xml")
    public void subscription_Test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));

        expectLastCall().times(1);

        replay(messageSender);

        // Create Mock parameters

        Parameter p1 = mockParameter("CIBX.400.LN4.RF/BoardRegisters");

        String[] fields = { "wrongFieldNames", "registerValues" };


        // Array with names array

        String[] fieldNames1 = { "FIELD1", "STATUS", "FIELD2"};
        String[] fieldNames2 = { "FIELD1", "FIELD2", "STATUS"};
        
        float[] valueArray1 = { 0, 1, 2};
        float[] valueArray2 = { 99, 0, 2};

        Object[] values1 = { fieldNames1, valueArray1 };
        Object[] values2 = { fieldNames2, valueArray2 };

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
        
        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals("field: registerNames not found", sdtv.getFirstValue(54675L).getQuality().getDescription());      
    }    
    
}
