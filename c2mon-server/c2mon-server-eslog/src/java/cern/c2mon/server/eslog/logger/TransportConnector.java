package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.queries.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
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
 * queries with a bulkProcessor to the ElasticSearch cluster. This is very light
 * for the cluster to be connected this way.
 * 
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public class TransportConnector implements Connector {
  /** Only used, if elasticsearch is started inside this JVM */
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

  @Value("${es.index.settings:INDEX_MONTH_SETTINGS}}")
  private String indexSettings;

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

  private final HashMap<String, Integer> bulkSettings = new HashMap<>();

  public TransportConnector() {
    declareClusterResearch();
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
          } while (!initTestPass());
        } catch (InterruptedException e) {
          log.debug("clusterFinder - Interrupted.");
        }
      }
    };
  }


  /*****************************************************************************
   * 
   * INITIALIZATION
   * 
   ****************************************************************************/


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

    log.debug("init() - initial test passed: Transport client is connected to the cluster " + cluster + ".");
    log.info("init() - Connected to cluster " + cluster + " with node " + node + ".");
    isConnected = true;
  }

  /**
   * Launch the Thread that is looking for an ElasticSearch cluster according to the parameters set.
   */
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
   * Instantiate a BulkProcessor for batch writes. Settings are in the Enum
   * BulkSettings.
   */
  public void initBulkSettings() {
    bulkSettings.put("bulkActions", BulkSettings.BULK_ACTIONS.getSetting());
    bulkSettings.put("bulkSize", BulkSettings.BULK_SIZE.getSetting());
    bulkSettings.put("flushInterval", BulkSettings.FLUSH_INTERVAL.getSetting());
    bulkSettings.put("concurrent", BulkSettings.CONCURRENT.getSetting());

    this.bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {
      public void beforeBulk(long executionId, BulkRequest request) {
        log.debug("Going to execute new bulk composed of {} actions", request.numberOfActions());
      }

      public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        log.debug("afterBulk() - Executed bulk composed of {} actions", request.numberOfActions());
        refreshClusterStats();
      }

      public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
        log.warn("Error executing bulk", failure);
      }
    })
        .setName(bulkProcessorName)
        .setBulkActions(bulkSettings.get("bulkActions"))
        .setBulkSize(new ByteSizeValue(bulkSettings.get("bulkSize"), ByteSizeUnit.GB))
        .setFlushInterval(TimeValue.timeValueSeconds(bulkSettings.get("flushInterval")))
        .setConcurrentRequests(bulkSettings.get("concurrent"))
        .build();

    log.debug("BulkProcessor created.");
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
   * Method that query the cluster to list all the present indices. Allows to
   * check if the cluster is well initialized.
   * 
   * @return if the query has been acked or not.
   */
  public boolean initTestPass() {
    if (client != null) {
      try {
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
        log.debug("initTestPass() - Everything is allright.");
        return true;

      } catch (NoNodeAvailableException e) {
        log.debug("initTestPass() - NoNodeAvailableException: cluster not reachable.");
        return false;
      }
    }
    else {
      log.warn("initTestPass() - client for the ElasticSearch cluster seems to have null value.");
      return false;
    }
  }

  public boolean bulkAdd(IndexRequest indexNewTag) {
    if (bulkProcessor != null && indexNewTag != null) {
      bulkProcessor.add(indexNewTag);
      log.trace("bulkAdd() - BulkProcessor will handle indexing of new index.");
      return true;
    }
    else {
      log.error("bulkProcessor is null. This should not happen!");
      return false;
    }
  }

  public void launchFallBackMechanism(IndexRequest indexNewTag) {
    findClusterAndLaunchBulk();
    saveDataToFile(indexNewTag);
  }

  private void saveDataToFile(IndexRequest indexNewTag) {
    //TODO
  }


  /*****************************************************************************
   * 
   * UTILITY FOR INDEXING
   * 
   ****************************************************************************/


  /**
   * Handles a query for the ElasticSearch cluster. This method only handles the
   * queries for listing admin values (indices, types and aliases)
   * 
   * @param query type of query.
   * @return List of retrieved responses according to the query.
   */
  @Override
  public List<String> handleListingQuery(Query query) {
    List<String> queryResponse = new ArrayList<>();

    if (client == null) {
      log.error("handleListingQuery() - Error: the client value is null.");
    }

    if (query instanceof QueryIndices) {
      log.debug("handleListingQuery() - Handling queryIndices.");
      queryResponse.addAll(((QueryIndices) query).getListOfAnswer());

    }
    else if (query instanceof QueryAliases) {
      log.debug("handleListingQuery() - Handling queryAliases.");
      queryResponse.addAll(((QueryAliases) query).getListOfAnswer());
    }
    else if (query instanceof QueryTypes) {
      log.debug("handleListingQuery() - Handling queryTypes.");
      queryResponse.addAll(((QueryTypes) query).getListOfAnswer());
    }
    else {
      log.error("handleListingQuery() - Unhandled query type.");
    }

    return queryResponse;
  }

  /**
   * Handles an indexing query for the ElasticSearch cluster. This method allows
   * to add a new index/type if needed directly to the cluster in addition to
   * adding the data itself.
   *
   * @param indexName of the index to add the data to.
   * @param settings of the index in the cluster.
   * @param type of the document to add to the cluster.
   * @param mapping contains the information for the indexing: routing, fields,
   *          fields types...
   * @return true if the client has been acked.
   */
  @Override
  public boolean handleIndexQuery(String indexName, Settings settings, String type, String mapping) {
    QueryIndexBuilder query = new QueryIndexBuilder(client);
    boolean isAcked = false;

    if (client == null) {
      log.error("handleIndexQuery() - Error: the client value is null.");
      return isAcked;
    }

    isAcked = query.indexNew(indexName, settings, type, mapping);
    return isAcked;
  }

  @Override
  public boolean handleAliasQuery(String indexMonth, String aliasName) {
    Query query = new QueryAliases(client);
    boolean isAcked = false;

    if (client == null) {
      log.error("handleAliasQuery() - Error: Client is null.");
      return isAcked;
    }

    isAcked = ((QueryAliases) query).addAlias(indexMonth, aliasName);
    return isAcked;
  }

  public Set<String> updateIndices() {
    Set<String> indices = new HashSet<>();
    if (client != null) {
      indices.addAll(handleListingQuery(new QueryIndices(client)));
      log.trace("updateIndices() - Updating list of indices.");
    }
    return indices;
  }

  public Set<String> updateTypes() {
    Set<String> types = new HashSet<>();
    if (client != null) {
      types.addAll(handleListingQuery(new QueryTypes(client)));
      log.trace("updateTypes() - Updating list of types.");
    }
    return types;
  }

  public Set<String> updateAliases() {
    Set<String> aliases = new HashSet<>();
    if (client != null) {
      aliases.addAll(handleListingQuery(new QueryAliases(client)));
      log.trace("updateAliases() - Updating list of aliases.");
    }
    return aliases;
  }

  public void refreshClusterStats() {
    log.debug("refreshClusterStats()");
    client.admin().indices().prepareRefresh().execute().actionGet();
    client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
  }

  public Settings getIndexSettings(String name) {
    IndexSettings settings = IndexSettings.valueOf(name);
    return Settings.settingsBuilder().put("number_of_shards", settings.getShards())
        .put("number_of_replicas", settings.getReplica()).build();
  }

  /**
   * Launch a local (to the JVM) ElasticSearch cluster that will answer to the Queries.
   * @return Node with which we can communicate.
   */
  private Node launchLocalCluster() {
    String home = ".";
    setLocal(true);
    log.info("launchLocalCLuster() - Launch a new local cluster: home=" + home + ", clusterName=" + cluster + ".");

    return nodeBuilder().settings(Settings.settingsBuilder()
        .put("path.home", home)
        .put("cluster.name", cluster)
        .put("node.local", isLocal)
        .put("node.name", "ClusterNode")
        .put("node.data", true)
        .put("node.master", true)
        .put("http.enabled", false)
        .build())
        .node();
  }

  /**
   * Close the bulk after it sent enough: reached bulkActions, bulkSize or
   * flushInterval. And create a new one for further requests.
   */
  public void closeBulk() {
    try {
      bulkProcessor.awaitClose(100, TimeUnit.MILLISECONDS);
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
}