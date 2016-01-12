/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.jec.address;

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
    public float getPhysicalMaxVal() {
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
