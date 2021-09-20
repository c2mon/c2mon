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
package cern.c2mon.daq.common.conf.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import cern.c2mon.daq.common.conf.equipment.*;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.timer.FreshnessMonitor;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.daq.tools.StackTraceHelper;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;
import cern.c2mon.shared.daq.config.*;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.process.ProcessConfigurationResponse;
import cern.c2mon.shared.daq.process.ProcessConnectionResponse;

/**
 * The ConfigurationController managing the configuration life cycle and allows
 * to access and change the current configuration.
 *
 * @author Andreas Lang
 * @author vilches (refactoring updates)
 */
@Slf4j
@Component("configurationController")
public class ConfigurationController {

  @Autowired
  private DaqProperties properties;

  @Setter
  @Autowired
  FreshnessMonitor freshnessMonitor;

  private long startUp;

  /**
   * The process configuration loader used at startup.
   */
  @Autowired
  private ProcessConfigurationLoader processConfigurationLoader;

  /**
   * The updater which applies changes to source data tags.
   */
  private final ConfigurationUpdater configurationUpdater = new ConfigurationUpdater();

  /**
   * Reference to the ProcessRequestSender (for requesting the XML config
   * document). This reference is injected in the Spring xml file, for ease of
   * configuration.
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

  @Autowired
  private EquipmentConfigurationFactory equipmentConfigurationFactory;

  /**
   * Map of data tag changers. It maps equipment id - > changer.
   */
  private final Map<Long, IDataTagChanger> dataTagChangers = new ConcurrentHashMap<>();
  /**
   * Map of command tag changers. It maps equipment id - > changer.
   */
  private final  Map<Long, ICommandTagChanger> commandTagChangers = new ConcurrentHashMap<>();
  /**
   * Map of equipment changers. It maps equipment id - > changer.
   */
  private final Map<Long, IEquipmentConfigurationChanger> equipmentChangers = new ConcurrentHashMap<>();
  /**
   * These are additional changers of the core. Which can be used to inform
   * other parts of the core about changes.
   */
  private final Map<Long, List<ICoreDataTagChanger>> coreDataTagChangers = new ConcurrentHashMap<>();
  /**
   * These are additional changers of the core. Which can be used to inform
   * other parts of the core about changes.
   */
  private final Map<Long, List<ICoreCommandTagChanger>> coreCommandTagChangers = new ConcurrentHashMap<>();
  /**
   * The core equipment configuration changers.
   */
  private final Map<Long, List<ICoreEquipmentConfigurationChanger>> coreEquipmentConfigurationChangers = new HashMap<>();

  /**
   * Loads configurations: Process PIK and Process Configuration. Catches
   * RuntimeException coming from errors while parsing PIK or Configurations XML
   * files.
   */
  public void initProcess() {
    this.startUp = System.currentTimeMillis();

    try {
      // Get the PIK from the server
      log.trace("initProcess - Process Connection called.");
      this.loadProcessConnection();

      // Configuration

      log.trace("initProcess - Process Configuration called.");
      this.loadProcessConfiguration();

    } catch (Exception ex) {
      throw new RuntimeException("Exception caught during DAQ startup", ex);
    }
  }

  /**
   * Loads the Process Connection with the identification key (PIK) from the
   * server. If the PIK request is rejected from the server the start up process
   * stops.
   */
  public void loadProcessConnection() {
    // Get the PIK from the server
    ProcessConnectionResponse processConnectionResponse = processConfigurationLoader.getProcessConnection();

    // If Process PIK is REJECTED we exit
    if (processConnectionResponse.getProcessPIK() == null || processConnectionResponse.getProcessPIK() <= ProcessConnectionResponse.PIK_REJECTED) {
      System.err.println("\nConnection rejected for process " + processConnectionResponse.getProcessName() +
              ": Either the process is already running or it didn't shut down cleanly. " +
              "Please stop the existing process or wait until its heartbeat expires.\n");
      System.exit(1);
    }

    // Set process PIK for future communications with the server (if it exists)
    // in a provisional ProcessConfiguration
    ProcessConfiguration processConfiguration = new ProcessConfiguration();
    processConfiguration.setProcessName(processConnectionResponse.getProcessName());
    processConfiguration.setprocessPIK(processConnectionResponse.getProcessPIK());

    ProcessConfigurationHolder.setInstance(processConfiguration);
  }

  /**
   * Loads the process configuration.
   */
  public void loadProcessConfiguration() {
    Document xmlConfiguration;
    ProcessConfigurationResponse processConfigurationResponse = null;
    log.trace("Configuration process started");

    String localConfigFile = properties.getLocalConfigFile();
    // e.g. $DAQ_HOME/conf/local/P_TEST.xml
    File defaultLocalConfigFile = new File(System.getProperty("user.dir") + "/conf/local/" + properties.getName().toUpperCase() + ".xml");

    boolean localConfiguration = true;

    if (localConfigFile != null) {
      xmlConfiguration = loadFromLocalConfigFile(localConfigFile);
    }
    else if (defaultLocalConfigFile.exists()) {
      xmlConfiguration = loadFromLocalConfigFile(defaultLocalConfigFile.toString());
    }
    else {
      localConfiguration = false;
      log.info("Loading configuration from server");
      processConfigurationResponse = this.processConfigurationLoader.getProcessConfiguration();

      // If Process Configuration is REJECTED we exit
      if (processConfigurationResponse.getConfigurationXML().equals(ProcessConfigurationResponse.CONF_REJECTED)) {
        sendDisconnectionNotification();
        throw new RuntimeException("CONF_REJECTED received");
      }

      // processConfigurationResponse will never be null at this point
      xmlConfiguration = this.processConfigurationLoader.fromXMLtoDOC(processConfigurationResponse.getConfigurationXML());
    }

    // If XML Configuration is wrong and cannot be parsed we exit
    if (xmlConfiguration == null) {
      sendDisconnectionNotification();
      throw new RuntimeException("Could not parse XML configuration");
    }

    // Save config if it was the option and it is not local config (pointless)
    if (properties.getSaveRemoteConfig() != null) {
      if (!localConfiguration) {
        saveConfiguration(xmlConfiguration);
      } else {
        log.info("Local configuration will not be saved. It is already in local disk");
      }
    }

    log.debug("Loading DAQ configuration properties from XML document...");

    // try to create process configuration object (with the PIK saved in the
    // provisional ProcessConfiguration)
    try {
      ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();
      configuration = this.processConfigurationLoader.createProcessConfiguration(configuration.getProcessName(),
          configuration.getprocessPIK(), xmlConfiguration);
      ProcessConfigurationHolder.setInstance(configuration);

      log.debug("Process configuration successfully loaded");
    } catch (ConfUnknownTypeException ex) {
      sendDisconnectionNotification();
      throw new RuntimeException("UNKNOWN configuration received");

    } catch (Exception ex) {
      sendDisconnectionNotification();
      throw new RuntimeException("Exception caught while configuring the DAQ. Check the configuration XML", ex);
    }
  }

  /**
   * Helper method to load configuration from local file
   */
  private Document loadFromLocalConfigFile(String localConfigFile) {
    log.info("Loading configuration from file: {}", localConfigFile);
    return this.processConfigurationLoader.fromFiletoDOC(localConfigFile);
  }

  /**
   * Saves the process configuration.
   */
  private void saveConfiguration(Document docXMLConfig) {
    String fileToSaveConf = properties.getSaveRemoteConfig();
    if (fileToSaveConf.length() > 0 && docXMLConfig != null) {
      log.info("saveConfiguration - saving the process configuration XML in a file " + fileToSaveConf + " due to user request");

      File file = new File(fileToSaveConf);
      if (file.isDirectory() || !fileToSaveConf.endsWith(".xml")) {
        throw new RuntimeException("Path to which to save remote config must end with '.xml'");
      }

      try {
        DOMImplementationLS domImplementation = (DOMImplementationLS) docXMLConfig.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        lsSerializer.writeToURI(docXMLConfig, file.toURI().toURL().toString());
      } catch (java.io.IOException ex) {
        log.error("saveConfiguration - Could not save the configuration to the file " + fileToSaveConf, ex);
      }
    }
  }

  /**
   * Sends disconnection notifications to all request senders.
   */
  private void sendDisconnectionNotification() {
    log.trace("sendDisconnectionNotification - Primary Request Sender disconnection");
    primaryRequestSender.sendProcessDisconnectionRequest(ProcessConfigurationHolder.getInstance(), startUp);

    // send in separate thread as may block if broker problem
    if (secondaryRequestSender != null) {
      log.trace("sendDisconnectionNotification - Secondary Request Sender disconnection (new thread)");
      Thread disconnectSend = new Thread(new Runnable() {
        @Override
        public void run() {
          secondaryRequestSender.sendProcessDisconnectionRequest(ProcessConfigurationHolder.getInstance(), startUp);
        }
      });
      disconnectSend.setDaemon(true);
      disconnectSend.start();
    }
  }

  /**
   * This is called if a data tag should be added to the configuration. It
   * applies the changes to the core and calls then the lower layer to also
   * perform the changes.
   *
   * @param dataTagAddChange The data tag add change.
   *
   * @return A report with information if the change was successful.
   */
  public synchronized ChangeReport onDataTagAdd(final DataTagAdd dataTagAddChange) {
    log.debug("onDataTagAdd - entering onDataTagAdd()");
    log.debug("changeId: " + dataTagAddChange.getChangeId());

    ChangeReport changeReport = new ChangeReport(dataTagAddChange);
    Long equipmentId = dataTagAddChange.getEquipmentId();

    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    // Check if the equipment id is a SubEquipment id.
    if (!configuration.getEquipmentConfigurations().containsKey(equipmentId)) {
      for (EquipmentConfiguration equipmentConfiguration : configuration.getEquipmentConfigurations().values()) {
        if (equipmentConfiguration.getSubEquipmentConfigurations().containsKey(equipmentId)) {
          equipmentId = equipmentConfiguration.getId();
        }
      }
    }

    SourceDataTag sourceDataTag = dataTagAddChange.getSourceDataTag();
    Long dataTagId = sourceDataTag.getId();
    Map<Long, SourceDataTag> sourceDataTags = getSourceDataTags(equipmentId);
    if (sourceDataTags == null) {
      log.warn("cannot add data tag - equipment id: " + dataTagAddChange.getEquipmentId() + " is unknown");
      changeReport.appendError("Equipment does not exist: " + equipmentId);
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

      log.warn("onDataTagAdd - cannot add data tag id: " + dataTagId + " to equipment id: " + dataTagAddChange.getEquipmentId() + " This equipment already" +
          " has tag with that id");

      changeReport.appendError("DataTag " + dataTagId + " is already in equipment " + equipmentId);
    } else {
      sourceDataTags.put(dataTagId, sourceDataTag);
      changeReport.appendInfo("Core added data tag with id " + sourceDataTag.getId() + " successfully to equipment " + equipmentId);
      List<ICoreDataTagChanger> coreChangers = coreDataTagChangers.get(equipmentId);
      if (coreChangers != null) {
        for (ICoreDataTagChanger dataTagChanger : coreChangers) {
          dataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        }
      }
      IDataTagChanger dataTagChanger = dataTagChangers.get(equipmentId);
      if (dataTagChanger != null) {
        dataTagChanger.onAddDataTag(sourceDataTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes" + "to the implementation part. No data tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    }
    log.debug("onDataTagAdd - exiting onDataTagAdd()");
    return changeReport;
  }

  /**
   * This is called if a command tag should be added to the configuration. It
   * applies the changes to the core and calls then the lower layer to also
   * perform the changes.
   *
   * @param commandTagAddChange The command tag add change.
   *
   * @return A report with information if the change was successful.
   */
  public synchronized ChangeReport onCommandTagAdd(final CommandTagAdd commandTagAddChange) {
    log.debug("entering onCommandTagAdd()");
    log.debug("changeId: " + commandTagAddChange.getChangeId());

    ChangeReport changeReport = new ChangeReport(commandTagAddChange);
    Long equipmentId = commandTagAddChange.getEquipmentId();

    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    // Check if the equipment id is a SubEquipment id.
    if (!configuration.getEquipmentConfigurations().containsKey(equipmentId)) {
      for (EquipmentConfiguration equipmentConfiguration : configuration.getEquipmentConfigurations().values()) {
        if (equipmentConfiguration.getSubEquipmentConfigurations().containsKey(equipmentId)) {
          equipmentId = equipmentConfiguration.getId();
        }
      }
    }

    Map<Long, SourceCommandTag> sourceCommandTags = getSourceCommandTags(equipmentId);
    if (sourceCommandTags == null) {
      log.warn("cannot add command tag - equipment id: " + commandTagAddChange.getEquipmentId() + " is unknown");
      changeReport.appendError("Equipment does not exist: " + equipmentId);
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

      log.warn("cannot add command tag id: " + commandTagId + " to equipment id: " + commandTagAddChange.getEquipmentId() + " This equipment already has " +
          "tag with that id");

      changeReport.appendError("CommandTag " + commandTagId + " is already in equipment " + equipmentId);
    } else {
      sourceCommandTags.put(sourceCommandTag.getId(), sourceCommandTag);
      changeReport.appendInfo("Core added command tag with id " + sourceCommandTag.getId() + " successfully to equipment " + equipmentId);
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
        changeReport.appendError("It was not possible to apply the changes" + "to the implementation part. No command tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    }
    return changeReport;
  }

  /**
   * Removes a data tag from an equipment.
   *
   * @param dataTagRemoveChange The change with all the data to remove the tag.
   *
   * @return A change report with success information.
   */
  public synchronized ChangeReport onDataTagRemove(final DataTagRemove dataTagRemoveChange) {
    log.debug("Entering onDataTagRemove: ");

    ChangeReport changeReport = new ChangeReport(dataTagRemoveChange);
    Long equipmentId = dataTagRemoveChange.getEquipmentId();
    Map<Long, SourceDataTag> sourceDataTags = getSourceDataTags(equipmentId);
    if (sourceDataTags == null) {
      changeReport.appendError("Equipment does not exist: " + equipmentId);
      return changeReport;
    }

    log.debug("onDataTagRemove - removing " + dataTagRemoveChange.getDataTagId());
    SourceDataTag sourceDataTag = sourceDataTags.get(dataTagRemoveChange.getDataTagId());

    if (sourceDataTag != null) {
      log.debug("onDataTagRemove - Core removed data tag with id " + dataTagRemoveChange.getDataTagId() + " successfully from equipment " + equipmentId);
      changeReport.appendInfo("Core removed data tag with id " + dataTagRemoveChange.getDataTagId() + " successfully from equipment " + equipmentId);
      List<ICoreDataTagChanger> coreChangers = coreDataTagChangers.get(equipmentId);

      if (coreChangers != null) {
        for (ICoreDataTagChanger dataTagChanger : coreChangers) {
          dataTagChanger.onRemoveDataTag(sourceDataTag, changeReport);
        }

      }
      IDataTagChanger dataTagChanger = dataTagChangers.get(equipmentId);
      if (dataTagChanger != null) {
        dataTagChanger.onRemoveDataTag(sourceDataTag, changeReport);
        // changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("It was not possible to apply the changes" + "to the implementation part. No data tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }

      freshnessMonitor.removeDataTag(sourceDataTag);

      // remove the tag from the core's map
      sourceDataTags.remove(dataTagRemoveChange.getDataTagId());

    } else {
      log.debug("onDataTagRemove - The data tag with id " + dataTagRemoveChange.getDataTagId() + " to remove was not found" + " in equipment with id " +
          equipmentId);
      // The data tag which should be removed was not found which means the same
      // result as foudn and removed.
      changeReport.appendWarn("The data tag with id " + dataTagRemoveChange.getDataTagId() + " to remove was not found" + " in equipment with id " +
          equipmentId);
      changeReport.setState(CHANGE_STATE.SUCCESS);
    }

    log.debug("Exiting onDataTagRemove: ");

    return changeReport;
  }

  /**
   * Updates a data tag.
   *
   * @param dataTagUpdateChange The object with all necessary to update the tag.
   *
   * @return A change report containing information about the success of the
   * update.
   */
  public synchronized ChangeReport onDataTagUpdate(final DataTagUpdate dataTagUpdateChange) {
    ChangeReport changeReport = new ChangeReport(dataTagUpdateChange);
    long equipmentId = dataTagUpdateChange.getEquipmentId();

    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    // Check if the equipment id is a SubEquipment id.
    if (!configuration.getEquipmentConfigurations().containsKey(equipmentId)) {
      for (EquipmentConfiguration equipmentConfiguration : configuration.getEquipmentConfigurations().values()) {
        if (equipmentConfiguration.getSubEquipmentConfigurations().containsKey(equipmentId)) {
          equipmentId = equipmentConfiguration.getId();
        }
      }
    }

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
          changeReport.appendInfo("Core Data Tag update successfully applied.");
          changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (ConfigurationException e) {
          sourceDataTags.put(dataTagId, oldSourceDataTag);
          changeReport.appendError("Error validating data tag");
          changeReport.appendError(StackTraceHelper.getStackTrace(e));
          return changeReport;
        }
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
   * @param commandTagRemoveChange The change object with all the information to
   *                               remove the command tag.
   *
   * @return A report with information about success of the change.
   */
  public synchronized ChangeReport onCommandTagRemove(final CommandTagRemove commandTagRemoveChange) {
    ChangeReport changeReport = new ChangeReport(commandTagRemoveChange);
    Long equipmentId = commandTagRemoveChange.getEquipmentId();
    Map<Long, SourceCommandTag> sourceCommandTags = getSourceCommandTags(equipmentId);
    if (sourceCommandTags == null) {
      changeReport.appendError("Equipment does not exists: " + equipmentId);
      return changeReport;
    }
    SourceCommandTag sourceCommandTag = sourceCommandTags.remove(commandTagRemoveChange.getCommandTagId());
    if (sourceCommandTag != null) {
      changeReport.appendInfo("Core removed command tag with id " + commandTagRemoveChange.getCommandTagId() + " successfully from equipment " + equipmentId);
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
        changeReport.appendError("It was not possible to apply the changes" + " to the implementation part. No command tag changer was found.");
        changeReport.setState(CHANGE_STATE.REBOOT);
      }
    } else {
      // The command tag which should be removed was not found which means the
      // same result as foudn and removed.
      changeReport.appendWarn("The command tag with id " + commandTagRemoveChange.getCommandTagId() + " to remove was not found" + " in equipment with id " +
          equipmentId);
      changeReport.setState(CHANGE_STATE.SUCCESS);
    }
    return changeReport;
  }

  /**
   * Updates a command tag.
   *
   * @param commandTagUpdateChange The object with all the information to update
   *                               the tag.
   *
   * @return A change report with information about the success of the update.
   */
  public synchronized ChangeReport onCommandTagUpdate(final CommandTagUpdate commandTagUpdateChange) {
    ChangeReport changeReport = new ChangeReport(commandTagUpdateChange);
    long equipmentId = commandTagUpdateChange.getEquipmentId();
    long commandTagId = commandTagUpdateChange.getCommandTagId();

    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    // Check if the equipment id is a SubEquipment id.
    if (!configuration.getEquipmentConfigurations().containsKey(equipmentId)) {
      for (EquipmentConfiguration equipmentConfiguration : configuration.getEquipmentConfigurations().values()) {
        if (equipmentConfiguration.getSubEquipmentConfigurations().containsKey(equipmentId)) {
          equipmentId = equipmentConfiguration.getId();
        }
      }
    }

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
              coreCommandTagChanger.onUpdateCommandTag(sourceCommandTag, oldSourceCommandTag, changeReport);
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
   * Updates the equipment configuration with the new values in the provided
   * EquipmentConfigurationUpdate.
   *
   * @param equipmentConfigurationUpdate The update with the changed values.
   *
   * @return A change report with information about the success of the update.
   */
  public synchronized ChangeReport onEquipmentConfigurationUpdate(final EquipmentConfigurationUpdate equipmentConfigurationUpdate) {
    long equipmentId = equipmentConfigurationUpdate.getEquipmentId();
    ChangeReport changeReport = new ChangeReport(equipmentConfigurationUpdate);
    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    try {
      EquipmentConfiguration equipmentConfiguration = configuration.getEquipmentConfiguration(equipmentId);
      if (equipmentConfiguration != null) {
        EquipmentConfiguration clonedEquipmentConfiguration = equipmentConfiguration.clone();
        synchronized (equipmentConfiguration) {
          configurationUpdater.updateEquipmentConfiguration(equipmentConfigurationUpdate, equipmentConfiguration);
        }
        IEquipmentConfigurationChanger equipmentConfigurationChanger = equipmentChangers.get(equipmentId);
        equipmentConfigurationChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, clonedEquipmentConfiguration, changeReport);
        if (changeReport.getState().equals(CHANGE_STATE.SUCCESS)) {
          List<ICoreEquipmentConfigurationChanger> coreChangers = coreEquipmentConfigurationChangers.get(equipmentId);
          if (coreChangers != null) {
            for (ICoreEquipmentConfigurationChanger equipmentChanger : coreChangers) {
              equipmentChanger.onUpdateEquipmentConfiguration(equipmentConfiguration, clonedEquipmentConfiguration, changeReport);
            }
          }
          // I do it here to avoid putting them back in the old state after an
          // error
          changeReport.appendInfo("Change fully applied.");
        } else {
          configuration.getEquipmentConfigurations().put(equipmentId, clonedEquipmentConfiguration);
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
  // public EquipmentConfiguration createEquipmentConfiguration(EquipmentUnitAdd
  // equipmentUnitAdd) {
  // return
  // processConfigurationLoader.createEquipmentConfiguration(equipmentUnitAdd);
  // }

  /**
   * Updates the process configuration with the new values provided in the
   * ProcessConfigurationUpdate.
   *
   * @param processConfigurationUpdate The update with the changed values.
   *
   * @return A change report with information about the success of the update.
   */
  public synchronized ChangeReport onProcessConfigurationUpdate(final ProcessConfigurationUpdate processConfigurationUpdate) {
    ChangeReport changeReport = new ChangeReport(processConfigurationUpdate);
    long processId = processConfigurationUpdate.getProcessId();
    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    try {
      if (processId == configuration.getProcessID()) {
        synchronized (configuration) {
          configurationUpdater.updateProcessConfiguration(processConfigurationUpdate, configuration);
        }
        changeReport.appendInfo("Process with id " + processId + " successfully updated.");
        changeReport.setState(CHANGE_STATE.SUCCESS);
      } else {
        changeReport.appendError("The process id of this DAQ is " + configuration.getProcessID() + " not " + processId + ".");
      }
    } catch (Exception e) {
      changeReport.appendError("Error while applying process changes: " + e.getMessage());
    }
    return changeReport;
  }

  /**
   * Updates the DAQ by removing a whole SubEquipment.
   *
   * @param subEquipmentUnitRemove the subequipment unit to be removed
   *
   * @return a change report with information about the success of the update.
   */
  public ChangeReport onSubEquipmentUnitRemove(SubEquipmentUnitRemove subEquipmentUnitRemove) {
    log.debug("onSubEquipmentUnitRemove - entering onSubEquipmentUnitRemove()..");
    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    ChangeReport changeReport = new ChangeReport(subEquipmentUnitRemove);
    changeReport.setState(CHANGE_STATE.SUCCESS);

    // Check if the parent equipment exists
    EquipmentConfiguration parentEquipmentConfiguration = configuration.getEquipmentConfiguration(subEquipmentUnitRemove.getParentEquipmentId());
    if (parentEquipmentConfiguration == null) {
      changeReport.appendError("Parent Equipment unit id: " + subEquipmentUnitRemove.getParentEquipmentId() + " for SubEquipment unit " +
          subEquipmentUnitRemove.getSubEquipmentId() + " is unknown");
      changeReport.setState(CHANGE_STATE.FAIL);
      return changeReport;
    }

    // Find the SubEquipment configuration
    SubEquipmentConfiguration subEquipmentConfiguration = parentEquipmentConfiguration.getSubEquipmentConfiguration(subEquipmentUnitRemove.getSubEquipmentId());
    if (subEquipmentConfiguration == null) {
      changeReport.appendWarn("SubEquipment unit id: " + subEquipmentUnitRemove.getSubEquipmentId() + " is unknown");
    } else {
      parentEquipmentConfiguration.getSubEquipmentConfigurations().remove(subEquipmentConfiguration.getId());
    }

    return changeReport;
  }

  /**
   * Updates the DAQ by injecting a new SubEquipment Unit.
   *
   * @param subEquipmentUnitAdd the newly injected sub equipment unit
   *
   * @return a change report with information about the success of the update.
   */

  public ChangeReport onSubEquipmentUnitAdd(final SubEquipmentUnitAdd subEquipmentUnitAdd) {
    log.debug("onSubEquipmentUnitAdd - entering onSubEquipmentUnitAdd()..");
    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();

    ChangeReport changeReport = new ChangeReport(subEquipmentUnitAdd);
    changeReport.setState(CHANGE_STATE.SUCCESS);

    // Check if the parent equipment exists
    EquipmentConfiguration parentEquipmentConfiguration = configuration.getEquipmentConfiguration(subEquipmentUnitAdd.getParentEquipmentId());
    if (parentEquipmentConfiguration == null) {
      changeReport.appendError("Parent Equipment unit id: " + subEquipmentUnitAdd.getParentEquipmentId() + " for SubEquipment unit " + subEquipmentUnitAdd
          .getSubEquipmentId() + " is unknown");
      changeReport.setState(CHANGE_STATE.FAIL);
      return changeReport;
    }

    // Check if a SubEquipment unit with same id is not already registered
    if (parentEquipmentConfiguration.getSubEquipmentConfiguration(subEquipmentUnitAdd.getSubEquipmentId()) != null) {
      changeReport.appendError("onSubEquipmentUnitAdd - SubEquipment unit id: " + subEquipmentUnitAdd.getSubEquipmentId() + " is already registered");
      changeReport.setState(CHANGE_STATE.FAIL);
      return changeReport;
    }

    SubEquipmentConfiguration subEquipmentConfiguration = null;

    // Create the configuration
    try {
      subEquipmentConfiguration = equipmentConfigurationFactory.createSubEquipmentConfiguration(subEquipmentUnitAdd.getSubEquipmentUnitXml());
    } catch (Exception e) {
      changeReport.setState(CHANGE_STATE.FAIL);
      changeReport.appendError(StackTraceHelper.getStackTrace(e));
      return changeReport;
    }

    // Add the configuration to the parent Equipment
    parentEquipmentConfiguration.addSubEquipmentConfiguration(subEquipmentConfiguration);

    return changeReport;
  }

  /**
   * Gets the source command tags for a provided equipment id.
   *
   * @param equipmentId The equipment id to get the source command tags.
   *
   * @return The SourceCommandTags or null if the equipment does not exist.
   */
  private Map<Long, SourceCommandTag> getSourceCommandTags(final Long equipmentId) {
    ProcessConfiguration configuration = ProcessConfigurationHolder.getInstance();
    Map<Long, EquipmentConfiguration> equipmentConfigurations = configuration.getEquipmentConfigurations();
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
   *
   * @return The SourceDataTags or null if the equipment does not exist.
   */
  private Map<Long, SourceDataTag> getSourceDataTags(final Long equipmentId) {
    ProcessConfiguration processConfiguration = ProcessConfigurationHolder.getInstance();
    Map<Long, EquipmentConfiguration> equipmentConfigurations = processConfiguration.getEquipmentConfigurations();
    Map<Long, SourceDataTag> sourceDataTags = null;

    EquipmentConfiguration equipmentConfiguration = equipmentConfigurations.get(equipmentId);
    if (equipmentConfiguration != null) {
      sourceDataTags = equipmentConfiguration.getDataTags();
    } else {
      // Try to find a SubEquipment that matches the given equipment ID
      for (EquipmentConfiguration configuration : processConfiguration.getEquipmentConfigurations().values()) {
        if (configuration.getSubEquipmentConfigurations().containsKey(equipmentId)) {
          log.debug("Getting source data tags of equipment " + configuration.getId() + " which is parent of SubEquipment " + equipmentId);
          sourceDataTags = configuration.getDataTags();
        }
      }
    }

    return sourceDataTags;
  }

  /**
   * Sets the process configuration of this DAQ process.
   *
   * @param processConfiguration The process configuration object.
   */
  public void setProcessConfiguration(final ProcessConfiguration processConfiguration) {
    ProcessConfigurationHolder.setInstance(processConfiguration);
  }

  /**
   * Gets the process configuration of this DAQ process.
   *
   * @return The process configuration object.
   */
  public ProcessConfiguration getProcessConfiguration() {
    return ProcessConfigurationHolder.getInstance();
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
   * Adds a core data tag changer to the configuration controller. This changers
   * should never fail to maintain a proper state of the DAQ.
   *
   * @param equipmentId    The equipment id to add the changer to.
   * @param dataTagChanger The changer to add.
   */
  public void addCoreDataTagChanger(final Long equipmentId, final ICoreDataTagChanger dataTagChanger) {
    List<ICoreDataTagChanger> changers = coreDataTagChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<>();
      coreDataTagChangers.put(equipmentId, changers);
    }
    changers.add(dataTagChanger);
  }

  /**
   * Adds a core command tag changer to the configuration controller. This
   * changers should never fail to maintain a proper state of the DAQ.
   *
   * @param equipmentId       The equipment id to add the changer to.
   * @param commandTagChanger The changer to add.
   */
  public void addCoreCommandTagChanger(final Long equipmentId, final ICoreCommandTagChanger commandTagChanger) {
    List<ICoreCommandTagChanger> changers = coreCommandTagChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<>();
      coreCommandTagChangers.put(equipmentId, changers);
    }
    changers.add(commandTagChanger);
  }

  /**
   * Adds a core equipment configuration changer to the controller.
   *
   * @param equipmentId                       The equipment id to add the controller to.
   * @param coreEquipmentConfigurationChanger The changer to add.
   */
  public void addCoreEquipmentConfigurationChanger(final long equipmentId, final ICoreEquipmentConfigurationChanger coreEquipmentConfigurationChanger) {
    List<ICoreEquipmentConfigurationChanger> changers = coreEquipmentConfigurationChangers.get(equipmentId);
    if (changers == null) {
      changers = new ArrayList<>();
      coreEquipmentConfigurationChangers.put(equipmentId, changers);
    }
    changers.add(coreEquipmentConfigurationChanger);
  }

  /**
   * Puts an implementation command tag changer to this controller. There can
   * only be one per equipment.
   *
   * @param equipmentId       The equipment id to add the changer to.
   * @param commandTagChanger The changer to add.
   */
  public void putImplementationCommandTagChanger(final long equipmentId, final ICommandTagChanger commandTagChanger) {
    // if null is passed, remove the existing changer for given equipment (if
    // exist)
    if (commandTagChanger == null) commandTagChangers.remove(equipmentId);
    else commandTagChangers.put(equipmentId, commandTagChanger);
  }

  /**
   * Puts an implementation data tag changer to this controller. There can only
   * be one per equipment.
   *
   * @param equipmentId    The equipment id to add the changer to.
   * @param dataTagChanger The changer to add.
   */
  public void putImplementationDataTagChanger(final long equipmentId, final IDataTagChanger dataTagChanger) {
    // if null is passed, remove the existing changer for given equipment (if
    // exist)
    if (dataTagChanger == null) dataTagChangers.remove(equipmentId);
    else dataTagChangers.put(equipmentId, dataTagChanger);
  }

  /**
   * Puts an implementation equipment configuration changer to this controller.
   * There can only be one per equipment.
   *
   * @param equipmentId                   The equipment id to add the changer to.
   * @param equipmentConfigurationChanger The changer to add.
   */
  public void putImplementationEquipmentConfigurationChanger(final long equipmentId, final IEquipmentConfigurationChanger equipmentConfigurationChanger) {
    equipmentChangers.put(equipmentId, equipmentConfigurationChanger);
  }

  /**
   * Gets the equipment configuration.
   *
   * @param equipmentId The id of the equipment configuration.
   *
   * @return The equipment configuration with the provided id or null if there
   * is none.
   */
  public EquipmentConfiguration getEquipmentConfiguration(final Long equipmentId) {
    return getProcessConfiguration().getEquipmentConfiguration(equipmentId);
  }

  /**
   * Searches a data tag with the provided id and returns the first found.
   *
   * @param dataTagId The data tag id to search for.
   *
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
   *
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
   * This method sets the startup time of the process (in milliseconds)
   *
   * @param pStartUp time in milliseconds
   */
  public final void setStartUp(final long pStartUp) {
    startUp = pStartUp;
  }

  /**
   * This method gets the startup time of the process (in milliseconds)
   *
   * @return long
   */
  public long getStartUp() {
    return startUp;
  }

	/**
	 * Set the equipmentConfigurationFactory.
	 *
	 * @param equipmentConfigurationFactory
	 */
	public void setEquipmentConfigurationFactory(EquipmentConfigurationFactory equipmentConfigurationFactory) {
		this.equipmentConfigurationFactory = equipmentConfigurationFactory;
	}
}
