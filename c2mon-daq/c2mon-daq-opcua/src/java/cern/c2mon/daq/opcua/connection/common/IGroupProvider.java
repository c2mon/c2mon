package cern.c2mon.daq.opcua.connection.common;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.daq.opcua.connection.common.impl.SubscriptionGroup;
import cern.tim.shared.daq.datatag.ISourceDataTag;

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
