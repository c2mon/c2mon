/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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

    public static String toJson(Object alarmTripplet) {
        String result = null;

        StringWriter str = new StringWriter();

        try {
            getObjectMapper().writeValue(str, alarmTripplet);
            result = str.toString();
        } catch (Exception ex) {
            LOG.error("deserialization from JSON failed", ex);
        }

        return result;
    }

}