package cern.c2mon.server.client.request;

import java.io.Serializable;
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
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.tim.server.cache.AlarmCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessXMLProvider;
import cern.tim.server.cache.TagFacadeGateway;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.exception.CacheElementNotFoundException;
import cern.tim.server.command.CommandExecutionManager;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.TagWithAlarms;
import cern.tim.server.common.process.Process;
import cern.tim.server.supervision.SupervisionFacade;
import cern.tim.shared.client.command.CommandExecuteRequest;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Handles tag requests received on JMS from C2MON clients.
 * 
 * <p>
 * The request is processed and a list of <code>TranferTag</code> objects is
 * returned as serialized JSON string
 * 
 * @author Matthias Braeger
 */
@Service("clientRequestHandler")
public class ClientRequestHandler implements SessionAwareMessageListener<Message> {

  /** Private class logger */
  private static final Logger LOG = Logger.getLogger(ClientRequestHandler.class);

  /**
   * Reference to the tag facade gateway to retrieve a tag copies with the
   * associated alarms
   */
  private final TagFacadeGateway tagFacadeGateway;

  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;

  /** Reference to the AlarmCache */
  private final AlarmCache alarmCache;

  /** Reference to the ConfigurationLoader */
  private final ConfigurationLoader configurationLoader;

  /** Reference to the CommandExecutionManager */
  private final CommandExecutionManager commandExecutionManager;
  
  /** Reference to the Process cache that provides a list of all the process names */
  private final ProcessCache processCache;

  /**
   * Reference to the supervision facade service for handling the supervision
   * request
   */
  private final SupervisionFacade supervisionFacade;

  /** Ref to the the bean providing DAQ XML */
  private final ProcessXMLProvider processXMLProvider;

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 120000;

  /**
   * Default Constructor
   * 
   * @param pTagLocationService
   *          Reference to the tag location service singleton
   * @param pTagFacadeGateway
   *          Reference to the tag facade gateway singleton
   * @param pSupervisionFacade
   *          Reference to the supervision facade singleton
   * @param pProcessXMLProvider
   *          Ref to the process XML provider bean
   * @param pAlarmCache
   *          Reference to the AlarmCache
   * @param pConfigurationLoader
   *          Reference to the ConfigurationLoader
   * @param pCommandExecutionManager
   *          Reference to the CommandExecutionManager
   */
  @Autowired
  public ClientRequestHandler(final TagLocationService pTagLocationService, final TagFacadeGateway pTagFacadeGateway,
      final SupervisionFacade pSupervisionFacade, final ProcessXMLProvider pProcessXMLProvider, final AlarmCache pAlarmCache,
      final ConfigurationLoader pConfigurationLoader, final CommandExecutionManager pCommandExecutionManager,
      final ProcessCache pProcessCache) {
    tagLocationService = pTagLocationService;
    tagFacadeGateway = pTagFacadeGateway;
    supervisionFacade = pSupervisionFacade;
    processXMLProvider = pProcessXMLProvider;
    alarmCache = pAlarmCache;
    configurationLoader = pConfigurationLoader;
    commandExecutionManager = pCommandExecutionManager;
    processCache = pProcessCache;
  }

  /**
   * This method is called when a C2MON client is sending a
   * <code>ClientRequest</code> to the server. The server retrieves the request
   * Tag and associated alarms information from the cache and sends them back
   * through the reply topic
   * 
   * @param message
   *          the JMS message which contains the Json <code>ClientRequest</code>
   * @param session
   *          The JMS session
   * @throws JMSException
   *           Is thrown, e.g. if the reply destination topic is not set.
   * @see ClientRequest
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {
    ClientRequest clientRequest = ClientRequestMessageConverter.fromMessage(message);
    Collection< ? extends ClientRequestResult> response = handleClientRequest(clientRequest);
    if (LOG.isDebugEnabled()) {
      LOG.debug("onMessage() : Client request received.");
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

      Message replyMessage = null;

      if (clientRequest.requiresObjectResponse()) {

        // Send response as an Object message
        replyMessage = session.createObjectMessage((Serializable) response);

      } else {

        // Send response as Json message
        replyMessage = session.createTextMessage(GSON.toJson(response));
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("onMessage() : Responded to ClientRequest: "+ clientRequest.getRequestType() + ". " + response.size() 
            + " tags included in the response.");
      }
      messageProducer.send(replyMessage);
    } else {
      LOG.error("onMessage() : JMSReplyTo destination is null - cannot send reply.");
      throw new MessageConversionException("JMS reply queue could not be extracted (returned null).");
    }
  }

  /**
   * Inner method for handling requests. Therefore it has to get for all tag ids
   * mentioned in that request the tag and alarm referenses.
   * 
   * @param clientRequest
   *          The request
   * @return The response that shall be transfered back to the C2MON client
   *         layer
   */
  private Collection< ? extends ClientRequestResult> handleClientRequest(@Valid final ClientRequest clientRequest) {
    switch (clientRequest.getRequestType()) {

      case TAG_CONFIGURATION_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a TAG_CONFIGURATION_REQUEST for " + clientRequest.getTagIds().size() + " tag configurations.");
        }
        return handleTagConfigurationRequest(clientRequest);

      case APPLY_CONFIGURATION_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received an APPLY_CONFIGURATION_REQUEST for " + clientRequest.getTagIds().size() + " tag Ids.");
        }
        return handleConfigurationReportRequest(clientRequest);
      case TAG_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a TAG_REQUEST for " + clientRequest.getTagIds().size() + " tags.");
        }
        return handleTagRequest(clientRequest);
      case ALARM_REQUEST:
        if (LOG.isDebugEnabled()) {
          // ! TagId field is also used for Alarm ids
          LOG.debug("handleClientRequest() - Received an ALARM_REQUEST for " + clientRequest.getTagIds().size() + " alarms.");
        }
        return handleAlarmRequest(clientRequest);
      case SUPERVISION_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a SUPERVISION_REQUEST.");
        }
        return supervisionFacade.getAllSupervisionStates();
      case COMMAND_HANDLE_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a COMMAND_HANDLE_REQUEST " + clientRequest.getTagIds().size() + " command handles.");
        }
        return handleCommandHandleRequest(clientRequest);
      case EXECUTE_COMMAND_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received an EXECUTE_COMMAND_REQUEST for " + clientRequest.getTagIds().size() + " command handles.");
        }
        return handleExecuteCommandRequest(clientRequest);
      case DAQ_XML_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a DAQ_XML_REQUEST");
        }
        return handleDaqXmlRequest(clientRequest);
      case PROCESS_NAMES_REQUEST:
        if (LOG.isDebugEnabled()) {
          LOG.debug("handleClientRequest() - Received a PROCESS_NAMES_REQUEST");
        }
        return handleProcessNamesRequest(clientRequest);
      default:
        LOG.error("handleClientRequest() - Client request not supported: " + clientRequest.getRequestType());
        return Collections.emptyList();
    } // end switch
  }
  
  /**
   * Inner method which handles the process names request
   * 
   * @param clientRequest A process name sent from the client
   * @return a Collection of all available process names
   */ 
  private Collection<? extends ClientRequestResult> handleProcessNamesRequest(ClientRequest clientRequest) {

    Collection<ProcessNameResponse> names = new ArrayList<ProcessNameResponse>();
    
    Iterator<Long> iterator = processCache.getKeys().iterator();
    
    while (iterator.hasNext()) {
      
      cern.tim.server.common.process.Process o = processCache.get((Long) iterator.next());
      names.add(new ProcessNameResponseImpl(o.getName()));
    }
    return names;    
  }

  /**
   * Inner method which handles the Daq Xml Requests
   * 
   * @param daqXmlRequest The daq Xml Request sent from the client
   * @return a ProcessXmlResponse
   */ 
  private Collection< ? extends ClientRequestResult> handleDaqXmlRequest(final ClientRequest daqXmlRequest) {

    Collection<ProcessXmlResponse> singleXML = new ArrayList<ProcessXmlResponse>(1);
    ProcessXmlResponseImpl processXmlResponse = new ProcessXmlResponseImpl();
    try {
      String xmlString = processXMLProvider.getProcessConfigXML((String) daqXmlRequest.getRequestParameter());
      processXmlResponse.setProcessXML(xmlString);
    } catch (CacheElementNotFoundException cacheEx) {
      String errorMessage = "Requested process not found.";
      LOG.warn(errorMessage, cacheEx);
      processXmlResponse.setErrorMessage(errorMessage);
    }
    singleXML.add(processXmlResponse);
    return singleXML;
  }

  /**
   * Helper method. Casts to an int. 
   * Logs a warning if the cast is unsafe.
   * @param l long
   * @return int
   */
  private int castLongToInt(final long l) {

    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
      LOG.warn("castLongToInt() - unsafe cast of long " + l + " to int");
    }
    return (int) l;
  }
  
  /**
   * Inner method which handles the CommandTagHandle Requests
   * 
   * @param commandRequest
   *          The command request sent from the client
   * @return a Collection of CommandTagHandles
   */ 
  private Collection< ? extends ClientRequestResult> handleCommandHandleRequest(final ClientRequest commandRequest) {

    switch (commandRequest.getResultType()) {
      case TRANSFER_COMMAND_HANDLES_LIST:
        return commandExecutionManager.processRequest(commandRequest.getTagIds());
      default:
        LOG.error("handleCommandHandleRequest() - Could not generate response message. Unknown enum ResultType " + commandRequest.getResultType());
    }
    return null;
  }

  /**
   * Inner method which handles the Configuration Requests
   * 
   * @param configurationRequest
   *          The configuration request sent from the client
   * @return Configuration Report
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends ClientRequestResult> handleConfigurationReportRequest(final ClientRequest configurationRequest) {

    // !!! TagId field is also used for Configuration Ids
    final Iterator<Long> iter = configurationRequest.getTagIds().iterator();
    final Collection reports = new ArrayList(configurationRequest.getTagIds().size());

    while (iter.hasNext()) {

      final int configId = castLongToInt(iter.next());

      switch (configurationRequest.getResultType()) {
        case TRANSFER_CONFIGURATION_REPORT:
          reports.add(configurationLoader.applyConfiguration(configId));
          break;
        default:
          LOG.error("handleConfigurationRequest() - Could not generate response message. Unknown enum ResultType " + configurationRequest.getResultType());
      }
    } // end while

    return reports;
  }

  /**
   * Inner method which handles the Execute Command Request
   * 
   * @param executeCommandRequest
   *          The command request send from the client
   * @return A command report
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends ClientRequestResult> handleExecuteCommandRequest(final ClientRequest executeCommandRequest) {

    final Collection<CommandReport> commandReports = new ArrayList<CommandReport>(1);
    commandReports.add(commandExecutionManager.execute((CommandExecuteRequest) executeCommandRequest.getObjectParameter()));
    return commandReports;
  }

  /**
   * Inner method which handles the Tag Configuration Requests
   * 
   * @param tagConfigurationRequest
   *          The configuration request sent from the client
   * @return A tag configuration list
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends ClientRequestResult> handleTagConfigurationRequest(final ClientRequest tagConfigurationRequest) {

    // !!! TagId field is also used for Configuration Ids
    final Iterator<Long> iter = tagConfigurationRequest.getTagIds().iterator();
    final Collection transferTags = new ArrayList(tagConfigurationRequest.getTagIds().size());

    while (iter.hasNext()) {

      final Long tagId = iter.next();
      if (tagLocationService.isInTagCache(tagId)) {
        final TagWithAlarms tagWithAlarms = tagFacadeGateway.getTagWithAlarms(tagId);
        switch (tagConfigurationRequest.getResultType()) {
          case TRANSFER_TAG_CONFIGURATION_LIST:
            transferTags.add(TransferObjectFactory.createTagConfiguration(tagWithAlarms));
            break;
          default:
            LOG.error("handleConfigurationRequest() - Could not generate response message. Unknown enum ResultType " + tagConfigurationRequest.getResultType());
        }
      } else {
        LOG.warn("Received client request (TagConfigRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while

    return transferTags;
  }

  /**
   * Inner method which handles the alarm requests
   * 
   * @param alarmRequest
   *          The alarm request sent from the client
   * @return Collection of alarms
   */
  @SuppressWarnings("unchecked")
  private Collection< ? extends ClientRequestResult> handleAlarmRequest(final ClientRequest alarmRequest) {

    // !!! TagId field is also used for Alarm Ids
    final Iterator<Long> iter = alarmRequest.getTagIds().iterator();
    final Collection alarms = new ArrayList(alarmRequest.getTagIds().size());

    while (iter.hasNext()) {

      final Long alarmId = iter.next();
      if (alarmCache.hasKey(alarmId)) {
        final Alarm alarm = alarmCache.get(alarmId);
        switch (alarmRequest.getResultType()) {
          case TRANSFER_ALARM_LIST:
            alarms.add(TransferObjectFactory.createAlarmValue(alarm));
            break;
          default:
            LOG.error("handleAlarmRequest() - Could not generate response message. Unknown enum ResultType " + alarmRequest.getResultType());
        }
      } else {
        LOG.warn("handleAlarmRequest() - request for unknown alarm with id " + alarmId);
      }
      
    } // end while

    return alarms;
  }

  /**
   * Inner method which handles the tag requests
   * 
   * @param tagRequest
   *          The tag request sent from the client
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
            LOG.error("handleTagRequest() - Could not generate response message. Unknown enum ResultType " + tagRequest.getResultType());
        }
      } else {
        LOG.warn("Received client request (TagRequest) for unrecognized Tag with id " + tagId);
      }
    } // end while

    return transferTags;
  }
}
