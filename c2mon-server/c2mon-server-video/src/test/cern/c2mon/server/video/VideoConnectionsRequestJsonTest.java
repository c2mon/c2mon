package cern.c2mon.server.video;

import static junit.framework.Assert.assertTrue;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import com.google.gson.Gson;

import cern.c2mon.server.video.VideoRequestMessageConverter;
import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.video.VideoRequest;
import cern.c2mon.shared.video.VideoRequest.RequestType;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.util.json.GsonFactory;

public class VideoConnectionsRequestJsonTest {

  private final String VIDEO_SYSTEM_NAME = "Test_Video_System_Name";

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  @Test
  public void testVideoConnectionRequestMessageConversion() {

    VideoRequest request = new VideoRequest(VIDEO_SYSTEM_NAME, VideoConnectionProperties.class);

    assertTrue(request.getRequestType() .equals(RequestType.VIDEO_CONNECTION_PROPERTIES_REQUEST));

    TextMessage message = new ActiveMQTextMessage();

    try {
      message.setText(GSON.toJson(request));
      VideoRequest receivedRequest = VideoRequestMessageConverter.fromMessage(message);

      System.out.println(receivedRequest.getVideoSystemName() + " - " + request.getVideoSystemName());
      assertTrue(receivedRequest.getVideoSystemName() .equals(request.getVideoSystemName()));
      assertTrue(receivedRequest.getRequestType() .equals(RequestType.VIDEO_CONNECTION_PROPERTIES_REQUEST ));
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  @Test
  public void testAuthorisationDetailsRequestMessageConversion() {

    VideoRequest request = new VideoRequest(VIDEO_SYSTEM_NAME, RbacAuthorizationDetails.class);

    assertTrue(request.getRequestType() .equals(RequestType.AUTHORIZATION_DETAILS_REQUEST));

    TextMessage message = new ActiveMQTextMessage();

    try {
      message.setText(GSON.toJson(request));
      VideoRequest receivedRequest = VideoRequestMessageConverter.fromMessage(message);

      System.out.println(receivedRequest.getVideoSystemName() + " - " + request.getVideoSystemName());
      assertTrue(receivedRequest.getVideoSystemName() .equals(request.getVideoSystemName()));
      assertTrue(receivedRequest.getRequestType() .equals(RequestType.AUTHORIZATION_DETAILS_REQUEST));
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }  
}


