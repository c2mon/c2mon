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

package cern.c2mon.shared.video;


import javax.validation.constraints.NotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.command.AuthorizationDetails;
import cern.c2mon.shared.util.json.GsonFactory;



/**
 * This class is used for REQUESTING a set of VideoConnectionProperties from 
 * the system.
 * The client sends a VideoConnectionsRequest object to the application server 
 * as an ObjectMessage on a specified queue.
 * <br>
 * The request must at least contain 
 * <OL>
 *   <LI> the name of the video system which is needed 
 *        to determine the relevant connections
 * </OL>
 * In addition, the message header must contain a reply topic. If no reply topic
 * is specified, the request will be ignored by the server.
 * 
 * @author Matthias Braeger, ekoufaki
 */
public class VideoRequest {

  /** The name of the video system whose connection list is requested */
  private final String videoSystemName;
  
  /** 
   * Enumeration for specifying the expected result type of the response.
   * The two values correspond to <code>RbacAuthorizationDetails</code> and
   * <code>VideoConnectionProperties</code>.
   */
  public enum RequestType {
    AUTHORIZATION_DETAILS_REQUEST,
    VIDEO_CONNECTION_PROPERTIES_REQUEST
  };
  
  /** The expected result type */
  @NotNull
  private final RequestType requestType; 
  
  /**
   * Default Constructor needs specifying the result type of the response message.
   * The request type is then automatically determined by the constructor.
   * @param videoSystemName The name of the video system whose connection list is requested
   * @param clazz Return type of the request 
   */
  public VideoRequest(final  String videoSystemName, final Class clazz) {
    
    if (clazz == RbacAuthorizationDetails.class) {
      requestType = RequestType.AUTHORIZATION_DETAILS_REQUEST;
    }    
    else if (clazz == VideoConnectionProperties.class) {
      requestType = RequestType.VIDEO_CONNECTION_PROPERTIES_REQUEST;
    } 
    else {
      throw new UnsupportedOperationException(
          "The result type " + clazz + " is not supported by this class.");
    }
    this.videoSystemName = videoSystemName;    
  }

  /**
   * @return the videoSystemName for which the connection properties are requested
   */
  public final String getVideoSystemName() {
    return videoSystemName;
  }
  
  /**
   * @return The expected <code>ResultType</code> of the response message
   */
  public RequestType getRequestType() {
    return requestType;
  }
}
