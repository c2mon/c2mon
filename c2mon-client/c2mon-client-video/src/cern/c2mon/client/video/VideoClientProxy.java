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


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.video.VideoConnectionPropertiesCollection;
import cern.c2mon.client.common.video.VideoConnectionsRequest;
import cern.tim.shared.client.auth.SessionInfo;
import cern.tim.shared.client.auth.impl.TimSessionInfoImpl;
import cern.tim.util.jms.ActiveJmsSender;

/**
 * The VideoClientProxy is responsible for establishing the connection to
 * the server in order to retrieve information about the video connection
 * properties. It uses therefore the JMSProxy class. Please note, that this
 * class is implemented as Singleton.
 * 
 * @author Matthias Braeger
 */
public final class VideoClientProxy {

  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(VideoClientProxy.class);
  
  /**
   * Reference to the bean managing the JMS
   * sending.
   */
  private ActiveJmsSender jmsSender; 
  
  /** Default timeout to be used for requests to the TIM server */
  private int timeout = 15000;
  
  /**
   * Name of request queue.
   */
  private String requestQueue;
  
  /**
   * Autowired constructor.
   * @param pJmsSender 
   */
  @Autowired
  public VideoClientProxy(ActiveJmsSender pJmsSender) {
    
    super();
    this.jmsSender = pJmsSender;       
  }
  
  private VideoConnectionPropertiesCollection executeVideoRequest (final TimSessionInfoImpl sessionInfo
      ,final String videoSystemName) {
    
    String jsonRequest = new VideoConnectionsRequest(sessionInfo, videoSystemName).toJson();
    
    String jsonResponse = jmsSender.sendRequestToQueue(jsonRequest, this.requestQueue, this.timeout);
    
    return VideoConnectionPropertiesCollection.fromJsonResponse(jsonResponse);
  }
  
  /**
   * Requests from the server all connection properties that are relevant for
   * the specific video system. The name of the video system is defined in
   * the dedicated System property variable.
   * 
   * @param sessionInfo Information about the current logged user
   * @param videoSystemName The name of the video system that defined in the 
   *                        Video Viewer properties file.
   * @return A collection of all VideoConnectionProperties objects that are
   *         relevant for the specific video system.
   */
  public VideoConnectionPropertiesCollection requestVideoConnectionInformation(
      final TimSessionInfoImpl sessionInfo, final String videoSystemName) {
    
    VideoConnectionPropertiesCollection result = null;
    
    if (sessionInfo != null && sessionInfo.isValidSession()) {
      
      result = executeVideoRequest (sessionInfo, videoSystemName) ;
    }
    return result;
  }

}
