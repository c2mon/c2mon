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

package cern.c2mon.client.video;

import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.config.GenericConfigLoader;
import cern.c2mon.client.video.actions.HelpWebPageAction;

/**
 * This Singleton class provides an instance of the ViewerConfigLoader that shall
 * be used to get the user configuration. The user configurations are stored in a
 * property file that is read via its URL. The URL path is provided by the system
 * property variable 'configurationFilePath'.
 * 
 * @author Matthias Braeger
 */
public class VideoViewerConfigLoader {
  
  /** The private Singleton instance that can be accessed via getInstance() */
  private static VideoViewerConfigLoader INSTANCE = null;
  
  /** a pointer to the config loader */
  private GenericConfigLoader configLoader = null;
  
  /** Log4J logger */
  protected static final Logger log = Logger.getLogger(HelpWebPageAction.class);
  
  /**
   * Default constructor. It is declared as private since this class shall only be
   * instantiated through the Singleton method getInstance()
   */
  private VideoViewerConfigLoader() {
    String viewerConfigFilePath = System.getProperty(VideoPropertyNames.CONFIGURATION_FILE_PATH);
    if (viewerConfigFilePath != null) {
      try {  
        URL viewerConfigFileUrl = new URL(viewerConfigFilePath);
        InputStream in = viewerConfigFileUrl.openStream();
        configLoader = new ConfigLoader(in);
        log.debug("Configuration loader created");
      } catch(Exception e) {
        log.warn("Configuration file error\n(file URL: " + viewerConfigFilePath + ")\nThe TimViewer is using a default configuration" , e);
        configLoader = new ConfigLoader();
      } 
    }
    else {
      configLoader = new ConfigLoader();
    }
  }
  
  /**
   * Retrieves a property from its name
   * @param name the name of the property
   * @return the property
   */    
  public Object getPropertyByName(String name) {
    return configLoader.getPropertyByName(name);
  }
  
  /**
   * @return The Singleton instance of this class
   */
  public static VideoViewerConfigLoader getInstance() {
    if ( INSTANCE == null ) {
      INSTANCE = new VideoViewerConfigLoader();
    }
    
    return INSTANCE;
  }
}
