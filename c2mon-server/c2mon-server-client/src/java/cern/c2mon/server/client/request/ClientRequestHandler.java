package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.tim.server.cache.TagFacadeGateway;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.common.alarm.TagWithAlarms;
import cern.tim.server.supervision.SupervisionFacade;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Handles tag requests received on JMS from C2MON clients.
 * 
 * <p>The request is processed and a list of <code>TranferTag</code>
 * objects is returned as serialized JSON string
 * 
 * @author Matthias Braeger
 */
@Service("clientRequestHandler")
public class ClientRequestHandler implements SessionAwareMessageListener<Message> {

  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(ClientRequestHandler.class);
  
  /** Reference to the tag facade gateway to retrieve a tag copies with the associated alarms */
  private final TagFacadeGateway tagFacadeGateway;
  
  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;
  
  /** Reference to the supervision facade service for handling the supervision request */
  private final SupervisionFacade supervisionFacade;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 120000;
  
  /**
   * Default Constructor
   * @param pTagLocationService Reference to the tag location service singleton
   * @param pTagFacadeGateway Reference to the tag facade gateway singleton
   * @param pSupervisionFacade Reference to the supervision facade singelton
   */
  @Autowired
  public ClientRequestHandler(final TagLocationService pTagLocationService, 
                              final TagFacadeGateway pTagFacadeGateway,
                              final SupervisionFacade pSupervisionFacade) {
    tagLocationService = pTagLocationService;
    tagFacadeGateway = pTagFacadeGateway;
    supervisionFacade = pSupervisionFacade;
  }
  
  /**
   * This method is called when a C2MON client is sending a <code>ClientRequest</code>
   * to the server. The server retrieves the request Tag and associated alarms information
   * from the cache and sends them back through the reply topic
   * @param message the JMS message which contains the Json <code>ClientRequest</code>
   * @param session The JMS session
   * @throws JMSException Is thrown, e.g. if the reply destination topic is not set.
   * @see ClientRequest
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    ClientRequest clientRequest = ClientRequestMessageConverter.fromMessage(message);
    Collection< ? extends ClientRequestResult> response = handleClientRequest(clientRequest);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Successully processed client request.");
    }
 
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
      replyMessage.setText(GSON.toJson(response));
      if (LOG.isDebugEnabled()) {
        LOG.debug("onMessage() : Sending ClientRequest response with " + response.size() + " tags to client.");
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
   * @param clientRequest The request
   * @return The response that shall be transfered back to the C2MON client layer
   */
  private Collection< ? extends ClientRequestResult> handleClientRequest(@Valid final ClientRequest clientRequest) {
    switch (clientRequest.getRequestType()) {
      case TAG_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("Received a client request for " + clientRequest.getTagIds().size() + " tags.");
        }
        return handleTagRequest(clientRequest);
      case SUPERVISION_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("Received a client request for the current supervision status.");
        }
        return supervisionFacade.getAllSupervisionStates();        
      default:
        LOG.error("handleClientRequest() - Client request not supported: " + clientRequest.getRequestType());
        return Collections.emptyList();
    } // end switch
  }
  
  /**
   * Inner method which handles the tag requests
   * @param tagRequest The tag request sent from the flient
   * @return Collection of 
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends ClientRequestResult> handleTagRequest(final ClientRequest tagRequest) {
    final Iterator<Long> iter = tagRequest.getTagIds().iterator();
    final Collection transferTags = new ArrayList(tagRequest.getTagIds().size());
    
    while (iter.hasNext()) {
      final Long tagId = iter.next();
      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);
        
        switch (tagRequest.getResultType()) {
          case TRANSFER_TAG_LIST:
            transferTags.add(TransferObjectFactory.createTransferTag(tagWithAlarms));
            break;
          case TRANSFER_TAG_VALUE_LIST:
            transferTags.add(TransferObjectFactory.createTransferTagValue(tagWithAlarms));
            break;
          default:
            LOG.error("handleTagRequest() - Could not generate response message. Unknown enum ResultType "
                + tagRequest.getResultType());
        }
      }
    } // end while
    
    return transferTags;
  }
}
