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
 * EsMapping for {@link cern.c2mon.server.eslog.structure.types.EsSupervisionEvent} in ElasticSearch.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
public class EsSupervisionMapping implements EsMapping {
  private SupervisionProperties mappings;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Instantiate a new EsSupervisionMapping by setting it Properties to supervision type.
   */
  public EsSupervisionMapping() {
    mappings = new SupervisionProperties();
  }

  /**
   * @return the mapping as JSON String for ElasticSearch.
   */
  @Override
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the supervision mapping: " + json);
    return json;
  }

  /**
   * Properties for a {@link cern.c2mon.server.eslog.structure.types.EsSupervisionEvent}.
   */
  private class SupervisionProperties {
    private Supervision supervision;

    SupervisionProperties() {
      this.supervision = new Supervision();
    }

    class Supervision {
      private Properties properties;

      Supervision() {
        properties = new Properties();
      }

      class Properties {
        private final Id id = new Id();
        private final Name name = new Name();
        private final Entity entity = new Entity();
        private final Message message = new Message();
        private final Status status = new Status();
        private final Timestamp timestamp = new Timestamp();

        class Id {
          private final String type = ValueType.LONG.toString();
        }

        class Name {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class Entity {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class Timestamp {
          private final String type = ValueType.DATE.toString();
          private final String format = epochMillisFormat;
        }

        class Message {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }

        class Status {
          private final String type = ValueType.STRING.toString();
          private final String index = indexNotAnalyzed;
        }
      }
    }
  }
}