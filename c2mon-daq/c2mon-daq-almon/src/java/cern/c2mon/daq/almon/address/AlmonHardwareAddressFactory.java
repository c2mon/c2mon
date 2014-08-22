/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import cern.c2mon.daq.almon.address.impl.AlmonHardwareAddressImpl;

/**
 * The <code>AlmonHardwareAddressFactory</code> is responsible for creating instances of
 * <code>AlmonHardwareAddress</code>
 * 
 * @author wbuczak
 */
public class AlmonHardwareAddressFactory {

    public static AlmonHardwareAddress fromJson(final String json) {
        AlmonHardwareAddress addr = null;
        if ((addr = AlmonHardwareAddressImpl.fromJson(json)) == null) {
            throw new IllegalArgumentException("address creation failed. Check the JSON declaration");
        }
        return addr;
    }
}
