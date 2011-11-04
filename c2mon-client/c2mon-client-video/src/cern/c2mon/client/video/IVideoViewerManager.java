/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.client.video;

import cern.c2mon.client.common.video.VideoConnectionProperties;


/**
 * This interface defines the functionalities that a manager of an 
 * <code>IVideoViewer</code> instance shall provide.<br>
 * A concrete instance of that interface is responsible of managing the video
 * connections and the depending video viewer. Every 
 * <code>IVideoViewerManager</code> is just responsible for one single 
 * <code>IVideoViewer</code> instance.
 * 
 * @see IVideoViewer
 * @author Matthias Braeger
 */
public interface IVideoViewerManager {

  /**
   * Closes all connections to the servers
   */
  void closeAllConnections();


  /**
   * @return <code>true</code>, if the managed viewer is showing a
   *         video stream could be found
   */
  boolean isShowingVideo();


  /**
   * Tries to display on the managed view a given camera signal 
   * 
   * @param connProperties The connection properties of the video streaming server
   * @return true, if connection was successful
   */
  boolean connectCamera(final VideoConnectionProperties connProperties);


  /**
   * Use this function to close on the managed viewer the streaming to the
   * specified camera.
   * 
   * @param connProperties the details about the camera view that shall be closed
   * @return true, if the camera view could be successfully closed
   */
  boolean closeCameraConn(final VideoConnectionProperties connProperties);
  
  
  /**
   * Determines whether the managed viewer is running the specified video
   * stream.
   * @param connProperties The connection properties
   * @return true, the viewer is currently showing this video stream
   */
  boolean isRunning(final VideoConnectionProperties connProperties);

  /**
   * Returns the connection properties of the currently selected video.
   * @return The specific connection properties or <code>null</code>, if no
   *         video stream is playing.
   */
  VideoConnectionProperties getActiveConnectionProperties();

  /**
   * Refreshes the actual running video connection on the managed viewer.
   * The call of that method shall solve problems with hanging video streams.
   */
  void refresh();
}
