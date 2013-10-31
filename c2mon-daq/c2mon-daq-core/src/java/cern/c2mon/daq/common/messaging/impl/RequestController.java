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
package cern.c2mon.daq.common.messaging.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.SourceCommandExecutor;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.RunOptions;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagReport;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.CommandTagAdd;
import cern.tim.shared.daq.config.CommandTagRemove;
import cern.tim.shared.daq.config.CommandTagUpdate;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.EquipmentConfigurationUpdate;
import cern.tim.shared.daq.config.ProcessConfigurationUpdate;
import cern.tim.shared.daq.datatag.DataTagValueUpdate;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValueRequest;
import cern.tim.shared.daq.datatag.SourceDataTagValueResponse;

/**
 * The RequestController is to direct request to the core to the right place in the core.
 * 
 * @author Andreas Lang
 */
public class RequestController {
    /**
     * The logger of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(RequestController.class);
    /**
     * The configuration controller as central access to all the configuration.
     */
    private ConfigurationController configurationController;
    /**
     * The command runners of this process mapped by equipmentId -> command runner.
     */
    private Map<Long, ICommandRunner> commandRunners = new ConcurrentHashMap<Long, ICommandRunner>();

    /**
     * Creates a new message controller which uses the provided configuration controller to perform configuration
     * changes triggered by messages.
     * 
     * @param configurationController The configuration controller to apply confiuration changes to.
     */
    @Autowired
    public RequestController(final ConfigurationController configurationController) {
        this.configurationController = configurationController;
    }

    /**
     * Applies a change to the configuration.
     * 
     * @param change The change to apply.
     * @return A report about the applied change and its success.
     */
    public ChangeReport applyChange(final Change change) {
        ChangeReport report;
        if (change instanceof DataTagAdd) {
            DataTagAdd addChange = (DataTagAdd) change;
            String logId = "not set";
            if (addChange.getSourceDataTag() != null) {
                logId = addChange.getSourceDataTag().getId().toString();
            }
            LOGGER.debug("Adding data tag with id: " + logId);
            report = configurationController.onDataTagAdd(addChange);
        } else if (change instanceof DataTagRemove) {
            LOGGER.debug("Removing data tag " + ((DataTagRemove) change).getDataTagId());
            report = configurationController.onDataTagRemove((DataTagRemove) change);
        } else if (change instanceof DataTagUpdate) {
            LOGGER.debug("Removing data tag" + ((DataTagUpdate) change).getDataTagId());
            report = configurationController.onDataTagUpdate((DataTagUpdate) change);
        } else if (change instanceof CommandTagAdd) {
            CommandTagAdd addChange = (CommandTagAdd) change;
            String logId = "not set";
            if (addChange.getSourceCommandTag() != null) {
                logId = addChange.getSourceCommandTag().getId().toString();
            }
            LOGGER.debug("Adding command tag with id: " + logId);
            report = configurationController.onCommandTagAdd(addChange);
        } else if (change instanceof CommandTagRemove) {
            LOGGER.debug("Removing command tag " + ((CommandTagRemove) change).getCommandTagId());
            report = configurationController.onCommandTagRemove((CommandTagRemove) change);
        } else if (change instanceof CommandTagUpdate) {
            LOGGER.debug("Updating command tag " + ((CommandTagUpdate) change).getCommandTagId());
            report = configurationController.onCommandTagUpdate((CommandTagUpdate) change);
        } else if (change instanceof EquipmentConfigurationUpdate) {
            report = configurationController.onEquipmentConfigurationUpdate((EquipmentConfigurationUpdate) change);
        } else if (change instanceof ProcessConfigurationUpdate) {
            report = configurationController.onProcessConfigurationUpdate((ProcessConfigurationUpdate) change);

        // NOTE: adding and removing EquipmentUnit(s) at runtime is handled directly by DriverKernel
        // see: ProcessMessageReceiver.onReconfigureProcess()
            
        //} else if (change instanceof EquipmentUnitAdd) {
        //    report = configurationController.onEquipmentUnitAdd((EquipmentUnitAdd) change);
        //} else if (change instanceof EquipmentUnitRemove) {
        //    report = configurationController.onEquipmentUnitRemove((EquipmentUnitRemove) change);
            
        } else {
            report = new ChangeReport(change);
            report.appendError("Change failed in DAQ core. " + change.getClass().getName()
                    + " is not supported by this version of the DAQ.");
        }
        return report;
    }

    /**
     * Executes the command specified in SourceCommandTagValue.
     * 
     * @param sourceCommandTagValue The value which specifies which command should be run.
     * @return A report about the comand execution.
     */
    public SourceCommandTagReport executeCommand(final SourceCommandTagValue sourceCommandTagValue) {
        long equipmentId = sourceCommandTagValue.getEquipmentId();
        long commandTagId = sourceCommandTagValue.getId();
        EquipmentConfiguration equipmentConfiguration = configurationController.getProcessConfiguration()
                .getEquipmentConfiguration(equipmentId);
        SourceCommandTagReport report;
        if (equipmentConfiguration != null) {
            SourceCommandTag sourceCommandTag = equipmentConfiguration.getCommandTags().get(commandTagId);
            if (sourceCommandTag != null) {
                int sourceRetries = sourceCommandTag.getSourceRetries();
                int sourceTimeout = sourceCommandTag.getSourceTimeout();
                ICommandRunner commandRunner = commandRunners.get(equipmentId);
                if (commandRunner != null) {
                    report = executeCommandOnImplementation(sourceCommandTagValue, commandRunner, sourceRetries,
                            sourceTimeout);
                } else {
                    report = new SourceCommandTagReport(SourceCommandTagReport.STATUS_NOK_FROM_EQUIPMENTD, "Equipment "
                            + equipmentId + " has no command runner. " + "Does it support command execution?");
                }
            } else {
                report = new SourceCommandTagReport(SourceCommandTagReport.STATUS_NOK_INVALID_COMMAND, "Command tag "
                        + commandTagId + " not found.");
            }
        } else {
            report = new SourceCommandTagReport(SourceCommandTagReport.STATUS_NOK_INVALID_EQUIPMENT, "Equipment "
                    + equipmentId + " not found.");
        }
        return report;
    }

    /**
     * Calls the implementation to execute the command. It will retry and timeout like specified with the provided
     * values.
     * 
     * @param sourceCommandTagValue The source command value which specifies he command.
     * @param commandRunner The command runner to use.
     * @param sourceRetries The number of retries if the command times out.
     * @param sourceTimeout The timeout of the command.
     * @return The command report about the success of the command.
     */
    private SourceCommandTagReport executeCommandOnImplementation(final SourceCommandTagValue sourceCommandTagValue,
            final ICommandRunner commandRunner, final int sourceRetries, final int sourceTimeout) {
        SourceCommandTagReport report = null;
        for (int i = 0; i < sourceRetries + 1; i++) {
            SourceCommandExecutor commandExecutor = new SourceCommandExecutor(commandRunner, sourceCommandTagValue);
            commandExecutor.start();
            try {
                commandExecutor.join(sourceTimeout);
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted while waiting for command execution." + "of command: "
                        + sourceCommandTagValue.getId());
            }
            report = commandExecutor.getSourceCommandTagReport();
            int status = report.getStatus();
            if (status == SourceCommandTagReport.STATUS_OK || status == SourceCommandTagReport.STATUS_TEST_OK) {
                break;
            } else {
                commandExecutor.interrupt();
            }
        }

        if (report == null) {
            report = new SourceCommandTagReport(SourceCommandTagReport.STATUS_NOK_FROM_EQUIPMENTD,
                    "Command could not be executed. Most likely the thread controling "
                            + "the execution was interrupted. See error logs for details.");
        }

        return report;
    }

    /**
     * Handles a SourceDataTagValueUpdateRequest.
     * 
     * @param sourceDataTagValueRequest The request to handle.
     * @return A SourceDataTagValueResponse which contains the result of the request. The return value is never null.
     */
    public SourceDataTagValueResponse onSourceDataTagValueUpdateRequest(
            final SourceDataTagValueRequest sourceDataTagValueRequest) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("entering handleSdtValueUpdateRequest()..");
        final SourceDataTagValueResponse dataTagValueResponse;
        String type = sourceDataTagValueRequest.getType();
        Long processId = configurationController.getProcessConfiguration().getProcessID();
        String processName = configurationController.getProcessConfiguration().getProcessName();
        List<DataTagValueUpdate> updates = new ArrayList<DataTagValueUpdate>();
        Map<Long, EquipmentConfiguration> equipmentMap = configurationController.getProcessConfiguration()
                .getEquipmentConfigurations();
        if (type.equals(SourceDataTagValueRequest.TYPE_PROCESS)) {
            LOGGER.debug("request type: PROCESS");
            if (processId.equals(sourceDataTagValueRequest.getId())) {
                for (EquipmentConfiguration equipmentConfiguration : equipmentMap.values()) {
                    updates.addAll(getDataTagUpdates(equipmentConfiguration));
                }
                dataTagValueResponse = new SourceDataTagValueResponse(updates);
            } else {
                String error = "process " + processName + " does not have id: " + sourceDataTagValueRequest.getId();
                LOGGER.error(error);
                dataTagValueResponse = new SourceDataTagValueResponse(error);
            }
        } else if (type.equals(SourceDataTagValueRequest.TYPE_EQUIPMENT)) {
            LOGGER.debug("request type: EQUIPMENT");
            Long equipmentId = sourceDataTagValueRequest.getId();
            EquipmentConfiguration configuration = equipmentMap.get(equipmentId);
            if (configuration != null) {
                updates.addAll(getDataTagUpdates(configuration));
                dataTagValueResponse = new SourceDataTagValueResponse(updates);
            } else {
                String error = "process " + processName + " does not have equipment with id: " + equipmentId;
                LOGGER.error(error);
                dataTagValueResponse = new SourceDataTagValueResponse(error);
            }
        } else if (type.equals(SourceDataTagValueRequest.TYPE_DATATAG)) {
            LOGGER.debug("request type: DATATAG");
            Long dataTagId = sourceDataTagValueRequest.getId();
            ISourceDataTag sourceDataTag = configurationController.findDataTag(dataTagId);
            if (sourceDataTag != null) {
                updates.add(getDataTagUpdate(sourceDataTag));
                dataTagValueResponse = new SourceDataTagValueResponse(updates);
            } else {
                String error = "process " + processName + " does not have a data tag with id: " + dataTagId;
                LOGGER.error(error);
                dataTagValueResponse = new SourceDataTagValueResponse(error);
            }
        } else {
            String error = "Unknown SourceDataTagValueRequest type: " + type;
            LOGGER.error(error);
            dataTagValueResponse = new SourceDataTagValueResponse(error);
        }
        return dataTagValueResponse;
    }

    /**
     * Creates a list of data tag updates.
     * 
     * @param equipmentConfiguration The equipment configuration which should be used to get the data tags.
     * @return List of updates for the provided equipment.
     */
    private List<DataTagValueUpdate> getDataTagUpdates(final EquipmentConfiguration equipmentConfiguration) {
        List<DataTagValueUpdate> resultList = new ArrayList<DataTagValueUpdate>();
        Map<Long, ISourceDataTag> sourceDataTags = equipmentConfiguration.getSourceDataTags();
        for (ISourceDataTag sourceDataTag : sourceDataTags.values()) {
            resultList.add(getDataTagUpdate(sourceDataTag));
        }
        return resultList;
    }

    /**
     * Creates a data tag value update for the provided data tag.
     * 
     * @param sourceDataTag The source data tag to use.
     * @return The update of the data tag value.
     */
    private DataTagValueUpdate getDataTagUpdate(final ISourceDataTag sourceDataTag) {
        Long processId = configurationController.getProcessConfiguration().getProcessID();
        Long processPIK = this.configurationController.getProcessConfiguration().getprocessPIK();
        
        RunOptions runOptions = this.configurationController.getRunOptions();
        // If we don't work with the PIK we act as we used to before PIK era, else we add the PIK to our communication process
        DataTagValueUpdate dataTagValueUpdate;
        if (runOptions.isNoPIK()) {
          dataTagValueUpdate = new DataTagValueUpdate(processId);
        }
        else {
          dataTagValueUpdate = new DataTagValueUpdate(processId, processPIK);
        }
        
        if (sourceDataTag.getCurrentValue() != null)
            dataTagValueUpdate.addValue(sourceDataTag.getCurrentValue().clone());
        return dataTagValueUpdate;
    }

    /**
     * Puts a command runner to this controller.
     * 
     * @param equipmentId The id of the equipment the runner belongs to.
     * @param commandRunner The command runner object.
     */
    public void putCommandRunner(final long equipmentId, final ICommandRunner commandRunner) {
        commandRunners.put(equipmentId, commandRunner);
    }
}
