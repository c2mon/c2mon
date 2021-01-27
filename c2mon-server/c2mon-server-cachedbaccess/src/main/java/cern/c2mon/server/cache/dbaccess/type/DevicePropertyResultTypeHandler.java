package cern.c2mon.server.cache.dbaccess.type;

import cern.c2mon.shared.client.device.ResultType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * iBatis TypeHandler for converting the result type of device properties or property fields into the corresponding
 * {@link ResultType} enum value.
 *
 */
@MappedTypes(ResultType.class)
public class DevicePropertyResultTypeHandler implements TypeHandler<ResultType> {

    @Override
    public ResultType getResult(ResultSet resultSet, String columnName) throws SQLException {
        String label = resultSet.getString(columnName);
        return ResultType.getOrDefault(label);
    }

    @Override
    public ResultType getResult(ResultSet resultSet, int i) throws SQLException {
        return null;
    }

    @Override
    public ResultType getResult(CallableStatement callableStatement, int i) throws SQLException {
        return null;
    }

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, ResultType field, JdbcType jdbcType) throws SQLException {
        if (field != null) {
            preparedStatement.setString(i, field.toString());
        } else {
            preparedStatement.setString(i, ResultType.getOrDefault("").toString());
        }
    }
}
