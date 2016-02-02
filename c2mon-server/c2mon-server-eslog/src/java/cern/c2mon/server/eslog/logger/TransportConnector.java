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

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.structure.queries.*;
import cern.c2mon.server.eslog.structure.types.AlarmES;
import cern.c2mon.server.eslog.structure.types.SupervisionES;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ack.ClusterStateUpdateRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Allows to connect to the cluster via a transport client. Handles all the
 * queries and writes data thanks to a bulkProcessor to the ElasticSearch cluster.
 * This is very light for the cluster to be connected this way.
 * 
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public class TransportConnector implements Connector {
  /** Only used, if elasticSearch is started inside this JVM */
  private final int LOCAL_PORT = 1;
  private final String LOCAL_HOST = "local";

  /** Default port for elastic search transport node */
  public final static int DEFAULT_ES_PORT = 9300;

  /** The Client communicates with the Node inside the ElasticSearch cluster.*/
  private Client client;

  /** Port to which to connect when using a client that is not local. By default 9300 should be used. */
  @Value("${es.port:9300}")
  private int port;

  /** Name of the host holding the ElasticSearch cluster. */
  @Value("${es.host:localhost}")
  private String host;

  /** Name of the cluster. Must be set in order to connect to the right one in case there are several clusters running at the host. */
  @Value("${es.cluster:c2mon}")
  private String cluster;

  /** Name of the node in the cluster (more useful for debugging and to know which one is connected to the cluster). */
  @Value("${es.node:c2mon-indexing-transport-node}")
  private String node;

  /** Setting this to true will make the connector connect to a cluster inside the JVM. To false to a real cluster. */
  @Value("${es.local:true}")
  private boolean isLocal;

  /** Connection settings for the node according to the host, port, cluster, node and isLocal. */
  private Settings settings;

  /** Allows to send the data by batch. */
  private BulkProcessor bulkProcessor;

  /** Name of the BulkProcessor (more for debugging). */
  private final String bulkProcessorName = "ES-BulkProcessor";

  /** Used only if no setting is defined to connect to the cluster. */
  private Node localNode;

  /** Used to look for an ElasticSearch cluster when not connected to any. */
  Thread clusterFinder;

  private boolean isConnected;

  /** BulkSettings */
  @Value("${es.config.bulk.actions:5600}")
  private int bulkActions;
  @Value("${es.config.bulk.size:5}")
  private int bulkSize;
  @Value("${es.config.bulk.flush:10}")
  private int flushInterval;
  @Value("${es.config.bulk.concurrent:1}")
  private int concurrent;

  public TransportConnector() {
    declareClusterResearch();
  }

  /**
   * Instantiate the Client to communicate with the ElasticSearch cluster. If it
   * is well instantiated, retrieve the indices and and create a bulkProcessor
   * for batch writes.
   */
  @PostConstruct
  public void init() {
    if (!host.equalsIgnoreCase("localhost") && !host.equalsIgnoreCase("local")) {
      setLocal(false);
    }
    findClusterAndLaunchBulk();
    log.debug("init() - Initial test passed: Transport client is connected to the cluster " + cluster + ".");
    log.info("init() - Connected to cluster " + cluster + " with node " + node + ".");
  }

  /**
   * Declares a new Thread that is used to look for an ElasticSearch cluster.
   */
  public void declareClusterResearch() {
    clusterFinder = new Thread() {
      public void run() {
        try {
          do {
            initializationSteps();
            Thread.sleep(1000);
            isConnected = waitForYellowStatus();
          } while (!isConnected);
        } catch (InterruptedException e) {
          log.debug("clusterFinder - Interrupted.");
        }
      }
    };
  }

  /**
   * Called by an independent thread in order to look for an ElasticSearch cluster and connect to it.
   * This creates a Transport client that is able to communicate with the nodes.
   */
  public void initializationSteps() {
    if (isLocal) {
      setHost(LOCAL_HOST);
      setPort(LOCAL_PORT);

      localNode = launchLocalCluster();
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is enabled.");
    }
    else {
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is disabled.");
    }
    this.client = createClient();
  }

  /**
   * Need a transportClient to communicate with the ElasticSearch cluster.
   * @return Client to communicate with the ElasticSearch cluster.
   */
  public Client createClient() {
    if (isLocal) {
      this.settings = Settings.settingsBuilder().put("node.local", isLocal).put("node.name", node).put("cluster.name", cluster).build();

      log.debug("Creating local client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
      return TransportClient.builder().settings(settings).build()
          .addTransportAddress(new LocalTransportAddress(String.valueOf(port)));

    }
    else {
      this.settings = Settings.settingsBuilder().put("cluster.name", cluster).put("node.name", node).build();

      log.debug("Creating  client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");

      try {
        return TransportClient.builder().settings(settings).build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
      }
      catch (UnknownHostException e) {
        log.error("createTransportClient() - Error whilst connecting to the ElasticSearch cluster (host=" + host + ", port=" + port + ").", e);
        return null;
      }
    }
  }

  /**
   * Launch the Thread that is looking for an ElasticSearch cluster according to the parameters set.
   */
  public void findClusterAndLaunchBulk() {
    clusterFinder.start();
    log.debug("init() - Connecting to ElasticSearch cluster " + cluster + " on host=" + host + ", port=" + port + ".");

    try {
      clusterFinder.join();
    } catch (InterruptedException e) {
      log.warn("init() - Interruption of the Thread when trying to wait for clusterFinder.");
    }

    /** The Connector found a connection to a cluster. */
    initBulkSettings();
  }

  /**
   * Instantiate a BulkProcessor for batch writes. Settings are in the Enum
   * BulkSettings.
   */
  public void initBulkSettings() {
    this.bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("beforeBulk() - Going to execute new bulk composed of {} actions", request.numberOfActions());
      }

      /**
       * Some failure exist in the BulkProcessor, will try to redo them
       * and launch the fallback mechanism if connection is lost.
       */
      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debug("afterBulk() - Executed bulk composed of {} actions", request.numberOfActions());
        waitForYellowStatus();
        refreshClusterStats();
      }

      /**
       * Will try to send the data again and launch the Fallback mechanism if we lost connection to the cluster.
       */
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.warn("afterBulk() - Error executing bulk", failure);
        closeBulk();
        waitForYellowStatus();
      }
    })
        .setName(bulkProcessorName)
        .setBulkActions(bulkActions)
        .setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.GB))
        .setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
        .setConcurrentRequests(concurrent)
        .build();

    log.debug("initBulkSettings() - BulkProcessor created.");
  }

  /**
   * Allows to check if the cluster is well initialized.
   * 
   * @return if the query has been acked or not.
   */
  public boolean waitForYellowStatus() {
    if (client != null) {
      try {
        new QueryIndices(client).checkYellowStatus();
        log.debug("waitForYellowStatus() - Everything is alright.");
        return true;
      }
      catch (ClusterNotAvailableException e) {
        log.debug("waitForYellowStatus() - NoNodeAvailableException: cluster not reachable.");
        findClusterAndLaunchBulk();
        return false;
      }
    }
    else {
      log.warn("waitForYellowStatus() - client for the ElasticSearch cluster seems to have null value.");
      return false;
    }
  }

  /**
   * Add an indexRequest to the BulkProcessor in order to write data to ElasticSearch.
   */
  public boolean bulkAdd(IndexRequest request) {
    if (bulkProcessor != null && request != null) {
      bulkProcessor.add(request);
      log.trace("bulkAdd() - BulkProcessor will handle indexing of new index.");
      return true;
    }
    else {
      log.error("bulkProcessor is null. This should not happen!");
      return false;
    }
  }

  /**
   * Handles a query for the ElasticSearch cluster. This method only handles the
   * queries for listing admin values (indices, types and aliases)
   * 
   * @param query type of query.
   * @return List of retrieved responses according to the query.
   */
  @Override
  public List<String> handleListingQuery(Query query, String index) throws IDBPersistenceException {
    List<String> queryResponse = new ArrayList<>();

    if (client == null) {
      log.error("handleListingQuery() - Error: the client value is null.");
    }

    try {
      if (query instanceof QueryIndices) {
        log.debug("handleListingQuery() - Handling queryIndices.");
        queryResponse.addAll(((QueryIndices) query).getListOfAnswer());
      } else if (query instanceof QueryTypes) {
        log.debug("handleListingQuery() - Handling queryTypes.");
        queryResponse.addAll(((QueryTypes) query).getListOfAnswer(index));
      } else if (query instanceof QueryAliases) {
        log.debug("handleListingQuery() - Handling queryAliases.");
        queryResponse.addAll(((QueryAliases) query).getListOfAnswer(index));
      } else {
        log.error("handleListingQuery() - Unhandled query type.");
      }
    }
    catch (ClusterNotAvailableException e) {
      log.debug("handleListingQuery() - Cluster is not reachable, aborting listing retrieval. Throw new IDBPersistenceException.");
      throw new IDBPersistenceException();
    }

    return queryResponse;
  }

  @Override
  public boolean handleIndexQuery(String indexName, String type, String mapping) throws ClusterNotAvailableException {
    boolean isAcked = false;

    QueryIndexBuilder query = new QueryIndexBuilder(client);
    if (client == null) {
      log.error("handleIndexQuery() - Error: the client is " + client + ".");
      return isAcked;
    }

    isAcked = query.indexNew(indexName, type, mapping);
    return isAcked;
  }

  @Override
  public boolean handleAliasQuery(String indexMonth, String aliasName) {
    boolean isAcked = false;
    try {
      Query query = new QueryAliases(client);

      if (client == null) {
        log.error("handleAliasQuery() - Error: the client is " + client + ".");
        return isAcked;
      }

      isAcked = ((QueryAliases) query).addAlias(indexMonth, aliasName);
    }
    catch (ClusterNotAvailableException e) {
      log.debug("handleAliasQuery() - Cluster not available, alias creation cancelled.");
    }
    return isAcked;
  }

  @Override
  public boolean handleSupervisionQuery(String indexName, String mapping, SupervisionES supervisionES) throws IDBPersistenceException {
    boolean isAcked = false;
    try {
      SupervisionQuery supervisionQuery = new SupervisionQuery(client, supervisionES);

      if (client == null) {
        log.error("handleSupervisionQuery() - Error: the client is " + client + ".");
        return isAcked;
      }

      isAcked = supervisionQuery.logSupervisionEvent(indexName, mapping);
    }
    catch (ClusterNotAvailableException e) {
      log.debug("handleSupervisionQuery() - Cluster not available, sending SupervisionEvent to fallback file.");
      throw new IDBPersistenceException();
    }
    return isAcked;
  }

  @Override
  public boolean handleAlarmQuery(String indexName, String mapping, AlarmES alarmES) throws IDBPersistenceException {
    log.debug("handleAlarmQuery() - Handling AlarmESQuery with mapping:" + mapping + " for index: " + indexName + ".");
    boolean isAcked = false;
    try {
      AlarmESQuery alarmESQuery = new AlarmESQuery(client, alarmES);

      if (client == null) {
        log.error("handleAlarmQuery() - Error: the client is " + client + ".");
        return isAcked;
      }

      isAcked = alarmESQuery.logAlarmES(indexName, mapping);
    }
    catch (ClusterNotAvailableException e) {
      log.debug("handleAlarmQuery() - Cluster not available, sending Alarm to fallback file.");
      throw new IDBPersistenceException();
    }
    return isAcked;
  }

  public Set<String> updateIndices() throws IDBPersistenceException {
    Set<String> indices = new HashSet<>();
    if (client != null) {
      try {
        log.trace("updateIndices() - Updating list of indices.");
        indices.addAll(handleListingQuery(new QueryIndices(client), null));
      }
      catch (ClusterNotAvailableException e) {
        log.debug("updateIndices() - Cluster cannot be reached.");
        throw new IDBPersistenceException();
      }
    }
    return indices;
  }

  public Set<String> updateTypes(String index) throws IDBPersistenceException {
    Set<String> types = new HashSet<>();
    if (client != null) {
      try {
        log.trace("updateTypes() - Updating list of types.");
        types.addAll(handleListingQuery(new QueryTypes(client), index));
      }
      catch (ClusterNotAvailableException e) {
        log.debug("updateTypes() - Cluster cannot be reached.");
        throw new IDBPersistenceException();
      }
    }
    return types;
  }

  public Set<String> updateAliases(String index) throws IDBPersistenceException {
    Set<String> aliases = new HashSet<>();
    if (client != null) {
      try {
        log.trace("updateAliases() - Updating list of aliases.");
        aliases.addAll(handleListingQuery(new QueryAliases(client), index));
      }
      catch (ClusterNotAvailableException e) {
        log.debug("updateAliases() - Cluster cannot be reached.");
        throw new IDBPersistenceException();
      }
    }
    return aliases;
  }

  public void refreshClusterStats() {
    log.debug("refreshClusterStats()");
    client.admin().indices().prepareRefresh().execute().actionGet();
    client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
  }

  public Settings getIndexSettings(int shards, int replica) {
    return Settings.settingsBuilder().put("number_of_shards", shards)
        .put("number_of_replicas", replica).build();
  }

  /**
   * Launch a local (to the JVM) ElasticSearch cluster that will answer to the Queries.
   * @return Node with which we can communicate.
   */
  private Node launchLocalCluster() {
    String home = System.getProperty("c2mon.home") + "/log/elasticsearch-node/";
    setLocal(true);
    log.info("launchLocalCLuster() - Launch a new local cluster: home=" + home + ", clusterName=" + cluster + ".");

    return nodeBuilder().settings(Settings.settingsBuilder()
        .put("path.home", home)
        .put("cluster.name", cluster)
        .put("node.local", isLocal)
        .put("node.name", "ClusterNode")
        .put("node.data", true)
        .put("node.master", true)
        .put("http.enabled", true)
        .put("http.cors.enabled", true)
        .put("http.cors.allow-origin", "/.*/")
        .build())
        .node();
  }

  /**
   * Close the bulk after it sent enough: reached bulkActions, bulkSize or
   * flushInterval. And create a new one for further requests.
   */
  public void closeBulk() {
    try {
      bulkProcessor.awaitClose(10, TimeUnit.MILLISECONDS);
      refreshClusterStats();
      log.debug("closeBulk() - closing bulkProcessor");
    }
    catch (InterruptedException e) {
      log.warn("closeBulk() - Error whilst awaitClose() the bulkProcessor.", e);
    }
    initBulkSettings();
  }

  /**
   * Method called to close the newly opened client.
   *
   * @param client transportClient for the cluster.
   */
  @Override
  public void close(Client client) {
    if (client != null) {
      client.close();
      log.info("close() - Closed client: " + client.settings().get("node.name"));
    }
  }

  /**
   * Retrieve the failed actions of the BulkProcessor.
   */
  private List<Integer> getFailedActions(BulkResponse bulkResponse) {
    List<Integer> failedActions = new ArrayList<>();
    if (bulkResponse.hasFailures()) {
      for (BulkItemResponse item : bulkResponse.getItems()) {
        if (item.isFailed()) {
          failedActions.add(item.getItemId());
        }
      }
    }
    return failedActions;
  }
}