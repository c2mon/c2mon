/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.server.video;

import java.sql.SQLException;
import java.util.Collection;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestErrorReportImpl;
import cern.c2mon.shared.client.request.ServerRequestException;
import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.video.VideoRequest;
import cern.c2mon.shared.video.VideoRequest.RequestType;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.util.json.GsonFactory;


/**
 * This message-driven bean handles all requests sent by the TIM video clients.
 * Requests are expected to be of {@link VideoRequest} type.
 *
 * @author Matthias Braeger, Emmanouil Koufakis
 */
@Service("videoRequestHandler")
public class VideoRequestHandler implements SessionAwareMessageListener<Message> {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(VideoRequestHandler.class);

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * The hashcode will allow us to uniquely link requests to responses in the 
   * log files. Seeing that one bean instance can only process one request at
   * a time but that several instances of the bean can process requests in 
   * parallel, this kind of link is necessary.
   */
  private int hashCode = hashCode();

  /** used to make property queries */
  private VideoConnectionMapper videoConnectionDAO;

  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 120000;

  /**
   * Default Constructor
   * @param pVideoConnectionDAO VideoConnectionPropertiesDAO used to make property queries
   */
  @Autowired
  public VideoRequestHandler(final VideoConnectionMapper pVideoConnectionDAO) {

    this.videoConnectionDAO = pVideoConnectionDAO;
  }

  /**
   * Inner method for handling video requests. Responses are sent back as JSON messages.
   * 
   * @param videoRequest The request. Can either be an AUTHORIZATION_DETAILS_REQUEST or a
   * VIDEO_CONNECTION_PROPERTIES_REQUEST.
   * @return The response that shall be transfered back to the video client. 
   * In case of an AUTHORIZATION_DETAILS_REQUEST the response is <code>RbacAuthorizationDetails</code>. 
   * In case of an VIDEO_CONNECTION_PROPERTIES_REQUEST the response is <code>VideoConnectionPropertiesCollection</code>. 
   * @throws SQLException In case an error occurs during the query
   * @throws ServerRequestException In case a null VideoRequest is received
   */
  public String handleVideoRequest(final VideoRequest videoRequest) throws SQLException, ServerRequestException {
    
    if (videoRequest == null) {      
      final String errorMessage = "videoRequest is null - cannot send reply";
      LOG.error("onMessage() -> handleVideoRequest()" + errorMessage);
      throw new ServerRequestException(errorMessage);
    }

    String messageText = null; // JSON reply

    if (videoRequest.getRequestType() == RequestType.AUTHORIZATION_DETAILS_REQUEST) {
      RbacAuthorizationDetails d = handleAuthorisationDetailsRequest(videoRequest);
      messageText = GSON.toJson(d);
    }
    else if (videoRequest.getRequestType() == RequestType.VIDEO_CONNECTION_PROPERTIES_REQUEST) {
      Collection<VideoConnectionProperties> p = handleVideoConnectionRequest(videoRequest);
      messageText = GSON.toJson(p); 
    }

    return messageText;
  }

  /**
   * Inner method for handling AUTHORIZATION_DETAILS_REQUEST.
   * 
   * @param videoRequest A AUTHORIZATION_DETAILS_REQUEST.
   * @return a VideoConnectionPropertiesCollection
   * @throws SQLException In case an error occurs during the query
   */
  private RbacAuthorizationDetails handleAuthorisationDetailsRequest(final VideoRequest videoRequest) throws SQLException {

    String videoName = videoRequest.getVideoSystemName();
    RbacAuthorizationDetails authDetails = videoConnectionDAO.selectAuthorizationDetails(videoName);

    return authDetails;
  }

  /**
   * Inner method for handling VIDEO_CONNECTION_PROPERTIES_REQUEST.
   * 
   * @param videoRequest A VIDEO_CONNECTION_PROPERTIES_REQUEST.
   * @return a collection of VideoConnectionProperties
   * @throws SQLException In case an error occurs during the query
   */
  private Collection<VideoConnectionProperties> handleVideoConnectionRequest(final VideoRequest videoRequest) throws SQLException {

    String videoName = videoRequest.getVideoSystemName();
    Collection<VideoConnectionProperties> properties = videoConnectionDAO.selectAllVideoConnectionProperties(videoName);

    return properties;
  }

  /**
   * This method is called when the Video client is sending a Video Request
   * to the server. The server retrieves the information 
   * and sends them back.
   * @param message the JMS message which contains the Json Video Request
   * @param session The JMS session
   * @throws JMSException Is thrown, e.g. if the reply destination topic is not set.
   * @see ClientRequest
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {

    if (LOG.isDebugEnabled()) {
      LOG.debug("Received video request.");
    }

    VideoRequest videoRequest;
    try {
      videoRequest = VideoRequestMessageConverter.fromMessage(message);
    } catch (MessageConversionException e) {
      ClientRequestErrorReport errorReport = new ClientRequestErrorReportImpl(false, e.getMessage());
      sendMessage(message, session, GSON.toJson(errorReport));
      return;      
    } 

    String result = null;
    try {
      result = handleVideoRequest(videoRequest);
    } 
    catch (Exception e) {
      final String errorMessage = "Runtime exception on the server! " + e.getMessage();
      LOG.error("onMessage() : handleVideoRequest():" + errorMessage + " :", e);
      ClientRequestErrorReport errorReport = new ClientRequestErrorReportImpl(false, errorMessage);
      sendMessage(message, session, GSON.toJson(errorReport));
      return;
    } 

    // No error occured => Sent the response

    // sent the error report first
    ClientRequestErrorReport errorReport = new ClientRequestErrorReportImpl(true, null);
    sendMessage(message, session, GSON.toJson(errorReport));    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Error report sent (no errors).");
    }

    // sent the result
    sendMessage(message, session, result);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Video request result sent. Succesfully processed request result.");
    }
  }

  /**
   * Private helper method.
   * Sends Messages.
   */
  private void sendMessage(final Message message, final Session session, final String messageText) throws JMSException {

    // Extract reply topic
    Destination replyDestination = null;
    try {
      replyDestination = message.getJMSReplyTo();
    } catch (JMSException jmse) {
      LOG.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
      throw jmse;
    }
    if (replyDestination != null) {
      MessageProducer messageProducer = session.createProducer(replyDestination);
      try {
        messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        messageProducer.setTimeToLive(DEFAULT_REPLY_TTL);
        TextMessage replyMessage = session.createTextMessage();      

        replyMessage.setText(messageText);
        if (LOG.isDebugEnabled()) {
          LOG.debug(new StringBuffer("onMessage() : Video connection response sent : ").append(hashCode));
        }
        messageProducer.send(replyMessage);
      } finally {
        messageProducer.close();
      }      
    } else {
      LOG.error("onMessage() : JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }    
  }
}
