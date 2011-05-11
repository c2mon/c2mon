/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.pmanager.fallback;

import org.apache.log4j.Logger;

import java.util.Properties;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
/**
 * It reads and stores the values of the properties defined in the "fallback.properties" file 
 * @author mruizgar
 *
 */

public final class FallbackProperties {
        
    /** Logger for the FallbackProperties class*/
    private static final Logger LOG = Logger.getLogger(FallbackProperties.class);
        
    /** It indicates the frequency (counted in number of lines in the file) for which the disc free space in the system will be checked**/   
    private static final String FREE_SPACE_CHECK_FREQUENCY = "10000";
    
    /** It indicates the minimum free disc space allowed in the system*/ 
    private static final String DISC_SIZE_CHECK = "1024";
    
    /** It indicates the number of lines that can be read from the fallback file at each time*/  
    public static final String NUMBER_LINES_FROM_FILE = "2000";
    
    /** It indicates with which frequency (each number of lines) the check for the disc size should be done*/
    private int freeSpaceCheckFrequency = Integer.parseInt(FREE_SPACE_CHECK_FREQUENCY);
    
    /** Minimum free space that should exist in the disc in megabytes*/
    private int minimunDiscFreeSpace = Integer.parseInt(DISC_SIZE_CHECK);
    
    /** Number of lines that will be read in each go from the fallback file */
    private int numberLinesToReadFromFile = Integer.parseInt(NUMBER_LINES_FROM_FILE);
    
    /** It indicates that an error while getting the free space in the system's disc has occurred*/
    public static final int CMD_FREE_SPACE_ERROR = -1;
    
    /** Name of the property file holding all fallback related properties*/
    public static final String PROPERTY_FILE_NAME = "fallback.properties";
    
    /** Unique instance of this class*/ 
    private static FallbackProperties fProperties;
    
    /** 
     * Gets the unique instance of the FallbackProperties class
     * @return The unique instance of the class
     */
    public static synchronized  FallbackProperties getInstance() {
        if (fProperties == null) {
            fProperties = new FallbackProperties();
        }
        return fProperties;
    }
    
    /** 
     * Class constructor. It loads all the properties defined in the "fallback.properties" file 
     */
    private FallbackProperties() {
        // Load client properties from file
        if (LOG.isDebugEnabled()) {
          LOG.debug(new StringBuffer("init() : loading properties from file ").append(PROPERTY_FILE_NAME));
        }
        try {
          Properties fallbackProperties = new Properties(); 
          InputStream fps = getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
          if (fps != null) {
            fallbackProperties.load(fps);
            this.minimunDiscFreeSpace = Integer.parseInt(fallbackProperties.getProperty("fallback.minimum.freespace", FREE_SPACE_CHECK_FREQUENCY));
            this.freeSpaceCheckFrequency = Integer.parseInt(fallbackProperties.getProperty("fallback.discsize.check", DISC_SIZE_CHECK));
            this.numberLinesToReadFromFile = Integer.parseInt(fallbackProperties.getProperty("fallback.read.lines.per.iteration", NUMBER_LINES_FROM_FILE));
          } else {
              LOG.warn(new StringBuffer("init() : Unable to find/read properties file ").append(PROPERTY_FILE_NAME));
              LOG.info("init() : Using default values for the fallback parameters");
          }
       } catch (FileNotFoundException fnfe) {
          LOG.warn(new StringBuffer("init() : Unable to find properties file ").append(PROPERTY_FILE_NAME));
          LOG.info("init() : Using default values as fallback parameters.");
        }
        catch (IOException ioe) {
          LOG.warn(new StringBuffer("init() : Unable to read properties file ").append(PROPERTY_FILE_NAME));
          LOG.info("init() : Using default values for the fallback properties.");
        }     
    }  
    
    
    /**
     * @param minDiscFreeSpace the minimunDiscFreeSpace to set
     */
    public void setMinimunDiscFreeSpace(final int minDiscFreeSpace) {
        this.minimunDiscFreeSpace = minDiscFreeSpace;
    }
        
    /**
     * @return the freeSpaceCheckFrequency
     */
    public int getFreeSpaceCheckFrequency() {
        return freeSpaceCheckFrequency;
    }

    /**
     * @return the minimunDiscFreeSpace
     */
    public int getMinimunDiscFreeSpace() {
        return minimunDiscFreeSpace;
    }    
    
    /**
     * @return the numberLinesReadFromFile
     */
    public int getNumberLinesToReadFromFile() {
        return numberLinesToReadFromFile;
    }
}
