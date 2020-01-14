/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.client.request;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.command.*;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.DeviceClassNameResponseImpl;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.device.TransferDeviceImpl;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.*;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class ClientRequestImplTest {

  static Gson gson = GsonFactory.createGsonBuilder().create();

  @Test
  public void testReports() {

    final int currentProgress = 1;
    final String errorMessage = "Serious Error. Matthias had too much food";

    ClientRequestResult progressReportResult = new ConfigurationReport(2, 1, 10, currentProgress, "In progress");

    assertTrue(((ClientRequestReport) progressReportResult).isProgressReport());
    assertTrue(currentProgress == ((ClientRequestReport) progressReportResult).getCurrentProgressPart());
    assertFalse(((ClientRequestReport) progressReportResult).isErrorReport());
    assertFalse(((ClientRequestReport) progressReportResult).isResult());

    ClientRequestResult errorReportResult = new ConfigurationReport(false, errorMessage);

    assertTrue(((ClientRequestReport) errorReportResult).isErrorReport());
    assertTrue(errorMessage.equals(((ClientRequestReport) errorReportResult).getErrorMessage()));
    assertFalse(((ClientRequestReport) errorReportResult).isProgressReport());
    assertFalse(((ClientRequestReport) errorReportResult).isResult());
  }

  @Test
  public void testRequestTimeouts() {

    ClientRequestImpl<TagValueUpdate> tagRequest =
      new ClientRequestImpl<>(TagValueUpdate.class);

    assertTrue(tagRequest.getTimeout() == 10000);

    ClientRequestImpl<ProcessXmlResponse> daqXmlRequest =
      new ClientRequestImpl<>(ProcessXmlResponse.class);

    assertTrue(daqXmlRequest.getTimeout() == 120000);
  }

  @Test
  public void testJsonMessageSerialization() {
    ClientRequestImpl<TagValueUpdate> tagRequest =
      new ClientRequestImpl<>(TagValueUpdate.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    String json = tagRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.TAG_REQUEST, receivedRequest.getRequestType());
    assertEquals(tagRequest.getTagIds().size(), receivedRequest.getTagIds().size());
    Collection<Long> receivedTags = receivedRequest.getTagIds();
    for (Long tagId : receivedTags) {
      assertFalse(tagRequest.addTagId(tagId));
    }
  }

  @Test
  public void testAlarmJsonMessageSerialization() {
    ClientRequestImpl<AlarmValue> tagRequest =
      new ClientRequestImpl<>(AlarmValue.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    String json = tagRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.ALARM_REQUEST, receivedRequest.getRequestType());
    assertEquals(tagRequest.getTagIds().size(), receivedRequest.getTagIds().size());
    Collection<Long> receivedTags = receivedRequest.getTagIds();
    for (Long tagId : receivedTags) {
      assertFalse(tagRequest.addTagId(tagId));
    }
  }

  @Test
  public void testActiveAlarmJsonMessageSerialization() {

    ClientRequestImpl<AlarmValue> alarmsRequest =
      new ClientRequestImpl<>(
          ClientRequest.ResultType.TRANSFER_ALARM_LIST,
          ClientRequest.RequestType.ACTIVE_ALARMS_REQUEST,
          10000
      );

    String json = alarmsRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);

    assertEquals(ClientRequest.RequestType.ACTIVE_ALARMS_REQUEST, receivedRequest.getRequestType());
    assertEquals(ClientRequest.ResultType.TRANSFER_ALARM_LIST, receivedRequest.getResultType());
  }

  @Test
  public void testTagConfigurationJsonMessageSerialization() {
    ClientRequestImpl<TagConfig> tagRequest =
      new ClientRequestImpl<>(TagConfig.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    String json = tagRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.TAG_CONFIGURATION_REQUEST, receivedRequest.getRequestType());
    assertEquals(tagRequest.getTagIds().size(), receivedRequest.getTagIds().size());
    Collection<Long> receivedTags = receivedRequest.getTagIds();
    for (Long tagId : receivedTags) {
      assertFalse(tagRequest.addTagId(tagId));
    }
  }

  @Test
  public void testConfigurationReportJsonMessageSerialization() {
    ClientRequestImpl<ConfigurationReport> tagRequest =
      new ClientRequestImpl<>(ConfigurationReport.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    String json = tagRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.APPLY_CONFIGURATION_REQUEST, receivedRequest.getRequestType());
    assertEquals(tagRequest.getTagIds().size(), receivedRequest.getTagIds().size());
    Collection<Long> receivedTags = receivedRequest.getTagIds();
    for (Long tagId : receivedTags) {
      assertFalse(tagRequest.addTagId(tagId));
    }
  }

  @Test
  public void testJsonResponseDeserialization() {
    ClientRequestImpl<TagUpdate> tagRequest =
      new ClientRequestImpl<>(TagUpdate.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L));



    Float responseTagValue = Float.valueOf(2342.456546f);
    String serverResponse = mockTagValueResponse(tagRequest.toJson(), responseTagValue);
    Collection<TagUpdate> responseList = tagRequest.fromJsonResponse(serverResponse);

    assertEquals(tagRequest.getTagIds().size(), responseList.size());
    for (TagUpdate tagValue : responseList) {
      assertTrue(tagValue instanceof TransferTagImpl);
      assertTrue(tagRequest.getTagIds().contains(tagValue.getId()));
      assertTrue(tagValue.getValue() instanceof Float);
      assertEquals(responseTagValue, tagValue.getValue());
    }
  }

  @Test
  public void testAlarmJsonResponseDeserialization() {
    ClientRequestImpl<AlarmValue> alarmRequest =
      new ClientRequestImpl<>(AlarmValue.class);
    alarmRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    AlarmValue originalAlarm = createAlarm(123L) ;

    Float responseTagValue = Float.valueOf(2342.456546f); // not used
    String serverResponse = mockTagValueResponse(alarmRequest.toJson(), responseTagValue);
    Collection<AlarmValue> responseList = alarmRequest.fromJsonResponse(serverResponse);

    assertEquals(alarmRequest.getTagIds().size(), responseList.size());
    for (AlarmValue receivedAlarmValue : responseList) {
      assertTrue(receivedAlarmValue instanceof AlarmValueImpl);
      assertTrue(alarmRequest.getTagIds().contains(receivedAlarmValue.getId()));

      assertTrue(originalAlarm.getFaultCode() == receivedAlarmValue.getFaultCode());
      assertTrue(originalAlarm.getTagId() .equals(receivedAlarmValue.getTagId()));

      assertTrue(originalAlarm.getFaultFamily() .equals(receivedAlarmValue.getFaultFamily()));
      assertTrue(originalAlarm.getFaultMember() .equals(receivedAlarmValue.getFaultMember()));
    }
  }

  @Test
  public void testConfigurationReportJsonResponseDeserialization() {
    ClientRequestImpl<ConfigurationReport> tagRequest =
      new ClientRequestImpl<>(ConfigurationReport.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    ConfigurationReport originalConfigurationReport = createConfigurationReport(123L) ;

    Float responseTagValue = Float.valueOf(2342.456546f); // not used
    String serverResponse = mockTagValueResponse(tagRequest.toJson(), responseTagValue);
    Collection<ConfigurationReport> responseList = tagRequest.fromJsonResponse(serverResponse);

    assertEquals(tagRequest.getTagIds().size(), responseList.size());
    for (ConfigurationReport receivedReport : responseList) {
      assertTrue(tagRequest.getTagIds().contains(receivedReport.getId()));

      assertTrue(originalConfigurationReport.getName() .equals(receivedReport.getName()));
      assertTrue(originalConfigurationReport.getStatus() .equals(receivedReport.getStatus()));
      assertTrue(originalConfigurationReport.getStatusDescription() .equals(receivedReport.getStatusDescription()));

      assertTrue(originalConfigurationReport.getUser() .equals(receivedReport.getUser()));
      assertTrue(originalConfigurationReport.getElementReports() .equals(receivedReport.getElementReports()));
    }
  }

  @Test
  public void testTagConfigurationJsonResponseDeserialization() {
    ClientRequestImpl<TagConfig> tagRequest =
      new ClientRequestImpl<>(TagConfig.class);
    tagRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    TagConfig originalTagConfig = createTagConfig(123L) ;

    Float responseTagValue = Float.valueOf(2342.456546f);  // not used
    String serverResponse = mockTagValueResponse(tagRequest.toJson(), responseTagValue);
    Collection<TagConfig> responseList = tagRequest.fromJsonResponse(serverResponse);

    assertEquals(tagRequest.getTagIds().size(), responseList.size());
    for (TagConfig receivedTagConfig : responseList) {
      assertTrue(receivedTagConfig instanceof TagConfigImpl);
      assertTrue(tagRequest.getTagIds().contains(receivedTagConfig.getId()));

      assertTrue(originalTagConfig.getPriority() == receivedTagConfig.getPriority());
      assertTrue(originalTagConfig.getTimeDeadband() == receivedTagConfig.getTimeDeadband());
      assertTrue(originalTagConfig.getValueDeadband() == receivedTagConfig.getValueDeadband());

      assertTrue(originalTagConfig.isControlTag() == receivedTagConfig.isControlTag());
      assertTrue(originalTagConfig.isGuaranteedDelivery() == (receivedTagConfig.isGuaranteedDelivery()));
      assertTrue(originalTagConfig.getRuleIds() .equals(receivedTagConfig.getRuleIds()));
      assertTrue(originalTagConfig.getAlarmIds() .equals(receivedTagConfig.getAlarmIds()));

      assertTrue(originalTagConfig.getDipPublication() .equals(receivedTagConfig.getDipPublication()));
      assertTrue(originalTagConfig.getJapcPublication() .equals(receivedTagConfig.getJapcPublication()));
      assertTrue(originalTagConfig.getHardwareAddress() .equals(receivedTagConfig.getHardwareAddress()));

      assertTrue(originalTagConfig.getMaxValue() .equals(receivedTagConfig.getMaxValue()));
      assertTrue(originalTagConfig.getMinValue() .equals(receivedTagConfig.getMinValue()));
      assertTrue(originalTagConfig.getRuleExpression() .equals(receivedTagConfig.getRuleExpression()));

      assertTrue(originalTagConfig.getTopicName() .equals(receivedTagConfig.getTopicName()));
      assertEquals(originalTagConfig.isLogged(), receivedTagConfig.isLogged());
      assertEquals(originalTagConfig.getProcessNames(), receivedTagConfig.getProcessNames());
    }
  }

  /*
   * We don't use Json for Command serialization any more
   *
   *@Test
  public void testCommandJsonResponseDeserialization() {
    ClientRequestImpl<CommandTagHandle> commandTagHandleRequest =
      new ClientRequestImpl<CommandTagHandle>(CommandTagHandle.class);

    commandTagHandleRequest.addTagIds(Arrays.asList(123L, 4324L, 4535L, 123L));

    CommandTagHandleImpl originalCommandTagHandle = createCommandTagHandleImpl(123L);

    Float responseTagValue = Float.valueOf(2342.456546f);  // not used
    String serverResponse = mockTagValueResponse(commandTagHandleRequest.toJson(), responseTagValue);
    Collection<CommandTagHandle> responseList = commandTagHandleRequest.fromJsonResponse(serverResponse);

    assertEquals(commandTagHandleRequest.getTagIds().size(), responseList.size());
    for (CommandTagHandle receivedTagHandle : responseList) {
      assertTrue(receivedTagHandle instanceof CommandTagHandleImpl);
      assertTrue(commandTagHandleRequest.getTagIds().contains(receivedTagHandle.getId()));
    }
  }



  /*
   * We don't use Json for Command serialization any more
   *
   *@Test
  public void testExecuteCommandJsonResponseDeserialization() {

    ClientRequestImpl<CommandReport> executeCommandRequest =
      new ClientRequestImpl<CommandReport>(CommandReport.class);
    CommandTagHandleImpl originalCommandTagHandle = createCommandTagHandleImpl(123L);
    executeCommandRequest.setObjectParameter(originalCommandTagHandle);

    String json = executeCommandRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.EXECUTE_COMMAND_REQUEST, receivedRequest.getRequestType());


    CommandTagHandleImpl h1 = (CommandTagHandleImpl)executeCommandRequest.getObjectParameter();
    CommandTagHandleImpl h2 = (CommandTagHandleImpl)receivedRequest.getObjectParameter();
    assertEquals(h1.getId(), h2.getId());

    CommandReport originalReport = createCommandReport(executeCommandRequest);

    Float responseTagValue = Float.valueOf(2342.456546f);  // not used
    String serverResponse = mockTagValueResponse(executeCommandRequest.toJson(), responseTagValue);
    Collection<CommandReport> responseList = executeCommandRequest.fromJsonResponse(serverResponse);

    assertEquals(executeCommandRequest.getTagIds().size(), responseList.size());
    for (CommandReport receivedReport : responseList) {
      assertTrue(receivedReport instanceof CommandReport);
      assertTrue(originalReport.getId() == (receivedReport.getId()));
      assertTrue(originalReport.getReportText() .equals(receivedReport.getReportText()));
    }
  }*/

  @Test
  public void testSupervisionEventRequest() {
    ClientRequestImpl<SupervisionEvent> tagRequest =
      new ClientRequestImpl<>(SupervisionEvent.class);

    String json = tagRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.SUPERVISION_REQUEST, receivedRequest.getRequestType());
    assertEquals(tagRequest.getTagIds().size(), receivedRequest.getTagIds().size());

    int size = 10;
    String serverResponse = mockSupervisionValueResponse(json, size);
    Collection<SupervisionEvent> responseList = tagRequest.fromJsonResponse(serverResponse);
    assertEquals(size, responseList.size());
    for (SupervisionEvent event : responseList) {
      assertEquals(SupervisionEntity.PROCESS, event.getEntity());
      assertEquals(SupervisionStatus.RUNNING, event.getStatus());
    }
  }

  @Test
  public void testProcessNamesRequest() {
    ClientRequestImpl<ProcessNameResponse> processNameRequest =
      new ClientRequestImpl<>(ProcessNameResponse.class);

    String json = processNameRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.PROCESS_NAMES_REQUEST, receivedRequest.getRequestType());

    String s = receivedRequest.getRequestParameter();
    assertEquals(processNameRequest.getRequestParameter(), s);

    //fake response from server
    ProcessNameResponse processResponse = new ProcessNameResponseImpl("process name");
    Collection<ProcessNameResponse> responseList = new ArrayList<>();
    responseList.add(processResponse);

    String jsonResponse = gson.toJson(responseList);

    Collection<ProcessNameResponse> receivedResponse = processNameRequest.fromJsonResponse(jsonResponse);
    assertEquals("process name", receivedResponse.iterator().next().getProcessName());
  }

  @Test
  public void testProcessXmlRequest() {
    ClientRequestImpl<ProcessXmlResponse> xmlRequest =
      new ClientRequestImpl<>(ProcessXmlResponse.class);
    xmlRequest.setRequestParameter("request parameter");
    String json = xmlRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.DAQ_XML_REQUEST, receivedRequest.getRequestType());

    String s = receivedRequest.getRequestParameter();
    assertEquals(xmlRequest.getRequestParameter(), s);

    //fake response from server
    ProcessXmlResponse xmlResponse = new ProcessXmlResponseImpl();
    ((ProcessXmlResponseImpl) xmlResponse).setProcessXML("xml string");
    Collection<ProcessXmlResponse> responseList = new ArrayList<>();
    responseList.add(xmlResponse);

    String jsonResponse = gson.toJson(responseList);


    Collection<ProcessXmlResponse> receivedResponse = xmlRequest.fromJsonResponse(jsonResponse);
    assertEquals("xml string", receivedResponse.iterator().next().getProcessXML());
  }

  @Test
  public void testDeviceClassNamesRequest() {
    ClientRequestImpl<DeviceClassNameResponse> deviceClassNamesRequest = new ClientRequestImpl<>(DeviceClassNameResponse.class);
    String json = deviceClassNamesRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.DEVICE_CLASS_NAMES_REQUEST, receivedRequest.getRequestType());

    // Fake response from server
    DeviceClassNameResponse className1 = new DeviceClassNameResponseImpl("test_device_class_name_1");
    DeviceClassNameResponse className2 = new DeviceClassNameResponseImpl("test_device_class_name_2");
    Collection<DeviceClassNameResponse> responseList = new ArrayList<>();
    responseList.add(className1);
    responseList.add(className2);

    String jsonResponse = gson.toJson(responseList);

    List<DeviceClassNameResponse> receivedResponse = (List<DeviceClassNameResponse>) deviceClassNamesRequest.fromJsonResponse(jsonResponse);
    assertTrue(receivedResponse.size() == 2);

    assertTrue(receivedResponse.get(0).getDeviceClassName().equals("test_device_class_name_1"));
    assertTrue(receivedResponse.get(1).getDeviceClassName().equals("test_device_class_name_2"));
  }

  @Test
  public void testDeviceRequest() {
    ClientRequestImpl<TransferDevice> devicesRequest = new ClientRequestImpl<>(TransferDevice.class);
    devicesRequest.setRequestParameter("test_device_class_name");
    String json = devicesRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);
    assertEquals(ClientRequest.RequestType.DEVICE_REQUEST, receivedRequest.getRequestType());

    // Fake response from server
    TransferDevice device1 = new TransferDeviceImpl(1000L, "test_device_1", 1L, "test_class_name");
    TransferDevice device2 = new TransferDeviceImpl(2000L, "test_device_2", 1L, "test_class_name");
    Collection<TransferDevice> responseList = new ArrayList<>();
    responseList.add(device1);
    responseList.add(device2);

    String jsonResponse = gson.toJson(responseList);

    List<TransferDevice> receivedResponse = (List<TransferDevice>) devicesRequest.fromJsonResponse(jsonResponse);
    assertTrue(receivedResponse.size() == 2);

    assertTrue(receivedResponse.get(0).getDeviceClassId().equals(1L));
    assertTrue(receivedResponse.get(1).getDeviceClassId().equals(1L));
    assertTrue(receivedResponse.get(0).getDeviceClassName().equals("test_class_name"));
    assertTrue(receivedResponse.get(0).getDeviceClassName().equals("test_class_name"));

  }

  @Test
  public void testTagStatisticsRequest() {
    // TODO
  }

  private String mockSupervisionValueResponse(final String jsonSupervisionRequest, final int size) {
    ClientRequest tagRequest = ClientRequestImpl.fromJson(jsonSupervisionRequest);

    Collection<SupervisionEvent> responseList = null;
    if (tagRequest.getRequestType() == ClientRequest.RequestType.SUPERVISION_REQUEST) {
      responseList = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        responseList.add(
            new SupervisionEventImpl(SupervisionEntity.PROCESS, Long.valueOf(i), "P_TEST", SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "supervision event " + i));
      }
    }
    else {
      assertTrue("Unsupported request type", false);
    }

    Gson gson = GsonFactory.createGson();
    return gson.toJson(responseList);
  }

  /**
   * @param jsonTagRequest The request as Json string
   * @param tagValue The value that shall be mocked as server response
   * @return Json string which represents a list of <code>TransferTagValue</code> or
   *         <code>TransferTag</code> objects
   */
  @SuppressWarnings("unchecked")
  private String mockTagValueResponse(final String jsonTagRequest, final Object tagValue) {
    ClientRequest tagRequest = ClientRequestImpl.fromJson(jsonTagRequest);

    Collection responseList = new ArrayList();
    if (tagRequest.getRequestType() == ClientRequest.RequestType.TAG_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {

          case TRANSFER_TAG_LIST:
            responseList.add(createTag(tagId, tagValue));
            break;
          case TRANSFER_TAG_VALUE_LIST:
            responseList.add(createTagValue(tagId, tagValue));
            break;

          default:
        }
      }
    }


    else if (tagRequest.getRequestType() == ClientRequest.RequestType.ALARM_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {
          case TRANSFER_ALARM_LIST:
            responseList.add(createAlarm(tagId));
            break;

          default:
        }
      }
    }

    else if (tagRequest.getRequestType() == ClientRequest.RequestType.APPLY_CONFIGURATION_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {
          case TRANSFER_CONFIGURATION_REPORT:
            responseList.add(createConfigurationReport(tagId));
            break;

          default:
        }
      }
    }

    else if (tagRequest.getRequestType() == ClientRequest.RequestType.TAG_CONFIGURATION_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {
          case TRANSFER_TAG_CONFIGURATION_LIST:
            responseList.add(createTagConfig(tagId));
            break;

          default:
        }
      }
    }

    else if (tagRequest.getRequestType() == ClientRequest.RequestType.COMMAND_HANDLE_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {
          case TRANSFER_COMMAND_HANDLES_LIST:
            responseList.add(createCommandTagHandleImpl(tagId));
            break;

          default:
        }
      }
    }

    else if (tagRequest.getRequestType() == ClientRequest.RequestType.EXECUTE_COMMAND_REQUEST) {
      for (Long tagId : tagRequest.getTagIds()) {
        switch (tagRequest.getResultType()) {
          case TRANSFER_COMMAND_REPORT:
            responseList.add(createCommandReport(tagRequest));
            break;

          default:
        }
      }
    }

    else {
      assertTrue("Unsupported request type", false);
    }

    Gson gson = GsonFactory.createGson();
    return gson.toJson(responseList);
  }

  private CommandReport createCommandReport(final ClientRequest clientRequest) {

    CommandTagHandleImpl handle = (CommandTagHandleImpl) clientRequest.getObjectParameter() ;

    CommandReport report = new CommandReportImpl(handle.getId(), CommandExecutionStatus.STATUS_OK, "test report") ;
    return report;
  }

  private ConfigurationReport createConfigurationReport (final Long id) {

    return new ConfigurationReport(id, "test name", "test user");
  }

  private CommandTagHandleImpl createCommandTagHandleImpl (final Long id) {

    CommandTagHandleImpl commandTagHadle = new CommandTagHandleImpl(id, "test name",
        "test description", "test datatype", 666, "666", "666"
        , "test Host Name", createAuthDetails());

    return commandTagHadle;
  }

  private RbacAuthorizationDetails createAuthDetails () {

    RbacAuthorizationDetails authDetails = new RbacAuthorizationDetails();

    authDetails.setRbacClass("Manos");
    authDetails.setRbacDevice("Mark");
    authDetails.setRbacProperty("Matias");

    return authDetails;
  }

  private TagConfigImpl createTagConfig(final Long id) {

    TagConfigImpl tagConfig = new TagConfigImpl(id);

    tagConfig.setControlTag(true);
    tagConfig.setGuaranteedDelivery(true);
    tagConfig.setHardwareAddress("test hardware address");
    tagConfig.setMaxValue("666");
    tagConfig.setMinValue("666");
    tagConfig.setPriority(666) ;
    tagConfig.setRuleExpressionStr("test rule expression");
    tagConfig.setTimeDeadband(666);
    tagConfig.setTopicName("test topic name");
    tagConfig.setValueDeadband(666);

    tagConfig.setAlarmIds(Arrays.asList(123L, 4324L, 4535L));
    tagConfig.setRuleIds(Arrays.asList(123L, 4324L, 4535L));

    tagConfig.setLogged(Boolean.FALSE);
    ArrayList<String> processNames = new ArrayList<>();
    processNames.add("Process1");
    processNames.add("Process2");
    tagConfig.setProcessNames(processNames);

    tagConfig.addPublication(Publisher.DIP, "test Dip topic");
    tagConfig.addPublication(Publisher.JAPC, "test JAPC topic");

    return tagConfig;
  }

  private AlarmValueImpl createAlarm(final Long id) {
    return new AlarmValueImpl(
        id,
        666,
        "test Fault Member",
        "test Fault Family",
        "test Info",
        123L,
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis() - 10),
        true);
  }

  private TransferTagValueImpl createTagValue(final Long tagId, final Object tagValue) {
    return new TransferTagValueImpl(
        tagId,
        tagValue,
        "test value desc",
        new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process Down"),
        TagMode.TEST,
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis()),
    "test description");
  }

  private TransferTagImpl createTag(final Long tagId, final Object tagValue) {
    TransferTagImpl result = new TransferTagImpl(
        tagId,
        tagValue,
        "test value desc",
        new DataTagQualityImpl(TagQualityStatus.PROCESS_DOWN, "Process Down"),
        TagMode.TEST,
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis()),
        new Timestamp(System.currentTimeMillis()),
        "test description",
        "tag name",
        "tag:topic");
    result.setValueClassName(tagValue.getClass().getName());
    return result;
  }
}
