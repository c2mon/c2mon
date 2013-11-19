/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
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
package cern.c2mon.shared.daq.config;
/**
 * A Change report contains information to single configuration change. 
 * 
 * @author Andreas Lang
 *
 */
public class ChangeReport {
    /**
     * The different possible states of this change.
     * 
     * @author Andreas Lang
     *
     */
    public static enum CHANGE_STATE { SUCCESS, FAIL, REBOOT, PENDING };
    /**
     * The changeId of this change.
     */
    private long changeId;
    /**
     * Indicates the success of the report.
     */
    private CHANGE_STATE state = CHANGE_STATE.PENDING;
    /**
     * An optional error message. If the change was unsuccessful this should be set.
     */
    private String errorMessage;
    /**
     * An optional warning message. For example i the configuration is done but
     * may cause problems.
     */
    private String warnMessage;
    /**
     * An optional info message to provide additional information about the 
     * configuration process.
     */
    private String infoMessage;
    
    /**
     * Creates a new ChangeReport.
     * 
     * @param changeId The changeId of this object. This has to be the same
     * as the id of the provided change.
     */
    public ChangeReport(final long changeId) {
        this.changeId = changeId;
    }
    
    /**
     * Creates a new ChangeReport matching the provided change.
     * 
     * @param change The change the report should belong to.
     */
    public ChangeReport(final Change change) {
        this(change.getChangeId());
    }

    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param changeReport The report object to copy.
     */
    public ChangeReport(final ChangeReport changeReport) {
        setState(changeReport.getState());
        errorMessage = changeReport.getErrorMessage();
        warnMessage = changeReport.getWarnMessage();
        infoMessage = changeReport.getInfoMessage();
    }

    /**
     * Appends an error message to the report and sets the state field to FAIL.
     * @param errorMessage The error message to append.
     */
    public void appendError(final String errorMessage) {
        if (this.errorMessage == null) {
            this.errorMessage = errorMessage;
        }
        else {
            this.errorMessage += ("\n" + errorMessage);
        }
        state = CHANGE_STATE.FAIL;
    }
    
    /**
     * Appends a warn message to the report.
     * @param warnMessage The warn message to append.
     */
    public void appendWarn(final String warnMessage) {
        if (this.warnMessage == null) {
            this.warnMessage = warnMessage;
        }
        else {
            this.warnMessage += ("\n" + warnMessage);
        }
    }
    
    /**
     * Appends an info message to the report.
     * @param infoMessage The info message to append.
     */
    public void appendInfo(final String infoMessage) {
        if (this.infoMessage == null) {
            this.infoMessage = infoMessage;
        }
        else {
            this.infoMessage += ("\n" + infoMessage);
        }
    }

    /**
     * @return the changeId
     */
    public long getChangeId() {
        return changeId;
    }

    /**
     * @return true if success else false
     */
    public boolean isSuccess() {
        return state == CHANGE_STATE.SUCCESS;
    }
    
    /**
     * @return true if pending else false
     */
    public boolean isPending() {
        return state == CHANGE_STATE.PENDING;
    }
    
    /**
     * @return true if fail else false
     */
    public boolean isFail() {
        return state == CHANGE_STATE.FAIL;
    }
    
    /**
     * @return true if pending else false
     */
    public boolean isReboot() {
        return state == CHANGE_STATE.REBOOT;
    }

    /**
     * @param state the state to set
     */
    public void setState(final CHANGE_STATE state) {
        this.state = state;
    }
    
    /**
     * Returns the state of this object.
     * @return The state of the report.
     */
    public CHANGE_STATE getState() {
        return state;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the warnMessage
     */
    public String getWarnMessage() {
        return warnMessage;
    }

    /**
     * @return the infoMessage
     */
    public String getInfoMessage() {
        return infoMessage;
    }
    
    /**
     * Returns a String representation of this change report.
     * 
     * @return String representation of this report.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Change (" + changeId + ")" + state.toString() + "\n");
        result.append("Error: " + errorMessage + "\n");
        result.append("Warn: " + warnMessage + "\n");
        result.append("Info: " + infoMessage + "\n");
        return result.toString();
    }
}
