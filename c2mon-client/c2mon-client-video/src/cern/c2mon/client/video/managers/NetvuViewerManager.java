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
package cern.c2mon.client.video.managers;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import uk.org.netvu.control.HostData;
import uk.org.netvu.swing.JViewer;
import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.IVideoViewerManager;
import cern.c2mon.client.video.TVVSplashScreen;
import cern.c2mon.client.video.VideoLayoutPlugin;
import cern.c2mon.client.video.actions.RefreshAction;
import cern.c2mon.client.video.viewers.NetvuViewer;

/**
 * This class handles all updates for the VideoPanel which represents the viewer.
 * Every VideoPanel instance creates automatically its own VideoPanelManager. In other
 * words an instance of this class should always be accessed via the VideoPanel object.
 * <br><br>
 * <b>TODO:</b> There is one known wrong behaviour when being first in 4 way mode having 3 videos open.
 * When the user switches then to single mode and removes the selected stream the manager
 * selects not the last selected stream. But the single mode works perfectly fine!
 * @author Matthias Braeger
 */
public class NetvuViewerManager implements IVideoViewerManager, PropertyChangeListener {
  
  /** Log4j logger */
  private static final Logger LOG = Logger.getLogger(NetvuViewerManager.class);
  
  /** Event name constant */
  public static final String CAMERA_EVENT   = "ViewerControl.Camera";
  /** Event name constant */
  public static final String POSITION_EVENT = "ViewerControl.ActivePosition";
  /** Event name constant */
  private static final String LAYOUT_EVENT = "LayoutPlugin.Layout";
  
  /** video viewer */
  private JViewer viewer = null;
  
  /** Handles the Layout of the Video viewer */
  private VideoLayoutPlugin layoutPlugin = null;
  
  /** keeps the information about the connected cameras */
  private VideoConnectionProperties[] connPropsArray = null;
  
  /** Counts which view should be used as next */
  private int nextVideoPosition = 0;
  /** the position in the array that should be updated in single mode */
  private int currActiveVideoConn = 0;
  /** the current selected video image in the viewer*/
  private int currActivePosition = 1;
  
  /** Used to check whether we did already an update on the view */
  private boolean isSingleViewLayout = false;
  
  /** the color that is used for the selection frame */
  private Color selectionColor = Color.green;
  
  /** if this is changed to false, the active selection is not highlighted */
  private boolean activePositionHighlighted = false;
  
  /**
   * Default Constructor
   * @param singleLayoutActivated true, in case single layout shall be activated
   */
  public NetvuViewerManager(boolean singleLayoutActivated) {
    
    this.connPropsArray = new VideoConnectionProperties[4];
    this.connPropsArray[0] = null;
    this.connPropsArray[1] = null;
    this.connPropsArray[2] = null;
    this.connPropsArray[3] = null;
    
    this.isSingleViewLayout = singleLayoutActivated;
    
    createViewer(singleLayoutActivated);
  }
  
  /**
   * Creates a JViewer instance and the appropriate layout manager.
   * @param singleLayoutActivated true, in case single layout shall be activated
   */
  private void createViewer(boolean singleLayoutActivated) {
    try {
      this.viewer = new JViewer(NetvuViewer.NO_SIGNAL_IMG);
      
      // Set some default properties for the video connection
      viewer.setPlayMode(true);
      viewer.setLiveResolution("hi");
      viewer.setImageFormat("mp4");
      viewer.setTitlesIncludeCameraNumber(false);
      viewer.setTitlesIncludeSystemName(true);
      viewer.setTitlesIncludeCameraName(true);
      viewer.setDragEnabled(false);
      viewer.setDoubleBuffering(true);
      
      // Create the layout manager
      layoutPlugin = new VideoLayoutPlugin(viewer);
      // Set the desired view
      if (singleLayoutActivated) {
        layoutPlugin.setDefaultLayout(VideoLayoutPlugin.SINGLE);
        nextVideoPosition = 1;
      }
      else {
        layoutPlugin.setDefaultLayout(VideoLayoutPlugin.FOUR_WAY);
      }
      layoutPlugin.addPropertyChangeListener(this);
      viewer.setDefaultLayoutPlugin(layoutPlugin);
    } catch (UnsatisfiedLinkError e) {
      TVVSplashScreen.setVisible(false);
      JOptionPane.showMessageDialog(null, 
          "An unforseen error occured while initializing the Video Viewer.\n"
          + "It seems that your PC misses some libraries which are needed for running"
          + " the application.\nPlease fix that problem first and try then to start "
          + "the TIM Video Viewer again:\n\n"
          + e.getMessage(),
          "Unsatisfied Link Error", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }
  
  /**
   * Returns always the same instance of the internally handled JViewer 
   * @return always the same instance of the internally handled JViewer
   */
  public JViewer getViewer() {
    return viewer;
  }
  
  /**
   * Changes the color of the selection frame. By default it is set
   * to green.
   * @param color the color of the selection frame
   */
  public void setSelectionColor(Color color) {
    this.selectionColor = color;
  }
  
  /**
   * Activates or deactivates the colored frame around the video image.
   * By default this is set to <code>false</code>.
   * @param activePositionHighlighted true, if it should be highlighted
   */
  public void setActivePositionHighlighted(boolean activePositionHighlighted) {
    this.activePositionHighlighted = activePositionHighlighted;
  }
  
  /**
   * Tries to connect a view on a given camera
   * 
   * @param connProperties The connection properties of the video streaming server
   * @return true, if connection was successful
   */
  public boolean connectCamera(VideoConnectionProperties connProperties) {
    LOG.info("Connecting camera "
        + connProperties.getCamera() + " ont host " + connProperties.getHost());
    // Store information in array
    if (layoutPlugin.getLayout().equalsIgnoreCase(VideoLayoutPlugin.FOUR_WAY)) {
      connPropsArray[nextVideoPosition++] = connProperties; //TODO: check nextVideoPosition
      currActivePosition = nextVideoPosition;
      
      // Reset counter if condition true 
      if ( nextVideoPosition >= connPropsArray.length)
        nextVideoPosition = 0;
    }
    else {
      // can happen when switching between single and four way
      if ( currActivePosition == 0 ) 
        currActivePosition = 1;
      connPropsArray[currActiveVideoConn] = connProperties;
    }
    
    LOG.debug("currActiveVideoConn = "+ currActiveVideoConn);
    // set user name and password
    HostData.getHostData(connProperties.getHost()).setPassword(connProperties.getPassword().toCharArray());
    HostData.getHostData(connProperties.getHost()).setUsername(connProperties.getLogin());
    
    // Update camera image
    viewer.beginUpdate();
    viewer.addConnection(connProperties.getHost(), connProperties.getCamera(), currActivePosition);

    if ( activePositionHighlighted ) {
      viewer.setActivePosition(currActivePosition);
      viewer.setActivePositionColor(selectionColor);
    }
  
    viewer.endUpdate();
    viewer.live();
    
    // This must be called since we saw sometimes painting problems
    // after connecting to a stream
    RefreshAction.getInstance().refresh();
    
    return true;
  }
  
  /**
   * Is used to determine whether a connection to the specified video stream exists.
   * @param connProperties The connection properties
   * @return true, if a connection to with the specified properties exists
   */
  public final boolean isRunning(final VideoConnectionProperties connProperties) {
    for (int arrayPos = 0; arrayPos < connPropsArray.length; arrayPos++) {
      if ( connProperties.equals(connPropsArray[arrayPos]) ) {
        LOG.debug("Found connection.");
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Use this function to close a specific connection to a camera
   * 
   * @param connProperties the details about the camera view that shall be closed
   * @return true, if the camera view could be successfully closed
   */
  public boolean closeCameraConn(VideoConnectionProperties connProperties) {
    int arrayPos = 0;
    boolean foundConn = false;
    
    for (arrayPos = 0; arrayPos < connPropsArray.length; arrayPos++) {
      if ( connProperties.equals(connPropsArray[arrayPos]) ) {
        LOG.debug("Found connection to remove.");
        connPropsArray[arrayPos] = null;
        foundConn = true;
        break;
      }
    }
    
    if ( foundConn ) {
    
      LOG.debug("Removed connection "+ arrayPos);
      
      // update the position of the active camera
      if ( arrayPos == currActiveVideoConn ) {
        selectLastActivePosition();
        if ( !isSingleViewLayout )
          updateActivePosition(currActivePosition);
      }
      else {
        LOG.debug("Do not need update focus, because  currActiveVideoConn="+ currActiveVideoConn);
      }
      
//      // update the viewer
      if ( isSingleViewLayout && arrayPos == currActiveVideoConn) {
        viewer.beginUpdate();
        viewer.disconnect();
        viewer.endUpdate();
      }
      else {
        updateCameraImages();
      }
    }
    else {
      LOG.debug("Nothing to remove from video viewer! ");
    }
    
    return foundConn;
  }
  
  /**
   * Closes all connections to the servers
   */
  public void closeAllConnections() {
    LOG.info("Close all connections to the video equipment");
    viewer.beginUpdate();
    viewer.disconnect();
    viewer.shutdown();
    viewer.endUpdate();
    
    // Clear connection array
    for(int i=0; i < connPropsArray.length; i++) {
      connPropsArray[0] = null;
    }
  }
  
  /**
   * Highlights on the viewer the active position
   * @param position
   */
  private void updateActivePosition(int position) {
    if ( !isSingleViewLayout ) {
      currActiveVideoConn = position - 1;
    } 
    viewer.beginUpdate();
    viewer.setActivePositionColor(Color.GREEN);
    viewer.setActivePosition(position);
    viewer.endUpdate();
    viewer.live();
  }
  
  /**
   * Puts the counters to the position of the last active view.
   * @return true, if there is an active connection left.
   */
  private boolean selectLastActivePosition() {
    boolean retval = true;
    int initialPos = currActiveVideoConn;
    
    do {
      if ( --currActiveVideoConn < 0 )
        currActiveVideoConn = connPropsArray.length - 1;
    } while ( connPropsArray[currActiveVideoConn] == null && currActiveVideoConn != initialPos );
    
    if ( initialPos == currActiveVideoConn ) {
      currActiveVideoConn = 0;
      currActivePosition = 0;
      nextVideoPosition = 0;
      retval = false;
    }
    else {
      currActivePosition = currActiveVideoConn + 1;
      // This is the next free view
      nextVideoPosition = currActivePosition;
      if ( nextVideoPosition >= connPropsArray.length )
        nextVideoPosition = 0;
    }
    
    LOG.debug("Active Position is now: currActiveVideoConn="+ currActiveVideoConn +", currActivePosition="+ currActivePosition);
    
    return retval;
  }
  
  /**
   * Updates the camera images on the viewer. 
   */
  public void updateCameraImages() {
    viewer.beginUpdate();
    viewer.removeAllConnections();
    for(int i=0; i < connPropsArray.length; i++) {
      if (connPropsArray[i] != null ) {
        viewer.beginUpdate();
        viewer.addConnection(connPropsArray[i].getHost(), connPropsArray[i].getCamera(), i + 1);
      }
    }
    viewer.endUpdate();
    viewer.live();
  }
  
  /**
   * This event is thrown when the layout of the viewer changes
   * @param evt
   */
  public void propertyChange(PropertyChangeEvent evt) {
    if ( evt.getPropertyName().equalsIgnoreCase(LAYOUT_EVENT)) {
      if (layoutPlugin.getLayout().equalsIgnoreCase(VideoLayoutPlugin.SINGLE) && !isSingleViewLayout) {
        if ( viewer.getActivePosition() > 0) {
          currActivePosition = viewer.getActivePosition();
          currActiveVideoConn = viewer.getActivePosition() - 1;
          if ( LOG.isDebugEnabled() )
            LOG.debug("Selection SINGLE! currActivePosition = "+ currActivePosition);
        }
        
        // to prevent that we update it two times
        isSingleViewLayout = true;
      }
      else if ( layoutPlugin.getLayout().equalsIgnoreCase(VideoLayoutPlugin.FOUR_WAY) && isSingleViewLayout ) {
        if ( LOG.isDebugEnabled() )
          LOG.debug("Selection FOUR_WAY! currActivePosition = "+ currActivePosition);
        // Update camera image
        updateCameraImages();
        
        // to prevent that we update it two times
        isSingleViewLayout = false;
      }
    }
    
    /**
     * The following code piece had to be written to keep the selection on the right place
     * and to prevent the user to switch it!
     */
    else if ( evt.getPropertyName().equalsIgnoreCase(CAMERA_EVENT)) {
      if ( !isSingleViewLayout ) {
        if ( LOG.isDebugEnabled() )
          LOG.debug("Update Layout on position "+ currActivePosition +" (ViewerControl.Camera)");
        updateActivePosition(currActivePosition);
      }
      else if ( layoutPlugin.getLayout().equalsIgnoreCase(VideoLayoutPlugin.SINGLE) && isSingleViewLayout) {
        if ( LOG.isDebugEnabled() )
          LOG.debug("Update Layout on position "+ 1 +" (ViewerControl.Camera)");
        updateActivePosition(1);
      }
    }
    else if ( evt.getPropertyName().equalsIgnoreCase(POSITION_EVENT)) {
      if ( viewer.getActivePosition() == 0 || connPropsArray[viewer.getActivePosition() - 1] == null)
        updateActivePosition(currActivePosition);
    }
  }
  
  /**
   * @return true, in case an active connection to a video server could be found
   */
  public final boolean isShowingVideo() {
    if ( isSingleViewLayout  && (connPropsArray[currActiveVideoConn] != null)) {
      return true;
    }
    else if ( !isSingleViewLayout ) {
      for (int i = 0; i < connPropsArray.length; i++)
        if ( connPropsArray[i] != null )
          return true;
    }
    
    return false;
  }
  
  /**
   * Returns the connection properties of the currently selected video
   * @return The specific connection properties
   */
  public final VideoConnectionProperties getActiveConnectionProperties() {
    return connPropsArray[currActiveVideoConn];
  }
  
  /**
   * Refreshes the JViewer instance
   */
  public final void refresh() {
    LOG.info("Restart camera");
    viewer.restart();
  }
}
