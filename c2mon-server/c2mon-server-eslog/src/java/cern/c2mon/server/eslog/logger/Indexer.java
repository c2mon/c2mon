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
package cern.c2mon.server.eslog.logger;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used to write (a.k.a. index) the data to elasticSearch.
 * Makes use of the Connector connection to an ElasticSearch cluster.
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public abstract class Indexer {
  /** Prefix used for every index in the ElasticSearch cluster, e.g., c2mon_2015-11 is a valid index. */
  @Value("${es.prefix.index:c2mon_}")
  protected String indexPrefix;

  /** Every tag or alias must begin with the same prefix, e.g., tag_string is a good type and tag_207807 is a good alias. */
  @Value("${es.prefix.tag:tag_}")
  protected String tagPrefix;

  /** The first index in the cluster is c2mon_1970-01 which corresponds to the Epoch time (ES stocks timestamps in milliseconds since Epoch). */
  protected String FIRST_INDEX = indexPrefix + "1970-01";

  @Value("${es.prefix.supervision:supervision_}")
  protected String supervisionPrefix;

  @Value("${es.prefix.alarm:alarm_}")
  protected String alarmPrefix;

  /**
   * Handles the connection with the ElasticSearch cluster.
   */
  protected Connector connector;

  /**
   * Is available if the Connector has found a connection.
   */
  protected boolean isAvailable;

  @Autowired
  public Indexer(final Connector connector) {
    this.connector = connector;
  }

  @PostConstruct
  public void init() {
    waitForConnection();
    log.info("init() - Indexer is ready to write data to ElasticSearch.");
  }

  public void waitForConnection() {
    while(!connector.isConnected()) {
      isAvailable = false;
    }
    isAvailable = true;
  }

  /**
   * Milliseconds since Epoch time to YYYY-MM.
   */
  public String millisecondsToYearMonth(long millis) {
    String timestamp = getSimpleDateFormatForMilliseconds("yyyy-MM-dd HH:mm:ss.SSSSSS", millis);
    return timestamp.substring(0, 7);
  }

  /**
   * Milliseconds since Epoch time to YYYY-ww.
   */
  public String millisecondsToYearWeek(long millis) {
    String timeStamp = getSimpleDateFormatForMilliseconds("yyyy-ww", millis);
    return timeStamp;
  }

  /**
   * Milliseconds since Epoch time to YYYY-MM-DD.
   */
  public String millisecondsToYearMonthDay(long millis) {
    String timestamp = getSimpleDateFormatForMilliseconds("yyyy-MM-dd HH:mm:ss.SSSSSS", millis);
    return timestamp.substring(0, 10);
  }

  private String getSimpleDateFormatForMilliseconds(String wantedPattern, long millis) {
    SimpleDateFormat sdf = new SimpleDateFormat(wantedPattern);
    Date date = new Date(millis);
    return sdf.format(date);
  }
}