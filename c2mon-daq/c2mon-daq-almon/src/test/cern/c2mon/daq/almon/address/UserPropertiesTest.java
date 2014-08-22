/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon.address;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

/**
 * JSON serialization/de-serialization tests of the <code>UserProperties</code> class
 * 
 * @author wbuczak
 */
public class UserPropertiesTest {

    @Test
    public void test() {
        Properties p = new Properties();

        p.put("x", "value1");
        p.put("ASI_PREFIX", "some string");

        UserProperties up = new UserProperties(p);

        String json = up.toJson();

        System.out.println(json);

        UserProperties up2 = UserProperties.fromJson(json);
        assertEquals(up, up2);

        assertEquals("value1", up2.getProperties().get("x"));
        assertEquals("some string", up2.getProperties().get("ASI_PREFIX"));

    }

}
