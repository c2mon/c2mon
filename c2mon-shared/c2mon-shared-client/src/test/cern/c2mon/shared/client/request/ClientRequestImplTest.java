package cern.c2mon.shared.client.request;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.client.tag.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.client.command.CommandTagHandleImpl;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClientRequestImplTest {
  
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
      new ClientRequestImpl<TagValueUpdate>(TagValueUpdate.class);

    assertTrue(tagRequest.getTimeout() == 10000);

    ClientRequestImpl<ProcessXmlResponse> daqXmlRequest =
      new ClientRequestImpl<ProcessXmlResponse>(ProcessXmlResponse.class);

    assertTrue(daqXmlRequest.getTimeout() == 120000);
  }

  @Test
  public void testJsonMessageSerialization() {
    ClientRequestImpl<TagValueUpdate> tagRequest =
      new ClientRequestImpl<TagValueUpdate>(TagValueUpdate.class);
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
      new ClientRequestImpl<AlarmValue>(AlarmValue.class);
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
      new ClientRequestImpl<AlarmValue>(
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
      new ClientRequestImpl<TagConfig>(TagConfig.class);
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
      new ClientRequestImpl<ConfigurationReport>(ConfigurationReport.class);
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
      new ClientRequestImpl<TagUpdate>(TagUpdate.class);
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
      new ClientRequestImpl<AlarmValue>(AlarmValue.class);
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
      new ClientRequestImpl<ConfigurationReport>(ConfigurationReport.class);
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
      new ClientRequestImpl<TagConfig>(TagConfig.class);
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

      assertTrue(originalTagConfig.isControlTag() .equals(receivedTagConfig.isControlTag()));
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
      new ClientRequestImpl<SupervisionEvent>(SupervisionEvent.class);

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
      new ClientRequestImpl<ProcessNameResponse>(ProcessNameResponse.class);

    String json = processNameRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);    
    assertEquals(ClientRequest.RequestType.PROCESS_NAMES_REQUEST, receivedRequest.getRequestType());

    String s = (String) receivedRequest.getRequestParameter();
    assertEquals((String)processNameRequest.getRequestParameter(), (String)s);

    //fake response from server
    ProcessNameResponse processResponse = new ProcessNameResponseImpl("process name");
    Collection<ProcessNameResponse> responseList = new ArrayList<ProcessNameResponse>();
    responseList.add(processResponse);

    Gson gson = TransferTagValueImpl.getGson();   
    String jsonResponse = gson.toJson(responseList);

    Collection<ProcessNameResponse> receivedResponse = processNameRequest.fromJsonResponse(jsonResponse);
    assertEquals("process name", receivedResponse.iterator().next().getProcessName());
  }

  @Test
  public void testProcessXmlRequest() {
    ClientRequestImpl<ProcessXmlResponse> xmlRequest =
      new ClientRequestImpl<ProcessXmlResponse>(ProcessXmlResponse.class);
    xmlRequest.setRequestParameter("request parameter");
    String json = xmlRequest.toJson();
    ClientRequest receivedRequest = ClientRequestImpl.fromJson(json);    
    assertEquals(ClientRequest.RequestType.DAQ_XML_REQUEST, receivedRequest.getRequestType());

    String s = (String) receivedRequest.getRequestParameter();
    assertEquals((String)xmlRequest.getRequestParameter(), (String)s);

    //fake response from server
    ProcessXmlResponse xmlResponse = new ProcessXmlResponseImpl();
    ((ProcessXmlResponseImpl) xmlResponse).setProcessXML("xml string");
    Collection<ProcessXmlResponse> responseList = new ArrayList<ProcessXmlResponse>();
    responseList.add(xmlResponse);
    
    Gson gson = TransferTagValueImpl.getGson();
    String jsonResponse = gson.toJson(responseList);


    Collection<ProcessXmlResponse> receivedResponse = xmlRequest.fromJsonResponse(jsonResponse);
    assertEquals("xml string", receivedResponse.iterator().next().getProcessXML());
  }


  private String mockSupervisionValueResponse(final String jsonSupervisionRequest, final int size) {
    ClientRequest tagRequest = ClientRequestImpl.fromJson(jsonSupervisionRequest);

    Collection<SupervisionEvent> responseList = null;
    if (tagRequest.getRequestType() == ClientRequest.RequestType.SUPERVISION_REQUEST) {
      responseList = new ArrayList<SupervisionEvent>();
      for (int i = 0; i < size; i++) {
        responseList.add(
            new SupervisionEventImpl(SupervisionEntity.PROCESS, Long.valueOf(i), SupervisionStatus.RUNNING, new Timestamp(System.currentTimeMillis()), "supervision event " + i));
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
    ArrayList<String> processNames = new ArrayList<String>();
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
    return new TransferTagImpl(
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
  }
}
