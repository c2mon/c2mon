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
package cern.c2mon.daq.common.conf.core;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

/**
 * Class storing the DAQ configuration details that are generic across all DAQs.
 * 
 * @author mbrightw
 * 
 */
@Service
public class CommonConfiguration {
    /**
     * Reference to the properties used at initialization.
     */
    @Resource
    private Properties daqProperties;

    /**
     * The capacity of the filter buffer, that is the maximum number of objects
     * it can hold (FIFO is applied thereafter).
     */
    private int filterBufferCapacity;

    /**
     * JMS message timeout for requests sent to the server layer.
     */
    private Long requestTimeout;

    /**
     * Init method called at bean initialization (must not be final!).
     */
    @PostConstruct
    public void init() {
        setFromProperties();
    }

    /**
     * At initialization, sets the fields that need setting from the Properties
     * injected bean.
     */
    private void setFromProperties() {
        String propValue = "";
        
        // get the maximum capacity of the filter SynchroBuffer
        // (FIFO thereafter: values added after this point will prompt the
        // buffer to
        // remove the oldest values)
        propValue = daqProperties.getProperty("FilterBufferCapacity", "1000");
        this.setFilterBufferCapacity(new Integer(propValue).intValue());

        propValue = daqProperties.getProperty("server.request.timeout", "120000");
        this.setRequestTimeout(Long.valueOf(propValue));
    }

    /**
     * Setter method
     * 
     * @param daqProperties
     *            the daqProperties to set
     */
    public final void setDaqProperties(final Properties daqProperties) {
        this.daqProperties = daqProperties;
    }

    /**
     * Returns the maximum capacity of the filter buffer.
     * 
     * @return the filterBufferCapacity
     */
    public final int getFilterBufferCapacity() {
        return filterBufferCapacity;
    }

    /**
     * Sets the maximum capacity of the filter synchrobuffer (FIFO thereafter).
     * 
     * @param pFilterBufferCapacity
     *            the filterBufferCapacity to set
     */
    public final void setFilterBufferCapacity(final int pFilterBufferCapacity) {
        filterBufferCapacity = pFilterBufferCapacity;
    }
    
    /**
     * Setter method.
     * 
     * @param requestTimeout
     *            millisecond timeout
     */
    private void setRequestTimeout(final Long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    /**
     * Getter method.
     * 
     * @return the timeout in milliseconds
     */
    public Long getRequestTimeout() {
        return requestTimeout;
    }
}
