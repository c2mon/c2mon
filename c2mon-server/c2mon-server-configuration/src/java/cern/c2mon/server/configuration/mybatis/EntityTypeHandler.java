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
  public void setParameter(PreparedStatement arg0, int arg1, Object arg2,
      JdbcType arg3) throws SQLException {
    // TODO Auto-generated method stub

  }

}
