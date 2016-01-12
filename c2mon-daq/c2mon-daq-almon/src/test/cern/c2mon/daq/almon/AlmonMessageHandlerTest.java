/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.almon;

import static cern.japc.ext.mockito.JapcMock.mockParameter;
import static cern.japc.ext.mockito.JapcMock.mpv;
import static cern.japc.ext.mockito.JapcMock.newSuperCycle;
import static cern.japc.ext.mockito.JapcMock.pe;
import static cern.japc.ext.mockito.JapcMock.resetJapcMock;
import static cern.japc.ext.mockito.JapcMock.sel;
import static cern.japc.ext.mockito.JapcMock.setAnswer;
import static cern.japc.ext.mockito.JapcMock.whenGetValueThen;
import static java.lang.System.setProperty;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.easymock.EasyMock;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.test.GenericMessageHandlerTst;
import cern.c2mon.daq.test.SourceDataTagValueCapture;
import cern.c2mon.daq.test.UseConf;
import cern.c2mon.daq.test.UseHandler;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.config.DataTagAdd;
import cern.c2mon.shared.daq.config.DataTagRemove;
import cern.c2mon.shared.util.parser.SimpleXMLParser;
import cern.japc.Parameter;
import cern.japc.Selector;
import cern.japc.ext.mockito.Cycle;
import cern.japc.ext.mockito.JapcMock;
import cern.japc.ext.mockito.SuperCycle;
import cern.japc.ext.mockito.answers.DefaultParameterAnswer;

/**
 * This class implements a set of JUnit tests for <code>AlmonMessageHandler</code>. All tests that require
 * AlmonMessageHandler's pre-configuration with XML based configuration shall be annotated with <code>UseConf</code>
 * annotation, specifying the XML file to be used.
 *
 * @see cern.c2mon.daq.almon.AlmonMessageHandler
 * @author wbuczak
 */
@UseHandler(AlmonMessageHandler.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AlmonMessageHandlerTest extends GenericMessageHandlerTst {

    static Logger LOG = LoggerFactory.getLogger(AlmonMessageHandlerTest.class);

    static final Selector GM_DEVICE_SELECTOR = sel(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR);
    static final String ERROR_SERVER_DOWN = "Server is down";

    static {
        // activate the TEST spring profile
        setProperty("spring.profiles.active", "TEST");
    }

    protected AlmonMessageHandler emh;

    @Override
    protected void beforeTest() throws Exception {
        LOG.info("entering beforeTest()..");
        emh = (AlmonMessageHandler) msgHandler;
        resetJapcMock();
        JapcMock.init();
        resetJapcMock();

        LOG.info("leaving beforeTest()");
    }

    @Override
    protected void afterTest() throws Exception {
        LOG.info("entering afterTest()..");
        emh.disconnectFromDataSource();

        JapcMock.init();
        resetJapcMock();
        EasyMock.reset(messageSender);

        LOG.info("leaving afterTest()");
    }

    @Test
    @UseConf("conf-gm-one-metric.xml")
    public void testGmAlarmActivation() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 1 };
        Object[] values2 = { 0 };

        setAnswer(p1, GM_DEVICE_SELECTOR, new DefaultParameterAnswer(mpv(fields, values1)));

        Thread.sleep(300);

        emh.connectToDataSource();

        Thread.sleep(1000);

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values1));
        Thread.sleep(300);
        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values2));

        Thread.sleep(500);

        verify(messageSender);

        assertEquals(3, sdtv.getNumberOfCapturedValues(54675L));

        assertEquals(SourceDataQuality.OK, sdtv.getFirstValue(54675L).getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, sdtv.getFirstValue(54675L).getValue());

        UserProperties uprops = UserProperties.fromJson(sdtv.getFirstValue(54675L).getValueDescription());
        assertNull(uprops);

        assertEquals(SourceDataQuality.OK, sdtv.getValueAt(1, 54675L).getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, sdtv.getValueAt(1, 54675L).getValue());
        assertNotNull(sdtv.getValueAt(1, 54675L).getValueDescription());
        uprops = UserProperties.fromJson(sdtv.getValueAt(1, 54675L).getValueDescription());
        assertNotNull(uprops);
        assertTrue(uprops.isEmpty());

        assertEquals(SourceDataQuality.OK, sdtv.getLastValue(54675L).getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, sdtv.getLastValue(54675L).getValue());
        assertNotNull(sdtv.getLastValue(54675L).getValueDescription());
        uprops = UserProperties.fromJson(sdtv.getLastValue(54675L).getValueDescription());
        assertNull(uprops);
    }

    /**
     * This test verifies the Almon DAQ's behavior when a request to add a new DataTag is received at runtime. The DAQ
     * is supposed to subscribe to it and when it receives the valu for the new tag is should propagate it to the
     * business layer
     *
     * @throws Exception x
     */
    @Test(timeout = 5000)
    @UseConf("conf-gm-one-metric.xml")
    public void reconfDatatagAddTest() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(6); // 4+2 - the first one (for each tag) is the default value (false) initialized by the
                                   // DAQ

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 1 };
        Object[] values2 = { 2 };
        Object[] values3 = { 0 };

        setAnswer(p1, GM_DEVICE_SELECTOR, new DefaultParameterAnswer(mpv(fields, values1)));

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
        str.append("    \"alarmTriplet\": {");
        str.append("        \"faultFamily\": \"ITM.CRFBU\",");
        str.append("        \"faultMember\": \"RFLNP\",");
        str.append("        \"faultCode\": \"2\"");
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

        Thread.sleep(500);

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values1));

        Thread.sleep(500);

        // trigger adding new DataTag
        SourceDataTag newTag = SourceDataTag.fromConfigXML(parser.parse(str.toString()).getDocumentElement());

        ChangeReport report = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(1000);

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values2));

        Thread.sleep(500);

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values3));

        Thread.sleep(500);

        // try adding once again the same tag
        ChangeReport report2 = configurationController.onDataTagAdd(new DataTagAdd(1L, equipmentConfiguration.getId(),
                newTag));

        Thread.sleep(500);

        verify(messageSender);

        SourceDataTagValue firstValue1 = sdtv.getFirstValue(54675L);
        SourceDataTagValue lastValue1 = sdtv.getLastValue(54675L);
        SourceDataTagValue secondValue1 = sdtv.getValueAt(1, 54675L);

        SourceDataTagValue firstValue2 = sdtv.getFirstValue(54676L);
        SourceDataTagValue secondValue2 = sdtv.getValueAt(1, 54676L);
        SourceDataTagValue lastValue2 = sdtv.getLastValue(54676L);

        assertEquals(SourceDataQuality.OK, firstValue1.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, firstValue1.getValue());

        assertEquals(SourceDataQuality.OK, secondValue1.getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, secondValue1.getValue());

        assertEquals(SourceDataQuality.OK, lastValue1.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, lastValue1.getValue());

        assertEquals(SourceDataQuality.OK, firstValue2.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, firstValue2.getValue());

        assertEquals(SourceDataQuality.OK, secondValue2.getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, secondValue2.getValue());

        assertEquals(SourceDataQuality.OK, lastValue2.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, lastValue2.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());
        // the second should fail, since the tag is already registered
        assertEquals(CHANGE_STATE.FAIL, report2.getState());
    }

    /**
     * This test verifies the Almon DAQ's behavior when a request to remove an existing DataTag is received at runtime.
     * The DAQ is expected to unregister such tag, and terminate the active alarm (if previously active)
     *
     * @throws Exception x
     */
    @Test(timeout = 5000)
    @UseConf("conf-gm-one-metric.xml")
    public void reconfDatatagRemoveTest() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().times(3);

        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 1 };

        setAnswer(p1, GM_DEVICE_SELECTOR, new DefaultParameterAnswer(mpv(fields, values1)));

        emh.connectToDataSource();

        Thread.sleep(1500);

        // emulate receiving request to remove a datatag
        ChangeReport report = configurationController.onDataTagRemove(new DataTagRemove(1L, 54675L,
                equipmentConfiguration.getId()));

        // set the new value
        p1.setValue(GM_DEVICE_SELECTOR, mpv(fields, values1));

        Thread.sleep(1000);

        // emulate receiving request to remove a datatag - again the same one
        ChangeReport report2 = configurationController.onDataTagRemove(new DataTagRemove(1L, 54675L,
                equipmentConfiguration.getId()));

        Thread.sleep(500);

        verify(messageSender);

        assertEquals(3, sdtv.getNumberOfCapturedValues(54675L));

        SourceDataTagValue firstValue = sdtv.getFirstValue(54675L);
        SourceDataTagValue secondValue = sdtv.getValueAt(1, 54675L);
        SourceDataTagValue lastValue = sdtv.getLastValue(54675L);

        assertEquals(SourceDataQuality.OK, firstValue.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, firstValue.getValue());

        assertEquals(SourceDataQuality.OK, secondValue.getQuality().getQualityCode());
        assertEquals(Boolean.TRUE, secondValue.getValue());

        assertEquals(SourceDataQuality.OK, lastValue.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, lastValue.getValue());

        // the first request should be successful
        assertEquals(CHANGE_STATE.SUCCESS, report.getState());

        // the other one should give some warning, but the status should still be SUCCESS
        assertEquals(CHANGE_STATE.SUCCESS, report2.getState());
        assertEquals("The data tag with id 54675 to remove was not found in equipment with id 5250",
                report2.getWarnMessage());

    }

    @Test(timeout = 5000)
    @UseConf("conf-gm-one-metric.xml")
    /**
     * Note: this test is to be executed as the very last one. It uses JAPC mockito super-cycle, which for some reason
     * is not reset correctly one it is stopped and therefore can impact other tests
     * @throws Exception
     */
    public void test__DeviceGoesDown() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        messageSender.sendCommfaultTag(107211, false, "Server is down or unreachable");
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));
        expectLastCall().anyTimes();
        replay(messageSender);

        // Create Mock parameters
        Parameter p1 = mockParameter("RFLNP/ALARM");

        String[] fields = { "value" };
        Object[] values1 = { 0 };

        whenGetValueThen(p1, GM_DEVICE_SELECTOR, mpv(fields, values1), pe(ERROR_SERVER_DOWN));

        emh.connectToDataSource();

        Thread.sleep(2000);

        SuperCycle superCycle = newSuperCycle(new Cycle(AlmonHardwareAddress.GM_JAPC_ALARM_SELECTOR, 500), new Cycle(
                "", 2000));

        superCycle.start();
        Thread.sleep(1200);
        superCycle.stop();

        verify(messageSender);

        // make sure data tag was invalidated as expected
        assertEquals(2, sdtv.getNumberOfCapturedValues(54675L));

        SourceDataTagValue firstValue = sdtv.getFirstValue(54675L);
        SourceDataTagValue secondValue = sdtv.getValueAt(1, 54675L);

        assertEquals(SourceDataQuality.OK, firstValue.getQuality().getQualityCode());
        assertEquals(Boolean.FALSE, firstValue.getValue());

        assertEquals(SourceDataQuality.DATA_UNAVAILABLE, secondValue.getQuality().getQualityCode());
        assertEquals("Server is down or unreachable", secondValue.getQuality().getDescription());

    }

}
