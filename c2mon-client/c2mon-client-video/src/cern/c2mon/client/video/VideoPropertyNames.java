/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2009  CERN
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
 ******************************************************************************/
package cern.c2mon.client.video;

/**
 * This interface contains the names of all properties that can be defined in
 * an XML file. This variables shall be forwarded to the ConfigLoader.
 * @author Matthias Braeger
 */
public abstract class VideoPropertyNames {
  
  /**
   * Hidden default constructor
   */
  private VideoPropertyNames() {
    // DO NOTHING!
  }
  
  /** System property variable that contains the URL to the TIM Video configuration file */
  public static final String CONFIGURATION_FILE_PATH = "configurationFilePath";
  
  /** Boolean variable to specify whether the VLC player shall be taken or not */
  public static final String VLC_PLAYER = "vlc-player";
  
  /** 
   * String variable that specifies the VLC arguments which shall be passed.
   * This variable is optional
   */
  public static final String VLC_ARGS = "vlc-args";
  
  /** 
   * Integer variable for providing the height of the screen which
   * will be taken into account for determine the frame height
   */
  public static final String SCREEN_HEIGHT = "screen-height";
  
  /** 
   * Integer variable for providing the width of the screen which
   * will be taken into account for determine the frame width
   */
  public static final String SCREEN_WIDTH = "screen-width";
  
  /** This variable provides the URL to the TIM video viewer help page */
  public static final String HELP_WEB_PAGE = "help-web-page";
  
  /** Integer variable that contains the size of the video panel queue */
  public static final String VIDEO_QUEUE_SIZE = "video-queue-size";
  
  /** 
   * Boolean variable to set whether the key taken counter of the 
   * VideoInformationPanel is needed or not. 
   */ 
  public static final String KEYS_TAKEN_COUNTER = "keys-taken-counter";
  
  /** String variable that contains the title of the Video Viewer application */
  public static final String VIDEO_VIEWER_TITLE = "video-viewer-title";
  
  /** String variable that contains the name of the video system that shall be supported */
  public static final String VIDEO_SYSTEM_NAME = "video-system-name";
  
  /** 
   * Boolean variable to determine whether  the TIM Video Viewer 
   * is only listening to requests of a specific host. The user will in
   * this case be prompted at startup to enter a host name for the requester.
   */
  public static final String SINGLE_HOST_CONTROLLER = "single-host-controller";
}
