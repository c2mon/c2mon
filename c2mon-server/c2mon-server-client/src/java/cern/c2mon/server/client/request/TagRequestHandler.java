package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.shared.client.TransferTagRequest;
import cern.c2mon.shared.client.TransferTagValue;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.server.cache.TagFacadeGateway;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.common.alarm.TagWithAlarms;

/**
 * Handles tag requests received on JMS from C2MON clients.
 * 
 * <p>The request is processed and a list of <code>TranferTag</code>
 * objects is returned as serialized JSON string
 * 
 * @author Matthias Braeger
 */
@Service("tagRequestHandler")
public class TagRequestHandler implements SessionAwareMessageListener<Message> {

  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(TagRequestHandler.class);
  
  /** Reference to the tag facade gateway to retrieve a tag copies with the associated alarms */
  private final TagFacadeGateway tagFacadeGateway;
  
  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;
  
  /**
   * Default Constructor
   * @param pTagLocationService Reference to the tag location service singleton
   * @param pTagFacadeGateway Reference to the tag facade gateway singleton
   */
  @Autowired
  public TagRequestHandler(final TagLocationService pTagLocationService, final TagFacadeGateway pTagFacadeGateway) {
    tagLocationService = pTagLocationService;
    tagFacadeGateway = pTagFacadeGateway;
  }
  
  /**
   * This method is called when a C2MON client is sending a <code>TransferTagRequest</code>
   * to the server. The server retrieves the request Tag and associated alarms information
   * from the cache and sends them back through the reply topic
   * @param message the JMS message which contains the Json <code>TransferTagRequest</code>
   * @param session The JMS session
   * @throws JMSException Is thrown, e.g. if the reply destination topic is not set.
   * @see TransferTagRequest
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    TransferTagRequest tagRequest = TagRequestMessageConverter.fromMessage(message);
    Collection< ? extends TransferTagValue> response = handleTransferTagRequest(tagRequest);
 
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
      TextMessage replyMessage = session.createTextMessage();
      
      //TODO: Please call here for each result type is associated Gson instance!
      switch (tagRequest.getResultType()) {
        case TransferTag:
          replyMessage.setText(TransferTagImpl.getGson().toJson(response));
          break;
        case TransferTagValue:
        default:
          replyMessage.setText(TransferTagValueImpl.getGson().toJson(response));
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("onMessage() : Sending TransferTagRequest response with " + response.size() + " tags to client.");
      }
      messageProducer.send(replyMessage);
    } else {
      LOG.error("onMessage() : JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }
  }
  
  
  /**
   * Inner method for handling the tag request. Therefore it has to get for all tag ids
   * mentioned in that request the tag and alarm referenses. 
   * @param tagRequest The request
   * @return The response that shall be transfered back to the C2MON client layer
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends TransferTagValue> handleTransferTagRequest(final TransferTagRequest tagRequest) {
    final List transferTags = new ArrayList(tagRequest.getTagIds().size());
    final Iterator<Long> iter = tagRequest.getTagIds().iterator();
    
    while (iter.hasNext()) {
      final Long tagId = iter.next();
      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);
        
        switch (tagRequest.getResultType()) {
          case TransferTag:
            transferTags.add(TransferObjectFactory.createTransferTag(tagWithAlarms));
            break;
          case TransferTagValue:
            transferTags.add(TransferObjectFactory.createTransferTagValue(tagWithAlarms));
            break;
          default:
            throw new MessageConversionException("Could not generate TransferTagRequestResponse message. Unknown enum ResultType!");
        }
      }
    } // end while
    
    return transferTags;
  }
}
