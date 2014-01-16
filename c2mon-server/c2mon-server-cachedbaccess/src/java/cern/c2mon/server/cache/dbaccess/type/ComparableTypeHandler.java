package cern.c2mon.server.cache.dbaccess.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;

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
  private static final Logger LOGGER = Logger.getLogger(ComparableTypeHandler.class); 
  
  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {    
    Object returnObject = null;    
    try {
      if (rs.getBlob(columnName) != null) {
        returnObject = new ObjectInputStream(rs.getBlob(columnName).getBinaryStream()).readObject();
      }
    } catch (IOException ex) {
      LOGGER.error("IOException caught when constructing a java.lang.Comparable from the database:", ex);
    } catch (ClassNotFoundException ex) {
      LOGGER.error("ClassNotFoundException caught when constructing a java.lang.Comparable from the database:", ex);
    } catch (Exception ex) {
      LOGGER.error("Unexpected exception caught when constructing a java.lang.Comparable from the database:", ex);
    }
    return returnObject;
    
  }

  @Override
  public Object getResult(CallableStatement arg0, int arg1) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, Object parameter, JdbcType arg3) throws SQLException {          
    try {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
      objectOutStream.writeObject(parameter);
      ps.setBytes(parameterIndex, outStream.toByteArray());      
    } catch (IOException ioEx) {
      LOGGER.error("IOException caught when setting a prepared statement parameter from a "
          + "java.lang.Comparable object (corresponds to Min or Max values)", ioEx);
    }
        
  }

 
}
