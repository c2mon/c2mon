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

package cern.c2mon.daq.almon.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Helper class providing JSON serialization & de-serialization utility methods
 * 
 * @author wbuczak
 */
public class JsonUtils {

    private static ObjectMapper objectMapper;

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }

        return objectMapper;
    }

    public static <T> T fromJson(final String json, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(new ByteArrayInputStream(json.getBytes()), clazz);
        } catch (Exception ex) {
            LOG.error("deserialization from JSON failed", ex);
        }
        return null;
    }

    public static String toJson(Object obj) {
        String result = null;

        StringWriter str = new StringWriter();

        try {
            getObjectMapper().writeValue(str, obj);
            result = str.toString();
        } catch (Exception ex) {
            LOG.error("serialization to JSON failed", ex);
        }

        return result;
    }

}
