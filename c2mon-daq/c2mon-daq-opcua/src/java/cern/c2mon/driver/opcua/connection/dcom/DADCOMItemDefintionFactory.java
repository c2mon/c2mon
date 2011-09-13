package cern.c2mon.driver.opcua.connection.dcom;

import cern.c2mon.driver.opcua.connection.common.impl.ClassicItemDefinitionFactory;

/**
 * Creates DADCOMItemDefinitons.
 * 
 * @author Andreas Lang
 *
 */
public class DADCOMItemDefintionFactory 
        extends ClassicItemDefinitionFactory<DADCOMItemDefintion>  {

    /**
     * Creates a DADCOMItemDefintion.
     * 
     * @param id The id of the defintion.
     * @param opcItemName the item name/address of the defintion.
     * @return The new definition.
     */
    @Override
    public DADCOMItemDefintion createItemDefinition(
            final long id, final String opcItemName) {
        return new DADCOMItemDefintion(id, opcItemName.trim());
    }

    /**
     * Creates a DADCOMItemDefintion.
     * 
     * @param id The id of the defintion.
     * @param opcItemName the item name/address of the defintion.
     * @param redundantOpcItemName the redundant item name/address of the
     * defintion.
     * @return The new definition.
     */
    @Override
    public DADCOMItemDefintion createItemDefinition(
            final long id, final String opcItemName,
            final String redundantOpcItemName) {
        return new DADCOMItemDefintion(id, opcItemName.trim(), redundantOpcItemName.trim());
    }

}
