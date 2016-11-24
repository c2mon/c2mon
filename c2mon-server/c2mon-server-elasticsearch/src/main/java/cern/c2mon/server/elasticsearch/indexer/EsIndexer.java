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
package cern.c2mon.server.elasticsearch.indexer;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.Connector;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

/**
 * Used to write (a.k.a. to index) the data to Elasticsearch.
 * Makes use of the {@link Connector} connection to an Elasticsearch cluster.
 *
 * @author Alban Marguet
 */
@Slf4j
@Component
public abstract class EsIndexer<T extends IFallback> implements IDBPersistenceHandler<T> {

  @Autowired
  protected ElasticsearchProperties properties;

  /**
   * Handles the connection with the Elasticsearch cluster.
   */
  protected Connector connector;

  /**
   * Is available if the Connector has found a connection.
   */
  @Getter
  protected boolean isAvailable = false;

  @Autowired
  public EsIndexer(final Connector connector, ElasticsearchProperties properties) {
    this.connector = connector;
    this.properties = properties;
  }

  /**
   * Be sure that the connection is alive.
   */
  @PostConstruct
  public void init() throws IDBPersistenceException {
    waitForConnection();
  }

  private void waitForConnection() {

    int count = 0;

    while(!isAvailable) {
      try {
        log.trace("waitForConnection() is sleeping for 1s before checking again for valid ES connection");
        Thread.sleep(1000L);

        isAvailable = connector.isConnected();

        if (count++ > 15) {
          log.error("Connection to Elasticsearch not properly established. Please check the cluster state and the configuration!");
          break;
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (!isAvailable) {
      log.info("EsIndexer is ready to write data to Elasticsearch");
    }
  }

  @Override
  public String getDBInfo() {
    return properties.getClusterName();
  }

  /**
   * Give the type of index to use in Elasticsearch.
   *
   * @param prefix letter corresponding to the type of index.
   * @param millis date in milliseconds since Epoch.
   * @return the index name.
   */
  protected String retrieveIndexFormat(String prefix, long millis) {
    String result;
    switch (properties.getIndexType()) {
      case "D":
      case "d":
        result = prefix + millisecondsToYearMonthDay(millis);
        break;
      case "W":
      case "w":
        result = prefix + millisecondsToYearWeek(millis);
        break;
      case "M":
      case "m":
      default:
        result = prefix + millisecondsToYearMonth(millis);
    }
    return result.toLowerCase();
  }

  /**
   * @return milliseconds since Epoch time to YYYY-MM.
   */
  protected String millisecondsToYearMonth(long millis) {
    return getSimpleDateFormatForMilliseconds("yyyy-MM", millis);
  }

  /**
   * @return milliseconds since Epoch time to YYYY-ww.
   */
  protected String millisecondsToYearWeek(long millis) {
    return getSimpleDateFormatForMilliseconds("yyyy-'W'ww", millis);
  }

  /**
   * @return milliseconds since Epoch time to YYYY-MM-DD.
   */
  protected String millisecondsToYearMonthDay(long millis) {
    return getSimpleDateFormatForMilliseconds("yyyy-MM-dd", millis);
  }

  private String getSimpleDateFormatForMilliseconds(String wantedPattern, long millis) {
    Date date = new Date(millis);
    return new SimpleDateFormat(wantedPattern).format(date);
  }
}
