/*
 * Copyright CERN 2014, All Rights Reserved.
 */
package cern.c2mon.daq.almon;

import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static java.lang.System.setProperty;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.util.parser.SimpleXMLParser;
import cern.japc.Parameter;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;

public abstract class AlmonDynReconfMessageHandlerTest extends GenericMessageHandlerTst {

    static {
        // activate the TEST spring profile
        setProperty("spring.profiles.active", "TEST");
    }

    protected AlmonMessageHandler emh;

    /**
     * The goal of this test is to verify the Almon DAQ's behavior when a request to add a new DataTag is received at
     * runtime.
     * 
     * @throws Exception
     */
    @Test(timeout=5000)
    @UseConf("conf-gm-one-metric.xml")
    public void reconfDatatagAddTest() throws Exception {

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

        emh.connectToDataSource();

        StringBuilder str = new StringBuilder();

        str.append("<DataTag id=\"54676\" name=\"BE.TEST:TEST2\" control=\"false\">");
        str.append("  <data-type>Boolean</data-type>");
        str.append("  <DataTagAddress>");

        str.append("<HardwareAddress class=\"cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl\">");

        str.append("<address>");
        str.append("{");
        str.append("    \"type\": \"GM\",");
        str.append("    \"device\": \"RFLNP\",");
        str.append("    \"property\": \"ALARM\",");
        str.append("    \"field\": \"value\",");
        str.append("    \"alarmTripplet\": {");
        str.append("        \"faultFamily\": \"ITM.CRFBU\",");
        str.append("        \"faultMember\": \"RFLNP\",");
        str.append("        \"faultCode\": \"1\"");
        str.append("     }");
        str.append("}");
        str.append("</address>");

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

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(54676L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(54676L);

        SourceDataTagValue firstValue2 = sdtv.getFirstValue(54676L);
        SourceDataTagValue lastValue2 = sdtv.getLastValue(54676L);

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
     * The goal of this test is to verify the Almon DAQ's behavior when a request to remove an existing DataTag is
     * received at runtime.
     * 
     * @throws Exception
     */
    @Test(timeout=5000)
    @UseConf("conf-gm-one-metric.xml")
    public void reconfDatatagRemoveTest() throws Exception {

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

        emh.connectToDataSource();

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
     * The goal of this test is to verify the Almon DAQ's behavior when a request to update an existing DataTag is
     * received at runtime.
     * 
     * @throws Exception
     */
    @Test(timeout=5000)
    @UseConf("conf-gm-one-metric.xml")
    public void reconfDataTagUpdateTest() throws Exception {

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

        emh.connectToDataSource();

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

}