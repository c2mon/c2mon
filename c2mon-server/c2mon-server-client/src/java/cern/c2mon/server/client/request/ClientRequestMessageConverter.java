package cern.c2mon.server.client.request;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;

import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;

import com.google.gson.JsonSyntaxException;

/**
 * This abstract class provides a static method for converting a JMS tag request 
 * message into a <code>TransferTagRequest</code> object.
 *
 * @author Matthias Braeger
 * @see ClientRequest
 */
abstract class ClientRequestMessageConverter {
  
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(ClientRequestMessageConverter.class);
  
  /**
   * Hidden default constructor
   */
  private ClientRequestMessageConverter() {
    // Do nothing
  }
  
  /**
   * Converts the received JMS message to a <code>TransferTagRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>TransferTagRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  public static final ClientRequest fromMessage(final Message message) throws JMSException, MessageConversionException {
    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();
      try {
        return ClientRequestImpl.fromJson(json);
      }
      catch (JsonSyntaxException jse) {
        StringBuffer str = new StringBuffer("fromMessage() : Unsupported JSON message (");
        str.append(json);
        str.append(") : Message discarded.");
        LOG.error(str); 
        throw new MessageConversionException("Unsupported JSON message received on tag request connection.");
      }   
    } else {
      StringBuffer str = new StringBuffer("fromMessage() : Unsupported message type(");
      str.append(message.getClass().getName());
      str.append(") : Message discarded.");
      LOG.error(str); 
      throw new MessageConversionException("Unsupported JMS message type received on tag request connection.");
    }
  }
}
