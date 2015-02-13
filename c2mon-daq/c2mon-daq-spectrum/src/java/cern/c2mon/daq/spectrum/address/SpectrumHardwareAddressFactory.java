/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.address;

import cern.c2mon.daq.spectrum.address.impl.SpectrumHardwareAddressImpl;

public class SpectrumHardwareAddressFactory {

    public static SpectrumHardwareAddress fromJson(final String json) {
        SpectrumHardwareAddress addr = null;
        if ((addr = SpectrumHardwareAddressImpl.fromJson(json)) == null) {
            throw new IllegalArgumentException("address creation failed. Check the JSON declaration");
        }
        return addr;
    }
    
}
