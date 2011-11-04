package cern.c2mon.server.video;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;

import com.google.gson.JsonSyntaxException;

import cern.c2mon.client.common.video.VideoConnectionsRequest;
import cern.c2mon.shared.client.request.ClientRequest;


public abstract class VideoRequestMessageConverter {
  
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(VideoRequestMessageConverter.class);
  
  /**
   * Hidden default constructor
   */
  private VideoRequestMessageConverter() {
    // Do nothing
  }

  /**
   * Converts the received JMS message to a <code>VideoConnectionsRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>VideoConnectionsRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  public static final VideoConnectionsRequest fromMessage(final Message message) throws JMSException, MessageConversionException {
    
    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();
      
      try {
        
        return VideoConnectionsRequest.fromJsonResponse(json);
        
      }
      catch (JsonSyntaxException jse) {
        StringBuffer str = new StringBuffer("fromMessage() : Unsupported JSON message (");
        str.append(json);
        str.append(") : Message discarded.");
        LOG.error(str); 
        throw new MessageConversionException("Unsupported JSON message received on tag request connection.");
      }   
    } 
    else {
      StringBuffer str = new StringBuffer("fromMessage() : Unsupported message type(");
      str.append(message.getClass().getName());
      str.append(") : Message discarded.");
      LOG.error(str); 
      throw new MessageConversionException("Unsupported JMS message type received on tag request connection.");
    }
  }
}
