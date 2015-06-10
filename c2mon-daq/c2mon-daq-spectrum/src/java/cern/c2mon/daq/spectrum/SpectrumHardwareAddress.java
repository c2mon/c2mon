/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import cern.c2mon.daq.spectrum.util.JsonUtils;

public class SpectrumHardwareAddress {

    private String hostname;
    
/*    // required by jackson as it creates objects via reflection
    private SpectrumHardwareAddressImpl() {
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpectrumHardwareAddressImpl other = (SpectrumHardwareAddressImpl) obj;
        if (hostname == null) {
            if (other.hostname != null)
                return false;
        } else if (!hostname.equals(other.hostname))
            return false;
        return true;
    }
*/


    public String getHostname() {
        return hostname;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }


    public static SpectrumHardwareAddress fromJson(final String json) {
        SpectrumHardwareAddress addr = null;
        if ((addr = SpectrumHardwareAddress.fromJson(json)) == null) {
            throw new IllegalArgumentException("address creation failed. Check the JSON declaration");
        }
        return addr;
    }
    

}
