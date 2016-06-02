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
package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.HashMap;
import java.util.Map;

import cern.c2mon.daq.opcua.connection.common.IGroupProvider;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * The default group provider. Provides groups based on a GroupIdentifier object.
 * The GroupIdentifier object is setup with the deadbands and provides an
 * equals and hashCode method to work if the objects have the same deadband.
 *
 *
 * @author Andreas Lang
 *
 * @param <IA>
 */
public class DefaultGroupProvider< IA extends ItemDefinition< ? > > implements IGroupProvider<IA> {

    /**
     * The already created groups in a map. Key is the DeadBandGroupIdentifier
     * object.
     */
    private final Map<DeadBandGroupIdentifier, SubscriptionGroup<IA>> groups =
        new HashMap<DeadBandGroupIdentifier, SubscriptionGroup<IA>>();

    /**
     * Gets or creates a SubscriptionGroup.
     *
     * @param sourceDataTag The source data tag whose group is required.
     * @return The subscription group the data tag fits in.
     */
    @Override
    public SubscriptionGroup<IA>
        getOrCreateGroup(final ISourceDataTag sourceDataTag) {
        float valueDeadband = (sourceDataTag.getValueDeadbandType()
                == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) ?
                        sourceDataTag.getValueDeadband() : 0.0f;
        int timeDeadband = sourceDataTag.getTimeDeadband();
        DeadBandGroupIdentifier groupIdentifier =
            new DeadBandGroupIdentifier(valueDeadband, timeDeadband);
        SubscriptionGroup<IA> group = groups.get(groupIdentifier);
        if (group == null) {
            group = new SubscriptionGroup<IA>(timeDeadband, valueDeadband);
            groups.put(groupIdentifier, group);
        }
        return group;
    }
}
