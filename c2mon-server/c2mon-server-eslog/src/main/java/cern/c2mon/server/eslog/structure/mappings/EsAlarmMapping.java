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
package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * EsMapping used for {@link cern.c2mon.server.eslog.structure.types.EsAlarm} in ElasticSearch.
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
   * @return the mapping as JSON String for ElasticSearch.
   */
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the alarm mapping: " + json);
    return json;
  }
  
  
  /**
   * Properties for a {@link cern.c2mon.server.eslog.structure.types.EsAlarm}.
   */
  private class AlarmProperties {
    private Alarm alarm;

    AlarmProperties() {
      this.alarm = new Alarm();
    }

    class Alarm {
      private final Properties properties;

      Alarm() {
        properties = new Properties();
      }

      class Properties {
        private final TagId tagId;
        private final AlarmId alarmId;
        private final FaultFamily faultFamily;
        private final FaultMember faultMember;
        private final FaultCode faultCode;
        private final Active active;
        private final Activity activity;
        private final ActiveNumeric activeNumeric;
        private final Priority priority;
        private final Info info;
        private final ServerTimestamp serverTimestamp;
        private final TimeZone timeZone;

        private final Metadata metadata;


        Properties() {
          this.tagId = new TagId();
          this.alarmId = new AlarmId();
          this.faultFamily = new FaultFamily();
          this.faultMember = new FaultMember();
          this.faultCode = new FaultCode();
          this.active = new Active();
          this.activity = new Activity();
          this.activeNumeric = new ActiveNumeric();
          this.priority = new Priority();
          this.info = new Info();
          this.serverTimestamp = new ServerTimestamp();
          this.timeZone = new TimeZone();
          this.metadata = new Metadata();
        }

        class TagId {
          private final String type = ValueType.LONG.toString();
        }

        class AlarmId {
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

        class Activity {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class ActiveNumeric {
          private final String type = ValueType.DOUBLE.toString();
        }

        class Priority {
          private final String type = ValueType.INTEGER.toString();
        }

        class Info {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class ServerTimestamp {
          private final String type = ValueType.DATE.toString();
          private final String format = epochMillisFormat;
        }

        class TimeZone {
          private final String type = ValueType.STRING.toString();
        }

        class Metadata {
          private final String type = ValueType.NESTED.toString();
          private final String dynamic = "true";
        }
      }
    }
  }
}