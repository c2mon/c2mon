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
