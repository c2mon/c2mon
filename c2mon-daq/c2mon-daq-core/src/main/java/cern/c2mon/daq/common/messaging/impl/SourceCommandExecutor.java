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
package cern.c2mon.daq.common.messaging.impl;

import cern.c2mon.shared.daq.command.SourceCommandTagReport.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;

import static cern.c2mon.shared.daq.command.SourceCommandTagReport.Status.*;

/**
 * This class models the threads responsible for command execution
 */
public class SourceCommandExecutor extends Thread {

    /**
     * Log4j Logger for this class
     */
    private final Logger logger = LoggerFactory.getLogger(SourceCommandExecutor.class);

    /**
     * The command runner to actually run the command on implementation level.
     */
    private final ICommandRunner commandRunner;

    /**
     * Value of the command to be executed
     */
    private SourceCommandTagValue sourceCommandTagValue = null;

    /**
     * Command execution synchronization thread lock
     */
    private final Object commandExecLock = new Object();

    /**
     * Execution status
     */
    private Status cmdExecutionStatus = STATUS_NOK_TIMEOUT;

    /**
     * Report description
     */
    private String cmdExecutionDescription;

    /**
     * Return value which is send back to the server
     */
    private String returnValue;

    /**
     * The SourceCommandExecutor constructor
     *
     * @param commandRunner
     *            The command runner to actually run the command.
     * @param sourceCommandTagValue
     *            The command to execute.
     */
    public SourceCommandExecutor(final ICommandRunner commandRunner, final SourceCommandTagValue sourceCommandTagValue) {
        super("SourceCommandExecutor");
        this.commandRunner = commandRunner;
        this.sourceCommandTagValue = sourceCommandTagValue;
    }

    /**
     * run() method for actually executing the command
     */
    public void run() {

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("trying to send command..");
            }

            String retValue = commandRunner.runCommand(sourceCommandTagValue);

            if (logger.isDebugEnabled()) {
                logger.debug("notifying the handler");
            }
            // if no exception occured, assume that everything went ok..
            synchronized (commandExecLock) {
                this.returnValue = retValue;
                cmdExecutionStatus = STATUS_OK;
                commandExecLock.notifyAll();
            }
        } catch (EqCommandTagException ex1) {
            logger.error("a problem with executing command encountered. problem description: " + ex1.getErrorDescription());

            synchronized (commandExecLock) {
                cmdExecutionStatus = STATUS_NOK_FROM_EQUIPMENTD;
                cmdExecutionDescription = ex1.getErrorDescription();
            }
        }
        catch (Exception ex2) {
            logger.error("run(): Unexpected error executing the command : " + ex2.getMessage(), ex2);
            synchronized (commandExecLock) {
                cmdExecutionStatus = STATUS_NOK_FROM_EQUIPMENTD;
                cmdExecutionDescription = ex2.getMessage();
            }
        }
    }

    /**
     * Returns the report to the current command.
     *
     * @return The report to the current running/finished command.
     */
    public SourceCommandTagReport getSourceCommandTagReport() {
        synchronized (commandExecLock) {
            // TODO these checks are a temporary hack because of a problem parsing empty elements. They can be removed after the fixes for the shared daq are deployed
            if (cmdExecutionDescription != null && cmdExecutionDescription.equals(""))
                cmdExecutionDescription = null;
            if (returnValue != null && returnValue.equals(""))
                returnValue = null;
            return new SourceCommandTagReport(sourceCommandTagValue.getId(), sourceCommandTagValue.getName(), cmdExecutionStatus, cmdExecutionDescription, returnValue, System.currentTimeMillis());
        }
    }
}
