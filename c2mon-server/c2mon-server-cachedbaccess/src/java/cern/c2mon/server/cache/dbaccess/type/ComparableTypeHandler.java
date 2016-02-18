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
package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.common.type.TypeConverter;

/**
 * iBatis TypeHandler used to convert between Comparable Java objects (used for
 * Min and Max value). If the columns are empty, a null object is returned when
 * loading from the database.
 *
 * @author Mark Brightwell
 *
 */
public class ComparableTypeHandler implements TypeHandler {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ComparableTypeHandler.class);

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    Object returnObject  = null;
    String valueAsString = null;
    String tagDataType   = null;
    
    try {
      if (rs.getString(columnName) != null) {
        valueAsString = rs.getString(columnName);
        
        tagDataType = getDataType(rs, columnName);
        returnObject = TypeConverter.cast(valueAsString, tagDataType);
      }
    } catch (Exception ex) {
      LOGGER.error("Unexpected exception caught when constructing a java.lang.Comparable from the database (it could be just a NULL DB column value):", ex);
    } 
    return returnObject;
  }

  private String getDataType(final ResultSet rs, final String columnName) {
    String result = "String";
    
    try {
      if (columnName.startsWith("TAG")) {
        result = rs.getString("TAGDATATYPE");
      }
      else if (columnName.startsWith("CMD")) {
        result = rs.getString("CMDDATATYPE");
      }
      else {
        LOGGER.warn("getDataType() - Column " + columnName + " is not supported. Using default data type (String).");
      }
    } catch (Exception ex) {
        LOGGER.error("getDataType() - Error occured whilst determine resulting Java type. Set to default (=String)", ex);
    }
    
    return result;
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, Object parameter, JdbcType arg3) throws SQLException {
    try {
      if (parameter != null) {
      	// Stored as VARCHAR, so cast to String
      	ps.setObject(parameterIndex, parameter.toString());
      }
      else {
      	ps.setString(parameterIndex, null);
      }
    } catch (Exception ex) {
      LOGGER.error(
        "Exception caught when setting a prepared statement parameter from a tag value "
        + "Object or java.lang.Comparable (used to Min or Max values)", ex);
       
    }
  }
}
