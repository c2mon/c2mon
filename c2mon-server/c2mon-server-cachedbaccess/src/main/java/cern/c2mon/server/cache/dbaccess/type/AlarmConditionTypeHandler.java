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
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.client.alarm.condition.AlarmCondition;

/**
 * iBatis TypeHandler class for converting AlarmCondition implementations
 * into XML String stored as VARCHAR in the database.
 * @author Mark Brightwell
 *
 */
@MappedTypes(AlarmCondition.class)
public class AlarmConditionTypeHandler implements TypeHandler {


  private static final Logger LOGGER = LoggerFactory.getLogger(AlarmConditionTypeHandler.class);

  @Override
  public Object getResult(final ResultSet rs, final String columnName) throws SQLException {
    String conditionString;
    AlarmCondition alarmCondition;

    if ((conditionString = rs.getString(columnName)) != null) {
      try {
        alarmCondition = AlarmCondition.fromConfigXML(conditionString);
      } catch (RuntimeException e) {
        LOGGER.error("Error during XML parsing of alarm condition - the condition will be set to null in the server.", e);
        alarmCondition = null;
      }
    }
    else {
      alarmCondition = null;
    }
    return alarmCondition;
  }

  /**
   * Not currently used or implemented.
   */
  @Override
  public Object getResult(final CallableStatement arg0, final int arg1) throws SQLException {
    throw new UnsupportedOperationException("getResult method is not implemented in AlarmConditionTypeHandler");
  }

  @Override
  public Object getResult(ResultSet arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(final PreparedStatement ps, final int parameterIndex, final Object alarmCondition, final JdbcType arg3) throws SQLException {
    if (alarmCondition != null) {
      String conditionXml = null;
      try {
        conditionXml = ((AlarmCondition) alarmCondition).toConfigXML();
      } catch (RuntimeException e) {
        LOGGER.error("Unable to encode alarm condition in XML - will be persisted as null.");
      }
      ps.setString(parameterIndex, conditionXml);
    } else {
      ps.setString(parameterIndex, null);
    }
  }
}
