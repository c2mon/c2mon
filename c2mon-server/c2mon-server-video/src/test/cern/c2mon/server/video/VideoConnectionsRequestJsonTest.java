package cern.c2mon.server.video;

import static junit.framework.Assert.assertTrue;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestErrorReportImpl;
import cern.c2mon.shared.client.request.ClientRequestErrorReport.RequestExecutionStatus;
import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.video.VideoRequest;
import cern.c2mon.shared.video.VideoRequest.RequestType;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.util.json.GsonFactory;

import com.google.gson.Gson;

public class VideoConnectionsRequestJsonTest {

  private final String VIDEO_SYSTEM_NAME = "Test_Video_System_Name";

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  @Test
  public void testClientRequestErrorReportMessageConversion() {

    ClientRequestErrorReport reportFail = new ClientRequestErrorReportImpl(false, "Error Message!");
    assertTrue(reportFail.getRequestExecutionStatus() .equals(RequestExecutionStatus.REQUEST_FAILED));

    ClientRequestErrorReport reportSuccess = new ClientRequestErrorReportImpl(true, null);
    assertTrue(reportSuccess.getRequestExecutionStatus() .equals(RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY));

    String result1 = (GSON.toJson(reportFail));
    String result2 = (GSON.toJson(reportSuccess));
    
    ClientRequestErrorReport reportFailReceived = GSON.fromJson(result1, ClientRequestErrorReportImpl.class);
    ClientRequestErrorReport reportSuccessReceived = GSON.fromJson(result2, ClientRequestErrorReportImpl.class);

    assertTrue(reportFailReceived.getRequestExecutionStatus() .equals(RequestExecutionStatus.REQUEST_FAILED));
    assertTrue(reportSuccessReceived.getRequestExecutionStatus() .equals(RequestExecutionStatus.REQUEST_EXECUTED_SUCCESSFULLY));
  }

  @Test
  public void testVideoConnectionRequestMessageConversion() {

    VideoRequest request = new VideoRequest(VIDEO_SYSTEM_NAME, VideoConnectionProperties.class);

    assertTrue(request.getRequestType() .equals(RequestType.VIDEO_CONNECTION_PROPERTIES_REQUEST));

    TextMessage message = new ActiveMQTextMessage();

    try {
      message.setText(GSON.toJson(request));
      VideoRequest receivedRequest = VideoRequestMessageConverter.fromMessage(message);

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

      assertTrue(receivedRequest.getVideoSystemName() .equals(request.getVideoSystemName()));
      assertTrue(receivedRequest.getRequestType() .equals(RequestType.AUTHORIZATION_DETAILS_REQUEST));
    }
    catch (JMSException e) {
      assertTrue(e.getMessage(), false);
    }
  }  
}


