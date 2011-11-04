/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.client.video;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.video.VideoConnectionProperties;


/**
 * This Class provides the TIM video main panel for the Netvu viewer.
 * @author Matthias Braeger
 *
 */
public class NetvuMainPanel extends DefaultMainPanel {

  /** Serial number for Java compiler */
  private static final long serialVersionUID = -7922129657453537453L;
  
  /** Log4j logger */
  private static final Logger LOG = Logger.getLogger(NetvuMainPanel.class);
  

  /**
   * Default Constructor <br>
   * Creates a video panel without a key counter and 4 viewer on the right side.
   * 
   * @param title The title that is displayed over the central Video Panel
   */
  public NetvuMainPanel(final String title) {
    super(title);
  }
  
  
  /**
   * Constructor<br>
   * Creates a video panel and 4 viewer on the right side.
   * @param title The title that is displayed over the central Video Panel
   * @param showKeyCounter true, if a key counter is needed
   */
  public NetvuMainPanel(final String title, final boolean showKeyCounter) {
    super(title, showKeyCounter);
  }
  
  
  /**
   * Constructor<br>
   * Creates a video panel
   * @param title The title that is displayed over the central Video Panel
   * @param showKeyCounter true, if a key counter is needed
   * @param viewersQueueSize The amount of viewers on the right video viewer queue.
   * @throws NumberFormatException in case 2 <= viewersQueueSize <= 6
   */
  public NetvuMainPanel(final String title,
      final boolean showKeyCounter,
      final int viewersQueueSize) throws NumberFormatException {
    super(title, showKeyCounter, viewersQueueSize);
  }
  
  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#setMainVideo(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  @Override
  public synchronized VideoConnectionProperties setMainVideo(
      final VideoConnectionProperties connProperties) {
    VideoConnectionProperties rejectedConnection = null;
    
    try {
      // Acquires access to the queue
      available.acquire();
      
      if (centralViewer.getVideoViewerManager().isShowingVideo()) {
        if (centralViewer.getVideoViewerManager().getActiveConnectionProperties().equals(connProperties)) {
          // If we are here the requested connection was already established
          LOG.debug("The requested connection was already established");
          // Release the access to the queue
          available.release();
          return null; 
        }
        
        // We have to push back the active video connection on the first place of the queue
        // In case that we find the requested main connection in the queue we declare it of course as the main viewer
        IVideoViewer dummy;
        boolean foundConnection = false; 
        for (int i = 0; i <  viewersQueueSize; i++) {
          // prepare for the push;
          dummy = centralViewer;
          
          // push
          centralViewer = viewerQueue[i];
          viewerQueue[i] = dummy;
          
          if (centralViewer.getVideoViewerManager().isShowingVideo()) {
            if (centralViewer.getVideoViewerManager().getActiveConnectionProperties().equals(connProperties)) {
              foundConnection = true;
              LOG.debug("Connection exists already, let's use it!");
              break; // Connection exists already, let's use it!
            }
          }
          else {
            LOG.debug("No more connections in the queue, lets stop the here");
            break; // No more connections in the queue, lets stop the here
          }
        }
        
        if (!foundConnection) {
          if (centralViewer.getVideoViewerManager().isShowingVideo()) {
            // Queue was already full. In that case we reject the last connection in the queue
            // and return it to the calling method
            rejectedConnection = centralViewer.getVideoViewerManager().getActiveConnectionProperties();
            LOG.debug("Queue was already full. In that case we reject the last connection.");
            this.closeMainVideoConnection();
          }
          centralViewer.getVideoViewerManager().connectCamera(connProperties);
        }
        recreateVideoQueueView();
        setViewerTitles();
        
        // Add the new main viewer
        ((JPanel) centerSplitPane.getTopComponent()).add(
            centralViewer.getVideoViewerComponent(), BorderLayout.CENTER);
      }
      else {
        // No current active connection!
        // In case that we find the requested main connection in the queue we declare it as the main viewer
        // The empty main viewer will be pushed to the last place of the stack 
        boolean foundConnection = false;
        int index = -1; // the index of the found connection (if any)
        for (int i = 0; i < viewersQueueSize; i++) {
          if (viewerQueue[i].getVideoViewerManager().isShowingVideo()) {
            if (viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties().equals(connProperties)) {
              foundConnection = true;
              index = i;
              LOG.debug("Connection exists already, let's use it!");
              break; // Connection exists already, let's use it!
            }
          }
          else {
            LOG.debug("No more connections in the queue, lets stop the here");
            break; // No more connections in the queue, lets stop the here
          }
        }
          
        IVideoViewer dummy;
        if (foundConnection) {
          // We found the connection! We put it on the central viewer and pop
          // the viewer behind one up.
          dummy = centralViewer;
          centralViewer = viewerQueue[index];
          viewerQueue[index] = dummy;
 
          orderViewerQueue();
          setViewerTitles();
          recreateVideoQueueView(); // Very important!

          // Add the new main viewer
          ((JPanel) centerSplitPane.getTopComponent()).add(
              centralViewer.getVideoViewerComponent(), BorderLayout.CENTER);

        }
        else {
          // We did not find the connection so we just open it ;-)
          centralViewer.getVideoViewerManager().connectCamera(connProperties);
        }
      } // end else case
      
      setAccessPointDescription(connProperties.getDescription());
      setKeysTaken(connProperties.getKeysTaken());
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    //  Release the access to the queue
    available.release();
    
    return rejectedConnection; 
  }

  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#closeQueueVideoConnection(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  public final boolean closeVideoConnection(final VideoConnectionProperties connProperties) {
    boolean retval = false;
    
    try {
      // Acquire access to the queue
      available.acquire();
      
      retval = centralViewer.getVideoViewerManager().closeCameraConn(connProperties);
      if (retval) {
        infoPanel.reset();
      }
      else {
        for (int i = 0; i < viewersQueueSize; i++) {
          retval = viewerQueue[i].getVideoViewerManager().closeCameraConn(connProperties);
          if (retval) {
            if (orderViewerQueue())
              recreateVideoQueueView();
            setViewerTitles();
            break;
          }
        }
      }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Release access to the queue
    available.release();
    
    return retval;
  }
  
  
  /**
   * Re-orders the viewers in the queue so that all inactive viewers are
   * at the end of the queue. If the queue was really not in order it initialises
   * a repaint of the view with the right order.
   * 
   * @return true, if the queue had to be reordered 
   */
  private boolean orderViewerQueue() {
    boolean validate = false;    
    
    // First re-order the queue to have the active connection all together
    for (int k = 0; k < (viewersQueueSize - 1); k++) {
      if (!viewerQueue[k].getVideoViewerManager().isShowingVideo())
        for (int i = k + 1; i < viewersQueueSize; i++) {
          if (viewerQueue[i].getVideoViewerManager().isShowingVideo()) {
            // Exchange the viewers
            IVideoViewer dummy = viewerQueue[k]; 
            viewerQueue[k] = viewerQueue[i];
            viewerQueue[i] = dummy;
            validate = true;
            break;
          }
        }
    }
    
    return validate;
  }
  
  /**
   * Sets the right title on the queue viewers 
   */
  private void setViewerTitles() {
    IVideoViewerManager manager = null;
    for (int i = 0; i < viewersQueueSize; i++) {
      manager = viewerQueue[i].getVideoViewerManager();
      if (manager.isShowingVideo()) {
        viewerQueue[i].setTitle(
            manager.getActiveConnectionProperties().getDescription());
      }
      else {
        // Ensures that we have at least a gap. This is important for the calculation of the
        // aspect ratio
        viewerQueue[i].setTitle(" ");
      }
    }

    centralViewer.setTitle("");
  }

  
  /**
   * Re-orders the viewers in the queue
   */
  private void recreateVideoQueueView() {
    for (int i = 0; i < viewersQueueSize; i++) {
      if (queuePanel.isAncestorOf(viewerQueue[i].getVideoViewerComponent()))
        queuePanel.remove(viewerQueue[i].getVideoViewerComponent());
    }
    if (queuePanel.isAncestorOf(centralViewer.getVideoViewerComponent()))
      queuePanel.remove(centralViewer.getVideoViewerComponent());
    
    
    // Now add all viewer that should be in the stack
    for (int i = 0; i < viewersQueueSize; i++)
      queuePanel.add(viewerQueue[i].getVideoViewerComponent());
    
    this.validate();
  }
}
