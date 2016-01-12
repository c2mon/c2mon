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
package cern.c2mon.server.configuration.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesTypeHandler implements TypeHandler {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesTypeHandler.class);

  @Override
  public Object getResult(ResultSet rs, String arg1) throws SQLException {
    Properties returnProperties = new Properties();
    do {
      returnProperties.put(
        rs.getString("elementfield"),
        rs.getString("elementvalue")
      );
    } while (rs.next());
    return returnProperties;
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    LOGGER.error("Running getResult(CallableStatement,...) method in PropertiesTypeHandler - this should not be happening, "
      + "so throwing a runtime exception");
    throw new RuntimeException("getResult(CallableStatement,...) method in PropertiesTypeHandler is not implemented!");
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement arg0, int arg1, Object arg2,
      JdbcType arg3) throws SQLException {
    LOGGER.error("Running setParameter method in PropertiesTypeHandler - this should not be happening, "
    		+ "so throwing a runtime exception");
    throw new RuntimeException("SetParameter method in PropertiesTypeHandler is not implemented!");
  }

}
