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
package cern.c2mon.daq.common.conf.core;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.daq.tools.CommandParamsHandler;

/**
 * Class holding fields describing the runtime state of the DAQ (running in test
 * or filter mode, start up time, etc.)
 * 
 * It also holds a reference to the process name and id, which are used at start
 * up and shut down.
 * 
 * @author mbrightw
 * 
 */
@Service
public class RunOptions {

    /**
     * The logger.
     */
    private static final Logger LOGGER = Logger.getLogger(RunOptions.class);

    /**
     * Reference to the command line parameter object.
     */
    @Resource
    private CommandParamsHandler commandParamsHandler;

    /**
     * Time (in milliseconds since 1/1/1970) of the daq startup.
     */
    private long startUp;

    /**
     * Flag indicating whether the filtered messages should or should not be
     * sent to the filter module.
     */
    private boolean filterMode = true;

    /**
     * This flag is used for switching on/off separate equipment logger's
     * appenders
     */
    private boolean eqLoggers = false;

    /**
     * This flag is used in pair with eqLoggers flag, to define whether
     * equipment loggers should append both process and equipment appenders or
     * only equipment
     */
    private boolean eqAppendersOnly = false;
    
    /**
     * If this flag is set to true, the daq will not send the PIK request
     * to the server. Use to be backward compatible with all versions with
     * no PIK reply. It will work as if the PIK procedure was never 
     * implemented
     */
    private boolean noPIK = false;

    /**
     * Initialization of the bean.
     */
    @PostConstruct
    public void init() {

        // set start up time on bean initialization
        setStartUp(System.currentTimeMillis());

        // set process name
        // check if user wants separate loggers for equipment units
        if (commandParamsHandler.hasParam("-eqLoggers")) {
            setEqLoggers(true);
            if (commandParamsHandler.hasParam("-eqAppendersOnly")) {
                setEqAppendersOnly(true);
            }
        }

        // check if the filtering should be turned off (default is on)
        if (commandParamsHandler.hasParam("-noFilter") || commandParamsHandler.hasParam("-nf")) {
            LOGGER.info("The DAQ process is starting without filtering (no JMS connections will be opened with Filter module)");
            setFilterMode(false);
        }
        
        // Check if we don't want to send the PIK requested from the server (default is we do want)
        if (commandParamsHandler.hasParam("-noPIK")) {
          LOGGER.info("The DAQ process is starting in noPIK mode (no PIK will be requested from the server and old communication protocol will be used)");
          this.setNoPIK(true);
        }
    }
    
    /**
     * This method sets the noPIK flag
     * 
     * @param value
     *            - the value to be set
     */
    public final void setNoPIK(final boolean value) {
        this.noPIK = value;
    }

    /**
     * This method returns the current state of noPIK flag.
     *  - if noPIK is set the we will not have the PIK value into account
     *  - otherwise we will use the PIK as part of our communication processes
     * 
     * @return boolean
     */
    public final boolean isNoPIK() {
        return this.noPIK;
    }

    /**
     * returns the filterMode of the process
     * 
     * @return the filterMode
     */
    public final boolean isFilterMode() {
        return filterMode;
    }

    /**
     * sets the filterMode for the process
     * 
     * @param filterMode
     *            to be set
     */
    public final void setFilterMode(final boolean filterMode) {
        this.filterMode = filterMode;
    }

    /**
     * This method sets the eqLoggers boolean flag
     * 
     * @param eqLoggers
     *            boolean flag stating whether each equipment unit should have
     *            separate file appender or not
     */
    public final void setEqLoggers(final boolean eqLoggers) {
        this.eqLoggers = eqLoggers;
    }

    /**
     * This method returns eqLoggers boolean flag
     * 
     * @return boolean
     */
    public final boolean getEqLoggers() {
        return this.eqLoggers;
    }

    /**
     * This method sets the setEqAppendersOnly boolean flag
     * 
     * @param eqAppendersOnly
     *            boolean flag stating whether each equipment logger should
     *            append only its related appender or also the process logger's
     *            appender.
     */
    public final void setEqAppendersOnly(final boolean eqAppendersOnly) {
        this.eqAppendersOnly = eqAppendersOnly;
    }

    /**
     * This method returns eqAppendersOnly boolean flag
     * 
     * @return boolean
     */
    public final boolean getEqAppendersOnly() {
        return this.eqAppendersOnly;
    }

    /**
     * Setter method.
     * 
     * @param commandParamsHandler
     *            the commandParamsHandler to set
     */
    public final void setCommandParamsHandler(final CommandParamsHandler commandParamsHandler) {
        this.commandParamsHandler = commandParamsHandler;
    }

    /**
     * This method sets the startup time of the process (in milliseconds)
     * 
     * @param pStartUp
     *            time in milliseconds
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
}
