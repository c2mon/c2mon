package cern.c2mon.server.video;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.support.converter.MessageConversionException;

import cern.c2mon.shared.video.VideoRequest;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This abstract class provides a static method for converting a VideoRequest.
 * 
 * @author ekoufaki
 */
public abstract class VideoRequestMessageConverter {
  
  /** Class logger */
  private static final Logger LOG = Logger.getLogger(VideoRequestMessageConverter.class);
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();
  
  /**
   * Hidden default constructor
   */
  private VideoRequestMessageConverter() {
    // Do nothing
  }

  /**
   * Converts the received JMS message to a <code>VideoRequest</code> object.
   * @param message The received JMS message
   * @return The deserialized <code>VideoRequest</code>
   * @throws JMSException In case of problems when getting the text from the JMS text message
   * @throws MessageConversionException In case of problems while deserializing the JMS message
   */
  public static final VideoRequest fromMessage(final Message message) throws JMSException, MessageConversionException {
    
    if (message instanceof TextMessage) {
      String json = ((TextMessage) message).getText();
      
      try {
        return GSON.fromJson(json, VideoRequest.class);
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
