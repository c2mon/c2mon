package cern.c2mon.server.cache.dbaccess.type;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.metadata.Metadata;
import cern.c2mon.shared.common.metadata.Metadata;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class MetadataTypeHandler implements TypeHandler {

  @Override
  public Object getResult(ResultSet rs, String columnName) throws SQLException {

    String metadataString;
    Metadata metadata = new Metadata();
    Map<String, Object> dataSet;

    if ((metadataString = rs.getString(columnName)) != null) {
      dataSet = Metadata.fromJSON(metadataString);
      metadata.setMetadata(dataSet);
    }
    else {
      metadata = null;
    }
    return metadata;
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
  public void setParameter(PreparedStatement ps, int parameterIndex, Object metadata, JdbcType arg3) throws SQLException {
    if (metadata != null) {
      ps.setString(parameterIndex, Metadata.toJSON((Metadata) metadata));
    } else {
      ps.setString(parameterIndex, null);
    }
  }
}
