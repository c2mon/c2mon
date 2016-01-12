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
package cern.c2mon.server.shorttermlog.management;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * JMX bean for managing the STL datasource.
 * 
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:type=datasource,name=shortTermLogManagement")
public class ShortTermLogManagement {

  /**
   * The cache datasource to close down.
   */
  private BasicDataSource stlDataSource;
  
  /**
   * Autowired constructor.
   * @param stlDataSource the short-term-log datasource
   */
  @Autowired
  public ShortTermLogManagement(@Qualifier("stlDataSource") BasicDataSource stlDataSource) {
    super();
    this.stlDataSource = stlDataSource;
  }
  
  /**
   * For management only.
   * @return the number of active DB connections in the short-term-log datasource pool
   */
  @ManagedOperation(description="The number of active DB connections in the short-term-log datasource pool.")
  public int getNumActiveDbConnections() {
    return stlDataSource.getNumActive();
  }
  
}
