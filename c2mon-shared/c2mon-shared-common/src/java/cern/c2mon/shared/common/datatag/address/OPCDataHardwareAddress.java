package cern.c2mon.shared.common.datatag.address;

public interface OPCDataHardwareAddress {

    /** 
     * Gets a second address
     * The item name can never be null.
     * @return the name of the OPC item not being currently used
     */  
    String getOpcRedundantItemName();
}
