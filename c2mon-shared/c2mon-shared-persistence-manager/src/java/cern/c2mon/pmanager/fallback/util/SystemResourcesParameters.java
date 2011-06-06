/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.pmanager.fallback.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


/**
 * Class that holds different methods to check different system related parameters
 * @author mruizgar
 *
 */

public final class SystemResourcesParameters {
    
  
    /** Command to be executed in the linux server */
    public static final String[] CMD_FREE_SPACE = {"sh", "-c", "df -m . | tail -n 1 | awk '{print $4}'"};
    
    /** Log4j logger for this class*/
    private static final Logger LOG = Logger.getLogger(SystemResourcesParameters.class);
    
    /** 
     * Private constructor to prevent the class from being instantiated
     */
    private SystemResourcesParameters() {
        
    }
    
    /**
     * It returns the number of bytes that are still free in the server's disc 
     * @return A value indicating the free space in the disc
     */
    public static int getFreeSpace() {        
        String line = null;
        int free = -1;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(CMD_FREE_SPACE);
            BufferedReader input = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            line = input.readLine();
            if (line != null) {
                free = Integer.valueOf(line).intValue();
                LOG.debug("getFreeSpace() : The free space in bytes is " + free);
            }
        } catch (IOException e) {
            LOG
                    .error("getFreeSpace() : Error while trying to determine the amount of free space "
                            + e.getMessage());
        } finally {
            if (p != null)
                p.destroy();
        }
        return free;
    }
}
    
    


