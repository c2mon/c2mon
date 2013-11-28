package cern.c2mon.daq.opcua.connection.common;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
/**
 * Factory to create item definitions based on a HardwareAddress.
 * 
 * @author Andreas Lang
 *
 * @param <ID> The item definition type which is created from this factory. It
 * has to extend the {@link ItemDefinition} object.
 */
public interface IItemDefinitionFactory<ID extends ItemDefinition< ? > > {

    /**
     * Creates a new ItemAddress.
     * 
     * @param id The id of the definition to create.
     * @param hardwareAddress The HardwareAddress which contains the
     * configuration supplied from the core.
     * @return The created ItemDefinition object.
     */
    ID createItemDefinition(long id, final HardwareAddress hardwareAddress);

}
