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
package cern.c2mon.pmanager.fallback.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class that holds different methods to check different system related parameters
 * @author mruizgar
 *
 */

public final class SystemResourcesParameters {
    
  
    /** Command to be executed in the linux server */
    public static final String[] CMD_FREE_SPACE = {"sh", "-c", "df -m . | tail -n 1 | awk '{print $4}'"};
    
    /** Log4j logger for this class*/
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemResourcesParameters.class);
    
    /** 
     * Private constructor to prevent the class from being instantiated
     */
    private SystemResourcesParameters() {
        
    }
    
    /**
     * It returns the number of bytes that are still free in the server's disc 
     * @return A value indicating the free space in the disc
     */
    public static long getFreeSpace() {             
        long free = -1;
        File currentDir = new File(".");
        try {
          free = currentDir.getFreeSpace();
        } catch (Exception e) {
          LOGGER.error("Exception caught while querying free disk space", e);
        }        
        return free;
    }
}
    
    


