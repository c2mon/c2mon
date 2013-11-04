package cern.c2mon.daq.opcua.connection.ua;

import org.opcfoundation.ua.builtintypes.NodeId;

import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;

/**
 * The OPC UA item definition.
 * 
 * @author Andreas Lang
 *
 */
public class UAItemDefintion extends ItemDefinition<NodeId> {

    /**
     * Creates a new UA item definiton.
     * 
     * @param id The id of the definiton.
     * @param address The address of the item.
     */
    public UAItemDefintion(final long id, final NodeId address) {
        super(id, address);
    }
    
    /**
     * Creates a new UA item definiton.
     * 
     * @param id The id of the definiton.
     * @param address The address of the item.
     * @param alternativeAddress The alternative address of the item.
     */
    public UAItemDefintion(final long id, final NodeId address,
            final NodeId alternativeAddress) {
        super(id, address, alternativeAddress);
    }

}
