package cern.c2mon.shared.common.datatag.address;

public interface OPCCommandHardwareAddress {
    
    static enum COMMAND_TYPE {CLASSIC, METHOD}
    
    COMMAND_TYPE getCommandType();
    
    /**
     * Command pulse length in milliseconds for boolean commands.
     * @return the command pulse length in milliseconds for boolean commands.
     */
    int getCommandPulseLength();

}