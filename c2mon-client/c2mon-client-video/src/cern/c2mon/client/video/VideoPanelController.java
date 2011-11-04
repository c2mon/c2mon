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

import cern.c2mon.client.common.video.VideoConnectionProperties;



/**
 * This interface is implemented by the VideoMainPanel and used from the
 * VideoMainHandler.
 * 
 * @author Matthias Braeger
 */
public interface VideoPanelController {

  
  /**
   * Sets the video connection on the main video viewer. Furthermore it provides
   * the information about taken keys and the access point description to the
   * information panel.<br>
   * In case there is still an active video, it is pushed back
   * into the queue on first position.
   * @param connProperties The connection properties of the new active Video
   * @return If there was an active video that had to be pushed back into the queue and the
   * queue was already full, the last queue entry is returned. Please note, that this is NOT the
   * last active video on the main viewer! If there was still a free place in the queue or the
   * main viewer had no active connection, a null pointer is returned. 
   */
  VideoConnectionProperties setMainVideo(
      final VideoConnectionProperties connProperties);
  
  /**
   * Adds at the end of the queue the new video connection. Please implement this method
   * as synchronized.
   * @param connProperties The connection properties
   * @return true, in case the video connection could successfully be added to the
   * end of the queue. False, if not or if video already in the queue.
   */
  boolean addVideoToQueue(
      final VideoConnectionProperties connProperties);
  
  /**
   * Checks whether the video was already added to the queue. The queue of
   * the VideoMainPanel includes also the active connection of the main panel.
   * @param connProperties the properties of the video connection
   * @return true, in case the video is already queued.
   */
  boolean isVideoInQueue(
      final VideoConnectionProperties connProperties);

  /**
   * Closes the specified connection on the viewer queue.
   * @param connProperties The connection properties
   * @return true, in case it could find and close the specified video connection.
   */
  boolean closeVideoConnection(
      final VideoConnectionProperties connProperties);

  /**
   * Closes all open stream connections.
   */
  void closeAllConnections();

  /**
   * @return The amount of viewers that are displayed on the right side of the
   * VideoMainPanel.
   */
  int getViewersQueueSize();
  
  /**
   * Updates the 'key taken' counter of the specific connection. If the connection is 
   * actually the active main connection, it also updates the LED display of the taken
   * keys.
   * @param connProperties The connection properties that contains the updated counter
   * @return true, if the connection could be found and the counter
   * successfully be updated.
   */
  boolean updateKeysTakenCounter(final VideoConnectionProperties connProperties);

  /**
   * Restarts the connections of the video viewer instances. This shall be used
   * in case the connection freezes.
   */
  void refresh();
}
