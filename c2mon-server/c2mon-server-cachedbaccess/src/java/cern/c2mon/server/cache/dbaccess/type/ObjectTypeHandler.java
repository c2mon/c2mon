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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.common.type.TypeConverter;

import static cern.c2mon.shared.common.type.TypeConverter.cast;
import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

/**
 * iBatis TypeHandler used to convert between Comparable Java objects (used for Min and Max value) and Java Object of the type 'Object'.
 * If the columns are empty, a null object is returned when loading from the database.
 * The objects are saved as json String. If loaded from the db jackson will deserialize the json string into the java object.
 *
 * @author Mark Brightwell
 * @author Franz Ritter
 *
 */
public class ObjectTypeHandler implements TypeHandler {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypeHandler.class);

  private static ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
  }

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    Object result  = null;
    String tagDataType;
    String valueAsString;

    try {
      if (rs.getString(columnName) != null) {
        tagDataType = getDataType(rs, columnName);

        valueAsString = rs.getString(columnName);
        result = mapper.readValue(valueAsString, Object.class);

        // check if the object is NOT an arbitrary object - if so make a cast to the original type
        if(isKnownClass(tagDataType)){

          result = cast(result, tagDataType);

        }
      }
    } catch (Exception ex) {
      LOGGER.error("Unexpected exception caught when constructing a value from the column "+columnName+" from the database (it could be just a NULL DB column value):", ex);
    }
    return result;
  }

  /**
   * Reading the datatype of the object based on the DataType column in the db.
   * All values saved in the db with the type objects have the data type of the value.
   *
   * @param rs Result set from the the db.
   * @param columnName the name of the column.
   * @return the requested data type.
   */
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
      	// Because the value is stored as VARCHAR in the db it have to be transformed to a json string
      	ps.setString(parameterIndex, mapper.writeValueAsString(parameter));
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
