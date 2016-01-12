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

package cern.c2mon.daq.filter.dynamic;

import java.util.Map;

import cern.c2mon.shared.common.datatag.SourceDataTag;

/**
 * The <code>DummyTimeDeadbandActivator</code> is used to satisfy spring dependencies for DAQs which are not using time
 * deadband filtering
 * 
 * @author wbuczak
 */
public class DummyTimeDeadbandActivator implements IDynamicTimeDeadbandFilterActivator {

    @Override
    public void newTagValueSent(long tagID) {

    }

    @Override
    public Map<Long, SourceDataTag> getDataTagMap() {

        return null;
    }

    @Override
    public void addDataTag(SourceDataTag sourceDataTag) {

    }

    @Override
    public void removeDataTag(SourceDataTag sourceDataTag) {

    }

    @Override
    public void clearDataTags() {

    }

}
