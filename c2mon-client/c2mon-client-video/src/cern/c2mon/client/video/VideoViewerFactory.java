/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN 
 * This program is free software; you can redistribute
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
 ******************************************************************************/
package cern.c2mon.client.video;

import cern.c2mon.client.video.viewers.NetvuViewer;
import cern.c2mon.client.video.viewers.VlcViewer;


/**
 * This abstract class decides which <code>IVideoViewer</code> instance shall
 * be created. Currently there are only two viewers supported. One for
 * connecting to Netvu servers and a JVLC viewer which can support any kind of
 * streams.
 * 
 * @author Matthias Braeger
 *
 */
public final class VideoViewerFactory {
  
  /** The singleton instance of this class */
  private static VideoViewerFactory instance = null;

  /** 
   * This variable can be overwritten by the appropriate value of the 
   * configuration file 
   */
  private Boolean createVlcViewer = Boolean.FALSE;

  /**
   * Hidden default constructor
   */
  private VideoViewerFactory() {
    createVlcViewer = 
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VLC_PLAYER);
    if (createVlcViewer) {
      TimVideoViewer.getInstance().setResizable(false);
    }
  }


  /**
   * Returns the singleton instance of this class.
   * @return The singleton instance
   */
  public static VideoViewerFactory getInstance() {
    if (instance == null) {
      instance = new VideoViewerFactory();
    }
    
    return instance;
  }


  /**
   * Creates a new <code>IVideoViewer</code> instance.
   * @return The created instance
   */
  public IVideoViewer createVideoViewer() {
    IVideoViewer viewer;
    if (createVlcViewer) {
      viewer = new VlcViewer();
    }
    else {
      viewer = new NetvuViewer(true, true);
    }
    
    return viewer;
  }
}
