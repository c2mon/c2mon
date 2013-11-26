/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.conf.core;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Document;

import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.ICoreCommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.ICoreDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.ICoreEquipmentConfigurationChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.vcm.ValueChangeMonitorEngine;
import cern.c2mon.daq.tools.CommandParamsHandler;
import cern.c2mon.daq.tools.StackTraceHelper;
import cern.c2mon.daq.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.datatag.ValueChangeMonitor;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.config.CommandTagAdd;
import cern.tim.shared.daq.config.CommandTagRemove;
import cern.tim.shared.daq.config.CommandTagUpdate;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.EquipmentConfigurationUpdate;
import cern.tim.shared.daq.config.ProcessConfigurationUpdate;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.process.ProcessConfigurationRequest;
import cern.tim.shared.daq.process.ProcessConfigurationResponse;
import cern.tim.shared.daq.process.ProcessConnectionResponse;

/**
 * The ConfigurationController managing the configuration life cycle and allows to access and change the current
 * configuration.
 * 
 * @author Andreas Lang
 * @author vilches (refactoring updates)
 */
public class ConfigurationController {
  /**
   * Constant of the configuration command line argument.
   */
  private static final String CONFIGURATION_PARAM = "-c";
  /**
   * Constant of the save location command line argument.
   */
  private static final String SAVE_PARAM = "-s";
  /**
   * Constant of the process name command line argument.
   */
  private static final String PROCESS_NAME_PARAM = "-processName";
  /**
   * The logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ConfigurationController.class);
  /**
   * Reference to the command line parameter object.
   */
  @Resource
  private CommandParamsHandler commandParamsHandler;
  /**
   * The run options of the DAQ process.
   */
  private RunOptions runOptions;
  /**
   * The common configuration object shared over all DAQs. This is loaded from a local configuration file.
   */
  private CommonConfiguration commonConfiguration;
  /**
   * The process configuration object.
   */
  private ProcessConfiguration processConfiguration;
  /**
   * The process configuration loader used at startup.
   */
  private ProcessConfigurationLoader processConfigurationLoader;
  /**
   * The updater which applies changes to source data tags.
   */
  private ConfigurationUpdater configurationUpdater = new ConfigurationUpdater();

  /**
   * Reference to the ProcessRequestSender (for requesting the XML config document). This reference is injected in the
   * Spring xml file, for ease of configuration.
   */
  @Autowired
  @Qualifier("primaryRequestSender")
  private ProcessRequestSender primaryRequestSender;

  /**
   * Request sender for disconnection notifications only. Can be null!
   */
  @Autowired(required = false)
  @Qualifier("secondaryRequestSender")
  private ProcessRequestSender secondaryRequestSender;

  /**
   * Map of data tag changers. It maps equipment id - > changer.
   */
  private Map<Long, IDataTagChanger> dataTagChangers = new ConcurrentHashMap<Long, IDataTagChanger>();
  /**
   * Map of command tag changers. It maps equipment id - > changer.
   */
  private Map<Long, ICommandTagChanger> commandTagChangers = new ConcurrentHashMap<Long, ICommandTagChanger>();
  /**
   * Map of equipment changers. It maps equipment id - > changer.
   */
  private Map<Long, IEquipmentConfigurationChanger> equipmentChangers = new ConcurrentHashMap<Long, IEquipmentConfigurationChanger>();
  /**
   * These are additional changers of the core. Which can be used to inform other parts of the core about changes.
   */
  private Map<Long, List<ICoreDataTagChanger>> coreDataTagChangers = new ConcurrentHashMap<Long, List<ICoreDataTagChanger>>();
  /**
   * These are additional changers of the core. Which can be used to inform other parts of the core about changes.
   */
  private Map<Long, List<ICoreCommandTagChanger>> coreCommandTagChangers = new ConcurrentHashMap<Long, List<ICoreCommandTagChanger>>();
  /**
   * The core equipment configuration changers.
   */
  private Map<Long, List<ICoreEquipmentConfigurationChanger>> coreEquipmentConfigurationChangers = new HashMap<Long, List<ICoreEquipmentConfigurationChanger>>();

  /**
   * The constructor of the configuration controller. It is auto wired of spring.
   * 
   * @param runOptions The run options.
   * @param commonConfiguration The common configuration.
   */
  @Autowired
  public ConfigurationController(final RunOptions runOptions, final CommonConfiguration commonConfiguration) {
    this.runOptions = runOptions;
    this.commonConfiguration = commonConfiguration;
  }

  /**
   * Loads configurations: Process PIK and Process Configuration. Catches RuntimeException coming from errors while
   * parsing PIK or Configurations XML files.
   */
  @PostConstruct
  public void initProcess() {
    // Catch spring Exceptions coming most likely from XML parsing
    try {

      // Connection

      // Only if noPIK is disable we ask for the PIK request
      if (!this.runOptions.isNoPIK()) {
        // Get the PIK from the server
        LOGGER.trace("initProcess - Process Connection called.");
        this.loadProcessConnection();
      } else {

        // Provisional ProcessConfiguration for calling createProcessConfiguration. It is done on
        // loadProcessConnection normally
        this.processConfiguration = new ProcessConfiguration();
        this.processConfiguration.setProcessName(getCommandParamsHandler().getParamValue(PROCESS_NAME_PARAM));
        this.processConfiguration.setprocessPIK(ProcessConfigurationRequest.NO_PIK);
      }

      // Configuration

      // If noPIK we call old Process Configuration method. Else the new one
      if (this.runOptions.isNoPIK()) {
        LOGGER.trace("initProcess - Old Process Configuration called.");
        this.old_loadProcessConfiguration();
      } else {
        LOGGER.trace("initProcess - Process Configuration called.");
        this.loadProcessConfiguration();
      }
    } catch (Exception ex) {
      LOGGER.error(ex);
      LOGGER.info("initProcess - Stopping the DAQ start up process...");
      System.exit(0);
    }
  }

  /**
   * Loads the Process Connection with the identification key (PIK) from the server. If the PIK request is rejected
   * from the server the start up process stops.
   */
  public void loadProcessConnection() {
    // Get the PIK from the server
    ProcessConnectionResponse processConnectionResponse = processConfigurationLoader.getProcessConnection();

    // If Process PIK is REJECTED we exit
    if (processConnectionResponse.getProcessPIK() == null
        || processConnectionResponse.getProcessPIK() <= ProcessConnectionResponse.PIK_REJECTED) {
      LOGGER.info("loadProcessConnection - PIK_REJECTED received!");
      LOGGER.info("loadProcessConnection - stopping the DAQ start up process...");
      System.exit(0);
    }

    // Set process PIK for future communications with the server (if it exists) in a provisional
    // ProcessConfiguration
    this.processConfiguration = new ProcessConfiguration();
    this.processConfiguration.setProcessName(processConnectionResponse.getProcessName());
    this.processConfiguration.setprocessPIK(processConnectionResponse.getProcessPIK());
  }

  /**
   * Loads the process configuration.
   */
  public void loadProcessConfiguration() {
    Document xmlConfiguration;
    ProcessConfigurationResponse processConfigurationResponse = null;
    LOGGER.trace("loadProcessConfiguration - Configuration process started");

    boolean localConfiguration = false;
    if (getCommandParamsHandler().hasParam(CONFIGURATION_PARAM)) {
      LOGGER.trace("loadProcessConfiguration - Taking Configuration from file");
      localConfiguration = true;
      String fileSystemLocation = getCommandParamsHandler().getParamValue(CONFIGURATION_PARAM);
      xmlConfiguration = this.processConfigurationLoader.fromFiletoDOC(fileSystemLocation);
    } else {
      LOGGER.trace("loadProcessConfiguration - Taking Configuration from remote server");
      processConfigurationResponse = this.processConfigurationLoader.getProcessConfiguration();

      // If Process Configuration is REJECTED we exit
      if (processConfigurationResponse.getConfigurationXML().equals(ProcessConfigurationResponse.CONF_REJECTED)) {
        LOGGER.error("loadProcessConfiguration - CONF_REJECTED received!");
        sendDisconnectionNotification();
        System.exit(0);
      }

      // processConfigurationResponse will never be null at this point
      xmlConfiguration = this.processConfigurationLoader.fromXMLtoDOC(processConfigurationResponse
          .getConfigurationXML());
    }

    // If XML Configuration is wrong and cannot be parsed we exit
    if (xmlConfiguration == null) {
      LOGGER.error("loadProcessConfiguration - Could not parse XML configuration");
      sendDisconnectionNotification();
      System.exit(0);
    }

    // Save config if it was the option and it is not local config (pointless)
    if (getCommandParamsHandler().hasParam(SAVE_PARAM)) {
      if (!localConfiguration) {
        saveConfiguration(xmlConfiguration);
      } else {
        LOGGER.info("loadProcessConfiguration - Local configuration will not be saved. It is already in local disk. ");
      }
    }

    LOGGER.debug("loadProcessConfiguration - Loading DAQ configuration properties from XML document...");

    // try to create process configuration object (with the PIK saved in the provisional ProcessConfiguration)
    try {
      this.processConfiguration = this.processConfigurationLoader.createProcessConfiguration(
          this.processConfiguration.getProcessName(), this.processConfiguration.getprocessPIK(),
          xmlConfiguration, localConfiguration);

      LOGGER.debug("loadProcessConfiguration - ... properties loaded successfully.");
    } catch (ConfUnknownTypeException ex) {
      LOGGER.info("loadProcessConfiguration - UNKNOWN configuration received!");
      sendDisconnectionNotification();
      System.exit(0);
    } catch (Exception ex) {
      LOGGER.fatal("loadProcessConfiguration - Exception caught while trying to configure the driver! Ex. message =", ex);
      LOGGER.info("loadProcessConfiguration - The ProcessConfiguration XML might contain some errors ! Check it please.");
      sendDisconnectionNotification();
      System.exit(0);
    }
  }

  /**
   * Saves the process configuration.
   */
  private void saveConfiguration(Document docXMLConfig) {
    String fileToSaveConf = getCommandParamsHandler().getParamValue(SAVE_PARAM);
    if (fileToSaveConf.length() > 0 && docXMLConfig != null) {
      LOGGER.info("saveConfiguration - saving the process configuration XML in a file " + fileToSaveConf
          + " due to user request");
      FileWriter fwr = null;
      try {
        fwr = new FileWriter(fileToSaveConf);
        // TODO default output format - check this works ok
        XMLSerializer serializer = new XMLSerializer(fwr, new OutputFormat());
        serializer.serialize(docXMLConfig);
        fwr.close();
      } catch (java.io.IOException ex) {
        LOGGER.error("saveConfiguration - Could not save the configuration to the file " + fileToSaveConf, ex);
      }
    }
  }

  /**
   * Sends disconnection notifications to all request senders.
   */
  private void sendDisconnectionNotification() {
    // If noPIK we call old Process Disconnection method. Else the new one
    if (this.runOptions.isNoPIK()) {
      old_sendDisconnectionNotification();
    } else {
      LOGGER.trace("sendDisconnectionNotification - Primary Request Sender disconnection");
      primaryRequestSender.sendProcessDisconnectionRequest();

      // send in separate thread as may block if broker problem
      if (secondaryRequestSender != null) {
        LOGGER.trace("sendDisconnectionNotification - Secondary Request Sender disconnection (new thread)");
        Thread disconnectSend = new Thread(new Runnable() {                  
          @Override
          public void run() {          
            secondaryRequestSender.sendProcessDisconnectionRequest();
          }
        });
        disconnectSend.setDaemon(true);
        disconnectSend.start();
      }
    }
  }


  /**
   * This is called if a data tag should be added to the configuration. It applies the changes to the core and calls
   * then the lower layer to also perform the changes.
   * 
   * @param dataTagAddChange The data tag add change.
   * @return A report with information if the change was successful.
   */
  public ChangeReport onDataTagAdd(final DataTagAdd dataTagAddChange) {
    LOGGER.debug("entering onDataTagAdd()");
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("changeId: " + dataTagAddChange.getChangeId());

    ChangeReport changeReport = new ChangeReport(dataTagAddChange);
    Long equipmentId = dataTagAddChange.getEquipmentId();
    SourceDataTag sourceDataTag = dataTagAddChange.getSourceDataTag();
    Long dataTagId = sourceDataTag.getId();
    Map<Long, SourceDataTag> sourceDataTags = getSourceDataTags(equipmentId);
    if (sourceDataTags == null) {
      LOGGER.warn("cannot add data tag - equipment id: " + dataTagAddChange.getEquipmentId() + " is unknown");
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    try {
      sourceDataTag.validate();
    } catch (ConfigurationException e) {
      changeReport.appendError("Error validating data tag");
      changeReport.appendError(StackTraceHelper.getStackTrace(e));
      return changeReport;
    }
    if (sourceDataTags.containsKey(dataTagId)) {

      LOGGER.warn("cannot add data tag id: " + dataTagId + " to equipment id: "
          + dataTagAddChange.getEquipmentId() + " This equipment already has tag with that id");

      changeReport.appendError("DataTag " + dataTagId + " is already in equipment " + equipmentId);
    } else {
      sourceDataTags.put(dataTagId, sourceDataTag);
      changeReport.appendInfo("Core added data tag with id " + sourceDataTag.getId()
          + " successfully to eqipment " + equipmentId);
      List<ICoreDataTagChanger> coreChangers = coreDataTagChangers.get(equipmentId);
      if (coreChangers != null) {
        for (ICoreDataTagChanger dataTagChanger : coreChangers) {
          dataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        }

        // register tag in the ValueChangeMonitorEngine if needed
        if (sourceDataTag.hasValueCheckMonitor()) {
          ValueChangeMonitor vcm = sourceDataTag.getValueCheckMonitor();
          ValueChangeMonitorEngine.getInstance().register(dataTagAddChange.getEquipmentId(), sourceDataTag,
              vcm);
        }

      }
      IDataTagChanger dataTagChanger = dataTagChangers.get(equipmentId);
      if (dataTagChanger != null) {
        dataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes"
            + "to the implementation part. No data tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    }
    return changeReport;
  }

  /**
   * This is called if a command tag should be added to the configuration. It applies the changes to the core and
   * calls then the lower layer to also perform the changes.
   * 
   * @param commandTagAddChange The command tag add change.
   * @return A report with information if the change was successful.
   */
  public ChangeReport onCommandTagAdd(final CommandTagAdd commandTagAddChange) {
    LOGGER.debug("entering onCommandTagAdd()");
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("changeId: " + commandTagAddChange.getChangeId());

    ChangeReport changeReport = new ChangeReport(commandTagAddChange);
    Long equipmentId = commandTagAddChange.getEquipmentId();
    Map<Long, SourceCommandTag> sourceCommandTags = getSourceCommandTags(equipmentId);
    if (sourceCommandTags == null) {
      LOGGER.warn("cannot add command tag - equipment id: " + commandTagAddChange.getEquipmentId()
          + " is unknown");
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    SourceCommandTag sourceCommandTag = commandTagAddChange.getSourceCommandTag();
    try {
      sourceCommandTag.validate();
    } catch (ConfigurationException e) {
      changeReport.appendError("Error validating command tag");
      changeReport.appendError(StackTraceHelper.getStackTrace(e));
      return changeReport;
    }
    Long commandTagId = sourceCommandTag.getId();
    if (sourceCommandTags.containsKey(commandTagId)) {

      LOGGER.warn("cannot add command tag id: " + commandTagId + " to equipment id: "
          + commandTagAddChange.getEquipmentId() + " This equipment already has tag with that id");

      changeReport.appendError("CommandTag " + commandTagId + " is already in equipment " + equipmentId);
    } else {
      sourceCommandTags.put(sourceCommandTag.getId(), sourceCommandTag);
      changeReport.appendInfo("Core added command tag with id " + sourceCommandTag.getId()
          + " successfully to equipment " + equipmentId);
      List<ICoreCommandTagChanger> coreChangers = coreCommandTagChangers.get(equipmentId);
      if (coreChangers != null) {
        for (ICoreCommandTagChanger commandTagChanger : coreChangers) {
          commandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        }
      }
      ICommandTagChanger commandTagChanger = commandTagChangers.get(equipmentId);
      if (commandTagChanger != null) {
        commandTagChanger.onAddCommandTag(sourceCommandTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes"
            + "to the implementation part. No command tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    }
    return changeReport;
  }

  /**
   * Removes a data tag from an equipment.
   * 
   * @param dataTagRemoveChange The change with all the data to remove the tag.
   * @return A change report with success information.
   */
  public ChangeReport onDataTagRemove(final DataTagRemove dataTagRemoveChange) {
    ChangeReport changeReport = new ChangeReport(dataTagRemoveChange);
    Long equipmentId = dataTagRemoveChange.getEquipmentId();
    Map<Long, SourceDataTag> sourceDataTags = getSourceDataTags(equipmentId);
    if (sourceDataTags == null) {
      changeReport.appendError("Equipment does not exist: " + equipmentId);
      return changeReport;
    }
    SourceDataTag sourceDataTag = sourceDataTags.remove(dataTagRemoveChange.getDataTagId());
    if (sourceDataTag != null) {
      changeReport.appendInfo("Core removed data tag with id " + dataTagRemoveChange.getDataTagId()
          + " successfully from equipment " + equipmentId);
      List<ICoreDataTagChanger> coreChangers = coreDataTagChangers.get(equipmentId);
      if (coreChangers != null) {
        for (ICoreDataTagChanger dataTagChanger : coreChangers) {
          dataTagChanger.onRemoveDataTag(sourceDataTag, changeReport);
        }

        // unregister tag from ValueChangeMonitorEngine (if it was previously registered)
        ValueChangeMonitorEngine.getInstance().unregister(sourceDataTag);
      }
      IDataTagChanger dataTagChanger = dataTagChangers.get(equipmentId);
      if (dataTagChanger != null) {
        dataTagChanger.onRemoveDataTag(sourceDataTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes"
            + "to the implementation part. No data tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    } else {
      // The data tag which should be removed was not found which means the same result as foudn and removed.
      changeReport.appendWarn("The data tag with id " + dataTagRemoveChange.getDataTagId()
          + " to remove was not found" + " in equipment with id " + equipmentId);
      changeReport.setState(CHANGE_STATE.SUCCESS);
    }
    return changeReport;
  }

  /**
   * Updates a data tag.
   * 
   * @param dataTagUpdateChange The object with all necessary to update the tag.
   * @return A change report containing information about the success of the update.
   */
  public ChangeReport onDataTagUpdate(final DataTagUpdate dataTagUpdateChange) {
    ChangeReport changeReport = new ChangeReport(dataTagUpdateChange);
    long equipmentId = dataTagUpdateChange.getEquipmentId();
    long dataTagId = dataTagUpdateChange.getDataTagId();
    Map<Long, SourceDataTag> sourceDataTags = getSourceDataTags(equipmentId);
    if (sourceDataTags == null) {
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    if (sourceDataTags.containsKey(dataTagId)) {
      try {
        SourceDataTag sourceDataTag = sourceDataTags.get(dataTagId);
        SourceDataTag oldSourceDataTag = sourceDataTag.clone();
        synchronized (sourceDataTag) {
          configurationUpdater.updateDataTag(dataTagUpdateChange, sourceDataTag);
        }
        try {
          sourceDataTag.validate();
        } catch (ConfigurationException e) {
          sourceDataTags.put(dataTagId, oldSourceDataTag);
          changeReport.appendError("Error validating data tag");
          changeReport.appendError(StackTraceHelper.getStackTrace(e));
          return changeReport;
        }
        changeReport.appendInfo("Core Data Tag update successfully applied.");
        IDataTagChanger dataTagChanger = dataTagChangers.get(equipmentId);
        dataTagChanger.onUpdateDataTag(sourceDataTag, oldSourceDataTag, changeReport);
        if (changeReport.getState().equals(CHANGE_STATE.SUCCESS)) {
          List<ICoreDataTagChanger> coreChangers = coreDataTagChangers.get(equipmentId);
          if (coreChangers != null) {
            // I do it here to avoid putting them back in the old state after an error
            for (ICoreDataTagChanger coreDataTagChanger : coreChangers) {
              coreDataTagChanger.onUpdateDataTag(sourceDataTag, oldSourceDataTag, changeReport);
            }
          }
          changeReport.appendInfo("Change fully applied.");
        } else {
          sourceDataTags.put(dataTagId, oldSourceDataTag);
        }
      } catch (Exception e) {
        changeReport.appendError("Error while applying data tag changes\n" + StackTraceHelper.getStackTrace(e));
      }
    } else {
      changeReport.appendError("Data Tag " + dataTagId + " to update was not found.");
    }
    return changeReport;
  }

  /**
   * Removes a command tag from an equipment.
   * 
   * @param commandTagRemoveChange The change object with all the information to remove the command tag.
   * @return A report with information about success of the change.
   */
  public ChangeReport onCommandTagRemove(final CommandTagRemove commandTagRemoveChange) {
    ChangeReport changeReport = new ChangeReport(commandTagRemoveChange);
    Long equipmentId = commandTagRemoveChange.getEquipmentId();
    Map<Long, SourceCommandTag> sourceCommandTags = getSourceCommandTags(equipmentId);
    if (sourceCommandTags == null) {
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    SourceCommandTag sourceCommandTag = sourceCommandTags.remove(commandTagRemoveChange.getCommandTagId());
    if (sourceCommandTag != null) {
      changeReport.appendInfo("Core removed command tag with id " + commandTagRemoveChange.getCommandTagId()
          + " successfully from equipment " + equipmentId);
      List<ICoreCommandTagChanger> coreChangers = coreCommandTagChangers.get(equipmentId);
      if (coreChangers != null) {
        for (ICoreCommandTagChanger commandTagChanger : coreChangers) {
          commandTagChanger.onRemoveCommandTag(sourceCommandTag, changeReport);
        }
      }
      ICommandTagChanger commandTagChanger = commandTagChangers.get(equipmentId);
      if (commandTagChanger != null) {
        commandTagChanger.onRemoveCommandTag(sourceCommandTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes"
            + " to the implementation part. No command tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    } else {
      // The command tag which should be removed was not found which means the same result as foudn and removed.
      changeReport.appendWarn("The command tag with id " + commandTagRemoveChange.getCommandTagId()
          + " to remove was not found" + " in equipment with id " + equipmentId);
      changeReport.setState(CHANGE_STATE.SUCCESS);
    }
    return changeReport;
  }

  /**
   * Updates a command tag.
   * 
   * @param commandTagUpdateChange The object with all the information to update the tag.
   * @return A change report with information about the success of the update.
   */
  public ChangeReport onCommandTagUpdate(final CommandTagUpdate commandTagUpdateChange) {
    ChangeReport changeReport = new ChangeReport(commandTagUpdateChange);
    long equipmentId = commandTagUpdateChange.getEquipmentId();
    long commandTagId = commandTagUpdateChange.getCommandTagId();
    Map<Long, SourceCommandTag> sourceCommandTags = getSourceCommandTags(equipmentId);
    if (sourceCommandTags == null) {
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    if (sourceCommandTags.containsKey(commandTagId)) {
      try {
        SourceCommandTag sourceCommandTag = sourceCommandTags.get(commandTagId);
        SourceCommandTag oldSourceCommandTag = sourceCommandTag.clone();
        synchronized (sourceCommandTag) {
          configurationUpdater.updateCommandTag(commandTagUpdateChange, sourceCommandTag);
          try {
            sourceCommandTag.validate();
          } catch (ConfigurationException e) {
            sourceCommandTags.put(commandTagId, oldSourceCommandTag);
            changeReport.appendError("Error validating command tag");
            changeReport.appendError(StackTraceHelper.getStackTrace(e));
            return changeReport;
          }
        }
        changeReport.appendInfo("Core Command Tag update successfully applied.");
        ICommandTagChanger commandTagChanger = commandTagChangers.get(equipmentId);
        commandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
        if (changeReport.getState().equals(CHANGE_STATE.SUCCESS)) {
          List<ICoreCommandTagChanger> coreChangers = coreCommandTagChangers.get(equipmentId);
          if (coreChangers != null) {
            for (ICoreCommandTagChanger coreCommandTagChanger : coreChangers) {
              coreCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag,
                  changeReport);
            }
          }
          changeReport.appendInfo("Change fully applied.");
        } else {
          sourceCommandTags.put(commandTagId, oldSourceCommandTag);
        }
      } catch (Exception e) {
        changeReport.appendError("Error while applying command tag changes: " + e.getMessage());
      }
    } else {
      changeReport.appendError("Command Tag " + commandTagId + " to update was not found.");
    }
    return changeReport;
  }

  /**
   * Updates the equipment configuration with the new values in the provided EquipmentConfigurationUpdate.
   * 
   * @param equipmentConfigurationUpdate The update with the changed values.
   * @return A change report with information about the success of the update.
   */
  public ChangeReport onEquipmentConfigurationUpdate(final EquipmentConfigurationUpdate equipmentConfigurationUpdate) {
    long equipmentId = equipmentConfigurationUpdate.getEquipmentId();
    ChangeReport changeReport = new ChangeReport(equipmentConfigurationUpdate);
    try {
      EquipmentConfiguration equipmentConfiguration = processConfiguration.getEquipmentConfiguration(equipmentId);
      if (equipmentConfiguration != null) {
        EquipmentConfiguration clonedEquipmentConfiguration = equipmentConfiguration.clone();
        synchronized (equipmentConfiguration) {
          configurationUpdater.updateEquipmentConfiguration(equipmentConfigurationUpdate,
              equipmentConfiguration);
        }
        IEquipmentConfigurationChanger equipmentConfigurationChanger = equipmentChangers.get(equipmentId);
        equipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration,
            clonedEquipmentConfiguration, changeReport);
        if (changeReport.getState().equals(CHANGE_STATE.SUCCESS)) {
          List<ICoreEquipmentConfigurationChanger> coreChangers = coreEquipmentConfigurationChangers
              .get(equipmentId);
          if (coreChangers != null) {
            for (ICoreEquipmentConfigurationChanger equipmentChanger : coreChangers) {
              equipmentChanger.onUpdateEquipmentConfiguration(equipmentConfiguration,
                  clonedEquipmentConfiguration, changeReport);
            }
          }
          // I do it here to avoid putting them back in the old state after an error
          changeReport.appendInfo("Change fully applied.");
        } else {
          processConfiguration.getEquipmentConfigurations().put(equipmentId, clonedEquipmentConfiguration);
        }
      } else {
        changeReport.appendError("Equipment configuration with id: " + equipmentId + " not found.");
      }
    } catch (Exception e) {
      changeReport.appendError("Error while applying equipment changes: " + e.getMessage());
    }
    return changeReport;
  }

  //
  // public EquipmentConfiguration createEquipmentConfiguration(EquipmentUnitAdd equipmentUnitAdd) {
  // return processConfigurationLoader.createEquipmentConfiguration(equipmentUnitAdd);
  // }

  /**
   * Updates the process configuration with the new values provided in the ProcessConfigurationUpdate.
   * 
   * @param processConfigurationUpdate The update with the changed values.
   * @return A change report with information about the success of the update.
   */
  public ChangeReport onProcessConfigurationUpdate(final ProcessConfigurationUpdate processConfigurationUpdate) {
    ChangeReport changeReport = new ChangeReport(processConfigurationUpdate);
    long processId = processConfigurationUpdate.getProcessId();
    try {
      if (processId == processConfiguration.getProcessID()) {
        synchronized (processConfiguration) {
          configurationUpdater.updateProcessConfiguration(processConfigurationUpdate, processConfiguration);
        }
        changeReport.appendInfo("Process with id " + processId + " successfully updated.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("The process id of this DAQ is " + processConfiguration.getProcessID()
            + " not " + processId + ".");
      }
    } catch (Exception e) {
      changeReport.appendError("Error while applying process changes: " + e.getMessage());
    }
    return changeReport;
  }

  /**
   * Gets the source command tags for a provided equipment id.
   * 
   * @param equipmentId The equipment id to get the source command tags.
   * @return The SourceCommandTags or null if the equipment does not exist.
   */
  private Map<Long, SourceCommandTag> getSourceCommandTags(final Long equipmentId) {
    Map<Long, EquipmentConfiguration> equipmentConfigurations = processConfiguration.getEquipmentConfigurations();
    EquipmentConfiguration equipmentConfiguration = equipmentConfigurations.get(equipmentId);
    Map<Long, SourceCommandTag> sourceCommandTags;
    if (equipmentConfiguration == null) {
      sourceCommandTags = null;
    } else {
      sourceCommandTags = equipmentConfiguration.getCommandTags();
    }
    return sourceCommandTags;
  }

  /**
   * Gets the source data tags for a provided equipment id.
   * 
   * @param equipmentId The equipment id to get the source command tags.
   * @return The SourceDataTags or null if the equipment does not exist.
   */
  private Map<Long, SourceDataTag> getSourceDataTags(final Long equipmentId) {
    Map<Long, EquipmentConfiguration> equipmentConfigurations = processConfiguration.getEquipmentConfigurations();
    EquipmentConfiguration equipmentConfiguration = equipmentConfigurations.get(equipmentId);
    Map<Long, SourceDataTag> sourceDataTags;
    if (equipmentConfiguration == null) {
      sourceDataTags = null;
    } else {
      sourceDataTags = equipmentConfiguration.getDataTags();
    }
    return sourceDataTags;
  }

  /**
   * gets the run options.
   * 
   * @return The run options of this process.
   */
  public RunOptions getRunOptions() {
    return runOptions;
  }

  /**
   * Gets the common configuration of this DAQ process.
   * 
   * @return The common configuration object.
   */
  public CommonConfiguration getCommonConfiguration() {
    return commonConfiguration;
  }

  /**
   * Sets the process configuration of this DAQ process.
   * 
   * @param processConfiguration The process configuration object.
   */
  public void setProcessConfiguration(final ProcessConfiguration processConfiguration) {
    this.processConfiguration = processConfiguration;
  }

  /**
   * Gets the process configuration of this DAQ process.
   * 
   * @return The process configuration object.
   */
  public ProcessConfiguration getProcessConfiguration() {
    return processConfiguration;
  }

  /**
   * Sets the process Configuration loader of this object.
   * 
   * @param processConfigurationLoader The process configuration loader.
   */
  public void setProcessConfigurationLoader(final ProcessConfigurationLoader processConfigurationLoader) {
    this.processConfigurationLoader = processConfigurationLoader;
  }

  /**
   * Gets the command parameter handler which holds the provided startup arguments.
   * 
   * @return The Command parameter handler.
   */
  public CommandParamsHandler getCommandParamsHandler() {
    return commandParamsHandler;
  }

  /**
   * Adds a core data tag changer to the configuration controller. This changers should never fail to maintain a
   * proper state of the DAQ.
   * 
   * @param equipmentId The equipment id to add the changer to.
   * @param dataTagChanger The changer to add.
   */
  public void addCoreDataTagChanger(final Long equipmentId, final ICoreDataTagChanger dataTagChanger) {
    List<ICoreDataTagChanger> changers = coreDataTagChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<ICoreDataTagChanger>();
      coreDataTagChangers.put(equipmentId, changers);
    }
    changers.add(dataTagChanger);
  }

  /**
   * Adds a core command tag changer to the configuration controller. This changers should never fail to maintain a
   * proper state of the DAQ.
   * 
   * @param equipmentId The equipment id to add the changer to.
   * @param commandTagChanger The changer to add.
   */
  public void addCoreCommandTagChanger(final Long equipmentId, final ICoreCommandTagChanger commandTagChanger) {
    List<ICoreCommandTagChanger> changers = coreCommandTagChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<ICoreCommandTagChanger>();
      coreCommandTagChangers.put(equipmentId, changers);
    }
    changers.add(commandTagChanger);
  }

  /**
   * Adds a core equipment configuration changer to the controller.
   * 
   * @param equipmentId The equipment id to add the controller to.
   * @param coreEquipmentConfigurationChanger The changer to add.
   */
  public void addCoreEquipmentConfigurationChanger(final long equipmentId,
      final ICoreEquipmentConfigurationChanger coreEquipmentConfigurationChanger) {
    List<ICoreEquipmentConfigurationChanger> changers = coreEquipmentConfigurationChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<ICoreEquipmentConfigurationChanger>();
      coreEquipmentConfigurationChangers.put(equipmentId, changers);
    }
    changers.add(coreEquipmentConfigurationChanger);
  }

  /**
   * Puts an implementation command tag changer to this controller. There can only be one per equipment.
   * 
   * @param equipmentId The equipment id to add the changer to.
   * @param commandTagChanger The changer to add.
   */
  public void putImplementationCommandTagChanger(final long equipmentId, final ICommandTagChanger commandTagChanger) {
    // if null is passed, remove the existing changer for given equipment (if exist)
    if (commandTagChanger == null)
      commandTagChangers.remove(equipmentId);
    else
      commandTagChangers.put(equipmentId, commandTagChanger);
  }

  /**
   * Puts an implementation data tag changer to this controller. There can only be one per equipment.
   * 
   * @param equipmentId The equipment id to add the changer to.
   * @param dataTagChanger The changer to add.
   */
  public void putImplementationDataTagChanger(final long equipmentId, final IDataTagChanger dataTagChanger) {
    // if null is passed, remove the existing changer for given equipment (if exist)
    if (dataTagChanger == null)
      dataTagChangers.remove(equipmentId);
    else
      dataTagChangers.put(equipmentId, dataTagChanger);
  }

  /**
   * Puts an implementation equipment configuration changer to this controller. There can only be one per equipment.
   * 
   * @param equipmentId The equipment id to add the changer to.
   * @param equipmentConfigurationChanger The changer to add.
   */
  public void putImplementationEquipmentConfigurationChanger(final long equipmentId,
      final IEquipmentConfigurationChanger equipmentConfigurationChanger) {
    equipmentChangers.put(equipmentId, equipmentConfigurationChanger);
  }

  /**
   * Gets the equipment configuration.
   * 
   * @param equipmentId The id of the equipment configuration.
   * @return The equipment configuration with the provided id or null if there is none.
   */
  public EquipmentConfiguration getEquipmentConfiguration(final Long equipmentId) {
    return getProcessConfiguration().getEquipmentConfiguration(equipmentId);
  }

  /**
   * Searches a data tag with the provided id and returns the first found.
   * 
   * @param dataTagId The data tag id to search for.
   * @return The first found data tag or null if none is found.
   */
  public ISourceDataTag findDataTag(final Long dataTagId) {
    Map<Long, EquipmentConfiguration> equipmentMap = getProcessConfiguration().getEquipmentConfigurations();
    for (EquipmentConfiguration equipmentConfiguration : equipmentMap.values()) {
      if (equipmentConfiguration.hasSourceDataTag(dataTagId)) {
        return equipmentConfiguration.getSourceDataTag(dataTagId);
      }
    }
    return null;
  }

  /**
   * Searches a command tag with the provided id and returns the first found.
   * 
   * @param commandTagId The command tag id to search for.
   * @return The first found command tag or null if none is found.
   */
  public ISourceCommandTag findCommandTag(final Long commandTagId) {
    Map<Long, EquipmentConfiguration> equipmentMap = getProcessConfiguration().getEquipmentConfigurations();
    for (EquipmentConfiguration equipmentConfiguration : equipmentMap.values()) {
      if (equipmentConfiguration.hasSourceCommandTag(commandTagId)) {
        return equipmentConfiguration.getSourceCommandTag(commandTagId);
      }
    }
    return null;
  }

  /**
   * @param commandParamsHandler the commandParamsHandler to set
   */
  public void setCommandParamsHandler(final CommandParamsHandler commandParamsHandler) {
    this.commandParamsHandler = commandParamsHandler;
  }

  /**
   * TODO: Backward compatibility. remove after updating server
   */

  /**
   * Loads the old process configuration.
   */
  public void old_loadProcessConfiguration() {
    Document xmlConfiguration;
    boolean localConfiguration = false;
    if (getCommandParamsHandler().hasParam(CONFIGURATION_PARAM)) {
      localConfiguration = true;
      String fileSystemLocation = getCommandParamsHandler().getParamValue(CONFIGURATION_PARAM);
      xmlConfiguration = processConfigurationLoader.loadConfigLocal(fileSystemLocation);
    } else {
      if (getCommandParamsHandler().hasParam(SAVE_PARAM)) {
        String saveLocation = getCommandParamsHandler().getParamValue(SAVE_PARAM);
        xmlConfiguration = processConfigurationLoader.loadConfigRemote(saveLocation);
      } else {
        xmlConfiguration = processConfigurationLoader.loadConfigRemote();
      }
    }
    LOGGER.debug("Loading DAQ configuration properties from XML document...");
    String processName = getCommandParamsHandler().getParamValue(PROCESS_NAME_PARAM);
    // try to create process configuration object.
    try {
      processConfiguration = processConfigurationLoader.createProcessConfiguration(processName,
          this.processConfiguration.getprocessPIK(), xmlConfiguration, localConfiguration);

      LOGGER.debug("... properties loaded successfully.");
    } catch (ConfUnknownTypeException ex) {
      LOGGER.info("UNKNOWN configuration received!");
      LOGGER.info("Sending ProcessDisconnection message and terminating..");
      processConfiguration = new ProcessConfiguration();
      processConfiguration.setProcessName(processName);
      processConfiguration.setProcessID(-1L);
      old_sendDisconnectionNotification();
      System.exit(0);
    } catch (ConfRejectedTypeException ex) {
      LOGGER.info("REJECTED configuration received!");
      LOGGER.info("Sending ProcessDisconnection message and terminating..");
      processConfiguration = new ProcessConfiguration();
      processConfiguration.setProcessName(processName);
      processConfiguration.setProcessID(-1L);
      // TODO remove this as would invalidate tags of DAQ already running?
      old_sendDisconnectionNotification();
      System.exit(0);
    } catch (Exception ex) {
      LOGGER.fatal("Exception caught while trying to configure the driver! Ex. message =", ex);
      LOGGER.info("The ProcessConfiguration XML might contain some errors ! Check it please.");
      LOGGER.info("Sending ProcessDisconnecttion message and terminating..");
      processConfiguration = new ProcessConfiguration();
      processConfiguration.setProcessName(processName);
      processConfiguration.setProcessID(-1L);
      old_sendDisconnectionNotification();
      System.exit(0);
    }
  }

  /**
   * Sends disconnection notifications to all request senders.
   */
  private void old_sendDisconnectionNotification() {
    LOGGER.trace("old_sendDisconnectionNotification - Primary Request Sender disconnection");
    primaryRequestSender.old_sendProcessDisconnection();
    // send in separate thread as may block if broker problem
    if (secondaryRequestSender != null) {
      LOGGER.trace("old_sendDisconnectionNotification - Secondary Request Sender disconnection (new Thread)");
      Thread disconnectSend = new Thread(new Runnable() {                  
        @Override
        public void run() {          
          secondaryRequestSender.old_sendProcessDisconnection();
        }
      });
      disconnectSend.setDaemon(true);
      disconnectSend.start();
    }
  }

}
