/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * This class holds configuration properties
 * 
 * @author wbuczak
 */
@Component("conf")
@ManagedResource(objectName = "cern.c2mon.daq.almon:name=AlmonConfig", description = "configuration holder")
public class AlmonConfig {
   
    @Value("${cern.c2mon.daq.almon.max_pls_line:64}")
    private int maxPlsLine;

    @Value("${cern.c2mon.daq.almon.subs_thread_pool_size:16}")
    private int subcriptionsThreadPoolSize;

   
    /**
     * @return Returns the maxPlsLine.
     */
    @ManagedAttribute
    public int getMaxPlsLine() {
        return maxPlsLine;
    }

    /**
     * @return Returns the subcriptionsThreadPoolSize.
     */
    @ManagedAttribute(description = "size of the thread pool used by the daq to execute parameter subscription tasks")
    public int getSubcriptionsThreadPoolSize() {
        return subcriptionsThreadPoolSize;
    }
}