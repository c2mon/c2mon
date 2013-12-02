package cern.c2mon.driver.jec.address;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;

public class TestPLCHardwareAddress implements PLCHardwareAddress {
    
    private int wordId;
    
    private int bitId;
    
    private String nativeAddress;
    
    private int resolutionFactor;
    
    private int blockType;

    public TestPLCHardwareAddress(String nativeAddress, int resolutionFactor, int wordId, int bitId) {
        super();
        this.wordId = wordId;
        this.bitId = bitId;
        this.nativeAddress = nativeAddress;
        this.resolutionFactor = resolutionFactor;
    }

    /**
     * Use PLChardwareAddress constants for block type.
     */
    public TestPLCHardwareAddress(String nativeAddress, int resolutionFactor, int wordId, int bitId, int blockType) {
      this(nativeAddress, resolutionFactor, wordId, bitId);
      this.blockType = blockType;
    }

    @Override
    public int getBitId() {
        return bitId;
    }

    @Override
    public int getBlockType() {       
        return blockType;
    }

    @Override
    public int getCommandPulseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getNativeAddress() {
        return nativeAddress;
    }

    @Override
    public float getPhysicMaxVal() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getPhysicalMinVal() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getResolutionFactor() {
        return resolutionFactor;
    }

    @Override
    public int getWordId() {
        return wordId;
    }

    @Override
    public String toConfigXML() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate() throws ConfigurationException {
        // TODO Auto-generated method stub

    }
    
    public cern.c2mon.shared.common.datatag.address.HardwareAddress clone() throws CloneNotSupportedException {
        return null;
    };

}
