/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
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
package cern.c2mon.shared.client.tag.ibatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.log4j.Logger;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
public class DataTagQualityTypeHandler implements TypeHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagQualityTypeHandler.class); 
  
  /**
   * Gson object for Json serialization/deserialization.
   */
  private Gson gson = GsonFactory.createGson();
  
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
        gson.fromJson(rs.getString(columnName), new TypeToken<HashMap<TagQualityStatus, String>>() { } .getType());
      
      if (qualityStates != null) {
        for (Map.Entry<TagQualityStatus, String> entry : qualityStates.entrySet()) {
          dataTagQuality.addInvalidStatus(entry.getKey(), entry.getValue());
        }
      } else {
        dataTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON, 
                                                          "Error on loading quality from DB (normal at migration to TIM2 - restart DAQ for latest invalidation message.)");  
      }
      
    } catch (JsonSyntaxException ex) {
      LOGGER.debug("Syntax error in parsing DataTagQuality when loading from DB: defaulting to UNKNOWN invalid quality");
      dataTagQuality.addInvalidStatus(TagQualityStatus.UNKNOWN_REASON, 
                                                          "Error on loading quality from DB (normal at migration to TIM2 - restart DAQ for latest invalidation message.)");
    } catch (JsonParseException ex) {
      LOGGER.debug("Parsing error caught while parsing DataTagQuality when loading from DB: defaulting to UNKNOWN invalid quality");
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
      ps.setString(parameterIndex, gson.toJson(((DataTagQuality) dataTagQuality).getInvalidQualityStates()));
    } else {
      ps.setString(parameterIndex, null);
    }
  }
  

}
