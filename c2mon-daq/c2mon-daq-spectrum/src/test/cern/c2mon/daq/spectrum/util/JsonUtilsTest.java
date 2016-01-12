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

package cern.c2mon.daq.spectrum.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtilsTest {

    Logger LOG = LoggerFactory.getLogger(JsonUtilsTest.class);
    
    //
    // --- TEST --------------------------------------------------------------------------------
    //    
    /**
     * Check that converting JSON forth and back results in the same object.
     */
    @Test
    public void testSerial()
    {
        JsonTestDataHolder x = new JsonTestDataHolder();
        x.name = "Hello DIAMON";
        x.value = 10;
        String json = JsonUtils.toJson(x);
        LOG.info(x + " as JSON is " + json);
        JsonTestDataHolder back = JsonUtils.fromJson(json, JsonTestDataHolder.class);
        
        LOG.info("Restored name: " + back.name);
        LOG.info("Restored val.: " + back.value);
        assertEquals(x, back);
    }

}

class JsonTestDataHolder
{
    public String name;
    public long value;
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())            
        {
            return false;
        }
        JsonTestDataHolder other = (JsonTestDataHolder) obj;
        if (other.name.equals(name) && other.value == this.value)
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = (int) (53 * hash + this.value);
        return hash;    
    }

}
