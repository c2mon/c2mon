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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.video.VideoConnectionProperties;

/**
 * This abstract class constructs the main view of the TIM Video viewer. It is
 * based on three split panes. The main space is reserved for the Main video
 * panel. In the eastern part is placed the video viewer queue. The lower part
 * is reserved for the Key Counter (if any) and the description field.<br>
 * 
 * @see NetvuMainPanel
 * @author Matthias Braeger
 */
public class DefaultMainPanel extends JSplitPane 
  implements ComponentListener, VideoPanelController {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = 2646111348080526548L;
  
  /** Log4j logger */
  private static final Logger LOG = Logger.getLogger(DefaultMainPanel.class);
  
  /** Change this value to change the viewer's queue size */
  protected final int viewersQueueSize;
  
  /** Used to make sure that only one thread is manipulating at the same time */
  final Semaphore available;
  
  /** The main viewer that contains the active video stream */
  protected IVideoViewer centralViewer;
  
  /** The array that holds instance of all video viewer shown on the right side */
  protected final IVideoViewer[] viewerQueue;
  
  /** Used by the DividerRecalculation class */
  protected static int queueTitleLabelHeight = 16;
  
  /** Panel that contains the video viewers of the right queue */
  protected JPanel queuePanel = null;
  
  /** A panel that provides to the user information about the access point and keys taken */
  protected final VideoInformationPanel infoPanel;

  /** An internal splitPane that is added as left component */
  protected JSplitPane centerSplitPane = null;
  
  /** Displaying the title provided by the constructor */
  private static JLabel titleLabel = new JLabel();

  
  /**
   * Default Constructor <br>
   * Creates a video panel without a key counter and 4 viewer on the right side.
   * @param title The title that is displayed over the central Video Panel
   */
  public DefaultMainPanel(final String title) {
    this(title, false);
  }
  
  
  /**
   * Constructor<br>
   * Creates a video panel and 4 viewer on the right side.
   * @param title The title that is displayed over the central Video Panel
   * @param showKeyCounter true, if a key counter is needed
   */
  public DefaultMainPanel(final String title, final boolean showKeyCounter) {
    this(title, showKeyCounter, 4);
  }
  
  
  /**
   * Constructor<br>
   * Creates a video panel
   * @param title The title that is displayed over the central Video Panel
   * @param showKeyCounter true, if a key counter is needed
   * @param viewersQueueSize The amount of viewers on the right video viewer queue.
   * @throws NumberFormatException in case 2 <= viewersQueueSize <= 6
   */
  public DefaultMainPanel(final String title,
                              final boolean showKeyCounter,
                              final int viewersQueueSize) throws NumberFormatException {
    super(JSplitPane.HORIZONTAL_SPLIT);
    
    // Only one thread should manipulate the queue at the same time;
    this.available = new Semaphore(1, true);
    
    initSplitPane();
    
    if (viewersQueueSize < 2 || viewersQueueSize > 6)
      throw new NumberFormatException("Integer viewersQueueSize is not between 2 and 6");
    
    // set video viewers queue size
    this.viewersQueueSize = viewersQueueSize;
    
    // create the title label
    titleLabel.setText(title);
    
    // Initialize main video viewer
    centralViewer = VideoViewerFactory.getInstance().createVideoViewer();
    // We don't want to see information for the central view.
    centralViewer.setTitle("");
    
    // Initialize the queue
    viewerQueue = new IVideoViewer[viewersQueueSize];
    for (int i = 0; i < viewersQueueSize; i++) {
      viewerQueue[i] = VideoViewerFactory.getInstance().createVideoViewer();
    }
    
    // Initialize the information Panel on the lower part
    infoPanel = VideoInformationPanel.getInstance(showKeyCounter);
    
    // now start building the view
    createCentralView();
    createVideoQueueView();
  }
  
  
  /**
   * Sets the queue title label height.
   * @param queueTitleLabelHeight the title height
   */
  protected static synchronized void setQueueTitleLabelHeight(final int queueTitleLabelHeight) {
    DefaultMainPanel.queueTitleLabelHeight = queueTitleLabelHeight;
  }
  
  
  /**
   * Find the first free viewer in the queue and connect there to the specified camera
   * @param connProperties Connection properties of the video stream
   * @see ch.cern.tim.client.video.VideoPanelController#addVideoToQueue(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  public final synchronized boolean addVideoToQueue(
                            final VideoConnectionProperties connProperties) {
    boolean result = false;
        
    try {
      // Acquires access to the queue
      available.acquire();
      result = doAddVideoToQueue(connProperties);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Release the access to the queue
    available.release();

    return result;
  }


  /**
   * Private class which contains the logic for adding a new video to the
   * queue.
   * @param connProperties The properties of the video connection to be added.
   * @return <code>true</code>, if command was successful
   * @see ch.cern.tim.client.video.VideoPanelController#addVideoToQueue(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  private synchronized boolean doAddVideoToQueue(final VideoConnectionProperties connProperties) {
    boolean requestSuccessful = false; 
    boolean retry = true;
    IVideoViewerManager manager = null;
    
    // Check whether the video is already running somewhere
    for (int i = 0; i < viewersQueueSize; i++) {
      manager = viewerQueue[i].getVideoViewerManager();
      if (manager.isShowingVideo() 
          && manager.getActiveConnectionProperties().equals(connProperties)) {
        // Video is already running!
        return true;
      }
    }
    
    // Find an empty viewer and connect show there the camera stream.
    for (int i = 0; i < viewersQueueSize; i++) {
      manager = viewerQueue[i].getVideoViewerManager();
      if (!manager.isShowingVideo()) {
        while (!requestSuccessful && retry) {
          requestSuccessful = manager.connectCamera(connProperties);
          if (requestSuccessful) {
            viewerQueue[i].setTitle(connProperties.getDescription());
            retry = false;
          }
          else {
            final int response = JOptionPane.showConfirmDialog(this, 
                "Failed to open camera request \"" + connProperties.getHost()
                + ":" + connProperties.getCamera()
                + "\" on the video queue.\nDo you want to retry?", "Error",
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            
            retry = (JOptionPane.YES_OPTION == response);
          }
        }
      }
    }
    
    return requestSuccessful;
  }


  /**
   * @see ch.cern.tim.client.video.VideoPanelController#closeAllConnections()
   */
  public final void closeAllConnections() {
    try {
      // Acquire access to the queue
      available.acquire();
      doCloseAllConnections();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Release access to the queue
    available.release();
  }
  
  
  private final void doCloseAllConnections() {
    closeMainVideoConnection();
    // Close all connections waiting in the queue
    for (int i = 0; i < viewersQueueSize; i++) {
      viewerQueue[i].getVideoViewerManager().closeAllConnections();
      viewerQueue[i].setTitle(" "); // The gap is important to keep the space!
    }
  }
  
  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#closeVideoConnection(ch.cern.tim.shared.video.VideoConnectionProperties)
   */
  @Override
  public boolean closeVideoConnection(final VideoConnectionProperties connProperties) {
    boolean retval = false;
    
    try {
      // Acquire access to the queue
      available.acquire();
      retval = doCloseVideoConnection(connProperties);
      reorderQueue();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Release access to the queue
    available.release();
    
    return retval;
  }


  /**
   * Private class for closing a specific video connection.
   * @param connProperties Connection properties of the video stream
   * @return <code>true</code>, if the action was successful.
   * @see ch.cern.tim.client.video.VideoPanelController#closeVideoConnection(ch.cern.tim.shared.video.VideoConnectionProperties)
   */
  private synchronized boolean doCloseVideoConnection(final VideoConnectionProperties connProperties) {
    boolean retval = false;
      
    retval = centralViewer.getVideoViewerManager().closeCameraConn(connProperties);
    if (retval) {
      infoPanel.reset();
    }
    else {
      for (int i = 0; i < viewersQueueSize; i++) {
        retval = viewerQueue[i].getVideoViewerManager().closeCameraConn(connProperties);
        if (retval) {
          viewerQueue[i].setTitle(" ");
          break;
        }
      }
    }
    
    return retval;
  }
  
  
  /**
   * This method is used to reorder the queue of running videos to avoid having
   * empty views in the list. At the end of the reordering the idle viewers are
   * at the bottom end of the queue.
   */
  private synchronized void reorderQueue() {
    if (LOG.isDebugEnabled())
      LOG.debug("entering reorderQueue() ...");
    IVideoViewerManager manager = null;
    boolean foundIdleViewer = false;

    LOG.debug("reorderQueue() - Queue needs to be sorted.");
    final VideoConnectionProperties[] queue = new VideoConnectionProperties[viewersQueueSize];
    int index = 0;
    foundIdleViewer = false;
    
    for (int i = 0; i < viewersQueueSize; i++) {
      manager = viewerQueue[i].getVideoViewerManager(); 
      final boolean isShowingVideo = manager.isShowingVideo();
      if (isShowingVideo && foundIdleViewer) {
        queue[index++] = manager.getActiveConnectionProperties();
        manager.closeAllConnections();
        viewerQueue[i].setTitle(" ");
      }
      else {
        foundIdleViewer = !isShowingVideo;
      }
    }
    
    index = 0;
    for (int i = 0; i < viewersQueueSize; i++) {
      if (queue[index] != null && !viewerQueue[i].getVideoViewerManager().isShowingVideo()) {
        viewerQueue[i].setTitle(queue[index].getDescription());
        viewerQueue[i].getVideoViewerManager().connectCamera(queue[index++]);
      }
    }
    
    if (LOG.isDebugEnabled())
      LOG.debug("leaving reorderQueue()");
  }
  
  
  /**
   * @return The number of visible video viewers.
   * @see ch.cern.tim.client.video.VideoPanelController#getViewersQueueSize()
   */
  public final int getViewersQueueSize() {
    return viewersQueueSize;
  }
  
  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#isVideoInQueue(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  public final boolean isVideoInQueue(final VideoConnectionProperties connProperties) {
    boolean retval = false;
    
    retval = centralViewer.getVideoViewerManager().isRunning(connProperties);
    if (!retval) {
      for (int i = 0; i < viewersQueueSize; i++) {
        retval = viewerQueue[i].getVideoViewerManager().isRunning(connProperties);
        if (retval)
          break;
      }
    }
    
    return retval;
  }

  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#updateKeyTakenCounter(ch.cern.tim.client.video.VideoConnectionProperties)
   */
  public final boolean updateKeysTakenCounter(final VideoConnectionProperties connProperties) {
    boolean retval = false;
    int keysTaken = connProperties.getKeysTaken();
    
    retval = centralViewer.getVideoViewerManager().isRunning(connProperties);
    if (retval) {
      setKeysTaken(connProperties.getKeysTaken());
      centralViewer.getVideoViewerManager().getActiveConnectionProperties().setKeysTaken(keysTaken);
    }
    else {
      for (int i = 0; i < viewersQueueSize; i++) {
        retval = viewerQueue[i].getVideoViewerManager().isRunning(connProperties);
        if (retval) {
          viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties().setKeysTaken(keysTaken);
          break;
        }
      }
    }
      
    return retval;
  }
  
  
  /**
   * @see ch.cern.tim.client.video.VideoPanelController#setMainVideo(ch.cern.tim.shared.video.VideoConnectionProperties)
   */
  @Override
  public VideoConnectionProperties setMainVideo(final VideoConnectionProperties connProperties) {
    VideoConnectionProperties rejectedConnection = null;
    
    try {
      // Acquires access to the queue
      available.acquire();
      IVideoViewerManager manager = centralViewer.getVideoViewerManager();
      if (manager.isShowingVideo()) {
        final VideoConnectionProperties mainConnProperties =
          manager.getActiveConnectionProperties();
        if (mainConnProperties.equals(connProperties)) {
          LOG.debug("The requested connection was already established");
          // Release the access to the queue
          available.release();
          return null; 
        }
        // We have to push back the active video connection on the first place of the queue
        // In case that we find the requested main connection in the queue we declare it of course as the main viewer
        boolean foundConnection = false;
        for (int i = 0; i <  viewersQueueSize; i++) {
          manager = viewerQueue[i].getVideoViewerManager();
          if (manager.isShowingVideo()
              && manager.getActiveConnectionProperties().equals(connProperties)) {
            foundConnection = true;
            VideoConnectionProperties[] queue = new VideoConnectionProperties[viewerQueue.length];
            int queueIndex = 0;
            // Pushing back the main view into the queue ...
            queue[queueIndex++] = mainConnProperties;
            for (int k = 0; k < viewersQueueSize && k < queue.length; k++) {
              // ... and adding the others at the end, except the one which goes
              // now on the main view.
              if (viewerQueue[k].getVideoViewerManager().isShowingVideo() && k != i) {
                queue[queueIndex++] = 
                  viewerQueue[k].getVideoViewerManager().getActiveConnectionProperties();
              }
            }

            doCloseAllConnections();
            // Establish the main viewer connection
            connectMainCamera(connProperties);
            for (int k = 0; k < viewersQueueSize; k++) {
              if (queue[k] != null)
                doAddVideoToQueue(queue[k]);
            }
            break; // Go out of the for loop.
          }
        }
        
        if (!foundConnection) { // Video is not running, yet...
          boolean isQueueFull = true;
          for (int i = 0; i <  viewersQueueSize; i++) {
            if (!viewerQueue[i].getVideoViewerManager().isShowingVideo()) {
              isQueueFull = false;
              break;
            }
          }
          if (isQueueFull) {
            LOG.debug("Queue was already full. In that case we reject the last connection.");
            // In that case we reject the last connection in the queue
            // and return it to the calling method
            rejectedConnection = 
              viewerQueue[viewersQueueSize - 1].getVideoViewerManager().getActiveConnectionProperties();
          } 
          VideoConnectionProperties[] queue = new VideoConnectionProperties[viewerQueue.length];
          int queueIndex = 0;
          // Pushing back the main view into the queue ...
          queue[queueIndex++] = mainConnProperties;
          for (int i = 0; i < viewersQueueSize && queueIndex < viewersQueueSize; i++) {
            // ... and adding the others at the end
            if (viewerQueue[i].getVideoViewerManager().isShowingVideo()) {
              queue[queueIndex++] = 
                viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties();
            }
          }
          
          doCloseAllConnections();
          connectMainCamera(connProperties);
          for (int i = 0; i < viewersQueueSize; i++) {
            if (queue[i] != null)
              doAddVideoToQueue(queue[i]);
          }
        }
      }
      else {
        // No current active connection! In case that we find the requested main
        // connection in the queue we declare it as the main viewer. 
        boolean foundConnection = false;
        for (int i = 0; i < viewersQueueSize; i++) {
          manager = viewerQueue[i].getVideoViewerManager();
          if (manager.isShowingVideo()
              && manager.getActiveConnectionProperties().equals(connProperties)) {
            foundConnection = true;
            LOG.debug("Connection exists already, let's use it!");
            break;
          }
        }
        
        if (foundConnection) {
          // We have to reorder the queue
          final VideoConnectionProperties[] queue = new VideoConnectionProperties[viewerQueue.length];
          int queueIndex = 0;
          for (int i = 0; i < viewersQueueSize && i < queue.length; i++) {
            // Adding the others at the end, except the one which goes
            // now on the main view.
            if (viewerQueue[i].getVideoViewerManager().isShowingVideo()
                && !viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties().equals(connProperties)) {
              queue[queueIndex++] = 
                viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties();
            }
          }
          doCloseAllConnections();
          connectMainCamera(connProperties);
          for (int i = 0; i < viewersQueueSize; i++) {
            if (queue[i] != null)
              doAddVideoToQueue(queue[i]);
          }
        }
        else {
          VideoConnectionProperties[] queue = 
            new VideoConnectionProperties[viewerQueue.length];
          for (int i = 0; i < viewersQueueSize; i++) {
            queue[i] = 
              viewerQueue[i].getVideoViewerManager().getActiveConnectionProperties();
          }
          doCloseAllConnections();
          connectMainCamera(connProperties);
          for (int i = 0; i < viewersQueueSize; i++) {
            if (queue[i] != null)
              doAddVideoToQueue(queue[i]);
          }
        }
      } // end else case
    } catch (InterruptedException e) {
      LOG.error("setMainVideo() - ", e);
    }
    //  Release the access to the queue
    available.release();  

    return rejectedConnection;
  }
  
  /**
   * This shows an error dialog to the user which enables him to choose whether
   * he wants that the system tries again to establish a connection to the
   * specific camera or not. 
   * @param connProperties The video connection properties
   * @return <code>true</code>, if the user wants that another attempt shall
   *         be tried.
   */
  private boolean connectMainCamera(final VideoConnectionProperties connProperties) {
    // Establish the main viewer connection
    boolean result = false; 
    boolean retry = true;
    final IVideoViewerManager manager = centralViewer.getVideoViewerManager();

    if (!manager.isShowingVideo()) {
      while (!result && retry) {
        result = centralViewer.getVideoViewerManager().connectCamera(connProperties);
        if (result) {
          setAccessPointDescription(connProperties.getDescription());
          setKeysTaken(connProperties.getKeysTaken());
          retry = false;
        }
        else {
          final int response = JOptionPane.showConfirmDialog(this, 
              "Failed to open camera request \"" + connProperties.getHost()
              + ":" + connProperties.getCamera()
              + "\" on the main viewer.\nDo you want to retry?", "Error",
              JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
          
          retry = (JOptionPane.YES_OPTION == response);
        }
      }
    }
    else if (manager.isRunning(connProperties)) {
      result = true;
    }
    
    return result;
  }  
  
  
  /**
   * This is used by the DividerRecalculation class in order to 
   * do proper calculations
   * @return the height of the title label that is show above
   * each viewer in the queue.
   */
  public static int getQueueTitleLabelsHeight() {
    return queueTitleLabelHeight;
  }
  
  
  /**
   * This is used by the DividerRecalculation class in order to 
   * do proper calculations
   * @return the title label that is show above the central viewer
   */
  public static JLabel getTitleLabel() {
    return titleLabel;
  }


  @Override
  public void componentHidden(ComponentEvent e) {
    // Do nothing
  }

  @Override
  public void componentMoved(ComponentEvent e) {
    // Do nothing
  }

  /**
   * When this method is called the it recomputes the
   * location of the two dividers. This has to be done in
   * order to keep the aspect ratio of the screens.
   * 
   *  @param e The Component event
   */
  @Override
  public final synchronized void componentResized(final ComponentEvent e) {
    
    // The new dimension of the VideoMainPanel instance  
    Dimension dim = e.getComponent().getSize();
    
    // To provide the proper value to the DividerRecalculation
    setQueueTitleLabelHeight(viewerQueue[0].getTitleLabelHeight());
      
    DividerRecalculation calc = DividerRecalculation.getInstance(dim);
    
    // Set size and location of the horizontal divider
    centerSplitPane.setDividerLocation(calc.getHorizontalDividerLocation());
    centerSplitPane.setDividerSize(calc.getHorizontalDividerSize());
    
    // Set size and location of the vertical divider
    this.setDividerSize(calc.getVerticalDividerSize());
    this.setDividerLocation(calc.getVerticalDividerLocation());
  }


  @Override
  public void componentShown(final ComponentEvent arg0) {
    // Do nothing
  }
  
  
  /**
   * Refreshes all video viewer instances
   */
  public final void refresh() {
    centralViewer.getVideoViewerManager().refresh();
    for (int i = 0; i < viewersQueueSize; i++) {
      viewerQueue[i].getVideoViewerManager().refresh();
    }
  }
  
  
  /**
   * Sets the amount of keys in the display that have been taken.
   * @param keysTaken The amount of keys that have been taken
   */
  protected final void setKeysTaken(final int keysTaken) {
    infoPanel.setKeysTaken(keysTaken);
  }
  
  
  /**
   * Sets the description of the access point into the text area.
   * @param accessPointDescription The description text of the access point
   */
  protected final void setAccessPointDescription(final String accessPointDescription) {
    infoPanel.setAccessPointDescription(accessPointDescription);
  }
  
  
  /**
   * Closes the connection of the main video viewer.
   * Please note, that this method should only be used within a semaphore!
   */
  protected final synchronized void closeMainVideoConnection() {
    centralViewer.getVideoViewerManager().closeAllConnections();
    infoPanel.reset();
  }
  
  
  /**
   * Creates the central View.
   */
  private void createCentralView() {
    JPanel centerPanel = new JPanel(new BorderLayout());
    
    // Font of the title
    Font titleFont = new Font("Arial", Font.BOLD, 24);
    
    // Configuration of the title label
    titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    titleLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
    titleLabel.setFont(titleFont);
    titleLabel.setForeground(Color.blue);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
    
    centerPanel.add(titleLabel, BorderLayout.NORTH);
    centerPanel.add(centralViewer.getVideoViewerComponent(), BorderLayout.CENTER);
    
    centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    centerSplitPane.setEnabled(false);
    centerSplitPane.add(centerPanel, JSplitPane.TOP);
    centerSplitPane.add(infoPanel, JSplitPane.BOTTOM);
    
    this.add(centerSplitPane, JSplitPane.LEFT);
  }
  
  
  /**
   * Creates the video queue view that is on the right side
   */
  private void createVideoQueueView() {
    queuePanel = new JPanel(new GridLayout(viewersQueueSize, 1, 0, 5));
    
    for (int i = 0; i < viewersQueueSize; i++)
      queuePanel.add(viewerQueue[i].getVideoViewerComponent());
    
    this.add(queuePanel, JSplitPane.RIGHT);
  }
  
  
  /**
   * Some initialization on the split pane view
   */
  private void initSplitPane() {
    this.addComponentListener(this);
    
    this.setDividerSize(10);
    this.setEnabled(false);
  }
}
