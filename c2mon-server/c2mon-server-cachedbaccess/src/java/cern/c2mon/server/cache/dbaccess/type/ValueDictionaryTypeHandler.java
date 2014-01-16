package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import cern.c2mon.shared.common.datatag.DataTagValueDictionary;

public class ValueDictionaryTypeHandler implements TypeHandler {
  
  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    
    String tagValueDictionaryString;
    DataTagValueDictionary valueDictionary;
    
    if ((tagValueDictionaryString = rs.getString(columnName)) != null) {
      valueDictionary = DataTagValueDictionary.fromXML(tagValueDictionaryString);
    }
    else {
      valueDictionary = null;
    }
    return valueDictionary;   
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, Object valueDictionary, JdbcType arg3) throws SQLException {
    if (valueDictionary != null) {
      ps.setString(parameterIndex, ((DataTagValueDictionary) valueDictionary).toXML());          
    } else {
      ps.setString(parameterIndex, null);
    }
  }
}
