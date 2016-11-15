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
package cern.c2mon.client.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.jms.*;

import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import cern.c2mon.shared.util.json.GsonFactory;

@Slf4j
@Service
public class FakeServerResponse implements SessionAwareMessageListener<Message> {

  @Override
  public void onMessage(final Message message, Session session) throws JMSException {
    try {
      Destination replyDestination = null;
      try {
        replyDestination = message.getJMSReplyTo();
      } catch (JMSException jmse) {
        log.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
        throw jmse;
      }

      ClientRequest clientRequest = fromMessage(message);
      Collection<? extends ClientRequestResult> response = new ArrayList<>();

      if (replyDestination != null) {

        MessageProducer messageProducer = session.createProducer(replyDestination);
        Message replyMessage = null;
        try {
          messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
          messageProducer.setTimeToLive(100L);


          if (clientRequest.requiresObjectResponse()) {

            // Send response as an Object message
            replyMessage = session.createObjectMessage((Serializable) response);

          } else {

            // Send response as Json message
            // use the Jackson parser for TransferTagValues
            switch (clientRequest.getResultType()){
              case TRANSFER_TAG_LIST:
              case TRANSFER_TAG_VALUE_LIST:
                replyMessage = session.createTextMessage(TransferTagSerializer.getJacksonParser().writeValueAsString(response));
                break;
              default:
                replyMessage = session.createTextMessage(GsonFactory.createGson().toJson(response));
            }
          }

          log.debug("onMessage() : Responded to ClientRequest.");
          messageProducer.send(replyMessage);
        } finally {
          messageProducer.close();
        }
      } else {
        log.error("onMessage() : JMSReplyTo destination is null - cannot send reply.");
        throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
      }
    } catch (Exception e) {
      log.error("Exception caught while processing client request - unable to process it; request will time out", e);
    }
  }


  /**
   * Converts the received JMS message to a <code>TransferTagRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>TransferTagRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  private static final ClientRequest fromMessage(final Message message) throws JMSException, MessageConversionException {

    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();
      try {
        return ClientRequestImpl.fromJson(json);

      }
      catch (JsonSyntaxException jse) {
        StringBuffer str = new StringBuffer("fromMessage() : Unsupported JSON message (");
        str.append(json);
        str.append(") : Message discarded.");
        log.error(str.toString());
        throw new MessageConversionException("Unsupported JSON message received on tag request connection.");
      }
    }

    else if (message instanceof ObjectMessage) {

      ObjectMessage oMessage = (ObjectMessage) message;

      Object object = oMessage.getObject();

      return ClientRequestImpl.fromObject(object);
    }

    else {
      StringBuffer str = new StringBuffer("fromMessage() : Unsupported message type(");
      str.append(message.getClass().getName());
      str.append(") : Message discarded.");
      log.error(str.toString());
      throw new MessageConversionException("Unsupported JMS message type received on tag request connection.");
    }
  }


}
