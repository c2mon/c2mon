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
package cern.c2mon.server.eslog.connector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import cern.c2mon.server.eslog.structure.types.tag.EsTag;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Allows to connect to the cluster via a transport client. Handles all the
 * queries and writes data thanks to a bulkProcessor for {@link EsTag}
 * or 1 by 1 for  {@link EsAlarm} and {@link EsSupervisionEvent} to the ElasticSearch cluster.
 * This is very light for the cluster to be connected this way.
 *
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public class TransportConnector implements Connector {
  /**
   * Only used, if elasticSearch is started inside this JVM
   */
  private final static int LOCAL_PORT = 1;
  private final static String LOCAL_HOST = "local";

  private final static String TYPE_ALARM = "alarm";
  private final static String TYPE_SUPERVISION = "supervision";

  /**
   * Default port for elastic search transport node
   */
  public final static int DEFAULT_ES_PORT = 9300;

  /**
   * The Client communicates with the Node inside the ElasticSearch cluster.
   */
  private Client client;

  /**
   * Port to which to connect when using a client that is not local. By default 9300 should be used.
   */
  @Value("${c2mon.server.eslog.port}")
  private int port;

  /**
   * Name of the host holding the ElasticSearch cluster.
   */
  @Value("${c2mon.server.eslog.host}")
  private String host;

  /**
   * Name of the cluster. Must be set in order to connect to the right one in case there are several clusters running at the host.
   */
  @Value("${c2mon.server.eslog.cluster}")
  private String cluster;

  /**
   * Name of the node in the cluster (more useful for debugging and to know which one is connected to the cluster).
   */
  @Value("${c2mon.server.eslog.node}")
  private String node;

  /**
   * Setting this to true will make the connector connect to a cluster inside the JVM. To false to a real cluster.
   */
  @Value("${c2mon.server.eslog.local}")
  private boolean isLocal;

  @Value("${c2mon.server.eslog.httpEnabled:false}")
  private boolean httpEnabled;

  /**
   * BulkSettings
   */
  @Value("${c2mon.server.eslog.config.bulk.actions}")
  private int bulkActions;

  @Value("${c2mon.server.eslog.config.bulk.size}")
  private int bulkSize;

  @Value("${c2mon.server.eslog.config.bulk.flush}")
  private int flushInterval;

  @Value("${c2mon.server.eslog.config.bulk.concurrent}")
  private int concurrent;

  @Autowired
  private Environment environment;

  /**
   * Connection settings for the node according to the host, port, cluster, node and isLocal.
   */
  private Settings settings = Settings.EMPTY;

  /**
   * Allows to send the data by batch.
   */
  private BulkProcessor bulkProcessor;

  /**
   * Name of the BulkProcessor (more for debugging).
   */
  private final String bulkProcessorName = "ES-BulkProcessor";

  /**
   * Used only if no setting is defined to connect to the cluster.
   */
  private Node localNode;

  /**
   * True if connected to ElasticSearch
   */
  private boolean isConnected;


  /**
   * Instantiate the Client to communicate with the ElasticSearch cluster. If it
   * is well instantiated, retrieve the indices and and create a bulkProcessor for batch writes.
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
   * Called by an independent thread in order to look for an ElasticSearch cluster and connect to it.
   * This creates a transport client that is able to communicate with the nodes.
   */
  private void initializationSteps() {
    if (isLocal) {
      setHost(LOCAL_HOST);
      setPort(LOCAL_PORT);

      localNode = launchLocalCluster();
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is enabled.");
    } else {
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is disabled.");
    }

    client = createClient();
  }

  /**
   * Creates a {@link Client} to communicate with the ElasticSearch cluster.
   *
   * @return the {@link Client} instance.
   */
  private Client createClient() {
    final Settings.Builder settingsBuilder = Settings.settingsBuilder();

    settingsBuilder.put("node.name", node)
            .put("cluster.name", cluster)
            .put("http.enabled", httpEnabled);

    if (isLocal) {
      this.settings = settingsBuilder.put("node.local", true)
              .build();

      log.debug("Creating local client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
      return TransportClient.builder()
              .settings(settings)
              .build()
              .addTransportAddress(new LocalTransportAddress(String.valueOf(port)));
    }

    this.settings = settingsBuilder.build();

    log.debug("Creating  client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
    try {
      return TransportClient.builder()
              .settings(settings)
              .build()
              .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    } catch(UnknownHostException e) {
      log.error("createTransportClient() - Error whilst connecting to the ElasticSearch cluster (host=" + host + ", port=" + port + ").", e);
      return null;
    }
  }

  /**
   * Launch the Thread that is looking for an ElasticSearch cluster,
   * according to the parameters set.
   */
  private void findClusterAndLaunchBulk() {
    Thread clusterFinder = new Thread(() -> {
      try {
        do {
          initializationSteps();
          Thread.sleep(1000);
          isConnected = waitForYellowStatus();
        } while(!isConnected);
      } catch(InterruptedException e) {
        log.debug("clusterFinder - Interrupted.");
      }
    }, "C2MON-ES-Module-Cluster-Health-Check");

    clusterFinder.start();
    log.debug("init() - Connecting to ElasticSearch cluster " + cluster + " on host=" + host + ", port=" + port + ".");

    try {
      clusterFinder.join();
    } catch(InterruptedException e) {
      log.warn("init() - Interruption of the Thread when trying to wait for clusterFinder.");
    }

    //The Connector found a connection to a cluster.
    initBulkProcessor();

  }

  /**
   * Instantiate a BulkProcessor for batch writings.
   */
  private void initBulkProcessor() {
    this.bulkProcessor = BulkProcessor.builder(client, createBulkProcessorListener())
            .setName(bulkProcessorName)
            .setBulkActions(bulkActions)
            .setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.GB))
            .setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
            .setConcurrentRequests(concurrent)
            .build();

    log.debug("initBulkSettings() - BulkProcessor created.");
  }

  /**
   * Creates a new {@link BulkProcessor.Listener}.
   *
   * @return {@link BulkProcessor.Listener} instance
   */
  private BulkProcessor.Listener createBulkProcessorListener() {
    return new BulkProcessor.Listener() {
      @Override
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("beforeBulk() - Going to execute new bulk composed of {} actions", request.numberOfActions());
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debug("afterBulk() - Executed bulk composed of {} actions", request.numberOfActions());
        waitForYellowStatus();
        refreshClusterStats();
      }

      @Override
      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.warn("afterBulk() - Error executing bulk", failure);
        waitForYellowStatus();
      }
    };
  }

  /**
   * Allows to check if the cluster is well initialized.
   *
   * @return if the query has been acknowledged or not.
   */
  @Override
  public boolean waitForYellowStatus() {
    if (client == null) {
      log.warn("waitForYellowStatus() - client for the ElasticSearch cluster seems to have null value.");
      return false;
    }

    checkYellowStatus();
    log.debug("waitForYellowStatus() - Everything is alright.");
    return true;
  }

  /**
   * Add an indexRequest to the BulkProcessor in order to write data to ElasticSearch.
   */
  @Override
  public boolean bulkAdd(IndexRequest request) {
    if (bulkProcessor == null) {
      log.error("bulkProcessor is null. This should not happen!");
      return false;
    }

    if (request == null) {
      return false;
    }

    bulkProcessor.add(request);
    log.trace("bulkAdd() - BulkProcessor will handle indexing of new index.");
    return true;
  }

  @Override
  public boolean createIndexTypeMapping(String index, String type, String mapping) {
    if (client == null) {
      log.error("Elasticsearch connection not (yet) initialized.");
      return false;
    }

    PutMappingResponse response = null;
    try {
      response = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping).get();
    } catch (Exception e) {
      log.error("Error occured whilst preparing the mapping for index={}, type={}, mapping={}", index, type, mapping, e);
    }

    return response == null ? false : response.isAcknowledged();
  }

  /**
   * @return the set of indices present in ElasticSearch.
   */
//  @Override
  public Set<String> retrieveIndicesFromES() {
    if (client == null) {
      return Collections.emptySet();
    }

    log.trace("updateIndices() - Updating list of indices.");
    return getListOfIndicesFromES().stream()
            .distinct()
            .collect(Collectors.toSet());
  }

  /**
   * @return the set of types associated to the index {@param index} in ElasticSearch.
   */
//  @Override
  public Set<String> retrieveTypesFromES(String index) {
    if (client == null) {
      return Collections.emptySet();
    }

    log.trace("updateTypes() - Updating list of types.");
    return getTypesFromES(index).stream()
            .distinct()
            .collect(Collectors.toSet());
  }

  /**
   * For near real-time retrieval.
   */
  @Override
  public void refreshClusterStats() {
    log.debug("refreshClusterStats()");
    client.admin().indices().prepareRefresh().execute().actionGet();
    checkYellowStatus();
  }

  /**
   * Launch a local (to the JVM) ElasticSearch cluster that will answer to the Queries.
   *
   * @return Node with which we can communicate.
   */
  private Node launchLocalCluster() {
    String home = environment.getRequiredProperty("c2mon.server.eslog.home");
    setLocal(true);
    log.info("Launching an embedded elasticsearch cluster: home=" + home + ", clusterName=" + cluster);

    return nodeBuilder().settings(Settings.settingsBuilder()
            .put("path.home", home)
            .put("cluster.name", cluster)
            .put("node.local", isLocal)
            .put("node.name", "ClusterNode")
            .put("node.data", true)
            .put("node.master", true)
            .put("network.host", "0.0.0.0")
            .put("http.enabled", true)
            .put("http.cors.enabled", true)
            .put("http.cors.allow-origin", "/.*/")
            .build())
            .node();
  }

  /**
   * Method called to close the newly opened client.
   *
   * @param transportClient {@link Client} for the cluster.
   */
  @Override
  public void close(Client transportClient) {
    if (transportClient != null) {
      transportClient.close();
      log.info("close() - Closed client: " + transportClient.settings().get("node.name"));
    }
  }

  /***************************************
   *
   * Queries
   *
   ***************************************/


  /**
   * Yellow status means the ElasticSearch is queryable.
   */
  public void checkYellowStatus() {
    client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
  }

  /**
   * Retrieve all the indices in ElasticSearch as a String array.
   */
  protected List<String> getIndicesFromCluster() {
    String[] indices = client.admin().indices().prepareGetIndex().get().indices();
    return Arrays.asList(indices);
  }

  /**
   * Query the cluster in order to do an Index operation.
   */
  protected CreateIndexRequestBuilder prepareCreateIndexRequestBuilder(String index) {
    return client.admin().indices().prepareCreate(index);
  }

  /**
   * Query the cluster in order to check, whether the given index is  existing
   */
  private boolean isIndexExisting(String index) {
    ActionFuture<IndicesExistsResponse> response = client.admin().indices().exists(new IndicesExistsRequest(index));

    return response.actionGet(1000L).isExists();
  }

  /**
   * Allows to retrieve all the mappings inside a cluster.
   */
  protected Iterator<ObjectCursor<IndexMetaData>> getIndicesWithMetadata() {
    return client.admin().cluster().prepareState().get().getState().getMetaData().indices().values().iterator();
  }

  /**
   * Allows to retrieve the mappings of an index in ElasticSearch.
   */
  protected ImmutableOpenMap<String, MappingMetaData> getIndexWithMetadata(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getMappings();
  }

  /**
   * @return true if the specified index exists.
   */
  protected boolean indexExists(String indexName) {
    if (Strings.isNullOrEmpty(indexName)) {
      return false;
    }

    log.debug("indexExists() - Look for the existence of the index " + indexName + ".");
    return client.admin().indices().prepareExists(indexName).get().isExists();
  }

  /**
   * Write a new {@link EsAlarm} to ElasticSearch.
   *
   * @param indexName to contain the data.
   * @param mapping   as JSON.
   * @param esAlarm   to be written.
   * @return true if the cluster has acknowledged the query.
   */
  @Override
  public boolean logAlarmEvent(String indexName, String mapping, EsAlarm esAlarm) {
    if (client == null) {
      log.error("Elasticsearch connection not (yet) initialized.");
      return false;
    }

    log.debug("logAlarmES() - Try to write new alarm event to in index = {}, mapping = {}.", indexName, mapping);

    String jsonSource = esAlarm.toString();
    String routing = String.valueOf(esAlarm.getAlarmId());

    if (indexExists(indexName)) {
      log.debug("logAlarmES() - Add new Alarm event to index " + indexName + ".");
      return client.prepareIndex().setIndex(indexName)
              .setType(TYPE_ALARM)
              .setSource(jsonSource)
              .setRouting(routing)
              .get().isCreated();
    }

    if (Strings.isNullOrEmpty(mapping)) {
      return false;
    }

    log.debug("logAlarmES() - Source query is: " + jsonSource + ".");
    return prepareCreateIndexRequestBuilder(indexName).setSource(mapping).get().isAcknowledged();
  }

  /**
   * Write a new {@link EsSupervisionEvent} to ElasticSearch.
   *
   * @param indexName          to contain the data.
   * @param mapping            as JSON.
   * @param esSupervisionEvent to be written.
   * @return true if the cluster has acknowledged the query.
   */
  @Override
  public boolean logSupervisionEvent(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent) {
    if (client == null) {
      log.error("Elasticsearch connection not (yet) initialized.");
      return false;
    }

    String jsonSource = esSupervisionEvent.toString();
    String routing = esSupervisionEvent.getId();

    if (indexExists(indexName)) {
      log.debug("logSupervisionEvent() - Add new Supervision event to index " + indexName + ".");
      return client.prepareIndex().setIndex(indexName)
              .setType(TYPE_SUPERVISION)
              .setSource(jsonSource)
              .setRouting(routing)
              .get()
              .isCreated();
    }

    if (Strings.isNullOrEmpty(mapping)) {
      return false;
    }

    log.debug("logSupervisionEvent() - Source query is: " + jsonSource + ".");
    return prepareCreateIndexRequestBuilder(indexName).setSource(mapping).get().isAcknowledged();

  }

  /**
   * Simple query to get all the indices in the cluster.
   *
   * @return List<String>: names of the indices.
   */
  public List<String> getListOfIndicesFromES() {
    if (client == null) {
      log.warn("getListOfIndicesFromES() - Warning: client has value " + client + ".");
      return Collections.emptyList();
    }

    List<String> indicesFromCluster = getIndicesFromCluster();
    log.debug("getListOfIndicesFromES() - got a list of indices, size=" + indicesFromCluster.size());

    return indicesFromCluster;
  }

  /**
   * Simple query to get all the types of the {@param index}.
   */
  public Collection<String> getTypesFromES(String index) {
    if (index == null) {
      return Collections.emptySet();
    }
    final Collection<String> types = Sets.newHashSet(getIndexWithMetadata(index).keysIt());

    log.info("QueryTypes - Got a list of types, size= " + types.size());
    return types;
  }

  @Override
  public boolean createIndex(String indexName) {
    if (client == null) {
      log.error("Elasticsearch connection not (yet) initialized.");
      return false;
    }

    if (isIndexExisting(indexName)) {
      return true;
    }

    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(indexName);
    try {
      CreateIndexResponse response = createIndexRequestBuilder.get();
      return response.isAcknowledged();
    } catch (IndexAlreadyExistsException ex) {
      return true;
    }
  }
}
