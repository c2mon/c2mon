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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.auth.SessionListener;
import cern.c2mon.client.auth.SessionManager;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.common.video.VideoConnectionPropertiesCollection;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.tim.shared.client.auth.SessionInfo;
import cern.tim.shared.client.auth.impl.TimSessionInfoImpl;


/**
 * This class registers all data tags which are relevant for the Video view.
 * Therefore it registers itself at the SessionManager instance which informs
 * it about the login and logout of the users. Only if the Role of the user
 * allows him to use the TIM Video Viewer it starts with the registration and
 * request of the data tags.<br>
 * Furthermore it creates new video connection requests in case that the
 * state of one of the registered data tag id's changes.
 * 
 * @author Matthias Braeger
 */
public class VideoConnectionRequester implements SessionListener, DataTagUpdateListener {
  
  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(VideoConnectionRequester.class);
  
  /** The handler of the VideoMainPanel */
  private VideoMainHandler handler = null;
  
  /** The Video Proxy Client */
  @Autowired
  private VideoClientProxy videoClientProxy;
  
  /** Handles the interaction with the server */
  private final C2monTagManager tagManager;
  
  /** A collection of all the subscribed data tags */
  private Set<Long> tagSubscriptions = null;
  
  /** The name of the supported video system */
  private final String videoSystemName;
  
  /** 
   * Contains the collection of relevant 
   * VideoConnectionProperties for the supported video system 
   */
  private VideoConnectionPropertiesCollection connections = null;
  
  /** 
   * Can be either NULL or Float representation of the host IP 
   * from which it is allowed to send video requests 
   */
  private final Float requesterIPAsFloat;
  
  /**
   * true, if the data tags for updating the keys taken counter
   * shall be registered
   */
  private final boolean registerKeysTakenDataTag;
  
  /**
   * Default Constructor
   * @param videoMainHandler The handler of the video panel
   */
  public VideoConnectionRequester(final VideoMainHandler videoMainHandler) throws UnknownHostException {
    this(videoMainHandler, null);
  }
  
  /**
   * Default Constructor
   * @param videoMainHandler The handler of the video panel
   * @param controllerHostName The name of the host that is allowed to control the application
   * @throws UnknownHostException can be thrown to indicate that the IP address of the
   * requester host can not be determined.
   */
  public VideoConnectionRequester(final VideoMainHandler videoMainHandler, final String controllerHostName) throws UnknownHostException {
    this.handler = videoMainHandler;
    
    if (controllerHostName != null) {
      InetAddress requesterIPAddress = InetAddress.getByName(controllerHostName);
      requesterIPAsFloat = getIPasFloat(requesterIPAddress);
    }
    else {
      requesterIPAsFloat = null;
    }
    
    // Create an asynchronous TimClient
    this.tagManager = C2monServiceGateway.getTagManager();
    
    // Load the user configuration for the video system name
    videoSystemName = 
      (String) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VIDEO_SYSTEM_NAME);
    
    // Load the user configuration
    registerKeysTakenDataTag = 
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.KEYS_TAKEN_COUNTER);
    
    // Register at session manager which will inform us when a user does a login or logout.
    SessionManager.getInstance().addSessionListener(this);
  }
  
  /**
   * Subscribes all relevant data tags to the TimClient.
   */
  @SuppressWarnings("unchecked")
  private void subscribe() {
    boolean isVlcUsed = 
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VLC_PLAYER);
    
    if (tagSubscriptions == null)
      tagSubscriptions = new HashSet<Long>();
    else
      tagSubscriptions.clear();
    
    Iterator<VideoConnectionProperties> iter =
      connections.iterator();
    
    VideoConnectionProperties props = null;
    while (iter.hasNext()) {
      props = iter.next();
      if (isVlcUsed && !(props.getCamera() > 0)) {
        // For VLC we are using the odserver and the camera id is representing
        // the remote port on which we can grab the signal. Zero means by convention
        // "not supported".
        LOG.info("Camera for "+ props.getDescription() + " is not yet supported: camera port is set to 0.");
      }
      else {
        tagSubscriptions.add(props.getQueueingTagId());
        tagSubscriptions.add(props.getActivationTagId());
        if (registerKeysTakenDataTag)
          tagSubscriptions.add(props.getKeysTakenTagId());
      }
      
    }
    
    if (tagSubscriptions.size() > 0)
      tagManager.subscribeDataTags(tagSubscriptions, this);
  }
  
  /**
   * Unsubscribe from all registered data tags 
   */
  public final void unsubscribe() {
    if (this.tagSubscriptions != null) {
      tagManager.unsubscribeDataTags(this.tagSubscriptions, this);
      tagSubscriptions.clear();
      connections.clear();
    }
    handler.closeAllConnections();
  }
  
  /**
   * Can be used to connect the requester to another video handler. Normally
   * this is not needed.
   * @param videoMainHandler The new video main handler. 
   */
  public final void setVideoMainHandler(final VideoMainHandler videoMainHandler) {
    this.handler = videoMainHandler;
  }
  
  /**
   * Triggers the <code>TimVideoViewer</code> to update the camera menu.  
   */
  private void createCameraMenu(final VideoConnectionPropertiesCollection connections) {
    TimVideoViewer.getInstance().createViewMenuItems(connections);
  }
  
  /**
   * Method implementation from interface SessionListener. This method is
   * called when a user has successfully logged in. It then requests the
   * camera connection information from the TIM server.
   * @param sessionInfo The session information
   */
  public final void onLogin(final TimSessionInfoImpl sessionInfo) { 
    new Thread(new Runnable() {
      public void run() {
        if (sessionInfo.isValidSession()) {
          connections = 
            videoClientProxy.requestVideoConnectionInformation(
                sessionInfo, videoSystemName);
          
          if (connections != null && connections.size() > 0) {
            subscribe();
            createCameraMenu(connections);
          }
          else {
            LOG.error("No video connection properties received by server.");
            JOptionPane.showMessageDialog(TimVideoViewer.getInstance(), "No video connection properties received by server."
                + "\nDo you really have the right priveleges to see the videos?\nThe best is"
                + " to write an email to tim.support@cern.ch",
                "Authorization Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }).start();
    
    // Returns the focus to the main frame
    TimVideoViewer.getInstance().getContentPane().requestFocus();
  }

  /**
   * Method implementation from interface SessionListener. This method is
   * called when a user has logged out. It then unsubscribes from all data tags
   * and removes the camera selection from the View menu.
   * @param sessionInfo The session information. 
   */
  public void onLogout(final SessionInfo sessionInfo) {
    unsubscribe();
    TimVideoViewer.getInstance().createViewMenuItems(null);
    // Returns the focus to the main frame
    TimVideoViewer.getInstance().getContentPane().requestFocus();
  }


  /**
   * Method implementation from interface DataTagUpdateListener. It is called
   * whenever there is an update on a subscribed data tag. <br>
   * Depending on the purpose of the data tag it can fire one of the 3 different
   * tasks:
   * <dl>
   * <li> Add a specific video connection request to the end of the queue of waiting requests
   * <li> Set immediately a specific video connection as the active video on the main screen
   * <li> Update the number of taken keys from a specific video connection. 
   * </dl> 
   * @param clientDataTag The data tag which was updated. 
   */
  @SuppressWarnings("unchecked")
  public final void onUpdate(final ClientDataTagValue clientDataTag) {
    if (LOG.isDebugEnabled())
      LOG.debug("onUpdate() : value received for tag " 
          + clientDataTag.getId() + " : "
          + clientDataTag.getValue() + " : valid? : " 
          + clientDataTag.isValid() + " : "
          + clientDataTag.getDataTagQuality().getDescription());
       
    
    try {
      if (clientDataTag.isValid() && clientDataTag.getDataTagQuality().isValid()) {
        Iterator<VideoConnectionProperties> iter =
          connections.iterator();
        
        // try to find corresponding connection property
        VideoConnectionProperties connProperties = null;
        while (iter.hasNext()) {
          connProperties = iter.next();
          
          if (clientDataTag.getId().longValue() == connProperties.getQueueingTagId()) {
            if (LOG.isDebugEnabled())
              LOG.debug("Queue video connection: host "
                  + connProperties.getHost() + ",camera "
                  + connProperties.getCamera());
            
            Float ipAddress = (Float) clientDataTag.getValue();
            if (ipAddress == 0) {
              // This means we have to close the video connection
              handler.closeVideoConnection(connProperties);
            }
            else if (isValidRequesterIP(ipAddress)) {
              // If we are here then the request is for us and not for another instance
              // of the TIM Video Viewer
              handler.addVideoRequest(connProperties);
            }
            else {
              // We have to close the video connection, because the requested call has been
              // taken by another TVV instance.
              handler.closeVideoConnection(connProperties);
            }
          }
          else if (clientDataTag.getId().longValue() == connProperties.getActivationTagId()) {
            if (LOG.isDebugEnabled())
              LOG.debug("Set new active video connection: host " + connProperties.getHost() + ", camera " + connProperties.getCamera());
            
            Float ipAddress = (Float) clientDataTag.getValue();
            if (ipAddress == 0) {
              // In case the IP address is zero we do nothing. To close the connection we have to set
              // the appropriate queuing tag to zero.
            }
            else if (isValidRequesterIP(ipAddress)) {
              // If we are here then the request comes from the right requester and not for another instance
              // of the TIM Video Viewer
              handler.setActiveVideoConncetion(connProperties);
            } 
          }
          else if (clientDataTag.getId().longValue() == connProperties.getKeysTakenTagId()) {
            // We expect a Float object that we cast then to an integer
            Float keysTaken = (Float) clientDataTag.getValue();
            
            if (LOG.isDebugEnabled())
              LOG.debug("Update of taken keys: " + keysTaken.intValue()
                  + " for host " + connProperties.getHost() + ", camera "
                  + connProperties.getCamera());
            
            connProperties.setKeysTaken(keysTaken.intValue());
            handler.updateKeysTaken(connProperties);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("An exception occured: " + e.getMessage());
      e.printStackTrace();
    }
    
  }
  
  /**
   * It is possible only to accept requests that comes from a
   * well defined source
   * @param ipAddress an integer representation of the IP address. If the
   *        value is 1, then it will be always return true. The reason is that
   *        1 is equals a broadcast call.
   * @return true, if the requester IP is valid or 1 was given as 
   *         <code>ipAddress</code> value.
   */
  private boolean isValidRequesterIP(final Float ipAddress) {
    boolean retval = false;
    
    if (requesterIPAsFloat == null || ipAddress.intValue() == 1) {
      retval = true;
    } else 
      retval = requesterIPAsFloat.equals(ipAddress);
    
    return retval;
  }
  
  /**
   * Translates the IP address into a float value
   * @param ipAddress the IP address
   * @return The IP address as long value
   */
  private Float getIPasFloat(final InetAddress ipAddress) {
    String[] test = ipAddress.getHostAddress().split("\\.");
    StringBuffer ipAsFloat = new StringBuffer();
    
    for (int i = 0; i < test.length; i++) {
      while (test[i].length() < 3)
        test[i] = "0" + test[i];
      
      ipAsFloat.append(test[i]);
    }
    
    return new Float(ipAsFloat.toString());
  }

  @Override
  public void onLogin(SessionInfo pSessionInfo) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onSuspend(SessionInfo pSessionInfo, boolean isSuspended) {
    // TODO Auto-generated method stub
    
  }
}
