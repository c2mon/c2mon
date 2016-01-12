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
package cern.c2mon.daq.opcua.connection.common;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.daq.opcua.connection.common.impl.SubscriptionGroup;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * Interface for OPC group providers. Every provider offers a strategy to
 * match a source data tag to a group.
 * 
 * @author Andreas Lang
 *
 * @param <ID> Subsclass of ItemDefinition. This definition depends on the
 * connection and is therfore parametrized.
 */
public interface IGroupProvider< ID extends ItemDefinition< ? > > {

    /**
     * Gets or creates the group for this source data tag's configuration.
     * Several consecutive calls with the same data tag should always return
     * the same group.
     * 
     * @param sourceDataTag The source data tag for which a group should be
     * found/created.
     * @return The found/created group.
     */
    SubscriptionGroup<ID> getOrCreateGroup(final ISourceDataTag sourceDataTag);

}
