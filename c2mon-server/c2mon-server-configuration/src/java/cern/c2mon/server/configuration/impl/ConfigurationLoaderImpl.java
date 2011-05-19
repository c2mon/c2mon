package cern.c2mon.server.configuration.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.dao.ConfigurationDAO;
import cern.c2mon.server.configuration.handler.AlarmConfigHandler;
import cern.c2mon.server.configuration.handler.CommandTagConfigHandler;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.DataTagConfigHandler;
import cern.c2mon.server.configuration.handler.EquipmentConfigHandler;
import cern.c2mon.server.configuration.handler.ProcessConfigHandler;
import cern.c2mon.server.configuration.handler.RuleTagConfigHandler;
import cern.c2mon.server.configuration.handler.SubEquipmentConfigHandler;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.ProcessFacade;
import cern.tim.server.common.config.DistributedParams;
import cern.tim.server.daqcommunication.out.ProcessCommunicationManager;
import cern.tim.shared.client.auth.TimSessionInfo;
import cern.tim.shared.client.auth.impl.TimSessionInfoImpl;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
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
 * TODO add to TC config for locking
 * 
 * <p>Creations of processes and equipments require a DAQ restart.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ConfigurationLoaderImpl implements ConfigurationLoader {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationLoaderImpl.class);
  

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
  public ConfigurationReport applyConfiguration(final int configId, final String sessionId) {
    TimSessionInfo sessionInfo = null;
    ConfigurationReport report = null;
    try {
      exclusiveConfigLock.lock();
      //TODO get session info from authentication cache...
      sessionInfo = new TimSessionInfoImpl(null, 0, null, null, null, new String[] {"WEBCONFIG_USER"}); //TODO faked user so far
      if (sessionInfo == null){
        return new ConfigurationReport(
            configId, 
            "UNKNOWN", 
            "UNKNOWN",
            Status.FAILURE,
            "No session info set in request!"
          ); 
      }
      
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
          new StringBuffer("applyConfiguration(configId: ").append(configId).append(", sessionId: ").append(sessionInfo.getSessionId()).append(") called.")
        );
      }
      
      if (!sessionInfo.hasPrivilege("WEBCONFIG_USER")) {
        return new ConfigurationReport(
            configId, 
            "UNKNOWN", 
            sessionInfo.getUserName(),
            Status.FAILURE,
            "Insufficient user privileges to carry out reconfigurations!"
          );      
      }
      
      String configName = configurationDAO.getConfigName(configId);    
      if (configName == null) {
        return new ConfigurationReport(
            configId, 
            "UNKNOWN", 
            sessionInfo.getUserName(),
            Status.FAILURE,
            "Configuration with id <" + configId + "> not found. Please try again with a valid configuration id"
          );
      }
      
      //map of element reports that need a DAQ child report adding
      Map<Long, ConfigurationElementReport> daqReportPlaceholder = new HashMap<Long, ConfigurationElementReport>();
      //map of lists, where each list needs sending to a particular DAQ (processId -> List of events)
      Map<Long, List<Change>> processLists = new HashMap<Long, List<Change>>();       
      
      report = new ConfigurationReport(configId, 
          configName,
          sessionInfo.getUserName());      
      List<ConfigurationElement> configElements = configurationDAO.getConfigElements(configId);            
      for (ConfigurationElement element : configElements) {
        //initialize success report
        ConfigurationElementReport elementReport = new ConfigurationElementReport(element.getAction(),
            element.getEntity(),
            element.getEntityId());        
        List<ProcessChange> processChanges = applyConfigElement(element, elementReport);        
        report.addElementReport(elementReport);       
        if (processChanges != null) {
          for (ProcessChange processChange : processChanges) {
            Long processId = processChange.getProcessId();
            if (!processLists.containsKey(processId)) {
              processLists.put(processId, new ArrayList<Change>());
            }
            processLists.get(processId).add((Change) processChange.getChangeEvent());   //cast to implementation needed as DomFactory uses this - TODO change to interface                  
          } 
          daqReportPlaceholder.put(element.getSequenceId(), elementReport);     
        }
      }
      
      //send events to Process, convert the responses and introduce them into the existing report
      for (Long processId : processLists.keySet()) {
        List<Change> processChangeEvents = processLists.get(processId);
        if (processFacade.isRunning(processId)) {
          ConfigurationChangeEventReport processReport = processCommunicationManager.sendConfiguration(processId, processChangeEvents);
          for (ChangeReport changeReport : processReport.getChangeReports()) {
            ConfigurationElementReport convertedReport = 
              ConfigurationReportConverter.fromProcessReport(changeReport, daqReportPlaceholder.get(changeReport.getChangeId()));
            daqReportPlaceholder.get(changeReport.getChangeId()).addSubReport(convertedReport);   
            //if change report has REBOOT status, mark this DAQ for a reboot in the configuration
            if (changeReport.isReboot()) {
              report.addProcessToReboot(processCache.get(processId).getName());
              //TODO set flag & tag to indicate that process restart is needed
            } else if (changeReport.isFail()) {
              report.setStatus(Status.FAILURE);
              report.setStatusDescription("Failed to apply the configuration successfully. See details in the report below.");
            }
          }
        } else {
          //TODO set process reconfiguraton tag to restart as not running
        }
      }      
      return report;
    
    } catch (Exception ex) {
      LOGGER.error("Exception caught while applying configuration " + configId, ex); 
      if (report == null) {
        String userName = null;
        if (sessionInfo != null) {
          userName = sessionInfo.getUserName();
        }
        report = new ConfigurationReport(
            configId, 
            "UNKNOWN", 
            userName,
            Status.FAILURE,
            "Exception caught when applying configuration with id <" + configId + ">."
          ); 
        report.setExceptionTrace(ex);
      } else {
        report.setStatus(Status.FAILURE);
        report.setExceptionTrace(ex);
      }
      return report;
    } finally {
      exclusiveConfigLock.unlock();
    }
  }
  
 

  /**
   * 
   * @param element the details of the configuration action
   * @param elementReport report that should be set to failed if there is a problem
   * @return list of DAQ configuration events; is null if nothing needs doing on the DAQ for this element
   **/
  private List<ProcessChange> applyConfigElement(final ConfigurationElement element, final ConfigurationElementReport elementReport) {
              
    if (element.getAction() == null || element.getEntity() == null || element.getEntityId() == null) {
      elementReport.setFailure("Parameter missing in configuration line with sequence id " + element.getSequenceId());
      return null; 
    }   
        
//    String fieldName = element.getEntity().toString().toLowerCase() + "ConfigHandler";
//    Object configHandler = this.getClass().getField(fieldName).get(this);
//    Method createMethod = configHandler.getClass().getMethod("create" + element.getEntity().toString().toLowerCase(), parameterTypes)
    
    //initialize the DAQ config event
    List<ProcessChange> daqConfigEvents = null;
    try {      
      switch (element.getAction()) {
      case CREATE :
        switch (element.getEntity()) {
        case DATATAG : daqConfigEvents = dataTagConfigHandler.createDataTag(element); break;
        case RULETAG : ruleTagConfigHandler.createRuleTag(element); break;
        case CONTROLTAG: controlTagConfigHandler.createControlTag(element); break;
        case COMMANDTAG : daqConfigEvents = commandTagConfigHandler.createCommandTag(element); break;
        case ALARM : alarmConfigHandler.createAlarm(element); break;
        case PROCESS : processConfigHandler.createProcess(element); break;
        case EQUIPMENT : equipmentConfigHandler.createEquipment(element); break;
        case SUBEQUIPMENT : subEquipmentConfigHandler.createSubEquipment(element); break;
        default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
          LOGGER.warn("Unrecognized reconfiguration entity: " + element.getEntity() 
              + " - see reconfiguration report for details.");
        }
        break;
      case UPDATE :
        switch (element.getEntity()) {
        case DATATAG :           
          daqConfigEvents = dataTagConfigHandler.updateDataTag(element.getEntityId(), element.getElementProperties()); break;      
        case CONTROLTAG : 
          daqConfigEvents = controlTagConfigHandler.updateControlTag(element.getEntityId(), element.getElementProperties()); break;
        case RULETAG : 
          ruleTagConfigHandler.updateRuleTag(element.getEntityId(), element.getElementProperties()); break;
        case COMMANDTAG : 
          daqConfigEvents = commandTagConfigHandler.updateCommandTag(element.getEntityId(), element.getElementProperties()); break;
        case ALARM : 
          alarmConfigHandler.updateAlarm(element.getEntityId(), element.getElementProperties()); break;
        case PROCESS : 
          processConfigHandler.updateProcess(element.getEntityId(), element.getElementProperties()); break;
        case EQUIPMENT : 
          daqConfigEvents = equipmentConfigHandler.updateAbstractEquipment(element.getEntityId(), element.getElementProperties()); break;
        case SUBEQUIPMENT : 
          daqConfigEvents = subEquipmentConfigHandler.updateAbstractEquipment(element.getEntityId(), element.getElementProperties()); break;
        default : elementReport.setFailure("Unrecognized reconfiguration entity: " + element.getEntity());
          LOGGER.warn("Unrecognized reconfiguration entity: " + element.getEntity() 
              + " - see reconfiguration report for details.");
        }
        break;
      case REMOVE :
        switch (element.getEntity()) {
        case DATATAG : daqConfigEvents = dataTagConfigHandler.removeDataTag(element.getEntityId(), elementReport); break;   
        case CONTROLTAG : daqConfigEvents = controlTagConfigHandler.removeControlTag(element.getEntityId(), elementReport); break;  
        case RULETAG : ruleTagConfigHandler.removeRuleTag(element.getEntityId(), elementReport); break;  
        case COMMANDTAG : daqConfigEvents = commandTagConfigHandler.removeCommandTag(element.getEntityId(), elementReport); break;
        case ALARM : alarmConfigHandler.removeAlarm(element.getEntityId(), elementReport); break; 
        case PROCESS : processConfigHandler.removeProcess(element.getEntityId(), elementReport); break;
        case EQUIPMENT : equipmentConfigHandler.removeEquipment(element.getEntityId(), elementReport); break;
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
      if (daqConfigEvents != null) 
        for (ProcessChange processChange : daqConfigEvents) {
          processChange.getChangeEvent().setChangeId(element.getSequenceId());
        }
    } catch (Exception ex) { //catches failed cache look ups amongst others (attempts at reconfiguring a non-existent cache object)
      LOGGER.error("Exception caught while applying the configuration change (Action, Entity, Entity id) = (" 
          + element.getAction() + "; " + element.getEntity() + "; " + element.getEntityId() + ")", ex);
      elementReport.setFailure("Exception caught while applying the configuration change.", ex);        
    }
    return daqConfigEvents;
  }

}
