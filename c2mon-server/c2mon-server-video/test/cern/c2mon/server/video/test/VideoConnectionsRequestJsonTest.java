package cern.c2mon.server.video.test;

import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import cern.c2mon.client.common.video.VideoConnectionsRequest;
import cern.c2mon.server.video.VideoRequestMessageConverter;
import cern.tim.shared.client.auth.impl.TimSessionInfoImpl;

public class VideoConnectionsRequestJsonTest {

  @Test
  public void testVideoConnectionRequestMessageConversion() {
    
    String videoSystemName = "Test_Video_System_Name";
    TimSessionInfoImpl sessionInfo = createSessionInfo ();
    VideoConnectionsRequest request = new VideoConnectionsRequest(sessionInfo, videoSystemName);

    TextMessage message = new ActiveMQTextMessage();
    
    try {
      message.setText(request.toJson());
      VideoConnectionsRequest receivedRequest = VideoRequestMessageConverter.fromMessage(message);

      System.out.println(receivedRequest.getVideoSystemName()+" - "+request.getVideoSystemName());
      assertTrue(receivedRequest.getVideoSystemName() .equals ( request.getVideoSystemName()));
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }
  
  private TimSessionInfoImpl createSessionInfo () {
    
    String pSessionId = "testSessionId";
    Long pUserId = 123L;
    String pUserName = "testSessionId";
    Timestamp pExpiryDate = new Timestamp(System.currentTimeMillis());
    String pClientHost = "testSessionId";
    String[] pPrivileges = {"testSessionId"};
    
    return new TimSessionInfoImpl(pSessionId, pUserId, pUserName, pExpiryDate, pClientHost, pPrivileges);
  }
}
