/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.video.test;

import java.sql.SQLException;

import java.sql.Time;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.common.video.VideoConnectionPropertiesCollection;
import cern.c2mon.server.video.VideoConnectionMapper;
import cern.c2mon.server.video.VideoConnectionPropertiesDAO;

/**
 * Tests the iBatis mapper against the Oracle DB.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/video/config/test/server-video-test.xml"})
public class VideoConnectionPropertiesDAOTest {

  private static String VIDEOSYSTEMNAME = "MANOS";
  
  /**
   * To test.
   */
  @Autowired
  private VideoConnectionMapper videoConnectionMapper;    
  
  /**
   * Removes test values from previous tests in case clean up failed.
   */
  @Before
  public void beforeTest() {
    removeTestData();
  }
  
  /**
   * Removes test values after test.
   */
  @After
  public void afterTest() {
    removeTestData();
  }
  
  /**
   * Removes test data.
   */
  private void removeTestData() {
    
//    videoConnectionMapper.deleteDataTagLog(ID);
  }
  
  /**
   * Tests selectAllVideoConnectionProperties
   * @throws SQLException 
   */
  @Test
  public void testSelectAllVideoConnectionProperties()  {
    
    List roleNamesList = null;
    
    try {
      roleNamesList = videoConnectionMapper.selectAllVideoConnectionProperties(VIDEOSYSTEMNAME);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    Iterator iter = roleNamesList.iterator();
    
    assert (roleNamesList.size()==2); // the query returns 2 valid results (defined in data.sql)
    
    while (iter.hasNext()) {
      
      Object o = iter.next();
      
      assert (o instanceof VideoConnectionProperties);
      if (o instanceof VideoConnectionProperties) {
        
        System.out.println(((VideoConnectionProperties) o).getLogin());
      }
    }
  }
  
  /**
   * Tests selectAllPermitedRoleNames
   * @throws SQLException 
   */
  @Test
  public void testSelectAllPermitedRoleNames()  {
    
    List roleNamesList = null;
    
    try {
      roleNamesList = videoConnectionMapper.selectAllPermitedRoleNames(VIDEOSYSTEMNAME);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    Iterator iter = roleNamesList.iterator();
    
    while (iter.hasNext()) {
      
        Object o = iter.next();
        System.out.println(o.toString());
    }
  }
}
