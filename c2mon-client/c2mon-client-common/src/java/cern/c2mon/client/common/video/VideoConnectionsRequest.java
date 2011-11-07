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

package cern.c2mon.client.common.video;


import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.tim.util.json.GsonFactory;



/**
 * This class is used for REQUESTING a set of VideoConnectionProperties from 
 * the system.
 * The client sends a VideoConnectionsRequest object to the application server 
 * as an ObjectMessage on a specified queue.
 * <br>
 * The request must at least contain 
 * <OL>
 *   <LI> the privileges of the User
 *   <LI> the name of the video system which is needed 
 *        to determine the relevant connections
 * </OL>
 * In addition, the message header must contain a reply topic. If no reply topic
 * is specified, the request will be ignored by the server.
 * 
 * @author Matthias Braeger
 */
public class VideoConnectionsRequest {

  /**
   * Version number of the class used during serialization/deserialization.
   * This is to ensure that minor changes to the class do not prevent us
   * from reading back VideoConnectionsRequest objects we have serialized 
   * earlier. If fields are added/removed from the class, the version 
   * number needs to change.
   */
  private static final long serialVersionUID = 4009739147206968637L;

  /** The session information that contains the user's privileges */
//  private final TimSessionInfoImpl sessionInfo;
  
  /** The name of the video system whose connection list is requested */
  private final String videoSystemName;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();
  
  /**
   * Default Constructor
   * @param sessionInfo  The session information that contains the user's privileges
   * @param videoSystemName The name of the video system whose connection list is requested
   */
  public VideoConnectionsRequest(final  String videoSystemName ) {
    
//    this.sessionInfo = sessionInfo;
    this.videoSystemName = videoSystemName;
  }
  
  /**
   * Checks whether the role name is in the TimSessionInfo object of the user requester
   * @param pPrivName The role name
   * @return true, if the role name is part of the internal TimSessionInfo
   */
  public final boolean hasPrivilege(String pPrivName) {
//    return sessionInfo.hasPrivilege(pPrivName);
    throw new UnsupportedOperationException () ;
  }

  /**
   * @return the videoSystemName for which the connection properties are requested
   */
  public final String getVideoSystemName() {
    return videoSystemName;
  }
  
  public static VideoConnectionsRequest fromJsonResponse(String text) {
    
    return GSON.fromJson(text, VideoConnectionsRequest.class);
  }

  public String toJson() {
    
    return GSON.toJson(this);
  }
}
