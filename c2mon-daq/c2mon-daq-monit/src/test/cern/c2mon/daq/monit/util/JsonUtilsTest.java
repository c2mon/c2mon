/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.monit.util.JsonUtils;

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
