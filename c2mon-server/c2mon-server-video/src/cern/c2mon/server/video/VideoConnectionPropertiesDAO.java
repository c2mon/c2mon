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

package cern.c2mon.server.video;

import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

import cern.c2mon.client.common.video.VideoConnectionProperties;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;


/**
 * This class represents a Data Access Object (DAO) to retrieve
 * VideoConnectionProperties information out of the data base. The
 * connection to the data base is realized with Apache iBATIS. <br>
 * iBATIS provides a very simple and flexible means of moving data between
 * Java objects and a relational database.<br><br>
 * The related iBATIS XML mapping files are located in the 
 * sqlmap/maps directory.
 * 
 * @author Matthias Braeger
 */
public class VideoConnectionPropertiesDAO {
  
  /** Log4j Logger for this class */
  private static final Logger LOG = Logger.getLogger(VideoConnectionPropertiesDAO.class);
  
  /** Private static instance of the SQLMapClient */
  private static SqlMapClient sqlMapper;
  
  /**
   * The iBatis factory used to acquire database sessions.
   */
  private SqlSessionFactory sqlSessionFactory;
  
  /**
   * The mapper class name used for creating the batch logger mapper from the session.
   */
  private Class< ? extends VideoConnectionMapper> mapperInterface;
  
  /**
   * 
   * @param sqlSessionFactory
   * @param mapperInterface the auto-commit mapper for single queries/inserts
   * @throws ClassNotFoundException 
   */
  public VideoConnectionPropertiesDAO(SqlSessionFactory sqlSessionFactory, String mapperInterface) throws ClassNotFoundException {
    
    super();
    this.sqlSessionFactory = sqlSessionFactory;   
    Class< ? > tmpInterface = Class.forName(mapperInterface);
    if (VideoConnectionMapper.class.isAssignableFrom(tmpInterface)) {
      this.mapperInterface = (Class<? extends VideoConnectionMapper>) tmpInterface;
    } else {
      throw new IllegalArgumentException("Unexpected class name passed to VideoConnectionPropertiesDAO constructor - unable to instantiate.");
    }
  }
  
  /**
   * SqlMapClient instances are thread safe, so you only need one.
   * In this case, we'll use a static singleton.
   */
  static {
    try {
      if (LOG.isDebugEnabled())
        LOG.debug("Initialising VideoConnectionPropertiesDAO...");
      String resource = "cern/c2mon/server/video/sqlmap/SqlMapVideoConfig.xml";
      Reader reader = Resources.getResourceAsReader(resource);
      sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
      reader.close();
      if (LOG.isDebugEnabled())
        LOG.debug("... Initialisation was successful!");
    } catch (Exception e) {
      // Fail fast.
      LOG.error("Something bad happened while building the SqlMapClient instance: " + e.getMessage());
      throw new RuntimeException("Something bad happened while building the SqlMapClient instance: " + e, e);
    }
  }
  
  /**
   * Sends a query to the database that returns all video connection properties of a specific
   * video system.
   * @param videoSystemName The name of the video system to which the connection properties belongs
   * @return A list of VideoConnectionProperties objects
   * @throws SQLException In case an error occurs during the query
   */
  public List selectAllVideoConnectionProperties(final String videoSystemName) throws SQLException {
    if (LOG.isDebugEnabled())
      LOG.debug("selectAllVideoConnectionProperties() called");
    
    VideoConnectionMapper mapper = sqlSessionFactory.openSession().getMapper(mapperInterface);
    return mapper.selectAllVideoConnectionProperties(videoSystemName);
  }
  
  /**
   * Sends a query to the database that returns all user roles that are privileged to connect
   * to a specific video system. 
   * @param videoSystemName The name of the video system to which the connection properties belongs
   * @return A list which contains the privileged role names (String representation).
   * @throws SQLException In case an error occurs during the query
   */
  public List selectAllPermitedRoleNames(final String videoSystemName) throws SQLException {
    if (LOG.isDebugEnabled())
      LOG.debug("selectAllPermitedRoleNames() called");
    
    VideoConnectionMapper mapper = sqlSessionFactory.openSession().getMapper(mapperInterface);
    return mapper.selectAllPermitedRoleNames(videoSystemName);
  }
}
