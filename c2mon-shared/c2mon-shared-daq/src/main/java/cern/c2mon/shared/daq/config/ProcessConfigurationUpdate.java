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
package cern.c2mon.shared.daq.config;
/**
 * The process update event.
 * TODO This is at the moment intentionally empty. If there are changes in the future do it here.
 * 
 * @author Andreas Lang
 * 
 */
public class ProcessConfigurationUpdate extends Change {
    /**
     * The id of the process to change
     */
    private long processId;
    
    /**
     * Creates a new process update change.
     */
    public ProcessConfigurationUpdate() {
    }
    
    /**
     * Creates a new process update change.
     * @param changeId The change id of the change.
     * @param processId The id of the process to change.
     */
    public ProcessConfigurationUpdate(final long changeId, final long processId) {
        setChangeId(changeId);
        this.processId = processId;
    }

    /**
     * @return the processId
     */
    public long getProcessId() {
        return processId;
    }

    /**
     * @param processId the processId to set
     */
    public void setProcessId(final long processId) {
        this.processId = processId;
    }
    
}
