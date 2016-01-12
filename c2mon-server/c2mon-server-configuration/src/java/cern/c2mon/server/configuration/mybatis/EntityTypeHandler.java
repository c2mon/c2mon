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

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;

public class EntityTypeHandler implements TypeHandler {

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    Entity entity = null;
    String entityString = rs.getString("elt_elementtype"); //TODO change to column name and change sql if no changes made to sql
    if (entityString.equalsIgnoreCase("DataTag")) {
      entity = Entity.DATATAG;
    } else if (entityString.equalsIgnoreCase("CommandTag")) {
      entity = Entity.COMMANDTAG;
    } else if (entityString.equalsIgnoreCase("Alarm")) {
      entity = Entity.ALARM;
    } else if (entityString.equalsIgnoreCase("Equipment")) {
      entity = Entity.EQUIPMENT;
    } else if (entityString.equalsIgnoreCase("Process")) {
      entity = Entity.PROCESS;
    } else if (entityString.equalsIgnoreCase("SubEquipment")) {
      entity = Entity.SUBEQUIPMENT;
    } else if (entityString.equalsIgnoreCase("ControlTag")) {
      entity = Entity.CONTROLTAG;
    } else if (entityString.equalsIgnoreCase("RuleTag")) {
      entity = Entity.RULETAG;
    } else if (entityString.equalsIgnoreCase("DeviceClass")) {
      entity = Entity.DEVICECLASS;
    } else if (entityString.equalsIgnoreCase("Device")) {
      entity = Entity.DEVICE;
    }
    if (entity == null) {
      throw new SQLException("Unrecognized ConfigConstants.Entity: " + entityString);
    } else {
      return entity;
    }
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getResult(ResultSet arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement arg0, int arg1, Object arg2,
      JdbcType arg3) throws SQLException {
    // TODO Auto-generated method stub

  }
}
