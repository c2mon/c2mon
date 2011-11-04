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
import java.util.Iterator;
import java.util.List;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.video.VideoConnectionPropertiesCollection;
import cern.c2mon.client.common.video.VideoConnectionsRequest;


/**
 * This message-driven bean handles all requests sent by the TIM video clients.
 * Requests are expected to be of VideoConnectionsRequest type.
 *
 * @author Matthias Braeger
 */
@Service("videoRequestHandler")
public class VideoRequestHandler implements SessionAwareMessageListener<Message> {
  
  /**
   * The generated serial version UID
   */
  private static final long serialVersionUID = 1686542119946353745L;

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(VideoRequestHandler.class);
  
  /**
   * The hashcode will allow us to uniquely link requests to responses in the 
   * log files. Seeing that one bean instance can only process one request at
   * a time but that several instances of the bean can process requests in 
   * parallel, this kind of link is necessary.
   */
  private int hashCode = hashCode();
   
  /** JMS factory name for the reply connection */
  private String jmsFactoryName;

  /** JMS user name for the reply connection */
  private String jmsUser;

  /** JMS password for the reply connection */
  private String jmsPassword;

  /** JMS ConnectionFactory for publishing replies to the drivers' requests */
  private TopicConnectionFactory factory;

  /** JMS Connection for publishing replies to the drivers' requests */
  private TopicConnection connection;

  /** TopicPublisher for publishing replies to the drivers' requests */
  private TopicPublisher publisher;

  /** JMS Session for publishing replies to the drivers' requests */
  private TopicSession session;

  /** Flag indicating whether the "reply" connection has been established */
  private boolean connected;
  
  /** A mutex flag */
  private Boolean connection_mutex = Boolean.FALSE;
  
  /** used to make property queries */
  private VideoConnectionPropertiesDAO videoConnectionDAO;
  
  // --- //
  
  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 120000;
  
  // -- //
  
  /**
   * Default Constructor
   * @param pVideoConnectionDAO VideoConnectionPropertiesDAO used to make property queries
   */
  @Autowired
  public VideoRequestHandler(final VideoConnectionPropertiesDAO pVideoConnectionDAO) {
    
    this.videoConnectionDAO = pVideoConnectionDAO;
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
  // TODO @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    
    VideoConnectionsRequest videoConnectionRequest = VideoRequestMessageConverter.fromMessage(message);
   
    if (LOG.isDebugEnabled()) {
      LOG.debug("Successully processed client request.");
    }
    
    // -- // 
    
    if (videoConnectionRequest == null)
      return;
    
    VideoConnectionPropertiesCollection propertiesList = new VideoConnectionPropertiesCollection();
    
    try {
      List roleNamesList = videoConnectionDAO.selectAllPermitedRoleNames(videoConnectionRequest.getVideoSystemName());
      
      Iterator iter = roleNamesList.iterator();
      boolean videoConnsAdded = false;
      while (iter.hasNext() && !videoConnsAdded) {
        String roleName = (String) iter.next();
        if (videoConnectionRequest.hasPrivilege(roleName)) {
          Collection connList = 
            videoConnectionDAO.selectAllVideoConnectionProperties(videoConnectionRequest.getVideoSystemName());
          propertiesList.addAll(connList);
          videoConnsAdded = true;
        }
      }
      
    } catch (SQLException sqle) {
      LOG.error("onMessage() : Unable to get connection to data base: ", sqle);
    }
    
    // -- //

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
      messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      messageProducer.setTimeToLive(DEFAULT_REPLY_TTL);
      TextMessage replyMessage = session.createTextMessage();      

      // Send response as  Json message
      replyMessage.setText(propertiesList.toJson());
      if (LOG.isDebugEnabled()) {
        LOG.debug(new StringBuffer("onMessage() : Video connection response sent : ").append(hashCode));
      }
      messageProducer.send(replyMessage);
    } else {
      LOG.error("onMessage() : JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }
  }
}
