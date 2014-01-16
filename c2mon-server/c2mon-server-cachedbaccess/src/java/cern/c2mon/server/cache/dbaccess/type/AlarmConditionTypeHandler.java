/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * iBatis TypeHandler class for converting AlarmCondition implementations
 * into XML String stored as VARCHAR in the database.
 * @author Mark Brightwell
 *
 */
public class AlarmConditionTypeHandler implements TypeHandler {

  
  private static final Logger LOGGER = Logger.getLogger(AlarmConditionTypeHandler.class);
  
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
