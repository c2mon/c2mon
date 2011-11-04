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

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.client.common.video.VideoConnectionProperties;

/**
 * This class handles the delegation of the incoming video request to the
 * viewers of the VideoMainPanel. Furthermore it queues the requests in case
 * that there are more requests than viewer available. The queue order is last
 * in last out. 
 * 
 * @author Matthias Braeger
 */
public class VideoMainHandler {
  
  /** Interface that provides the control method for the VideoMainPanel */
  private final VideoPanelController controller;
  
  /** The queue of waiting connection requests for the VideoPanelController */
  private final List<VideoConnectionProperties> connectionQueue;
  
  /**
   * Default Constructor
   * @param controller Represents the control methods of the VideoMainPanel
   */
  public VideoMainHandler(final VideoPanelController controller) {
    this.controller = controller;
    connectionQueue = new ArrayList<VideoConnectionProperties>();
  }
  
  
  /**
   * Adds a new video Request to the queue
   * @param connProperties The connection properties
   */
  public final synchronized void addVideoRequest(final VideoConnectionProperties connProperties) {
    if (!controller.isVideoInQueue(connProperties)
        && !connectionQueue.contains(connProperties)) {
      // If we are here we have a new video connection request
      if (!controller.addVideoToQueue(connProperties)) {
        // Queue seems to be full. In that case we store it in the handler
        connectionQueue.add(connProperties);
      }
    }
  }
  
  /**
   * Call this method to specify which video shall be displayed on the main view.
   * @param connProperties The connection properties to the camera that shall 
   *                       be displayed on the main view 
   */
  public final synchronized void setActiveVideoConncetion(final VideoConnectionProperties connProperties) {
    VideoConnectionProperties lastQueueElem;
    
    lastQueueElem = controller.setMainVideo(connProperties);
    if (lastQueueElem != null) {
      // video panel queue was already full. The last included video was rejected and we
      // have to put it back into the waiting connection queue.
      connectionQueue.add(0, lastQueueElem);
    }
    else {
      if (connectionQueue.size() > 0) {
        // Add the first element in the queue to the video panel
        boolean isDisplayed = controller.addVideoToQueue(connectionQueue.get(0));
        if (isDisplayed) // Was there a free viewer?
          connectionQueue.remove(0);
      }
    }
  }
  
  /**
   * Tries to close the specific connection
   * @param connProperties The connection properties
   * @return true, if the specific connection was found and successfully closed
   */
  public final synchronized boolean closeVideoConnection(final VideoConnectionProperties connProperties) {
    boolean retval = false;
    
    if (controller.closeVideoConnection(connProperties)) {
        if (connectionQueue.size() > 0)
          // Add the first element in the queue to the video panel
          controller.addVideoToQueue(connectionQueue.remove(0));
        
        retval = true;
    }
    else if (connectionQueue.remove(connProperties)) {
      retval = true;
    }
    
    return retval;
  }
  
  /**
   * Closes all connections on the main video panel and removes all 
   * waiting requests from the queue.
   */
  public final synchronized void closeAllConnections() {
    controller.closeAllConnections();
    connectionQueue.clear();
  }
  
  /**
   * To update the key counter of one of the connection request that is queued. In case
   * that the update belongs to the active video connection, the 'keys taken' counter
   * will be updated.
   * @param connProperties The connection properties that shall correspond with the
   * host name and camera with one of the queued connection requests. Furthermore it shall
   * contain the updated counter.
   * @return true, if a corresponding connection could be found in the queue and successfully
   * be updated.
   */
  public final synchronized boolean updateKeysTaken(final VideoConnectionProperties connProperties) {
    if (connectionQueue.contains(connProperties)) {
      int index = connectionQueue.indexOf(connProperties);
      connectionQueue.get(index).setKeysTaken(connProperties.getKeysTaken());
    }
    else if (controller.isVideoInQueue(connProperties)) {
      controller.updateKeysTakenCounter(connProperties);
    }
    else return false;
      
    return true;
  }
}
