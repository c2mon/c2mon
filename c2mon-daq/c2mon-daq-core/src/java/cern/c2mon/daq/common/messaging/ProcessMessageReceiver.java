/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2009 CERN This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.daq.common.messaging;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.messaging.impl.RequestController;
import cern.c2mon.daq.tools.StackTraceHelper;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.config.ConfigurationObjectFactory;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitRemove;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;

/**
 * The ProcessMessageReceiver is responsible for listening to incoming messages from the server, initiating the required
 * action and sending a response to the server. Common behavior is implemented in this abstract class. Different
 * implementations can be provided for different JMS connections. Only one can be used at a time and it is specified in
 * the Spring XML configuration file daq-core-service.xml.
 * 
 * @author mbrightw
 */
public abstract class ProcessMessageReceiver {

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessMessageReceiver.class);

    /**
     * Reconfiguration logger to log the reconfiguration separately.
     */
    private static final Logger RECONF_LOGGER = Logger.getLogger("ReconfigurationLogger");

    /**
     * Constant indicating a unknown message.
     */
    public static final int MSG_UNKNOWN = -1;
    /**
     * Constant for a change logging level message.
     */
    public static final int MSG_CHANGE_LOGGING_LEVEL = 0;
    /**
     * Constant for a execute command message.
     */
    public static final int MSG_EXECUTE_COMMAND = 1;
    /**
     * Constant for a reconfiguration message.
     */
    public static final int MSG_RECONFIGURE_PROCESS = 2;
    /**
     * Constant for a source data tag update request.
     */
    public static final int MSG_SRC_DATATAG_VALUE_UPDATE_REQUEST = 3;

    /**
     * Lock used across the DAQ to prevent multiple requests from being handled at once (in particular when receiving
     * commands through both Sonic and ActiveMQ).
     */
    private static ReentrantReadWriteLock requestLock = new ReentrantReadWriteLock();

    /**
     * Reference to the DriverKernel
     */
    @Resource
    private DriverKernel kernel;

    /**
     * The configuration object factory to create the objects for the reconfiguration from an XML message.
     */
    private ConfigurationObjectFactory configurationObjectFactory = new ConfigurationObjectFactory();

    // TODO Use autowired instead of resource
    /**
     * The RequestController delivers the request from the server to the right parts of the core and the equipment.
     */
    @Resource
    private RequestController requestController;

    /**
     * Send the current values of all data tags to the server (retrieved from DAQ memory - no equipment specific call)
     * 
     * @param sourceDataTagValueResponse object wrapping the update values
     * @param replyTopic the JMS topic to send the reply to
     * @param session the JMS session to use
     * @throws JMSException Throws a JMS exception if the sending of the answer fails.
     */
    public abstract void sendDataTagValueResponse(final SourceDataTagValueResponse sourceDataTagValueResponse,
            final Topic replyTopic, Session session) throws JMSException;

    /**
     * Sends a command report to the server.
     * 
     * @param sourceCommandTagReport The report object to use.
     * @param destination The JMS destination.
     * @param session The JMS session.
     * @throws JMSException Throws a JMSException if the sending of the answer message failed.
     */
    public abstract void sendCommandReport(final SourceCommandTagReport sourceCommandTagReport,
            final Destination destination, final Session session) throws JMSException;

    /**
     * Stop listening for requests from the server.
     * <p>
     * Also gently close any connection this object has to the message brokers (except shared connections in the case of
     * the ActiveMQ implementation, where the shared connections are closed by the JmsSender implementation).
     */
    public abstract void disconnect();

    /**
     * Perform final shutdown of this message receiver (no prior disconnect call is needed).
     */
    public abstract void shutdown();

    /**
     * Connect to the message broker and start listening for requests from the server.
     */
    public abstract void connect();

    /**
     * Sends a configuration report to the server.
     * 
     * @param configurationChangeEventReport The report object to use.
     * @param destination The JMS destination.
     * @param session The JMS session.
     * @throws JMSException Throws a JMSException if the sending of the message failed.
     * @throws IllegalAccessException Throws an exception if an method constructor or field used in a reflective call
     *             was not accessible.
     * @throws InstantiationException Called if no default constructor could be called from the newInstance method via
     *             reflection.
     * @throws ParserConfigurationException Indicates a serious configuration error with the XML parser.
     * @throws TransformerException An error occurred while transforming the XML.
     */
    public abstract void sendConfigurationReport(final ConfigurationChangeEventReport configurationChangeEventReport,
            final Destination destination, final Session session) throws TransformerException,
            ParserConfigurationException, IllegalAccessException, InstantiationException, JMSException;

    /**
     * The method simply distinguish between different possible types of messages coming to the DAQ.
     * 
     * @param rootNode The name of the root node to identify the message type.
     * @return The message type as an int. See constants of this class.
     */
    protected int checkMessageType(final String rootNode) {
        int messageType = MSG_UNKNOWN;

        if (rootNode.equalsIgnoreCase("ChangeLoggingLevel")) {
            messageType = MSG_CHANGE_LOGGING_LEVEL;
        } else if (rootNode.equalsIgnoreCase("ConfigurationChangeEvent")) {
            messageType = MSG_RECONFIGURE_PROCESS;
        } else if (rootNode.equalsIgnoreCase("CommandTag")) {
            messageType = MSG_EXECUTE_COMMAND;
        } else if (rootNode.equalsIgnoreCase("DataTagValueUpdateRequest")) {
            messageType = MSG_SRC_DATATAG_VALUE_UPDATE_REQUEST;
        }

        return messageType;
    }

    /**
     * This method is called each time ProcessMessageReceiver receives MSG_RECONFIGURE_PROCESS message
     * 
     * @param doc DOM message document
     * @param destination The destination where the answer should go to.
     * @param jmsSession The JMS session to use.
     */
    public final void onReconfigureProcess(final Document doc, final Destination destination, final Session jmsSession) {
        requestLock.writeLock().lock();
        try {
            LOGGER.debug("entering onReconfigureProcess()..");
            ConfigurationChangeEventReport configurationReport = new ConfigurationChangeEventReport();
            List<Change> changes = null;
            try {
                changes = configurationObjectFactory.generateChanges(doc.getDocumentElement());
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("got " + changes.size() + " reconfiguration events");
            } catch (NoSuchFieldException e) {
                configurationReport.setError(StackTraceHelper.getStackTrace(e));
            } catch (IllegalAccessException e) {
                /*
                 * This should not happen if there is no major bug in the code which sets the fields of the objects.
                 */
                configurationReport.setError(StackTraceHelper.getStackTrace(e));
            } catch (NoSuchMethodException e) {
                configurationReport.setError("The NoSuchMethodException might be caused through a not "
                        + "supported change in the XML. The reflective call of" + "the right method will fail then.\n"
                        + StackTraceHelper.getStackTrace(e));
            } catch (InvocationTargetException e) {
                configurationReport.setError(StackTraceHelper.getStackTrace(e));
            } catch (Exception e) {
                configurationReport.setError(StackTraceHelper.getStackTrace(e));
            }

            if (changes != null) {
                for (Change change : changes) {
                    ChangeReport changeReport = null;

                    if (change instanceof EquipmentUnitAdd) {
                        changeReport = kernel.onEquipmentUnitAdd((EquipmentUnitAdd) change);
                    } else if (change instanceof EquipmentUnitRemove) {
                        changeReport = kernel.onEquipmentUnitRemove((EquipmentUnitRemove) change);
                    } else {
                        changeReport = requestController.applyChange(change);
                    }

                    configurationReport.appendChangeReport(changeReport);
                }
            }
            
            RECONF_LOGGER.info(configurationReport);
            
            try {
                sendConfigurationReport(configurationReport, destination, jmsSession);
            } catch (TransformerException e) {
                LOGGER.error("Error while transforming configuration report to a text message.", e);
            } catch (ParserConfigurationException e) {
                LOGGER.error("A serious configuration error or library conflict of the xml parser occured", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("IllegalAccessException while generating configuration chage report.", e);
            } catch (InstantiationException e) {
                LOGGER.error("InstantiationException while generating configuration chage report.", e);
            } catch (JMSException e) {
                LOGGER.error("Could not send the generated report to JMS.", e);
            }
            LOGGER.debug("leaving onReconfigureProcess()");
        } finally {
            requestLock.writeLock().unlock();
        }
    }

    /**
     * New implementation that does not assume we are using a unique session.
     * 
     * @param doc The received XML as DOM tree.
     * @param replyTo The JMS reply topic.
     * @param session The JMS session to use.
     * @throws JMSException May throw a JMS exception.
     */
    public void onSourceDatatagValueUpdateRequest(final Document doc, final Topic replyTo, final Session session)
            throws JMSException {
        requestLock.writeLock().lock();
        try {
            LOGGER.debug("entering onSourceDatatagValueUpdateRequest()... with session info");
            if (replyTo == null) {
                LOGGER.warn("\tthe \"replyTo\" property was not set in the JMS message. Ignoring request.");
            } else {
                Element rootEl = doc.getDocumentElement();
                if (rootEl != null) {
                    SourceDataTagValueRequest sdtValueRequest = SourceDataTagValueRequest.fromXML(rootEl);
                    sdtValueRequest.setReplyTopic(replyTo);
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("received SourceDataTagValueRequest:\n" + sdtValueRequest.toXML());

                    SourceDataTagValueResponse sourceDataTagValueResponse = requestController
                            .onSourceDataTagValueUpdateRequest(sdtValueRequest);
                    sendDataTagValueResponse(sourceDataTagValueResponse, replyTo, session);
                }
            } // else

            LOGGER.debug("leaving onSourceDatatagValueUpdateRequest()");
        } finally {
            requestLock.writeLock().unlock();
        }
    }

    // TODO in shared change Topic to Destination
    /**
     * This method is called each time ProcessMessageReceiver receives MSG_EXECUTE_COMMAND message
     * 
     * @param doc DOM message document
     * @param destination The JMS reply topic.
     * @param session The JMS session.
     */
    public void onExecuteCommand(final Document doc, final Destination destination, final Session session) {
        requestLock.writeLock().lock();
        try {
            LOGGER.debug("entering onExecuteCommand()..");

            if (destination == null) {
                LOGGER
                        .warn("\tthe \"replyTo\" topic of the JMS command execution request has not been specified. The execution will be skipped..");
            } else {
                Element rootEl = doc.getDocumentElement();
                if (rootEl != null) {
                    SourceCommandTagValue sctv = SourceCommandTagValue.fromXML(rootEl);
                    sctv.log();

                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("received CommandTag:\n" + sctv.toXML());

                    // this.kernel.executeCommand(sctv);
                    SourceCommandTagReport sourceCommandTagReport = requestController.executeCommand(sctv);

                    if (sourceCommandTagReport != null) {
                        sourceCommandTagReport.log();
                    }

                    try {
                        sendCommandReport(sourceCommandTagReport, destination, session);
                    } catch (JMSException e) {
                        LOGGER.error("Could not send command report", e);
                    }
                }
            } // else

            LOGGER.debug("leaving onExecuteCommand()");
        } finally {
            requestLock.writeLock().unlock();
        }
    }

    /**
     * This method is called each time ProcessMessageReceiver receives MSG_CHANGE_LOGGING_LEVEL message. For schema
     * definition and some examples of the XML go to the TIMDriverLog4j document.
     * 
     * @param doc - DOM message document
     */
    public void onChangeLoggingLevel(final Document doc) {
        requestLock.writeLock().lock();
        try {
            LOGGER.debug("entering onChangeLoggingLevel()..");

            Element rootEl = doc.getDocumentElement();
            NodeList n1 = rootEl.getElementsByTagName("process");
            if (n1.getLength() == 1) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("calling kernel\'s setRootLoggerLevel() with level : "
                            + n1.item(0).getFirstChild().getNodeValue());

                this.kernel.setRootLoggerLevel(n1.item(0).getFirstChild().getNodeValue());
            }

            Element eqUnitsBlock = (Element) rootEl.getElementsByTagName("EquipmentUnits").item(0);
            NodeList eqUnits = eqUnitsBlock.getElementsByTagName("EquipmentUnit");
            for (int i = 0; i < eqUnits.getLength(); i++) {
                Element eqUnit = (Element) eqUnits.item(i);
                String eqID = eqUnit.getAttribute("id");
                String eqName = eqUnit.getAttribute("name");

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("calling kernel\'s setEqLoggerLevel() for eqUnit id : " + eqID + " name : " + eqName
                            + " with level : " + eqUnit.getFirstChild().getNodeValue());

                this.kernel.setEqLoggerLevel(Long.getLong(eqID), eqName, eqUnit.getFirstChild().getNodeValue());
            }

            LOGGER.debug("leaving onChangeLoggingLevel()");
        } finally {
            requestLock.writeLock().unlock();
        }
    }

}
