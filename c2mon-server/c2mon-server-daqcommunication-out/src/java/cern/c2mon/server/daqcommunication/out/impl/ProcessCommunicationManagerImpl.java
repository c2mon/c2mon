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
package cern.c2mon.server.daqcommunication.out.impl;

import java.util.List;

import javax.jms.ConnectionFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daqcommunication.out.ProcessCommunicationManager;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.command.CommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.config.ConfigurationDOMFactory;
import cern.c2mon.shared.daq.config.ConfigurationObjectFactory;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import cern.c2mon.shared.daq.exception.ProcessRequestException;
import cern.c2mon.shared.util.parser.XmlParser;


/**
 * Implementation of the ProcessCommunicationManager using ActiveMQ middleware and Spring.
 */
@Service
public class ProcessCommunicationManagerImpl implements ProcessCommunicationManager, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessCommunicationManagerImpl.class); 
  
  /**
   * Time spent waiting for a DAQ to send a response when reconfiguration.
   */  
  @Value("${c2mon.jms.daq.configuration.timeout}")
  private int configurationTimeout = 180000;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;
  
  /**
   * Reference to the equipment cache.
   */
  private EquipmentCache equipmentCache;
  
  /**
   * Reference to process cache.
   */
  private ProcessCache processCache;
  
  /**
   * Reference to process facade.
   */
  private ProcessFacade processFacade;
  
  /**
   * Reference to the bean managing the JMS
   * sending.
   */
  private JmsProcessOut jmsProcessOut; 
  
  /**
   * Simple XML parser.
   */
  private XmlParser xmlParser;
  
  /**
   * Pooled JMS connection factory. 
   */
  private ConnectionFactory processOutConnectionFactory;
  
  
  /**
   * Autowired constructor.
   * @param equipmentCache
   * @param equipmentFacade
   * @param processCache
   * @param processFacade
   * @param jmsProcessOut
   * @param parser
   */
  @Autowired
  public ProcessCommunicationManagerImpl(EquipmentCache equipmentCache,
                                            ProcessCache processCache, ProcessFacade processFacade,
                                            JmsProcessOut jmsProcessOut, 
                                            @Qualifier("processCommunicationXMLParser") XmlParser parser,
                                            @Qualifier("processOutConnectionFactory") ConnectionFactory connectionFactory) {
    super();
    this.equipmentCache = equipmentCache;
    this.processCache = processCache;
    this.processFacade = processFacade;    
    this.jmsProcessOut = jmsProcessOut;    
    this.xmlParser = parser;
    this.processOutConnectionFactory = connectionFactory;
  }
  
  @Override
  public SourceDataTagValueResponse requestDataTagValues(final SourceDataTagValueRequest pRequest) throws ProcessRequestException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Requesting datatag values for " + pRequest.getType());
    }
    
    SourceDataTagValueResponse result = null;
    
    if (pRequest == null) {
      String errorMessage = "requestDataTagValues() : called with null parameter.";      
      throw new ProcessRequestException(errorMessage);
    } else {
      try {
        //first retrieve process id of the request
        Long processId = null;
        if (pRequest.getType().equals(SourceDataTagValueRequest.TYPE_PROCESS)) {
          processId = pRequest.getId();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("requestDataTagValues() for PROCESS " + processId);
          }
        } else if (pRequest.getType().equals(SourceDataTagValueRequest.TYPE_EQUIPMENT)) {
          Long equipmentId = pRequest.getId();

          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("requestDataTagValues() for EQUIPMENT " + equipmentId);
          }
        
          try {
            Equipment equipment = equipmentCache.get(equipmentId);
            processId = equipment.getProcessId();
          } catch (CacheElementNotFoundException cacheEx) {            
            String errorMessage = "Unable to treat data tag request.";             
            throw new ProcessRequestException(errorMessage, cacheEx);
          }
                              
        } else if (pRequest.getType().equals(SourceDataTagValueRequest.TYPE_DATATAG)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("requestDataTagValues() for DATATAG " + pRequest.getId());
          }
          String errorMessage = "requestDataTagValues() : request for individual tags currently not supported.";          
          throw new ProcessRequestException(errorMessage);
        } else {
          String errorMessage = "SourceDataTagValueRequest type not recognized - unable to process it.";
          LOGGER.error(errorMessage);
          throw new ProcessRequestException(errorMessage);
        }
        
        //if managed to set the processId
        
        try {
          Process process = processCache.get(processId);
          if (processFacade.isRunning(process)) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("requestDataTagValues() : associated process is running.");
            }
            //treat request
            String reply = jmsProcessOut.sendTextMessage(pRequest.toXML(), process.getJmsListenerTopic(), 10000);            
            if (reply != null) {
              if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("requestDataTagValues() : reply received: " + reply);
              }
              Document doc = xmlParser.parse(reply);  //can throw ParserException
              if (doc != null) {
                result = SourceDataTagValueResponse.fromXML(doc.getDocumentElement());  //can throw ProcessRequestException        
              } 
            } else {
              String errorMessage = "No response received for a SourceDataTagValueRequest (request timeout?)";              
              throw new ProcessRequestException(errorMessage);
            }
          } else {            
            String errorMessage = "requestDataTagValues() : Process " + processId + " is not running.";
            throw new ProcessRequestException(errorMessage);            
          }
        } catch (CacheElementNotFoundException cacheEx) {
          String errorMessage = "Unable to process data tag request.";
          throw new ProcessRequestException(errorMessage, cacheEx);
        }
                             
      } catch (Exception e) {
        String errorMessage = "requestDataTagValues() : Exception caught";
        LOGGER.error(errorMessage, e);
        throw new RuntimeException(errorMessage, e);       
      }    
    }        
    return result;
  }

  @Override
  public <T> CommandReport executeCommand(final CommandTag<T> commandTag, final T value) {      
    // Before attempting anything else, make sure none of the parameters is null
    if (commandTag == null) {
      LOGGER.warn("executeCommand() : called with null CommandTagHandler parameter.");
      throw new NullPointerException("executeCommand(..) method called with a null CommandTagHandle.");
    }
    if (value == null) {
      LOGGER.warn("executeCommand() : called with null value parameter.");
      throw new NullPointerException("executeCommand(..) method called with a null value parameter.");
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("executeCommand() : called for command id " + commandTag.getId());
    }
    
    CommandReport result = null;

    try {
      Process process = processCache.get(commandTag.getProcessId());
      if (processFacade.isRunning(process)) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("executeCommand() : associated process is running.");
        }
        // treat command
        SourceCommandTagValue val = 
          new SourceCommandTagValue(commandTag.getId(), commandTag.getName(), commandTag.getEquipmentId(), commandTag.getMode(), 
                                    value, commandTag.getDataType());
        String reply = jmsProcessOut.sendTextMessage(val.toXML(), process.getJmsListenerTopic(), commandTag.getExecTimeout());         
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("executeCommand() : reply received: " + reply);
        }
        if (reply != null) {
          Document doc = xmlParser.parse(reply);
          if (doc != null) {
            SourceCommandTagReport report = SourceCommandTagReport.fromXML(doc.getDocumentElement());
            if (report.getStatus() == SourceCommandTagReport.STATUS_OK) {
              CommandReportImpl commandReport = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_OK, report.getFullDescription());
              commandReport.setReturnValue(report.getReturnValue());
              return commandReport;
            } else if (report.getStatus() == SourceCommandTagReport.STATUS_TEST_OK) {
              return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_NOT_EXECUTED, report.getFullDescription());
            } else {
              return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_EXECUTION_FAILED, 
                                       report.getFullDescription());
            }
          } else {
            return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_SERVER_ERROR, 
                                     "Reply received from DAQ could not be unmarshalled");
          }
        } else {
          return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_TIMED_OUT);
        }
      } else {
        LOGGER.warn("executeCommand() : Process is not running.");
        result = 
            new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_PROCESS_DOWN, "The associated DAQ process is not running.");
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("executeCommand() : Process (id=" + commandTag.getProcessId() + ") related to command tag (id=" 
          + commandTag.getId() + ") not found in cache.", cacheEx);
      result = 
          new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_SERVER_ERROR, "Process related to command not found in cache.");
    } catch (Exception e) {
      LOGGER.error("executeCommand() : Exception", e);
    }
    return result;
  }
  
  @Override
  public ConfigurationChangeEventReport sendConfiguration(final Long processId, final List<Change> changeList) 
          throws ParserConfigurationException, IllegalAccessException, InstantiationException, 
                  TransformerException, NoSuchFieldException, NoSimpleValueParseException {
    ConfigurationDOMFactory domFactory = new ConfigurationDOMFactory();
  
    String xmlConfigString = domFactory.createConfigurationXMLString(changeList);
    String reply = jmsProcessOut.sendTextMessage(xmlConfigString, processCache.get(processId).getJmsListenerTopic(), configurationTimeout);
    if (reply != null) {
      Document replyDoc = xmlParser.parse(reply);
      ConfigurationObjectFactory objectFactory = new ConfigurationObjectFactory();
      ConfigurationChangeEventReport report = objectFactory.createConfigurationChangeEventReport(replyDoc.getDocumentElement());
      return report;
    } else {
      LOGGER.warn("Null reconfiguration reply received from DAQ - probably due to timeout, current set at " + configurationTimeout);
      //TODO create new default failure report for all changes in changeList... and return (changes only applied on server then)
      throw new RuntimeException("Null reconfiguration reply received from DAQ - probably due to timeout, current set at " + configurationTimeout);
    }    
  }

  @Override
  public boolean isAutoStartup() {    
    return false;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {    
    running = true;
  }

  @Override
  public void stop() {
    if (running) {
      running = false;
      try {     
        LOGGER.debug("Shutting down ProcessCommunicationManager bean and associated JMS connections.");
        if (processOutConnectionFactory instanceof SingleConnectionFactory) {
          ((SingleConnectionFactory) processOutConnectionFactory).destroy();
        }      
      } catch (Exception e) {
        LOGGER.error("Exception caught when trying to shutdown the server->DAQ JMS connections:", e);
        e.printStackTrace(System.err);          
      }
    }    
  }

  @Override
  public int getPhase() {    
    return ServerConstants.PHASE_INTERMEDIATE;
  }

}
