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
package cern.c2mon.statistics.consumer;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Tests the DB connection starts up correctly.
 * 
 * @author Mark Brightwell
 *
 */
public class DbConnectionTest {

  @Test
  public void initSqlMap() {
    SqlMapClient client = SqlMapper.getSqlMapInstance();
    assertNotNull(client);
  }
  
}
