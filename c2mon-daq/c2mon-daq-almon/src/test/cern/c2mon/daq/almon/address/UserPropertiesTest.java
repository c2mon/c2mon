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

package cern.c2mon.daq.almon.address;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * JSON serialization/de-serialization tests of the <code>UserProperties</code> class
 * 
 * @author wbuczak
 */
public class UserPropertiesTest {

    @Test
    public void test() {
        UserProperties up = new UserProperties();

        up.put("x", "value1");
        up.put("ASI_PREFIX", "some string");

        String json = up.toJson();

        System.out.println(json);

        UserProperties up2 = UserProperties.fromJson(json);
        assertEquals(up, up2);

        assertEquals("value1", up2.get("x"));
        assertEquals("some string", up2.get("ASI_PREFIX"));

        assertNull(UserProperties.fromJson(""));
        up = UserProperties.fromJson("{}");
        assertEquals(up, UserProperties.fromJson(up.toJson()));
    }

}
