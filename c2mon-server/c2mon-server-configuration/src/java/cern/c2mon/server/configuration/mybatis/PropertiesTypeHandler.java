package cern.c2mon.server.configuration.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;

public class PropertiesTypeHandler implements TypeHandler {

  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(PropertiesTypeHandler.class); 
  
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
  public void setParameter(PreparedStatement arg0, int arg1, Object arg2,
      JdbcType arg3) throws SQLException {
    LOGGER.error("Running setParameter method in PropertiesTypeHandler - this should not be happening, "
    		+ "so throwing a runtime exception");
    throw new RuntimeException("SetParameter method in PropertiesTypeHandler is not implemented!");
  }

}
