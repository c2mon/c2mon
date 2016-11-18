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
package cern.c2mon.server.client.request;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.validation.Valid;

import cern.c2mon.shared.client.serializer.TransferTagSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import cern.c2mon.server.supervision.SupervisionFacade;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.util.json.GsonFactory;

/**
 * Handles tag requests received on JMS from C2MON clients.
 *
 * <p>
 * The request is processed and a list of <code>TranferTag</code> objects is
 * returned as serialized JSON string
 *
 * <p>
 * Handles requests on both client request and admin queues.
 *
 * @author Matthias Braeger
 */
@Slf4j
@Service("clientRequestDelegator")
public class ClientRequestDelegator implements SessionAwareMessageListener<Message> {

  private final ClientAlarmRequestHandler clientAlarmRequestHandler;

  private final ClientAlarmRequestHandlerNew clientAlarmRequestHandlerNew;

  private final ClientCommandRequestHandler clientCommandRequestHandler;

  private final ClientTagRequestHelper tagrequestHelper;

  private final ClientDeviceRequestHelper clientDeviceRequestHelper;

  private final ClientConfigurationRequestHandler clientConfigurationRequestHandler;

  private final ClientProcessRequestHandler clientProcessRequestHandler;

  /**
   * Reference to the supervision facade service for handling the supervision
   * request
   */
  private final SupervisionFacade supervisionFacade;

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 5400000;

  /**
   * Default Constructor
   */
  @Autowired
  public ClientRequestDelegator(final SupervisionFacade supervisionFacade,
                                final ClientAlarmRequestHandler clientAlarmRequestHandler,
                                final ClientAlarmRequestHandlerNew clientAlarmRequestHandlerNew,
                                final ClientCommandRequestHandler clientCommandRequestHandler,
                                final ClientTagRequestHelper tagrequestHelper,
                                final ClientDeviceRequestHelper clientDeviceRequestHelper,
                                final ClientConfigurationRequestHandler clientConfigurationRequestHandler,
                                final ClientProcessRequestHandler clientProcessRequestHandler) {

    this.supervisionFacade = supervisionFacade;
    this.clientAlarmRequestHandler = clientAlarmRequestHandler;
    this.clientAlarmRequestHandlerNew = clientAlarmRequestHandlerNew;
    this.clientCommandRequestHandler = clientCommandRequestHandler;
    this.tagrequestHelper = tagrequestHelper;
    this.clientDeviceRequestHelper = clientDeviceRequestHelper;
    this.clientConfigurationRequestHandler = clientConfigurationRequestHandler;
    this.clientProcessRequestHandler = clientProcessRequestHandler;
  }

  /**
   * This method is called when a C2MON client is sending a
   * <code>ClientRequest</code> to the server. The server retrieves the request
   * Tag and associated alarms information from the cache and sends them back
   * through the reply topic
   *
   * @param message the JMS message which contains the Json
   *          <code>ClientRequest</code>
   * @param session The JMS session
   * @throws JMSException Is thrown, e.g. if the reply destination topic is not
   *           set.
   * @see ClientRequest
   */
  @Override
  public void onMessage(final Message message, final Session session) throws JMSException {

    if (log.isDebugEnabled()) {
      log.debug("onMessage() : Client request received.");
    }
    try {
      Destination replyDestination = null;
      try {
        replyDestination = message.getJMSReplyTo();
      } catch (JMSException jmse) {
        log.error("onMessage() : Cannot extract ReplyTo from message.", jmse);
        throw jmse;
      }

      ClientRequest clientRequest = ClientRequestMessageConverter.fromMessage(message);
      Collection<? extends ClientRequestResult> response = handleClientRequest(clientRequest, session, replyDestination);

      if (replyDestination != null) {

        MessageProducer messageProducer = session.createProducer(replyDestination);
        try {
          messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
          messageProducer.setTimeToLive(DEFAULT_REPLY_TTL);

          Message replyMessage = null;

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
                replyMessage = session.createTextMessage(GSON.toJson(response));
            }
          }

          if (log.isDebugEnabled()) {
            log.debug("onMessage() : Responded to ClientRequest.");
          }
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
   * Inner method for handling requests. Therefore it has to get for all tag ids
   * mentioned in that request the tag and alarm referenses.
   *
   * @param clientRequest The request
   * @param session Used by the ReportHandler to send reports
   * @param replyDestination Used by the ReportHandler to send reports
   * @return The response that shall be transfered back to the C2MON client
   *         layer
   */
  private Collection<? extends ClientRequestResult> handleClientRequest(@Valid final ClientRequest clientRequest,
                                                                        final Session session,
                                                                        final Destination replyDestination) {

    switch (clientRequest.getRequestType()) {

    case TAG_CONFIGURATION_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug(String.format("handleClientRequest() - Received a TAG_CONFIGURATION_REQUEST for %d tags (with configuration details).", clientRequest.getIds().size()));
      }
      return tagrequestHelper.handleTagConfigurationRequest(clientRequest);

    case APPLY_CONFIGURATION_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received an APPLY_CONFIGURATION_REQUEST with " + clientRequest.getIds().size() + " configurations.");
      }
      return clientConfigurationRequestHandler.handleApplyConfigurationRequest(clientRequest, session, replyDestination);
    case RETRIEVE_CONFIGURATION_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a RETRIEVE_CONFIGURATION_REQUEST.");
      }
      return clientConfigurationRequestHandler.handleRetrieveConfigurationsRequest(clientRequest, session, replyDestination);
    case TAG_REQUEST:
      if (log.isDebugEnabled()) {
        if (clientRequest.getIds().isEmpty()) {
          log.debug(String.format("handleClientRequest() - Received a TAG_REQUEST with %d wildcard(s) for tag name search: %s", clientRequest.getRegexList().size(), clientRequest.getRegexList()));
        }
        else {
          log.debug("handleClientRequest() - Received a TAG_REQUEST for " + clientRequest.getIds().size() + " tags.");
        }
      }
      return tagrequestHelper.handleTagRequest(clientRequest);
    case ALARM_REQUEST:
      if (log.isDebugEnabled()) {
        // ! TagId field is also used for Alarm ids
        log.debug("handleClientRequest() - Received an ALARM_REQUEST for " + clientRequest.getIds().size() + " alarms.");
      }
      return clientAlarmRequestHandler.handleAlarmRequest(clientRequest);
    case ACTIVE_ALARMS_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received an ACTIVE_ALARMS_REQUEST.");
      }
      return clientAlarmRequestHandler.handleActiveAlarmRequest(clientRequest);
    case ALARM_REQUEST_NEW:
      if (log.isDebugEnabled()) {
        // ! TagId field is also used for Alarm ids
        log.debug("handleClientRequest() - Received an ALARM_REQUEST_NEW for " + clientRequest.getIds().size() + " alarms.");
      }
      return clientAlarmRequestHandlerNew.handleAlarmRequest(clientRequest);
    case ACTIVE_ALARMS_REQUEST_NEW:
      log.debug("handleClientRequest() - Received an ACTIVE_ALARMS_REQUEST_NEW.");
      return clientAlarmRequestHandlerNew.handleActiveAlarmRequest();
    case SUPERVISION_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a SUPERVISION_REQUEST.");
      }
      return supervisionFacade.getAllSupervisionStates();
    case COMMAND_HANDLE_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a COMMAND_HANDLE_REQUEST for " + clientRequest.getIds().size() + " commands.");
      }
      return clientCommandRequestHandler.handleCommandHandleRequest(clientRequest);
    case EXECUTE_COMMAND_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received an EXECUTE_COMMAND_REQUEST.");
      }
      return clientCommandRequestHandler.handleExecuteCommandRequest(clientRequest);
    case DAQ_XML_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a DAQ_XML_REQUEST");
      }
      return clientProcessRequestHandler.handleDaqXmlRequest(clientRequest);
    case PROCESS_NAMES_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a PROCESS_NAMES_REQUEST");
      }
      return clientProcessRequestHandler.handleProcessNamesRequest(clientRequest);
    case DEVICE_CLASS_NAMES_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a DEVICE_CLASS_NAMES_REQUEST");
      }
      return clientDeviceRequestHelper.handleDeviceClassNamesRequest(clientRequest);
    case DEVICE_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a DEVICE_REQUEST");
      }
      return clientDeviceRequestHelper.handleDeviceRequest(clientRequest);
    case TAG_STATISTICS_REQUEST:
      if (log.isDebugEnabled()) {
        log.debug("handleClientRequest() - Received a TAG_STATISTICS_REQUEST");
      }
      return tagrequestHelper.handleTagStatisticsRequest(clientRequest);
    default:
      log.error("handleClientRequest() - Client request not supported: " + clientRequest.getRequestType());
      return Collections.emptyList();
    } // end switch
  }
}
