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
 * Simple class for a top level change event 
 * which means has a change id.
 * @author alang
 *
 */
public abstract class Change extends ChangePart implements IChange {
    /**
     * The id of this change.
     */
    private long changeId;

    /**
     * @return the changeId
     */
    @Override
    public long getChangeId() {
        return changeId;
    }

    /**
     * @param changeId the changeId to set
     */
    @Override
    public void setChangeId(final long changeId) {
        this.changeId = changeId;
    }
}
