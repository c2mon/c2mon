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

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.structure.queries.ClusterNotAvailableException;
import cern.c2mon.server.eslog.structure.queries.QueryIndices;
import cern.c2mon.server.eslog.structure.queries.QueryTypes;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Used to write (a.k.a. index) the data to elasticSearch.
 * Makes use of the Connector connection to an ElasticSearch cluster.
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public abstract class Indexer implements IDBPersistenceHandler {
  /** Prefix used for every index in the ElasticSearch cluster, e.g., c2mon_2015-11 is a valid index. */
  @Value("${es.prefix.tag:c2mon-tag_}")
  protected String indexPrefix;

  @Value("${es.config.index.format:M}")
  protected String indexFormat;

  /** Every tag or alias must begin with the same prefix, e.g., tag_string is a good type and tag_207807 is a good alias. */
  @Value("${es.prefix.type:tag_}")
  protected String typePrefix;

  /** The first index in the cluster is c2mon_1970-01 which corresponds to the Epoch time (ES stocks timestamps in milliseconds since Epoch). */
  protected String FIRST_INDEX = indexPrefix + "1970-01";

  @Value("${es.prefix.supervision:c2mon-supervision_}")
  protected String supervisionPrefix;

  @Value("${es.prefix.alarm:c2mon-alarm_}")
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
  public void init() throws IDBPersistenceException {
    waitForConnection();
    log.info("init() - Indexer is ready to write data to ElasticSearch.");
  }

  public void waitForConnection() {
    while(!connector.isConnected()) {
      isAvailable = false;
    }
    isAvailable = true;
  }

  @Override
  public String getDBInfo() {
    return connector.getCluster();
  }

  public String retrieveIndexFormat(String prefix, long millis) {
    switch (indexFormat) {
      case "D":
      case "d":
        return prefix + millisecondsToYearMonthDay(millis);
      case "W":
      case "w":
        return prefix + millisecondsToYearWeek(millis);
      case "M":
      case "m":
      default:
        return prefix + millisecondsToYearMonth(millis);
    }
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

  public MappingMetaData retrieveMappingES(String index, String type) throws IDBPersistenceException {
    try {
      ClusterState state = connector.createClient().admin().cluster().prepareState().execute().actionGet().getState();
      return state.getMetaData().index(index).mapping(type);
    }
    catch (Exception e) {
      log.error("retrieveMappingES() - error connecting to the ElasticSearch cluster.", e);
      throw new IDBPersistenceException();
    }
  }

  protected List<String> retrieveIndicesFromES() throws IDBPersistenceException {
    try {
      return connector.handleListingQuery(new QueryIndices(connector.getClient()), null);
    }
    catch (ClusterNotAvailableException e) {
      throw new IDBPersistenceException();
    }
  }

  protected List<String> retrieveTypesFromES(String index) throws IDBPersistenceException {
    try {
      return connector.handleListingQuery(new QueryTypes(connector.getClient()), index);
    }
    catch (ClusterNotAvailableException e) {
      throw new IDBPersistenceException();
    }
  }
}