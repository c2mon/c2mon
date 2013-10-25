/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.common;

import org.apache.log4j.Logger;

import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagReport;
import cern.tim.shared.daq.command.SourceCommandTagValue;

/**
 * This class models the threads responsible for command execution
 */
public class SourceCommandExecutor extends Thread {

    /**
     * Log4j Logger for this class
     */
    private final Logger logger = Logger.getLogger(SourceCommandExecutor.class);

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
    private int cmdExecutionStatus = SourceCommandTagReport.STATUS_NOK_TIMEOUT;

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
                cmdExecutionStatus = SourceCommandTagReport.STATUS_OK;
                commandExecLock.notifyAll();
            }
        } catch (EqCommandTagException ex1) {
            logger.error("a problem with executing command encountered. problem description: " + ex1.getErrorDescription());

            synchronized (commandExecLock) {
                cmdExecutionStatus = SourceCommandTagReport.STATUS_NOK_FROM_EQUIPMENTD;
                cmdExecutionDescription = ex1.getErrorDescription();
            }
        }
        catch (Exception ex2) {
            logger.error("run(): Unexpected error executing the command : " + ex2.getMessage(), ex2);
            synchronized (commandExecLock) {
                cmdExecutionStatus = SourceCommandTagReport.STATUS_NOK_FROM_EQUIPMENTD;
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
