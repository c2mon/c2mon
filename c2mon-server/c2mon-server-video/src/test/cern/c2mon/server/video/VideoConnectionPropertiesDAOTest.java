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

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.video.VideoConnectionMapper;
import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;

/**
 * Tests the iBatis mapper against the Oracle DB.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/video/config/server-video-test.xml" })
public class VideoConnectionPropertiesDAOTest {

  /** the video system name used in the queries */
  private final String VIDEOSYSTEMNAME = "MANOS";
  
  /**
   * To test.
   */
  @Autowired
  private VideoConnectionMapper videoConnectionMapper;    
  
  
  /**
   * Tests selectAllVideoConnectionProperties
   * @throws SQLException 
   */
  @Test
  public void testSelectAllVideoConnectionProperties()  {
    
    List vcpList = null;
    
    try {
      vcpList = videoConnectionMapper.selectAllVideoConnectionProperties(VIDEOSYSTEMNAME);
      
      assert (vcpList.size() == 2); // the query must return 2 valid VCP's (defined in data.sql)
      
      Iterator iter = vcpList.iterator();
      while (iter.hasNext()) {
        
        Object o = iter.next();
        
        assert (o instanceof VideoConnectionProperties);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Tests selectAllPermitedRoleNames
   * @throws SQLException 
   */
  @Test
  public void testselectRbacDetails()  {
    
    RbacAuthorizationDetails details = null;
    
    try {
      details = videoConnectionMapper.selectAuthorizationDetails(VIDEOSYSTEMNAME);
      
      assertTrue (details != null);
      assertTrue (details.getRbacDevice() != null);
      assertTrue (details.getRbacProperty() != null);
      assertTrue (details.getRbacClass() != null);
      
      details.getRbacDevice();
      details.getRbacProperty();
      details.getRbacClass();
      
      // the correct query results (defined in data.sql)
      
      assert (details.getRbacDevice().equals("DEVICE3"));
      assert (details.getRbacProperty().equals("PROPERTY3"));
      assert (details.getRbacClass().equals("Class3"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
