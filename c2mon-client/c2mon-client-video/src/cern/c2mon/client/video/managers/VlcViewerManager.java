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
package cern.c2mon.client.video.managers;

import java.awt.Canvas;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.videolan.jvlc.JVLC;
import org.videolan.jvlc.MediaDescriptor;
import org.videolan.jvlc.MediaPlayer;
import org.videolan.jvlc.event.MediaPlayerListener;

import com.sun.jna.Platform;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.IVideoViewerManager;
import cern.c2mon.client.video.VideoPropertyNames;
import cern.c2mon.client.video.VideoViewerConfigLoader;
import ch.cern.od.client.OdClient;

/**
 * An instance of this manager controls exactly one instance 
 * of the <code>VlcViewer</code>. It handles the communication with the VLC
 * library via JVLC.
 * 
 * @author Matthias Braeger
 *
 */
public class VlcViewerManager implements IVideoViewerManager {
  
  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(VlcViewerManager.class);

  /** The default VLC arguments to pass during the JVLC instantiation */
  private static final String[] VLC_ARGS; 
    
  /** Is used for displaying the redirected VLC image. */
  private final Canvas viewerCanvas;
  
  /** The JVLC which is used to communicate with the native libraries */
  private final JVLC jvlc;
  
  /** Used to start and stop the running video stream */
  private MediaDescriptor mediaDescriptor = null;
  
  /** The properties of the current connection, if any. */
  private VideoConnectionProperties currentConn = null;
  
  /** The localPort on which the current Connection is being forwarded */
  private Integer localPort = null;
  
  /** The current odclient to use to communicate with the odserver */
  private OdClient odclient = null;
  
  /** The watchdog timer process */
  private Timer watchdog = null;
  
  /**
   * Initializing VLC
   */
  static {
    if (Platform.isWindows()) {
      LOG.info("Trying to load VLC library 'libvlccore'...");
      System.loadLibrary("libvlccore");
      LOG.info("Trying to load VLC library 'libvlc'...");
      System.loadLibrary("libvlc");
      LOG.info("Trying to load VLC library 'axvlc'...");
      System.loadLibrary("axvlc");
    }
    String args = 
      (String) VideoViewerConfigLoader.getInstance().getPropertyByName(VideoPropertyNames.VLC_ARGS);
    LOG.info("vlc arguments: "+ args);
    VLC_ARGS = args.split(" ");
  }
  
  
  /**
   * Default constructor.
   */
  public VlcViewerManager() {
    this.viewerCanvas = new Canvas();
    
    jvlc = new JVLC(VLC_ARGS);
  }
  

  /**
   * Starts playing the video stream
   * @return <code>true</code>, in case the video stream could successfully be started.
   */
  private boolean play(final VideoConnectionProperties connProperties) {
    final MediaPlayer mediaPlayer = mediaDescriptor.getMediaPlayer();
    mediaPlayer.setParent(viewerCanvas);
    new Thread(new Runnable() {
      @Override
      public void run() {
        final MediaPlayer mediaPlayer = mediaDescriptor.getMediaPlayer();
        mediaPlayer.play();
      }
    }).start();
    
    int count = 0;
    
    while (!mediaPlayer.hasVideoOutput() && count < 10) { // Sleeps up to 5 sec
      try {
        LOG.debug("play() - Waiting that VLC reports video output..."); 
        count++;
        Thread.sleep(500);
      } catch (InterruptedException e) {
        LOG.error(e);
      } 
    }
    
    return mediaPlayer.hasVideoOutput();
  }


  /**
   * Returns the viewer <code>Canvas</code> which is used for integrating
   * the VLC video image into Java.
   * @return The viewer canvas.
   */
  public final Canvas getViewerCanvas() {
    return viewerCanvas;
  }


  @Override
  public void closeAllConnections() {
    LOG.debug("closeAllConnections() called");
    stopWatchdog();
    if (mediaDescriptor != null) {
      mediaDescriptor.getMediaPlayer().stop();
      mediaDescriptor.getMediaPlayer().release();
      mediaDescriptor.release();
      
      LOG.info("closeAllConnections() - Stop on demand stream.");
      odclient.stop(this.localPort, this.currentConn.getCamera());
      mediaDescriptor = null;
    }
    currentConn = null;
  }


  @Override
  public boolean closeCameraConn(final VideoConnectionProperties connProperties) {
    if (currentConn != null && connProperties.equals(currentConn)) {
      closeAllConnections();
      return true;
    }
    
    return false;
  }


  @Override
  public boolean connectCamera(final VideoConnectionProperties connProperties) {
    boolean retval = false; // Return value 
    Integer localPortAddr = null; // The local port received by the odserver
    
    // By definition a camera port for VLC must be greater than zero.
    // If this is not the case the call is ignored.
    if (connProperties != null && connProperties.getCamera() > 0) {
      try {
        // Creating a new OdClient
        try {
          odclient = new OdClient(connProperties.getHost());
        } catch (Exception e) {
          LOG.error("connectCamera() - An exception occured while trying to create an odclient", e); 
        }
        if (odclient != null) {
          LOG.debug("connectCamera() - Request video from odserver for camera on port "+ connProperties.getCamera());
          localPortAddr = odclient.requestVideo(connProperties.getCamera(), true);
          final String ipAddr = InetAddress.getLocalHost().getHostAddress();
          if (localPortAddr != null) {
            this.mediaDescriptor = 
              new MediaDescriptor(jvlc, "rtp://@" + ipAddr + ':' + localPortAddr.toString());
            this.currentConn = connProperties;
            this.localPort = localPortAddr;
          }
          else {
            LOG.warn("connectCamera() - odserver declined the video request! "
                + "This host does not to have the rights to ask for streams");
            return false;
          }
        }
      } catch (IOException e) {
        LOG.error(e);
      }
  
      if (mediaDescriptor != null) {
        final MediaPlayer mediaPlayer = mediaDescriptor.getMediaPlayer();
        mediaPlayer.addListener(new MediaPlayerListener() {
  
          public void endReached(final MediaPlayer mediaPlayer) {
            LOG.debug("Media instance end reached. MRL: " + mediaPlayer.getMediaDescriptor().getMrl());
          }
  
          public void paused(final MediaPlayer mediaPlayer) {
            LOG.debug("Media instance paused. MRL: " + mediaPlayer.getMediaDescriptor().getMrl());
          }
  
          public void playing(final MediaPlayer mediaPlayer) {
            LOG.debug("Media instance played. MRL: " + mediaPlayer.getMediaDescriptor().getMrl());
          }
          
          public void positionChanged(final MediaPlayer mediaPlayer) {
            // TODO Auto-generated method stub
          }
          
          public void timeChanged(final MediaPlayer mediaPlayer, final long newTime) {
            // TODO Auto-generated method stub
          }
          
          public void stopped(final MediaPlayer mediaPlayer) {
            LOG.debug("Media player stopped. MRL: " + mediaPlayer.getMediaDescriptor().getMrl());
          }
          
          public void errorOccurred(final MediaPlayer mediaPlayer) {
            LOG.debug("An error has occurred.");
          }
        });
        
        if (play(connProperties)) {
          // Set the properties as actual properties
          startWatchdog(connProperties, localPortAddr);
          retval = true;
        }
        else {
          closeAllConnections();
        }
      } // end if
    } // end if
    
    if (!retval && connProperties != null) {
      LOG.warn(
          "Couldn't initialize the connection to " 
          + connProperties.getDescription() + ".");
    }
      
    return retval;
  }


  /**
   * Creates a new watchdog timer thread for the given video stream.
   * 
   * @param connProperties The video connection properties
   * @param pLocalPort The local port on which the video stream is sent to
   */
  private void startWatchdog(final VideoConnectionProperties connProperties, final Integer pLocalPort) {
    stopWatchdog();
    watchdog = new Timer();
    // Runs every 30 sec and starts with 30 seconds delay
    watchdog.schedule(new OdWatchdog(connProperties, pLocalPort), 30000L, 30000L);
  }


  /**
   * Stops the currently running watchdog process.
   */
  private void stopWatchdog() {
    if (watchdog != null) { 
      watchdog.cancel();
      watchdog = null; 
    }
  }


  @Override
  public boolean isRunning(final VideoConnectionProperties connProperties) {
    if (connProperties != null && currentConn != null) {
      return connProperties.equals(currentConn);
    }
    
    return false;
  }


  @Override
  public boolean isShowingVideo() {
    if (currentConn != null)
      return true;
    
    return false;
  }


  @Override
  public void refresh() {
    // TODO Auto-generated method stub
    
  }


  @Override
  public VideoConnectionProperties getActiveConnectionProperties() {
    return currentConn;
  }
  
  
  /**
   * This inner class is used for setting up a watchdog process to confirm
   * regularly the playing stream. That prevents the server to stop the
   * port forwarding.
   * 
   * @author Matthias Braeger
   *
   */
  class OdWatchdog extends TimerTask {
    /** The video properties for this watchdog process */ 
    private final VideoConnectionProperties connProperties;
    /** The local port on which the video stream is sent to. */
    private final Integer localPort;    
    
    /**
     * Default Constructor
     * @param connProperties Connection properties
     * @param pLocalPort The local port on which the video stream is sent to
     */
    protected OdWatchdog(final VideoConnectionProperties connProperties, Integer pLocalPort) {
      this.connProperties = connProperties;
      this.localPort = pLocalPort;
    }
    
    @Override
    public void run() {
      LOG.debug("Running watchdog for " + this.connProperties.getCamera());
      if (currentConn != null && !odclient.confirmRequest(this.localPort, this.connProperties.getCamera())) {
        LOG.warn("Received KO for watchdog!");
      }
    }
  }
}
