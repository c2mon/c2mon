/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import java.util.Properties;

import cern.c2mon.daq.almon.util.JsonUtils;

/**
 * This is a wrapper class for user properties. The <code>UserProperties</code> class extends the standard java
 * <code>Properties</code> class providing JSON serialization/de-serialization functionality
 * 
 * @author wbuczak
 */
public class UserProperties extends Properties {

    private static final long serialVersionUID = 519985348392772451L;

    public UserProperties() {
    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }

    public static UserProperties fromJson(final String json) {
        if (json == null) {
            return null;
        }

        return JsonUtils.fromJson(json, UserProperties.class);
    }

}