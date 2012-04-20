/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.server.configuration.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.configuration.ConfigProgressMonitor;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.impl.CommandTagConfigHandler;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.common.config.DistributedParams;
import cern.tim.server.daqcommunication.out.ProcessCommunicationManager;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.client.configuration.ConfigurationException;
import cern.tim.shared.client.configuration.ConfigurationReport;
import cern.tim.shared.client.configuration.ConfigConstants.Status;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ConfigurationChangeEventReport;

/**
 * Implementation of the server ConfigurationLoader bean.
 * 
 * <p>This implementation uses the injected DAO for all database access,
 * so alternative DAO implementation can be wired in if required. The
 * default provided DAO uses iBatis in the background.
 * 
 * <p>Notice that creating a cache object will also notify any update
 * listeners. In particular, new datatags, rules and control tags will
 * be passed on to the client, STL module etc.
 * 
 * <p>Creations of processes and equipments require a DAQ restart.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  //TODO element & element report status always both need updating - redesign this part
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationLoaderImpl.class);
  
  int changeId = 0; //unique id for all generated changes (including those recursive ones during removal)
  
  /**
   * Distributed lock used to get exclusive configuration access to the server
   * (applying configurations is forced to be sequential).
   */
  private ReentrantReadWriteLock.WriteLock exclusiveConfigLock;
  
  private ProcessCommunicationManager processCommunicationManager;
  
  private ConfigurationDAO configurationDAO;
  
  private DataTagConfigHandler dataTagConfigHandler;
  
  private ControlTagConfigHandler controlTagConfigHandler;
  
  private CommandTagConfigHandler commandTagConfigHandler;
  
  private AlarmConfigHandler alarmConfigHandler;
    
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  private EquipmentConfigHandler equipmentConfigHandler;
  
  private SubEquipmentConfigHandler subEquipmentConfigHandler;
  
  private ProcessConfigHandler processConfigHandler;
  
  private ProcessFacade processFacade;
  
  private ProcessCache processCache;  

  /**
   * Flag recording if configuration events should be sent to the DAQ layer (set in XML).
   */
  private boolean daqConfigEnabled;
  
  /**
   * The directory name in C2MON home where the configreports will be saved.
   */
  private String reportDirectory; 
  
  /**
   * Flag indicating if a cancel request has been made.
   */
  private volatile boolean cancelRequested = false;
    
  @Autowired
  public ConfigurationLoaderImpl(ProcessCommunicationManager processCommunicationManager,
      ConfigurationDAO configurationDAO, DataTagConfigHandler dataTagConfigHandler,
      ControlTagConfigHandler controlTagConfigHandler, CommandTagConfigHandler commandTagConfigHandler,
      final AlarmConfigHandler alarmConfigHandler, RuleTagConfigHandler ruleTagConfigHandler,
      EquipmentConfigHandler equipmentConfigHandler, SubEquipmentConfigHandler subEquipmentConfigHandler,
      ProcessConfigHandler processConfigHandler, ProcessFacade processFacade, DistributedParams distributedParams,
      ProcessCache processCache) {
    super();
    this.processCommunicationManager = processCommunicationManager;
    this.configurationDAO = configurationDAO;
    this.dataTagConfigHandler = dataTagConfigHandler;
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.commandTagConfigHandler = commandTagConfigHandler;
    this.alarmConfigHandler = alarmConfigHandler;
    this.ruleTagConfigHandler = ruleTagConfigHandler;
    this.equipmentConfigHandler = equipmentConfigHandler;
    this.subEquipmentConfigHandler = subEquipmentConfigHandler;
    this.processConfigHandler = processConfigHandler;
    this.processFacade = processFacade;
    this.processCache = processCache;
    exclusiveConfigLock = distributedParams.getExclusiveConfigLock();
  }



  @Override
  public ConfigurationReport applyConfiguration(final int configId, final ConfigProgressMonitor configProgressMonitor) {
    
    LOGGER.info("Applying configuration " + configId);
    ConfigurationReport report = null;
    try {
      exclusiveConfigLock.lock();
                
      String configName = configurationDAO.getConfigName(configId);    
      if (configName == null) {
        LOGGER.warn("Unable to locate configuration with id " + configId + " - cannot be applied.");
        return new ConfigurationReport(
            configId, 
            "UNKNOWN",
            "", //TODO set user name through RBAC once available
            Status.FAILURE,
            "Configuration with id <" + configId + "> not found. Please try again with a valid configuration id"
          );
      }
      
      //map of element reports that need a DAQ child report adding
      Map<Long, ConfigurationElementReport> daqReportPlaceholder = new HashMap<Long, ConfigurationElementReport>();
      //map of elements themselves elt_seq_id -> element
      Map<Long, ConfigurationElement> elementPlaceholder = new HashMap<Long, ConfigurationElement>();
      //map of lists, where each list needs sending to a particular DAQ (processId -> List of events)
      Map<Long, List<Change>> processLists = new HashMap<Long, List<Change>>();                   
      
      report = new ConfigurationReport(configId, 
          configName,
          "");  
      
      List<ConfigurationElement> configElements;
      try {
        configElements = configurationDAO.getConfigElements(configId);            
      } catch (Exception e) {
        String message = "Exception caught while loading the configuration from the DB, so unable to apply any elements of this configuration."; 
        LOGGER.error(message, e);
        throw new RuntimeException(message, e);
      }
      
      AtomicInteger progressCounter = new AtomicInteger(1);
      if (configProgressMonitor != null){
        configProgressMonitor.serverTotalParts(configElements.size());        
      }            
      for (ConfigurationElement element : configElements) {
        if (!cancelRequested) {
          //initialize success report
          ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(),
              element.getEntity(),
              element.getEntityId());
          report.addElementReport(elementReport);       
          List<ProcessChange> processChanges = null; 
          try {
            processChanges = applyConfigElement(element, elementReport);  //never returns null                
            element.setDaqStatus(Status.RESTART); //default to restart; if successful on DAQ, set to OK
            for (ProcessChange processChange : processChanges) {
              
              Long processId = processChange.getProcessId();
              if (processChange.processActionRequired()) {
                if (!processLists.containsKey(processId)) {
                  processLists.put(processId, new ArrayList<Change>());
                }
                processLists.get(processId).add((Change) processChange.getChangeEvent());   //cast to implementation needed as DomFactory uses this - TODO change to interface
                daqReportPlaceholder.put(processChange.getChangeEvent().getChangeId(), elementReport);
                elementPlaceholder.put(processChange.getChangeEvent().getChangeId(), element);
              } else if (processChange.requiresReboot()) {              
                elementReport.requiresReboot();
                report.addStatus(Status.RESTART);
                report.addProcessToReboot(processCache.get(processId).getName());
                element.setStatus(Status.RESTART);
                processFacade.requiresReboot(processId, true);
              }              
            }                      
          } catch (Exception ex) {
            String errMessage = "Exception caught while applying the configuration change (Action, Entity, Entity id) = (" 
              + element.getAction() + "; " + element.getEntity() + "; " + element.getEntityId() + ")"; 
            LOGGER.error(errMessage, ex);
            elementReport.setFailure("Exception caught while applying the configuration change.", ex);
            element.setStatus(Status.FAILURE);          
            report.addStatus(Status.FAILURE);
            report.setStatusDescription("Failure: see details below.");
          }
          if (configProgressMonitor != null){
            configProgressMonitor.onServerProgress(progressCounter.getAndIncrement());
          }
        } else {
          LOGGER.info("Interrupting configuration " + configId + " due to cancel request.");
        }               
      }
      
      //send events to Process if enabled, convert the responses and introduce them into the existing report; else set all DAQs to restart
      if (daqConfigEnabled) {
        if (configProgressMonitor != null){
          configProgressMonitor.daqTotalParts(processLists.size());
        }
        AtomicInteger daqProgressCounter = new AtomicInteger(1);
        for (Long processId : processLists.keySet()) {
          if (!cancelRequested){
            List<Change> processChangeEvents = processLists.get(processId);
            if (processFacade.isRunning(processId) && !processFacade.isRebootRequired(processId)) {
              try {
                LOGGER.trace("Sending " + processChangeEvents.size() + " change events to Process " + processId + "...");
                ConfigurationChangeEventReport processReport = processCommunicationManager.sendConfiguration(processId, processChangeEvents);
                if (!processReport.getChangeReports().isEmpty()) {
                  LOGGER.trace("Received " + processReport.getChangeReports().size() + " back from Process.");
                } else {
                  LOGGER.trace("Received 0 reports back from Process");
                }                
                for (ChangeReport changeReport : processReport.getChangeReports()) {
                  ConfigurationElementReport convertedReport = 
                    ConfigurationReportConverter.fromProcessReport(changeReport, daqReportPlaceholder.get(changeReport.getChangeId()));
                  daqReportPlaceholder.get(changeReport.getChangeId()).addSubReport(convertedReport);   
                  //if change report has REBOOT status, mark this DAQ for a reboot in the configuration
                  if (changeReport.isReboot()) {
                    report.addProcessToReboot(processCache.get(processId).getName()); 
                    elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.RESTART);
                    //TODO set flag & tag to indicate that process restart is needed
                  } else if (changeReport.isFail()) {
                    report.addStatus(Status.FAILURE);
                    report.setStatusDescription("Failed to apply the configuration successfully. See details in the report below.");
                    elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.FAILURE);
                  } else { //success, override default failure
                    if (elementPlaceholder.get(changeReport.getChangeId()).getDaqStatus().equals(Status.RESTART)) {
                      elementPlaceholder.get(changeReport.getChangeId()).setDaqStatus(Status.OK);
                    }                
                  }
                }
              } catch (Exception e) {
                String errorMessage = "Error during DAQ reconfiguration: unsuccessful application of configuration to Process (possible timeout)" + processCache.get(processId).getName();
                LOGGER.error(errorMessage, e);
                processFacade.requiresReboot(processId, true);
                report.addProcessToReboot(processCache.get(processId).getName());
                report.addStatus(Status.FAILURE);
                report.setStatusDescription(errorMessage);
              }            
            } else {
              processFacade.requiresReboot(processId, true);
              report.addProcessToReboot(processCache.get(processId).getName());
              report.addStatus(Status.RESTART);
            }
            if (configProgressMonitor != null) {
              configProgressMonitor.onDaqProgress(daqProgressCounter.getAndIncrement());
            }
          } else {
            LOGGER.info("Interrupting configuration " + configId + " due to cancel request.");
          }          
        }
      } else {
        LOGGER.debug("DAQ runtime reconfiguration not enabled - setting required restart flags");
        if (!processLists.isEmpty()){
          report.addStatus(Status.RESTART);
          for (Long processId : processLists.keySet()) {
            processFacade.requiresReboot(processId, true);
            report.addProcessToReboot(processCache.get(processId).getName());          
          }
        }        
      }
      
      
      //save Configuration element status information in the DB tables
      for (ConfigurationElement element : configElements) {
        configurationDAO.saveStatusInfo(element);
      }
      //mark the Configuration as applied in the DB table, with timestamp set
      configurationDAO.markAsApplied(configId);
      LOGGER.info("Finished applying configuraton " + configId);
      return report;
    
    } catch (Exception ex) {
      LOGGER.error("Exception caught while applying configuration " + configId, ex); 
      if (report == null) {
        String userName = null;       
        report = new ConfigurationReport(
            configId, 
            "UNKNOWN", 
            "",
            Status.FAILURE,
            "Exception caught when applying configuration with id <" + configId + ">."
          ); 
        report.setExceptionTrace(ex);
      } else {
        report.addStatus(Status.FAILURE);
        report.setExceptionTrace(ex);
      }
      throw new ConfigurationException(report, ex);
    } finally {
      if (report != null) {
        archiveReport(configId, report.toXML());
      }
      exclusiveConfigLock.unlock();
    }
  }
  
 

  /**
   * Applies a single configuration element. On the DB level, this action should
   * either be entirely applied or the transaction rolled back. In the case of
   * a rollback, the cache should also reflect this rollback (emptied and reloaded
   * for instance). 
   * 
   * @param element the details of the configuration action
   * @param elementReport report that should be set to failed if there is a problem
   * @param changeId first free id to use in the sequence of changeIds, used for sending to DAQs *is increased by method*
   * @return list of DAQ configuration events; is never null but may be empty
   * @throws IllegalAccessException 
   **/
  private List<ProcessChange> applyConfigElement(final ConfigurationElement element, 
                                                 final ConfigurationElementReport elementReport) throws IllegalAccessException {
    if (LOGGER.isTraceEnabled()){
      LOGGER.trace("Applying configuration element with sequence id " + element.getSequenceId());
    }
    
    if (element.getAction() == null || element.getEntity() == null || element.getEntityId() == null) {
      elementReport.setFailure("Parameter missing in configuration line with sequence id " + element.getSequenceId());
      return null; 
    }   
        
//    String fieldName = element.getEntity().toString().toLowerCase() + "ConfigHandler";
//    Object configHandler = this.getClass().getField(fieldName).get(this);
//    Method createMethod = configHandler.getClass().getMethod("create" + element.getEntity().toString().toLowerCase(), parameterTypes)
    
    //initialize the DAQ config event
    List<ProcessChange> daqConfigEvents = new ArrayList<ProcessChange>();
         
    switch (element.getAction()) {
    case CREATE :
      switch (element.getEntity()) {
      case DATATAG : daqConfigEvents.add(dataTagConfigHandler.createDataTag(element)); break;
      case RULETAG : ruleTagConfigHandler.createRuleTag(element); break;
      case CONTROLTAG: daqConfigEvents.add(controlTagConfigHandler.createControlTag(element));
                       element.setDaqStatus(Status.RESTART); break;
      case COMMANDTAG : daqConfigEvents = commandTagConfigHandler.createCommandTag(element); break;
      case ALARM : alarmConfigHandler.createAlarm(element); break;
      case PROCESS : daqConfigEvents.add(processConfigHandler.createProcess(element));
                     element.setDaqStatus(Status.RESTART); break;
      case EQUIPMENT : daqConfigEvents.add(equipmentConfigHandler.createEquipment(element)); break;
      case SUBEQUIPMENT : daqConfigEvents.add(subEquipmentConfigHandler.createSubEquipment(element));
                          element.setDaqStatus(Status.RESTART); break;
      default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
        LOGGER.warn("Unrecognized reconfiguration entity: " + element.getEntity() 
            + " - see reconfiguration report for details.");
      }
      break;
    case UPDATE :
      switch (element.getEntity()) {
      case DATATAG :           
        daqConfigEvents.add(dataTagConfigHandler.updateDataTag(element.getEntityId(), element.getElementProperties())); break;      
      case CONTROLTAG : 
        daqConfigEvents.add(controlTagConfigHandler.updateControlTag(element.getEntityId(), element.getElementProperties())); break;
      case RULETAG : 
        ruleTagConfigHandler.updateRuleTag(element.getEntityId(), element.getElementProperties()); break;
      case COMMANDTAG : 
        daqConfigEvents.addAll(commandTagConfigHandler.updateCommandTag(element.getEntityId(), element.getElementProperties())); break;
      case ALARM : 
        alarmConfigHandler.updateAlarm(element.getEntityId(), element.getElementProperties()); break;
      case PROCESS : 
        daqConfigEvents.add(processConfigHandler.updateProcess(element.getEntityId(), element.getElementProperties())); break;
      case EQUIPMENT : 
        daqConfigEvents.addAll(equipmentConfigHandler.updateEquipment(element.getEntityId(), element.getElementProperties())); break;
      case SUBEQUIPMENT : 
        daqConfigEvents.addAll(subEquipmentConfigHandler.updateSubEquipment(element.getEntityId(), element.getElementProperties())); break;
      default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
        LOGGER.warn("Unrecognized reconfiguration entity: " + element.getEntity() 
            + " - see reconfiguration report for details.");
      }
      break;
    case REMOVE :
      switch (element.getEntity()) {
      case DATATAG : daqConfigEvents.addAll(dataTagConfigHandler.removeDataTag(element.getEntityId(), elementReport)); break;   
      case CONTROLTAG : daqConfigEvents.add(controlTagConfigHandler.removeControlTag(element.getEntityId(), elementReport)); break;  
      case RULETAG : ruleTagConfigHandler.removeRuleTag(element.getEntityId(), elementReport); break;  
      case COMMANDTAG : daqConfigEvents.addAll(commandTagConfigHandler.removeCommandTag(element.getEntityId(), elementReport)); break;
      case ALARM : alarmConfigHandler.removeAlarm(element.getEntityId(), elementReport); break; 
      case PROCESS : daqConfigEvents.add(processConfigHandler.removeProcess(element.getEntityId(), elementReport)); break;
      case EQUIPMENT : daqConfigEvents.add(equipmentConfigHandler.removeEquipment(element.getEntityId(), elementReport)); break;
      case SUBEQUIPMENT : subEquipmentConfigHandler.removeSubEquipment(element.getEntityId(), elementReport); break;                          
      default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
      LOGGER.warn("Unrecognized reconfiguration entity: " + element.getEntity() 
          + " - see reconfiguration report for details.");
      }                 
      break;
    default : elementReport.setFailure("Unrecognized reconfiguration action: " + element.getAction());
    LOGGER.warn("Unrecognized reconfiguration action: " + element.getAction() 
        + " - see reconfiguration report for details."); 
    }
    //set *unique* change id (single element may trigger many changes e.g. rule removal)
    if (!daqConfigEvents.isEmpty()) {            
      for (ProcessChange processChange : daqConfigEvents) { 
        if (processChange.processActionRequired()) {
          processChange.getChangeEvent().setChangeId(changeId);       
          changeId++;
        }        
      }
    }      
    return daqConfigEvents;
  }


  /**
   * Save the report to disk.
   * @param configId id of the config
   * @param xmlReport the XML report in String format
   */
  private void archiveReport(int configId, String xmlReport) {
    try {
      File outFile = new File(reportDirectory, "report_" + configId + ".xml");
      FileWriter fileWriter;    
      fileWriter = new FileWriter(outFile);    
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(xmlReport);
      bufferedWriter.close();
    } catch (Exception e) {
      LOGGER.error("Exception caught while writing configuration report to directory: " 
          + reportDirectory, e);
    }
  }
  
  /**
   * @param daqConfigEnabled the daqConfigEnabled to set
   */
  public void setDaqConfigEnabled(final boolean daqConfigEnabled) {
    this.daqConfigEnabled = daqConfigEnabled;
  }

  /**
   * Set the (absolute) directory where the config reports should be saved.
   * @param reportDirectory report directory
   */
  public void setReportDirectory(final String reportDirectory) {
    this.reportDirectory = reportDirectory;
  }

  @Override
  public void cancelCurrentConfiguration() {
    cancelRequested = true;
  }

  @Override
  public ConfigurationReport applyConfiguration(int configId) {    
    return applyConfiguration(configId, null);
  }

}
