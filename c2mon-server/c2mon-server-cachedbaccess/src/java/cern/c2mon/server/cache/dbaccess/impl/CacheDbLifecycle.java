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
package cern.c2mon.server.cache.dbaccess.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.config.ServerConstants;

/**
 * Manages the closing of the cache DB connections.
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:type=datasource,name=cacheDbLifecycle")
public class CacheDbLifecycle implements SmartLifecycle {

  private static final Logger LOGGER = Logger.getLogger(CacheDbLifecycle.class);
  
  /**
   * The cache datasource to close down.
   */
  private DataSource cacheDataSource;
  
  /**
   * Only designed to 
   */
  private volatile boolean started = false;
  
  @Autowired
  public CacheDbLifecycle(@Qualifier("cacheDataSource") DataSource cacheDataSource) {
    super();
    this.cacheDataSource = cacheDataSource;
  }

  
  /**
   * For management only.
   * @return the number of active DB connections in the cache datasource pool
   */
  @ManagedOperation(description="The number of active DB connections in the cache datasource pool (only works for Apache BasicDataSource)")
  public int getNumActiveDbConnections() {
    if (cacheDataSource instanceof BasicDataSource)
      return ((BasicDataSource) cacheDataSource).getNumActive();
    else
      return 0;
  }
  
  @Override
  public boolean isRunning() {
    return started;
  }

  /**
   * Nothing to start, as already done in datasource initialisation.
   */
  @Override
  public void start() {
    started = true;
  }

  @Override
  public void stop() {
    LOGGER.info("Closing down cache DB connections (only available for Apache BasicDataSource)");
    try {
      if (cacheDataSource instanceof BasicDataSource)
        ((BasicDataSource) cacheDataSource).close();
    } catch (SQLException ex) {
      LOGGER.error("Exception caught while closing down cache DB connections.", ex);
      ex.printStackTrace();
    }
  }

  @Override
  public boolean isAutoStartup() {    
    return false;
  }

  /**
   * Smart lifecycle stop implementation.
   * Closes the DB connection pool.
   */
  @Override
  public void stop(Runnable arg0) {
    stop();
    arg0.run();
  }

  @Override
  public int getPhase() {    
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

}
