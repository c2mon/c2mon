/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.elasticsearch;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocument;

/**
 * Manages index name definitions.
 */
@Component
public class IndexNameManager {

  private Clock clock;

  @Getter
  private final ElasticsearchProperties properties;

  /**
   * @param properties of Elasticsearch server the application is communicating with.
   */
  @Autowired
  public IndexNameManager(ElasticsearchProperties properties) {
    this.properties = properties;
    this.clock = Clock.systemDefaultZone();
  }

  /**
   * Generate an index for the given {@link TagDocument} based on its
   * timestamp.
   *
   * @param tag the tag to generate an index for
   * @return the generated index name
   */
  public String indexFor(TagDocument tag) {
    String prefix = properties.getIndexPrefix() + "-tag_";
    return getIndexName(prefix, Instant.now(clock));
  }

  /**
   * Generate an index for the given {@link TagConfigDocument}.
   *
   * @param tag the tag to generate an index for
   * @return the generated index name
   */
  public String indexFor(TagConfigDocument tag) {
    return properties.getTagConfigIndex();
  }

  /**
   * Generate an index for the given {@link AlarmDocument} based on its
   * timestamp.
   *
   * @param alarm the alarm to generate an index for
   * @return the generated index name
   */
  public String indexFor(AlarmDocument alarm) {
    String prefix = properties.getIndexPrefix() + "-alarm_";
    return getIndexName(prefix, Instant.now(clock));
  }

  /**
   * Generate an index for the given {@link SupervisionEventDocument}
   * based on its timestamp.
   *
   * @param supervisionEvent the supervision event to generate an index for
   * @return the generated index name
   */
  public String indexFor(SupervisionEventDocument supervisionEvent) {
    String prefix = properties.getIndexPrefix() + "-supervision_";
    return getIndexName(prefix, Instant.now(clock));
  }

  /**
   * Generate an index for the given prefix and timestamp, based on the current
   * time series indexing strategy.
   *
   * @param prefix  the index prefix
   * @param instant the instant which will be used to generate the index
   * @return the generated index name
   */
  private String getIndexName(String prefix, Instant instant) {
    String indexType = properties.getIndexType();
    String dateFormat;

    switch (indexType.toLowerCase(Locale.getDefault())) {
      case "d":
        dateFormat = "yyyy-MM-dd";
        break;
      case "w":
        dateFormat = "yyyy-'W'ww";
        break;
      case "m":
      default:
        dateFormat = "yyyy-MM";
        break;
    }

    return prefix + new SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date.from(instant));
  }

  /**
   * Used for testing to set up the clock for determined instant
   *
   * @param clock instance to be used
   */
  protected void setClock(Clock clock) {
    this.clock = clock;
  }
}
