package cern.c2mon.server.client.request;

import java.util.Collections;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static junit.framework.Assert.*;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TransferTag;
import cern.c2mon.shared.client.tag.TransferTagValue;

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
    JsonRequest<TransferTag> request = new ClientRequestImpl<TransferTag>(TransferTag.class);
    
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
    JsonRequest<TransferTagValue> request = new ClientRequestImpl<TransferTagValue>(TransferTagValue.class);
    
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
}
