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

import cern.c2mon.server.eslog.structure.mappings.EsMapping;
import cern.c2mon.server.eslog.structure.types.EsAlarm;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import com.carrotsearch.hppc.ObjectContainer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import cern.c2mon.server.eslog.structure.types.EsTagImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * Allows to connect to the cluster via a transport client. Handles all the
 * queries and writes data thanks to a bulkProcessor for {@link EsTagImpl}
 * or 1 by 1 for  {@link EsAlarm} and {@link EsSupervisionEvent} to the ElasticSearch cluster.
 * This is very light for the cluster to be connected this way.
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
  @Value("${c2mon.server.eslog.port}")
  private int port;

  /** Name of the host holding the ElasticSearch cluster. */
  @Value("${c2mon.server.eslog.host}")
  private String host;

  /** Name of the cluster. Must be set in order to connect to the right one in case there are several clusters running at the host. */
  @Value("${c2mon.server.eslog.cluster}")
  private String cluster;

  /** Name of the node in the cluster (more useful for debugging and to know which one is connected to the cluster). */
  @Value("${c2mon.server.eslog.node}")
  private String node;

  /** Setting this to true will make the connector connect to a cluster inside the JVM. To false to a real cluster. */
  @Value("${c2mon.server.eslog.local}")
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
  private Thread clusterFinder;

  /** True if connected to ElasticSearch */
  private boolean isConnected;

  /** BulkSettings */
  @Value("${c2mon.server.eslog.config.bulk.actions}")
  private int bulkActions;
  @Value("${c2mon.server.eslog.config.bulk.size}")
  private int bulkSize;
  @Value("${c2mon.server.eslog.config.bulk.flush}")
  private int flushInterval;
  @Value("${c2mon.server.eslog.config.bulk.concurrent}")
  private int concurrent;

  /** Launch a Thread to discover the ElasticSearch cluster. */
  public TransportConnector() {
    declareClusterResearch();
  }

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

  /** Declares a new Thread that is used to look for an ElasticSearch cluster. */
  private void declareClusterResearch() {
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
   * This creates a transport client that is able to communicate with the nodes.
   */
  private void initializationSteps() {
    if (isLocal) {
      setHost(LOCAL_HOST);
      setPort(LOCAL_PORT);

      localNode = launchLocalCluster();
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is enabled.");
    }
    else {
      log.info("init() - Connecting to local ElasticSearch instance (inside same JVM) is disabled.");
    }
    client = createClient();
  }

  /**
   * Need a transport client to communicate with the ElasticSearch cluster.
   * @return Client to communicate with the ElasticSearch cluster.
   */
  private Client createClient() {
//    if (client != null) {
//      return client;
//    }

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

  /** Launch the Thread that is looking for an ElasticSearch cluster according to the parameters set. */
  private void findClusterAndLaunchBulk() {
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

  /** Instantiate a BulkProcessor for batch writings. */
  private void initBulkSettings() {
    this.bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("beforeBulk() - Going to execute new bulk composed of {} actions", request.numberOfActions());
      }

      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debug("afterBulk() - Executed bulk composed of {} actions", request.numberOfActions());
        waitForYellowStatus();
        refreshClusterStats();
      }

      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.warn("afterBulk() - Error executing bulk", failure);
        //closeBulk();
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
   * @return if the query has been acknowledged or not.
   */
  public boolean waitForYellowStatus() {
    if (client != null) {
      checkYellowStatus();
      log.debug("waitForYellowStatus() - Everything is alright.");
      return true;
    }
    else {
      log.warn("waitForYellowStatus() - client for the ElasticSearch cluster seems to have null value.");
      return false;
    }
  }

  /** Add an indexRequest to the BulkProcessor in order to write data to ElasticSearch. */
  public boolean bulkAdd(IndexRequest request) {
    if (bulkProcessor != null && request != null) {
      bulkProcessor.add(request);
      log.trace("bulkAdd() - BulkProcessor will handle indexing of new index.");
      return true;
    }
    else {
      log.error("bulkProcessor is null. This should not happen!");
    }
    return false;
  }

  /**
   * Create a new QueryIndexBuilder and execute it to create a new index with name {@param indexName} and
   * @param type and {@param mapping} as JSON.
   * @return true if acknowledged by the cluster.
   */
  @Override
  public boolean handleIndexQuery(String indexName, String type, String mapping) {
    boolean isAcked = false;
    if (client == null) {
      log.error("handleIndexQuery() - Error: the client is " + client + ".");
      return isAcked;
    }

    isAcked = indexNew(indexName, type, mapping);
    return isAcked;
  }

  /**
   * Create a new alias aliasName inside the index indexName.
   * @param indexName index to which add the alias.
   *                   must be of format "indexPrefix_index-format".
   * @param aliasName reference to be used on the client side.
   *                  must of format "tag_tagname".
   * @return the acknowledgement.
   */
  @Override
  public boolean handleAliasQuery(String indexName, String aliasName) {
    boolean isAcked = false;
    if (client == null) {
      log.error("handleAliasQuery() - Error: the client is " + client + ".");
      return isAcked;
    }

    isAcked = addAliasToES(indexName, aliasName);
    return isAcked;
  }

  /**
   * Add a new {@link EsSupervisionEvent} to the ElasticSearch cluster.
   * @param indexName to which add the data.
   * @param mapping as JSON.
   * @param esSupervisionEvent the data.
   * @return true if acknowledged by the cluster.
   */
  @Override
  public boolean handleSupervisionQuery(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent) {
    boolean isAcked = false;
    if (client == null) {
      log.error("handleSupervisionQuery() - Error: the client is " + client + ".");
      return isAcked;
    }

    isAcked = logSupervisionEvent(indexName, mapping, esSupervisionEvent);
    return isAcked;
  }

  /**
   * Add a new {@link EsAlarm} to the ElasticSearch cluster.
   * @param indexName to which add the data.
   * @param mapping as JSON.
   * @param esAlarm the data.
   * @return true if acknowledged by the cluster.
   */
  @Override
  public boolean handleAlarmQuery(String indexName, String mapping, EsAlarm esAlarm) {
    log.debug("handleAlarmQuery() - Handling AlarmESQuery with mapping:" + mapping + " for index: " + indexName + ".");
    boolean isAcked = false;

    if (client == null) {
      log.error("handleAlarmQuery() - Error: the client is " + client + ".");
      return isAcked;
    }

    isAcked = logAlarmES(indexName, mapping, esAlarm);
    return isAcked;
  }

  /** @return the set of indices present in ElasticSearch. */
  public Set<String> retrieveIndicesFromES() {
    Set<String> indices = new HashSet<>();
    if (client != null) {
      log.trace("updateIndices() - Updating list of indices.");
      indices.addAll(getListOfIndicesFromES());
    }
    return indices;
  }

  /** @return the set of types associated to the index {@param index} in ElasticSearch.*/
  public Set<String> retrieveTypesFromES(String index) {
    Set<String> types = new HashSet<>();
    if (client != null) {
      log.trace("updateTypes() - Updating list of types.");
      types.addAll(getListOfTypesFromES(index));
    }
    return types;
  }

  /** @return the set of aliases associated to the index {@param index} in ElasticSearch.*/
  public Set<String> retrieveAliasesFromES(String index) {
    Set<String> aliases = new HashSet<>();
    if (client != null) {
      log.trace("updateAliases() - Updating list of aliases.");
      aliases.addAll(getListOfAliasesFromES(index));
    }
    return aliases;
  }

  /** For near real-time retrieval. */
  public void refreshClusterStats() {
    log.debug("refreshClusterStats()");
    client.admin().indices().prepareRefresh().execute().actionGet();
    client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
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
   * Close the bulk after it sent enough: reached bulkActions, bulkSize or flushInterval.
   * And create a new one for further requests.
   */
//  public void closeBulk() {
//    try {
//      bulkProcessor.awaitClose(10, TimeUnit.MILLISECONDS);
//      refreshClusterStats();
//      log.debug("closeBulk() - closing bulkProcessor");
//    }
//    catch (InterruptedException e) {
//      log.warn("closeBulk() - Error whilst awaitClose() the bulkProcessor.", e);
//    }
//    initBulkSettings();
//  }

  /**
   * Method called to close the newly opened client.
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


  /***************************************
   *
   * Queries
   *
   ***************************************/


  /** Yellow status means the ElasticSearch is queryable. */
  public void checkYellowStatus() {
    client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
  }

  /** Retrieve all the indices in ElasticSearch as a String array. */
  protected String[] getIndicesFromCluster() {
    return client.admin().indices().prepareGetIndex().get().indices();
  }

  /** Query the cluster in order to do an Index operation. */
  protected CreateIndexRequestBuilder prepareCreateIndexRequestBuilder(String index) {
    return client.admin().indices().prepareCreate(index);
  }

  /** Query the cluster in order to do an Alias operation. */
  protected IndicesAliasesRequestBuilder prepareAliases() {
    return client.admin().indices().prepareAliases();
  }

  /** Allows to retrieve all the mappings inside a cluster. */
  protected Iterator<ObjectCursor<IndexMetaData>> getIndicesWithMetadata() {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().values().iterator();
  }

  /** Allows to retrieve the mappings of an index in ElasticSearch. */
  protected ImmutableOpenMap<String, MappingMetaData> getIndexWithMetadata(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getMappings();
  }

  /** Retrieve the aliases associated to the specified index. */
  protected ObjectContainer<AliasMetaData> getAliases(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getAliases().values();
  }

  /** @return true if the specified index exists. */
  protected boolean indexExists(String indexName) {
    log.debug("indexExists() - Look for the existence of the index " + indexName + ".");
    return client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
  }

  /**
   * Write a new {@link EsAlarm} to ElasticSearch.
   * @param indexName to contain the data.
   * @param mapping as JSON.
   * @param esAlarm to be written.
   * @return true if the cluster has acknowledged the query.
   */
  public boolean logAlarmES(String indexName, String mapping, EsAlarm esAlarm) {
    log.debug("logAlarmES() - Try to create a writing query for Alarm.");
    String jsonSource = esAlarm.toString();
    String routing = String.valueOf(esAlarm.getAlarmId());

    if (!indexExists(indexName) && mapping != null) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
      log.debug("logAlarmES() - Source query is: " + jsonSource + ".");
    }

    if (indexExists(indexName)) {
      log.debug("logAlarmES() - Add new Alarm event to index " + indexName + ".");
      IndexResponse response = client.prepareIndex().setIndex(indexName).setType(EsMapping.ValueType.alarmType.toString()).setSource(jsonSource).setRouting(routing).execute().actionGet();
      return response.isCreated();

    }

    return false;
  }

  /**
   * Write a new {@link EsSupervisionEvent} to ElasticSearch.
   * @param indexName to contain the data.
   * @param mapping as JSON.
   * @param esSupervisionEvent to be written.
   * @return true if the cluster has acknowledged the query.
   */
  public boolean logSupervisionEvent(String indexName, String mapping, EsSupervisionEvent esSupervisionEvent) {
    String jsonSource = esSupervisionEvent.toString();
    String routing = esSupervisionEvent.getId();
    if (!indexExists(indexName) && mapping != null) {
      client.admin().indices().prepareCreate(indexName).setSource(mapping).execute().actionGet();
      log.debug("logSupervisionEvent() - Source query is: " + jsonSource + ".");
    }

    if (indexExists(indexName)) {
      log.debug("logSupervisionEvent() - Add new Supervision event to index " + indexName + ".");
      IndexResponse response = client.prepareIndex().setIndex(indexName).setType(EsMapping.ValueType.supervisionType.toString()).setSource(jsonSource).setRouting(routing).execute().actionGet();
      return response.isCreated();
    }
    return false;
  }

  /**
   * Simple query to get all the indices in the cluster.
   * @return List<String>: names of the indices.
   */
  public List<String> getListOfIndicesFromES() {
    if (client != null) {
      String[] indicesFromCluster = getIndicesFromCluster();
      log.debug("getListOfAnswer() - got a list of indices, size=" + indicesFromCluster.length);
      return Arrays.asList(indicesFromCluster);
    }
    else {
      log.warn("getListOFAnswer() - Warning: client has value " + client + ".");
      return new ArrayList<>();
    }
  }

  /** Simple query to get all the types of the {@param index}. */
  public List<String> getListOfTypesFromES(String index) {
    List<String> result = new ArrayList<>();
    if (index != null) {
      Iterator<ObjectCursor<String>> typesIt = getIndexWithMetadata(index).keys().iterator();
      addTypesToResult(typesIt, result);
      log.info("QueryTypes - Got a list of types, size= " + result.size());
    }
    return result;
  }

  private void addTypesToResult(Iterator<ObjectCursor<String>> typesIt, List<String> result) {
    while(typesIt.hasNext()) {
      result.add(typesIt.next().value);
    }
  }

  private boolean addAliasToES(String indexMonth, String aliasName) {
    if (client != null) {
      String tagId = aliasName.substring("tag_".length());
      String json = "{\"term\" : { \"id\" : " + tagId + " } }";
      IndicesAliasesRequestBuilder preparedAliases = prepareAliases();
      IndicesAliasesResponse response = preparedAliases.addAlias(indexMonth, aliasName, json).execute().actionGet();
      return response.isAcknowledged();
    }
    else {
      log.debug("addAlias() - client has null value.");
    }

    return false;
  }

  /** Simple query to get all the aliases of the {@param index}. */
  public List<String> getListOfAliasesFromES(String index) {
    List<String> result = new ArrayList<>();

    if (index != null) {
      Iterator<ObjectCursor<AliasMetaData>> aliasesIt = getAliases(index).iterator();
      addAliasesToResult(aliasesIt, result);
      log.info("QueryAliases - got a list of aliases, size=" + result.size());
    }
    return result;
  }

  private void addAliasesToResult(Iterator<ObjectCursor<AliasMetaData>> aliases, List<String> result) {
    while(aliases.hasNext()) {
      result.add(aliases.next().value.getAlias());
    }
  }

  /**
   * Write a new index to ElasticSearch if {@param type} and {@param mapping} are null.
   * Write a new mapping to the index {@param index} otherwise.
   * @return true if the cluster has acknowledged the query.
   */
  public boolean indexNew(String index, String type, String mapping) {
    if (type == null && mapping == null) {
      return handleAddingIndex(index);
    }
    else {
      return handleAddingMapping(index, type, mapping);
    }
  }

  private boolean handleAddingIndex(String index) {
    CreateIndexRequestBuilder createIndexRequestBuilder = prepareCreateIndexRequestBuilder(index);
    CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet();
    return response.isAcknowledged();
  }

  private boolean handleAddingMapping(String index, String type, String mapping) {
    PutMappingResponse response = client.admin().indices().preparePutMapping(index).setType(type).setSource(mapping).execute().actionGet();
    return response.isAcknowledged();
  }
}
