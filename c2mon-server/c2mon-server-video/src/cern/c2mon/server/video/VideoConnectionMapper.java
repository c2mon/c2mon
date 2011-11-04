package cern.c2mon.server.video;

import java.sql.SQLException;
import java.util.List;

import cern.c2mon.client.common.video.VideoConnectionProperties;

public interface  VideoConnectionMapper {

   public List selectAllPermitedRoleNames (final String videoSystemName) 
     throws SQLException;
   
   public List selectAllVideoConnectionProperties (final String videoSystemName) 
     throws SQLException;
     
}
