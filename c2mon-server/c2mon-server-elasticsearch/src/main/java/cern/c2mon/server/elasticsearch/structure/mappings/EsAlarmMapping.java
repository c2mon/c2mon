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
package cern.c2mon.server.elasticsearch.structure.mappings;

import java.util.List;

import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.singletonList;

/**
 * EsMapping used for {@link EsAlarm} in Elasticsearch.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
public class EsAlarmMapping implements EsMapping {

  private AlarmProperties mappings;
  private transient long tagId;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Instantiate am EsAlarmMapping by setting its properties with the ALARM.
   */
  public EsAlarmMapping() {
    mappings = new AlarmProperties();
  }

  /**
   * @return the mapping as JSON String for Elasticsearch.
   */
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the alarm mapping: " + json);
    return json;
  }


  /**
   * Properties for a {@link EsAlarm}.
   */
  private class AlarmProperties {
    private Alarm alarm;

    AlarmProperties() {
      this.alarm = new Alarm();
    }

    class Alarm {
      private final Properties properties;
      private final List<DynamicTemplate> dynamic_templates = singletonList(new DynamicTemplate());

      Alarm() {
        properties = new Properties();
      }

      class Properties {
        private final Id id = new Id();
        private final TagId tagId = new TagId();
        private final FaultFamily faultFamily = new FaultFamily();
        private final FaultMember faultMember = new FaultMember();
        private final FaultCode faultCode = new FaultCode();
        private final Active active = new Active();
        private final ActiveNumeric activeNumeric = new ActiveNumeric();
        private final Info info = new Info();
        private final Timestamp timestamp = new Timestamp();
        private final Metadata metadata = new Metadata();


        class TagId {
          private final String type = ValueType.LONG.toString();
        }

        class Id {
          private final String type = ValueType.LONG.toString();
        }

        class FaultFamily {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class FaultMember {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class FaultCode {
          private final String type = ValueType.INTEGER.toString();
        }

        class Active {
          private final String type = ValueType.BOOLEAN.toString();
        }

        class ActiveNumeric {
          private final String type = ValueType.DOUBLE.toString();
        }

        class Info {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class Timestamp {
          private final String type = ValueType.DATE.toString();
          private final String format = epochMillisFormat;
        }

        class Metadata {
          private final String type = ValueType.NESTED.toString();
          private final String dynamic = "true";
        }
      }
    }
  }
}
