package cern.c2mon.driver.opcua.connection.dcom;

import cern.c2mon.driver.opcua.connection.common.impl.ItemDefinition;

/**
 * Item definition for the DCOM endpoint.
 * 
 * @author Andreas Lang
 *
 */
public class DADCOMItemDefintion extends ItemDefinition<String> {

    /**
     * Creates a new DCOM item definition.
     * 
     * @param id The id of the definition.
     * @param address The address of this item.
     */
    public DADCOMItemDefintion(final long id, final String address) {
        super(id, address);
    }
    
    /**
     * Creates a new DCOM item definition.
     * 
     * @param id The id of the definition.
     * @param address The address of this item.
     * @param alternativeAddress The alternative address of this item.
     */
    public DADCOMItemDefintion(
            final long id, final String address,
            final String alternativeAddress) {
        super(id, address, alternativeAddress);
    }

}
