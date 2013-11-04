package cern.c2mon.daq.opcua.connection.soap;

import cern.c2mon.daq.opcua.connection.common.impl.ClassicItemDefinitionFactory;

/**
 * Factory for DASoapItemDefinitions.
 * 
 * @author Andreas Lang
 *
 */
public class DASoapItemDefintionFactory 
        extends ClassicItemDefinitionFactory<DASoapItemDefintion> {

    /**
     * Creates a new DASoapItemDefintion.
     * 
     * @param id The id of the new definition.
     * @param opcItemName The name/address of the opc item.
     * @return The DASoapItemDefinition.
     */
    @Override
    public DASoapItemDefintion createItemDefinition(
            final long id, final String opcItemName) {
        return new DASoapItemDefintion(id, opcItemName);
    }

    /**
     * Creates a new DASoapItemDefintion.
     * 
     * @param id The id of the new definition.
     * @param opcItemName The name/address of the opc item.
     * @param redundantOpcItemName The redundant name/address of the opc item.
     * @return The DASoapItemDefinition.
     */
    @Override
    public DASoapItemDefintion createItemDefinition(
            final long id, final String opcItemName,
            final String redundantOpcItemName) {
        return new DASoapItemDefintion(id, opcItemName, redundantOpcItemName);
    }



}
