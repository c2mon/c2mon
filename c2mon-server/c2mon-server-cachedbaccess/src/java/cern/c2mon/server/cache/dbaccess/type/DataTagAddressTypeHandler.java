package cern.c2mon.server.cache.dbaccess.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import cern.c2mon.shared.common.datatag.DataTagAddress;

public class DataTagAddressTypeHandler implements TypeHandler {
  
  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {
    
    String tagAddressString;
    DataTagAddress tagAddress;
    
    if ((tagAddressString = rs.getString(columnName)) != null) {
      tagAddress = DataTagAddress.fromConfigXML(tagAddressString);
    }
    else {
      tagAddress = null;
    }
    return tagAddress;   
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, Object dataTagAddress, JdbcType arg3) throws SQLException {
    if (dataTagAddress != null) {
      ps.setString(parameterIndex, ((DataTagAddress) dataTagAddress).toConfigXML());          
    } else {
      ps.setString(parameterIndex, null);
    }
  }
}
