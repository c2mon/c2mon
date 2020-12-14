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

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * iBatis TypeHandler for converting the VARCHAR Tag quality
 * description column into a DataTagQuality object.
 *
 * <p>For migration to TIM2: if the description is null, it can
 * be assumed the quality is good, and the dataTagQuality is
 * set to null. All others are invalid and are set to have status
 * UNKNOWN_REASON.
 *
 * @author Mark Brightwell
 *
 */
@MappedTypes(DataTagQuality.class)
public class DataTagQualityTypeHandler implements TypeHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DataTagQualityTypeHandler.class);

  /**
   * Gson object for Json serialization/deserialization.
   */
  private static ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
  }

  /**
   * Decodes the Json string stored in the DB into a DataTagQuality
   * object. If the String is not in Json format, the quality is
   * set to UNKNOWN_REASON (this will happen at migration to TIM2).
   */
  @Override
  public DataTagQuality getResult(final ResultSet rs, final String columnName) throws SQLException {
    DataTagQuality dataTagQuality = new DataTagQualityImpl();
    dataTagQuality.validate();
    try {
      Map<TagQualityStatus, String> qualityStates =
        mapper.readValue(rs.getString(columnName), new TypeReference<HashMap<TagQualityStatus, String>>() { });

      if (qualityStates != null) {
        for (Map.Entry<TagQualityStatus, String> entry : qualityStates.entrySet()) {
          dataTagQuality.addInvalidStatus(entry.getKey(), entry.getValue());
        }
      } else {
        dataTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON,
                                                          "Error on loading quality from DB (normal at migration to TIM2 - restart DAQ for latest invalidation message.)");
      }

    } catch (com.fasterxml.jackson.core.JsonParseException e) {
      LOGGER.debug("Parsing error caught while parsing DataTagQuality when loading from DB: defaulting to UNKNOWN invalid quality");
      dataTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON,
          "Error on loading quality from DB (normal at migration to TIM2 - restart DAQ for latest invalidation message.)");
    } catch (IOException e) {
      LOGGER.debug("I/O error in parsing DataTagQuality when loading from DB: defaulting to UNKNOWN invalid quality");
      dataTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON,
          "Error on loading quality from DB (normal at migration to TIM2 - restart DAQ for latest invalidation message.)");
    }
    return dataTagQuality;
  }

  /**
   * Not implemented.
   * @throws UnsupportedOperationException if called
   */
  @Override
  public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
    throw new UnsupportedOperationException("getResult method not implemented for iBatis DataTagQualityHandler.");
  }

  /**
   * Converts a DataTagQuality object into a Json String encoding the qualities and
   * associated descriptions.
   */
  @Override
  public void setParameter(final PreparedStatement ps, final int parameterIndex,
                            final Object dataTagQuality, final JdbcType jdbcType) throws SQLException {
    if (dataTagQuality != null) {
      try {
        ps.setString(parameterIndex, mapper.writeValueAsString(((DataTagQuality) dataTagQuality).getInvalidQualityStates()));

      } catch (JsonProcessingException e) {
        LOGGER.warn("Processing error while writing DataTagQuality to the DB", e);
      }
    } else {
      ps.setString(parameterIndex, null);
    }
  }

  @Override
  public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
    return null;
  }


}
