/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.messaging.impl;

import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.config.ConfigurationDOMFactory;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

/**
 * Implementation of the ProcessMessageReceiver interface for ActiveMQ JMS
 * middleware (in fact, implementation is generic - except in init() - and could
 * be used with other providers if the connection and session management can be
 * done in the Spring XML).
 * 
 * In the XML, the message listener destination is set to a temporary value,
 * which is then overridden at runtime in the init() method below to the correct
 * value. Notice this causes a circular reference, which can be resolved ().
 * 
 * @author mbrightw
 * 
 */
public class ActiveMessageReceiver extends ProcessMessageReceiver implements SessionAwareMessageListener {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActiveMessageReceiver.class);

    /**
     * Reference to the unique XML parser (singleton).
     */
    private SimpleXMLParser simpleXMLparser;

    /**
     * Wire field to resolve circular reference.
     */
    private DefaultMessageListenerContainer listenerContainer;

    /**
     * The configuration controller which allows the ActiveMessageReceiver
     * the configuration to know on which topic he has to listen.
     */
    private ConfigurationController configurationController;

    /**
     * The DOM Factory to create the DOM tree and String representations
     * of reconfiguration objects.
     */
    private ConfigurationDOMFactory domFactory = new ConfigurationDOMFactory();

    /**
     * Unique constructor (uses Qualifier annotation for wiring the listener
     * container.
     * 
     * @param simpleXMLparser
     *            the XML parser bean
     * @param configurationController
     *            the DAQ configuration
     * @param serverRequestJmsContainer
     *            the message listener
     */
    @Autowired
    public ActiveMessageReceiver(final SimpleXMLParser simpleXMLparser, final ConfigurationController configurationController,
            @Qualifier("serverRequestListenerContainer") final DefaultMessageListenerContainer serverRequestJmsContainer) {
        super();
        this.simpleXMLparser = simpleXMLparser;
        this.configurationController = configurationController;
        this.listenerContainer = serverRequestJmsContainer;
    }

    /**
     * Initialization method that must be called after bean creation (done
     * automatically in Spring). Sets the destination and registers as listener
     * in the Spring listener container.
     */
    @PostConstruct
    public void init() {
        ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("init - Setting ActiveMessageReceiver listener destination to " + processConfiguration.getJmsDaqCommandQueue());
        }
        listenerContainer.setMessageListener(this);
        listenerContainer.setDestination(new ActiveMQQueue(processConfiguration.getJmsDaqCommandQueue()));
        listenerContainer.initialize();
        listenerContainer.start();
    }

    /**
     * Connect method not needed in this case. Reliable connections are managed
     * by ActiveMQ Spring library.
     */
    @Override
    public void connect() {
        // do nothing
    }

    /**
     * Shutdown listener container.
     */
    @Override
    public void disconnect() {
      ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("disconnect - Disconnecting ActiveMessageReceiver listener with destination to " + processConfiguration.getJmsDaqCommandQueue());
    }
        listenerContainer.shutdown();
    }

    /**
     * Sends a command report to the server.
     * 
     * @param commandReport The command report to send to the server.
     * @param destination The destination to send to.
     * @param session The JMS session to use to send the message.
     * @throws JMSException Throws a JMSException if the sending of 
     * the message failed.
     */
    @Override
    public void sendCommandReport(final SourceCommandTagReport commandReport, 
            final Destination destination, final Session session) throws JMSException {
        sendTextMessage(commandReport.toXML(), destination, session);
    }

    /**
     * Sends a configuration report to the server.
     * 
     * @param configurationChangeEventReport The configuration change event 
     * report to send.
     * @param destination The destination (Queue/Topic) to send to.
     * @param session The JMS session to use.
     * @throws JMSException Throws a JMSException if the sending of 
     * the message failed.
     * @throws IllegalAccessException Throws an exception if an method constructor
     * or field used in a reflective call was not accessible.
     * @throws InstantiationException Called if no default constructor could
     * be called from the newInstance method via reflection.
     * @throws ParserConfigurationException Indicates a serious configuration
     * error with the XML parser.
     * @throws TransformerException An error occurred while transforming the XML.
     */
    @Override
    public void sendConfigurationReport(
            final ConfigurationChangeEventReport configurationChangeEventReport,
            final Destination destination, final Session session) throws TransformerException,
            ParserConfigurationException, IllegalAccessException, InstantiationException, JMSException {
        String reportString = domFactory.createConfigurationChangeEventReportXMLString(configurationChangeEventReport);
        sendTextMessage(reportString, destination, session);
    }

    /**
     * Sends a data tag value response to the server.
     * @param sourceDataTagValueResponse The response to a source data 
     * tag value request.
     * @param replyTopic The topic to reply to.
     * @param session The JMSSession which should be used.
     * @throws JMSException Throws a JMSException if the sending of 
     * the message failed.
     */
    @Override
    public void sendDataTagValueResponse(
            final SourceDataTagValueResponse sourceDataTagValueResponse, 
            final Topic replyTopic, final Session session) throws JMSException {
        sendTextMessage(sourceDataTagValueResponse.toXML(), replyTopic, session);
    }

    /**
     * Sends a text message via JMS to the server.
     * 
     * @param messageText The text of the message to send.
     * @param destination The destination of the message.
     * @param session The session to use.
     * @throws JMSException Throws a JMSException if the sending of the message failed.
     */
    public void sendTextMessage(final String messageText, final Destination destination, final Session session) throws JMSException {
        MessageProducer messageProducer = session.createProducer(destination);
        try {
          TextMessage message = session.createTextMessage();
          message.setText(messageText);
          if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Sending response to DataTagValueRequest.");
          }
          messageProducer.send(destination, message);
        } finally {
          messageProducer.close();
        }        
    }

    /**
     * Called after a message arrived from the server.
     * 
     * @param message The received message.
     * @param session The session to of the received message.
     * @throws JMSException Throws a JMSException if the sending of the 
     * answer message failed.
     */
    @Override
    // TODO change commands & requests to Queue (need change in DAQ tim-shared,
    // so postponed until prototype is working)
    public void onMessage(final Message message, final Session session) 
        throws JMSException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request/Command received from the server.");
        }

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String messageContent = textMessage.getText();
            Document xmlContent;
            
            if (LOGGER.isTraceEnabled())
              LOGGER.trace("message received from server: "+textMessage.getText());
            
            try {
                xmlContent = simpleXMLparser.parse(messageContent);
                switch (checkMessageType(xmlContent.getDocumentElement().getTagName())) {
                case MSG_CHANGE_LOGGING_LEVEL:
                    this.onChangeLoggingLevel(xmlContent);
                    break;

                case MSG_EXECUTE_COMMAND:
                    onExecuteCommand(xmlContent, message.getJMSReplyTo(), session); // already
                    break;

                case MSG_RECONFIGURE_PROCESS:
                    onReconfigureProcess(xmlContent, message.getJMSReplyTo(), session);
                    break;

                case MSG_SRC_DATATAG_VALUE_UPDATE_REQUEST:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Processing server request for current data tag values.");
                    }
                    onSourceDatatagValueUpdateRequest(xmlContent, (Topic) message.getJMSReplyTo(), session);
                    break;

                default:
                    LOGGER.warn("Request received from server not recognized");
                    break;

                } // switch
            } catch (ParserException e) {
                LOGGER.error("ParserException caught on processing incoming DAQ message: ", e);
                LOGGER.error("   message being ignored.");
                // throw new
                // JMSException("ParserException caught on processing incoming DAQ message (see logs for details).");
            } catch (Exception e) {
                LOGGER.error("Unexpected exception caught while processing server request.", e);
            }

        } // if
        else {
            LOGGER.warn("Received non-text message - unable to process.");
        }
    }

    @Override
    public void shutdown() {
      disconnect(); 
    }

    /**
     * Useful for overwriting the defaultListener container (which is wired in automatically).
     * @param listenerContainer the listenerContainer to set
     */
    public void setListenerContainer(final DefaultMessageListenerContainer listenerContainer) {
      this.listenerContainer = listenerContainer;
    }
}
