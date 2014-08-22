/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import java.util.Properties;

import cern.c2mon.daq.almon.util.JsonUtils;

/**
 * TODO Auto-generated comment for <code>UserProperties</code>
 * 
 * @author wbuczak
 */
public class UserProperties {

    Properties properties;

    @SuppressWarnings("unused")
    private UserProperties() {
    }

    public UserProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        UserProperties other = (UserProperties) obj;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        return true;
    }

}
