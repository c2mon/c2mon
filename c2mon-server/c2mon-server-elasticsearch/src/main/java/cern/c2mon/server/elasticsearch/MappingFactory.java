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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocument;

/**
 * Factory for creating Elasticsearch mapping sources.
 *
 * @author Justin Lewis Salmon
 */
public final class MappingFactory {

  private static final String TAG_MAPPING = "mappings/tag.json";
  private static final String TAG_CONFIG_MAPPING = "mappings/tag-config.json";
  private static final String ALARM_MAPPING = "mappings/alarm.json";
  private static final String SUPERVISION_MAPPING = "mappings/supervision.json";

  private MappingFactory() {
    // only static methods below
  }

  /**
   * Create the Elasticsearch mapping for a {@link TagConfigDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createTagConfigMapping() {
    return loadMapping(TAG_CONFIG_MAPPING);
  }

  /**
   * Create the Elasticsearch mapping for an {@link AlarmDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createAlarmMapping() {
    return loadMapping(ALARM_MAPPING);
  }

  /**
   * Create the Elasticsearch mapping for a {@link TagDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createTagMapping() {
    return loadMapping(TAG_MAPPING);
  }

  /**
   * Create the Elasticsearch mapping for a {@link SupervisionEventDocument}.
   *
   * @return the JSON mapping source
   */
  public static String createSupervisionMapping() {
    return loadMapping(SUPERVISION_MAPPING);
  }

  private static String loadMapping(String location) {
    return new BufferedReader(new InputStreamReader(loadResource(location)))
        .lines()
        .collect(Collectors.joining(""));
  }

  private static InputStream loadResource(String location) {
    try {
      return new ClassPathResource(location).getInputStream();
    } catch (IOException e) {
      throw new RuntimeException("Error loading resource", e);
    }
  }
}
