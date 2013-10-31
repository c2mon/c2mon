package cern.c2mon.daq.japc;

import static cern.japc.ext.mockito.JapcMatchers.anySelector;
import static cern.japc.ext.mockito.JapcMock.acqVal;
import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.Parameter;
import cern.japc.ParameterValue;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;
import cern.japc.factory.ParameterValueFactory;
import cern.c2mon.driver.test.SourceDataTagValueCapture;
import cern.c2mon.driver.test.UseConf;
import cern.c2mon.driver.test.UseHandler;
import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.util.parser.SimpleXMLParser;

/**
 * This class implements a set of JUnit tests for GenericJapcMessageHandler. THe class uses mockito for JAPC simulation.
 * All tests that requiring ClicJapcMessageHandler's pre-configuration with XML based configuration shall be annotated
 * with UseConf annotation, specifying the XML file to be used, and the handler class
 * 
 * @author wbuczak
 */
@UseHandler(GenericJapcMessageHandler.class)
public class GenericJapcMessageHandlerTest extends AbstractGenericJapcMessageHandlerTst {

    /**
     * This tests verifies the default GenericJapcMessageHandler's subscription mechanism. The generic handler by
     * default expects values to be organized in in triplets: [value,timestamp,description]
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_test-generic-handler1.xml")
    public void subscription_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        long f1_timestamp = System.currentTimeMillis() - 5000;
        long f1_new_timestamp = f1_timestamp + 2000;

        long f2_timestamp = f1_timestamp + 500;
        long f2_new_timestamp = f2_timestamp + 2000;

        String[] fields = { "f1", "f1.ts", "f1.details", "f2", "f2.ts", "f2.details" };
        Object[] values1 = { 0, f1_timestamp, "details of f1", 1, f2_timestamp, "details of f2" };
        Object[] values2 = { 1, f1_new_timestamp, "new details of f1", 2, f2_new_timestamp, "new details of f2" };

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
        assertEquals(0, sdtv.getFirstValue(54675L).getValue());
        assertEquals(f1_timestamp, sdtv.getFirstValue(54675L).getTimestamp().getTime());
        assertEquals("details of f1", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getLastValue(54675L).getValue());
        assertEquals(f1_new_timestamp, sdtv.getLastValue(54675L).getTimestamp().getTime());
        assertEquals("new details of f1", sdtv.getLastValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54676L).getQuality().getQualityCode());
        assertEquals(1, sdtv.getFirstValue(54676L).getValue());
        assertEquals(f2_timestamp, sdtv.getFirstValue(54676L).getTimestamp().getTime());
        assertEquals("details of f2", sdtv.getFirstValue(54676L).getValueDescription());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54676L).getQuality().getQualityCode());
        assertEquals(2, sdtv.getLastValue(54676L).getValue());
        assertEquals(f2_new_timestamp, sdtv.getLastValue(54676L).getTimestamp().getTime());
        assertEquals("new details of f2", sdtv.getLastValue(54676L).getValueDescription());
    }

    /**
     * This tests verifies the default GenericJapcMessageHandler's subscription mechanism. The generic handler by
     * default expects values to be organized in in triplets: [value,timestamp,description] This time we test what
     * happens when some fields are missing
     * 
     * @throws Exception
     */

    @Test
    @UseConf("e_japc_test-generic-handler1.xml")
    @Ignore("test temporarly disabled")
    public void subscription_Test2() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        // no timestamps available !!
        String[] fields = { "f1", "f1.details", "f2", "f2.details" };
        Object[] values1 = { 0, "details of f1", 1, "details of f2" };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        Thread.sleep(500);

        // set the new value
        p1.setValue(null, mpv(fields, values1));

        Thread.sleep(500);

        verify(messageSender);

        // the DataTag shall be invalidated
        // assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getFirstValue(54675L).getQuality()
        // .getQualityCode());

        // no value shell be available
        // assertEquals(null, sdtv.getFirstValue(54675L).getValue());

        // no description shall be set
        assertEquals("", sdtv.getFirstValue(54675L).getValueDescription());

        assertEquals(SourceDataQuality.INCORRECT_NATIVE_ADDRESS, sdtv.getFirstValue(54676L).getQuality()
                .getQualityCode());

        // no value shell be available
        assertEquals(null, sdtv.getFirstValue(54676L).getValue());
        // no description shall be set
        assertEquals("", sdtv.getFirstValue(54676L).getValueDescription());
    }

    /**
     * uncomment this test only for integration test for tests with real PING JAPC-Yami agent
     * 
     * @throws Exception
     */
    // @Test
    @UseConf("e_japc_test-generic-handler2.xml")
    public void subscription_TestPingAgent1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        Capture<SourceDataTagValue> sdtv = new Capture<SourceDataTagValue>();

        messageSender.addValue(and(EasyMock.capture(sdtv), isA(SourceDataTagValue.class)));
        expectLastCall().atLeastOnce();

        replay(messageSender);

        japcHandler.connectToDataSource();

        // wait long enough to see the changes coming
        Thread.sleep(30000);

        verify(messageSender);

        assertEquals(54675L, (Object) sdtv.getValue().getId());
    }

    @Test
    @UseConf("e_japc_test7.xml")
    public void commandExecutionGetTest1() throws Exception {

        Parameter p1 = mockParameter("D10/P10");
        Parameter p2 = mockParameter("D10/P11");

        Parameter p3 = mockParameter("D10/P12");

        String[] fields = new String[] { "procname" };
        SimpleParameterValue[] spvArray = new SimpleParameterValue[1];
        SimpleParameterValue spv = ParameterValueFactory.newParameterValue("processA");
        spvArray[0] = spv;

        String[] fields2 = new String[] { "field1", "field2", "field3" };
        SimpleParameterValue[] spvArray2 = new SimpleParameterValue[3];
        spvArray2[0] = ParameterValueFactory.newParameterValue("1");
        spvArray2[1] = ParameterValueFactory.newParameterValue("2");
        spvArray2[2] = ParameterValueFactory.newParameterValue(new String[] { "3" });

        Selector selector = ParameterValueFactory.newSelector(null,
                ParameterValueFactory.newParameterValue(fields, spvArray));

        Selector selector2 = ParameterValueFactory.newSelector(null,
                ParameterValueFactory.newParameterValue(fields2, spvArray2));

        when(p1.getValue(selector)).thenReturn(acqVal("D10/P10", "result string"));
        when(p2.getValue(anySelector())).thenReturn(acqVal("D10/P11", "result string 2"));
        when(p3.getValue(selector2)).thenReturn(acqVal("D10/P12", "result string 3"));

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100850L, "TEST:TESTCMD4", 5250L, (short) 0,
                    "processA", "String");

            String result = japcHandler.runCommand(sctv);

            assertEquals("result string", result);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100850L, "TEST:TESTCMD4", 5250L, (short) 0,
                    "processB", "String");

            japcHandler.runCommand(sctv);

            fail("EqCommandTagException was expected at this point!");

        } catch (EqCommandTagException ex) {
        }

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100851L, "TEST:TESTCMD5", 5250L, (short) 0,
                    "unused", "String");

            String result = japcHandler.runCommand(sctv);

            assertEquals("result string 2", result);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

        try {
            SourceCommandTagValue sctv = new SourceCommandTagValue(100852L, "TEST:TESTCMD6", 5250L, (short) 0,
                    "1;2;{3}", "String");

            String result = japcHandler.runCommand(sctv);

            assertEquals("result string 3", result);

        } catch (EqCommandTagException ex) {
            fail("EqCommandTagException was NOT expected at this point. Exception message: " + ex.getErrorDescription());
        }

    }

    /**
     * The goal of this test is to verify the Generic JAPC DAQ's behavior when a request to add a new DataTag is
     * received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_test-generic-handler3.xml")
    public void reconfigure_AddDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(4);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        long f1_timestamp = System.currentTimeMillis() - 5000;
        long f1_new_timestamp = f1_timestamp + 2000;

        long f2_timestamp = f1_timestamp + 500;
        long f2_new_timestamp = f2_timestamp + 500;

        String[] fields = { "f1", "f1.ts", "f1.details", "f2", "f2.ts", "f2.details" };
        Object[] values1 = { 0, f1_timestamp, "details of f1", 1, f2_timestamp, "details of f2" };

        Object[] values2 = { 1, f1_new_timestamp, "new details of f1", 2, f2_new_timestamp, "new details of f2" };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        StringBuilder str = new StringBuilder();

        str.append("<DataTag id=\"1000001\" name=\"JAPC-TEST-TAG02\" control=\"false\">");
        str.append("  <data-type>Integer</data-type>");
        str.append("  <DataTagAddress>");
        str.append("    <HardwareAddress class=\"ch.cern.tim.shared.datatag.address.impl.JAPCHardwareAddressImpl\">");
        str.append("       <protocol>mockito</protocol>");
        str.append("       <service>mockito</service>");
        str.append("       <device-name>D1</device-name>");
        str.append("       <property-name>P1</property-name>");
        str.append("       <data-field-name>f2</data-field-name>");
        str.append("    </HardwareAddress>");
        str.append("    <time-to-live>3600000</time-to-live>");
        str.append("    <priority>2</priority>");
        str.append("    <guaranteed-delivery>false</guaranteed-delivery>");
        str.append("  </DataTagAddress>");
        str.append("</DataTag>");

        SimpleXMLParser parser = new SimpleXMLParser();

        Thread.sleep(1000);

        // set the new value
        p1.setValue(null, mpv(fields, values1));

        Thread.sleep(1000);

        // trigger adding new DataTag
        SourceDataTag newTag = SourceDataTag.fromConfigXML(parser.parse(str.toString()).getDocumentElement());

        ChangeReport report = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(1000);

        // set the new value
        p1.setValue(null, mpv(fields, values2));

        Thread.sleep(1000);

        // try adding once again the same tag
        ChangeReport report2 = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(500);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(1000000L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(1000000L);

        SourceDataTagValue firstValue2 = sdtv.getFirstValue(1000001L);
        SourceDataTagValue lastValue2 = sdtv.getLastValue(1000001L);

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(0, firstValue1.getValue());
        assertEquals(f1_timestamp, firstValue1.getTimestamp().getTime());
        assertEquals("details of f1", firstValue1.getValueDescription());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(1, lastValue1.getValue());
        assertEquals(f1_new_timestamp, lastValue1.getTimestamp().getTime());
        assertEquals("new details of f1", lastValue1.getValueDescription());

        assertEquals(SourceDataQuality.OK, firstValue2.getQuality().getQualityCode());
        assertEquals(1, firstValue2.getValue());
        assertEquals(f2_timestamp, firstValue2.getTimestamp().getTime());
        assertEquals("details of f2", firstValue2.getValueDescription());

        assertEquals(SourceDataQuality.OK, lastValue2.getQuality().getQualityCode());
        assertEquals(2, lastValue2.getValue());
        assertEquals(f2_new_timestamp, lastValue2.getTimestamp().getTime());
        assertEquals("new details of f2", lastValue2.getValueDescription());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());
        // the second should fail, since the tag is already registered
        assertEquals(CHANGE_STATE.FAIL, report2.getState());
    }

    /**
     * The goal of this test is to verify the Generic JAPC DAQ's behavior when a request to remove an existing DataTag
     * is received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_test-generic-handler4.xml")
    public void reconfigure_RemoveDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        long f1_timestamp = System.currentTimeMillis() - 5000;
        long f1_new_timestamp = f1_timestamp + 2000;

        long f2_timestamp = f1_timestamp + 500;
        long f2_new_timestamp = f2_timestamp + 500;

        String[] fields = { "f1", "f1.ts", "f1.details", "f2", "f2.ts", "f2.details" };
        Object[] values1 = { 0, f1_timestamp, "details of f1", 1, f2_timestamp, "details of f2" };

        Object[] values2 = { 1, f1_new_timestamp, "new details of f1", 2, f2_new_timestamp, "new details of f2" };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        Thread.sleep(500);

        // set the new value
        p1.setValue(null, mpv(fields, values1));
        Thread.sleep(500);

        // emulate receiving request to remove a datatag
        ChangeReport report = configurationController.onDataTagRemove(new DataTagRemove(1L, 1000001L,
                equipmentConfiguration.getId()));
        Thread.sleep(500);

        // set the new value
        p1.setValue(null, mpv(fields, values2));

        Thread.sleep(500);

        // emulate receiving request to remove a datatag - again the same one
        ChangeReport report2 = configurationController.onDataTagRemove(new DataTagRemove(1L, 1000001L,
                equipmentConfiguration.getId()));

        Thread.sleep(500);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(1000000L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(1000000L);

        SourceDataTagValue firstValue2 = sdtv.getFirstValue(1000001L);

        // there should be only one update received for that tag
        assertEquals(1, sdtv.getNumberOfCapturedValues(1000001L));

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(0, firstValue1.getValue());
        assertEquals(f1_timestamp, firstValue1.getTimestamp().getTime());
        assertEquals("details of f1", firstValue1.getValueDescription());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(1, lastValue1.getValue());
        assertEquals(f1_new_timestamp, lastValue1.getTimestamp().getTime());
        assertEquals("new details of f1", lastValue1.getValueDescription());

        assertEquals(SourceDataQuality.OK, firstValue2.getQuality().getQualityCode());
        assertEquals(1, firstValue2.getValue());
        assertEquals(f2_timestamp, firstValue2.getTimestamp().getTime());
        assertEquals("details of f2", firstValue2.getValueDescription());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());

        // the other one should give some warning, but the status should still be SUCCESS
        assertEquals(CHANGE_STATE.SUCCESS, report2.getState());
        assertEquals("The data tag with id 1000001 to remove was not found in equipment with id 5250",
                report2.getWarnMessage());

    }

    /**
     * The goal of this test is to verify the Generic JAPC DAQ's behavior when a request to update an existing DataTag
     * is received at runtime.
     * 
     * @throws Exception
     */
    @Test
    @UseConf("e_japc_test-generic-handler3.xml")
    public void reconfigure_UpdateDataTag_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        long f1_timestamp = System.currentTimeMillis() - 5000;
        long f1_new_timestamp = f1_timestamp + 2000;

        long f2_timestamp = f1_timestamp + 500;
        long f2_new_timestamp = f2_timestamp + 500;

        String[] fields = { "f1", "f1.ts", "f1.details", "f2", "f2.ts", "f2.details" };
        Object[] values1 = { 0, f1_timestamp, "details of f1", 1, f2_timestamp, "f2.details" };

        Object[] values2 = { 1, f1_new_timestamp, "new details of f1", 2, f2_new_timestamp, "new details of f2" };

        setAnswer(p1, null, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        Thread.sleep(500);

        // set the new value
        p1.setValue(null, mpv(fields, values1));

        Thread.sleep(500);

        DataTagUpdate update = new DataTagUpdate(1L, 1000000L, equipmentConfiguration.getId());

        // set new tag name
        update.setName("JAPC-TEST-TAG02");

        // set new tag id
        update.setDataTagId(1000000L);

        ChangeReport report = configurationController.onDataTagUpdate(update);

        Thread.sleep(500);

        // set the new value
        p1.setValue(null, mpv(fields, values2));

        Thread.sleep(500);

        verify(messageSender);

        // there should be only one update received for that tag
        assertEquals(2, sdtv.getNumberOfCapturedValues(1000000L));

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());
    }

    @Test
    @UseConf("e_japc_test-generic-handler5.xml")
    public void subscription_TestFilters() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(2);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("D1/P1");

        long f1_timestamp = System.currentTimeMillis() - 5000;
        long f1_new_timestamp = f1_timestamp + 2000;

        String[] fields = { "f1", "f1.ts" };
        Object[] values1 = { 0, f1_timestamp };
        Object[] values2 = { 1, f1_new_timestamp };

        // split the filter ( expected format: key=value )

        Map<String, SimpleParameterValue> df = new HashMap<String, SimpleParameterValue>();
        df.put("testFilter", ParameterValueFactory.newParameterValue("testValue"));
        ParameterValue dataFilter = ParameterValueFactory.newParameterValue(df);
        Selector selector = ParameterValueFactory.newSelector(null, dataFilter, false);

        setAnswer(p1, selector, new DefaultParameterAnswer(mpv(fields, values1)));

        japcHandler.connectToDataSource();

        Thread.sleep(500);

        // set the new value
        p1.setValue(selector, mpv(fields, values1));

        Thread.sleep(500);

        // set the new value
        p1.setValue(selector, mpv(fields, values2));

        Thread.sleep(500);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(1000000L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(1000000L);

        // there should be only one update received for that tag
        assertEquals(2, sdtv.getNumberOfCapturedValues(1000000L));

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(0, firstValue1.getValue());
        assertEquals(f1_timestamp, firstValue1.getTimestamp().getTime());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(1, lastValue1.getValue());
        assertEquals(f1_new_timestamp, lastValue1.getTimestamp().getTime());
    }

}
