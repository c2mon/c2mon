package cern.c2mon.server.client.request;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static junit.framework.Assert.*;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandReportImpl;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.command.CommandTagHandleImpl;
import cern.tim.shared.client.configuration.ConfigurationReport;

public class ClientRequestMessageConverterTest {

  @Test
  public void testSupervisionMessageConversion() {
    JsonRequest<SupervisionEvent> request = new ClientRequestImpl<SupervisionEvent>(SupervisionEvent.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.SUPERVISION_REQUEST);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }


  @Test
  public void testTransferTagMessageConversion() {
    JsonRequest<TagUpdate> request = new ClientRequestImpl<TagUpdate>(TagUpdate.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.TAG_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_TAG_LIST);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }


  @Test
  public void testTransferTagValueMessageConversion() {
    JsonRequest<TagValueUpdate> request = new ClientRequestImpl<TagValueUpdate>(TagValueUpdate.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.TAG_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_TAG_VALUE_LIST);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  @Test
  public void testAlarmValueMessageConversion() {
    JsonRequest<AlarmValue> request = new ClientRequestImpl<AlarmValue>(AlarmValue.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.ALARM_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_ALARM_LIST);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }  

  @Test
  public void testTagConfigMessageConversion() {
    JsonRequest<TagConfig> request = new ClientRequestImpl<TagConfig>(TagConfig.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.TAG_CONFIGURATION_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_TAG_CONFIGURATION_LIST);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }   

  @Test
  public void testConfigurationReportMessageConversion() {
    JsonRequest<ConfigurationReport> request = new ClientRequestImpl<ConfigurationReport>(ConfigurationReport.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.APPLY_CONFIGURATION_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_CONFIGURATION_REPORT);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }   
  
  @Test
  public void testCommandTagHandleMessageConversion() {
    JsonRequest<CommandTagHandle> request = new ClientRequestImpl<CommandTagHandle>(CommandTagHandle.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.COMMAND_HANDLE_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_COMMAND_HANDLES_LIST);
      assertTrue(receivedRequest.requiresObjectResponse());
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }
  
  @Test
  public void testExecuteCommandMessageConversion() {
    JsonRequest<CommandReport> request = new ClientRequestImpl<CommandReport>(CommandReport.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertTrue(receivedRequest.getRequestType() == ClientRequest.RequestType.EXECUTE_COMMAND_REQUEST);
      assertTrue(receivedRequest.getResultType() == ClientRequest.ResultType.TRANSFER_COMMAND_REPORT);
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }
  
}
