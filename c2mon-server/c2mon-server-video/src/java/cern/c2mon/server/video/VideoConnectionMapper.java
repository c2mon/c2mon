/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.video;

import java.sql.SQLException;
import java.util.List;

import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;

/**
 * @author ekoufaki
 */
public interface  VideoConnectionMapper {
  
  /**
   * Sends a query to the database that returns Authorization Details for a specific video system. 
   * @param videoSystemName The name of the video system to which the Authorization Details belong.
   * @return RbacAuthorizationDetails for the requested video system.
   * @throws SQLException In case an error occurs during the query
   */
  RbacAuthorizationDetails selectAuthorizationDetails(final String videoSystemName) throws SQLException;

  /**
   * Sends a query to the database that returns all video connection properties of a specific
   * video system.
   * @param videoSystemName The name of the video system to which the connection properties belongs
   * @return A list of VideoConnectionProperties objects
   * @throws SQLException In case an error occurs during the query
   */
  List<VideoConnectionProperties> selectAllVideoConnectionProperties(final String videoSystemName) throws SQLException;
}
