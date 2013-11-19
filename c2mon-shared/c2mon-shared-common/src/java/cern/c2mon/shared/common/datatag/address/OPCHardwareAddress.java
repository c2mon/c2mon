package cern.c2mon.shared.common.datatag.address;

/**
 * The OPCHardwareAddress interface is used by the OPCMessageHandler.
 * 
 * @see cern.c2mon.daq.opc.OPCMessageHandler
 * @author W. Buczak
 */
public interface OPCHardwareAddress extends OPCCommandHardwareAddress, 
        OPCDataHardwareAddress, HardwareAddress {

    static enum ADDRESS_TYPE {STRING, NUMERIC, GUID}
    
    ADDRESS_TYPE getAddressType();
    
    int getNamespaceId();
    /**
     * Get the name of the OPC tag name. The item name can never be null.
     * 
     * @return the name of the primary OPC item
     */
    String getOPCItemName();

}
