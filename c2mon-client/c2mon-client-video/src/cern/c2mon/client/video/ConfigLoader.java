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

import cern.c2mon.client.common.config.ConfigurationProperty;
import cern.c2mon.client.common.config.GenericConfigLoader;

/**
 * This class is an adapted version of the 
 * ch.cern.tim.viewer.jviews.ViewerConfigLoader class. It is derived from the
 * GenericConfigLoader and provides some default properties for the TIM
 * Video Viewer which can of course be overwritten by a user properties file.
 * 
 * @author Matthias Braeger
 */
class ConfigLoader extends GenericConfigLoader {

  /**
   * Default constructor used when there is no configuration
   * file specified
   */
  public ConfigLoader() {
    super();
    loadDefaultProperties();
  }

  /**
   * Constructor
   * @param in InputStream that shall provide an TIM configuration XML document
   * @throws org.xml.sax.SAXParseException In case the XML document contains errors
   */
  public ConfigLoader(InputStream in) throws org.xml.sax.SAXParseException {
    super(in);
    loadDefaultProperties();
  }
  
  /**
   * Defines the default values of the properties that are necessary to
   * the TimVideoViewer. However, if a user configuration file has been
   * forwarded to the constructor it can overwrite these values.  
   */
  private void loadDefaultProperties() {
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.HELP_WEB_PAGE, "http://timweb/wiki/doku.php?id=documentation:tim-video-viewer"));
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.KEYS_TAKEN_COUNTER, Boolean.TRUE));
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.VIDEO_QUEUE_SIZE, Integer.valueOf(4)));
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.SINGLE_HOST_CONTROLLER, Boolean.FALSE));
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.VLC_PLAYER, Boolean.FALSE));
    addBasicProperty(new ConfigurationProperty(VideoPropertyNames.VLC_ARGS, "--sout-udp-caching 100 --rtp-caching 100 --audio 0"));
  }
}