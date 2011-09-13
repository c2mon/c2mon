package cern.c2mon.driver.opcua.connection.soap;

import cern.c2mon.driver.opcua.connection.common.impl.ItemDefinition;

/**
 * The SOAP (OPC XML DA) item definition.
 * 
 * @author Andreas Lang
 *
 */
public class DASoapItemDefintion extends ItemDefinition<String> {

    /**
     * Creates a new Soap item definiton.
     * 
     * @param id The id of the definition.
     * @param address The address of the item.
     */
    public DASoapItemDefintion(final long id, final String address) {
        super(id, address);
    }
    
    /**
     * Creates a new Soap item definiton.
     * 
     * @param id The id of the definition.
     * @param address The address of the item.
     * @param alternativeAddress The alternative address of the item.
     */
    public DASoapItemDefintion(
            final long id, final String address,
            final String alternativeAddress) {
        super(id, address, alternativeAddress);
    }

}
