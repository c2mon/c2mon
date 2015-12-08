package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.queries.*;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Allows to connect to the cluster via a transport client.
 * Handles all the queries with a bulkProcessor to the ElasticSearch cluster.
 * This is very light for the cluster to be connected this way.
 * @author Alban Marguet.
 */
@Slf4j
@Service
@Data
public class TransportConnector implements Connector {
    private final int PORT_LOCAL = 9300;
    private final String HOST_LOCAL = "127.0.0.1";
    private final String CLUSTER_LOCAL = "elasticsearch";
    private final String NODE_LOCAL = "TransportNode-javaClient";
    private final String DEFAULT_BULK_PROCESSOR_NAME = "BulkProcessor-javaClient";

    private Client client;
    private DataUtils dataUtils;

    private int port;
    private String host;
    private String cluster;
    private String node;
    private Settings settings;

    private BulkProcessor bulkProcessor;
    private String bulkProcessorName;

    @Autowired
    public TransportConnector(DataUtils dataUtils) {
        this.dataUtils = dataUtils;

        this.port = PORT_LOCAL;
        this.host = HOST_LOCAL;
        this.cluster = CLUSTER_LOCAL;
        this.node = NODE_LOCAL;
        this.settings = Settings.settingsBuilder().put("cluster.name", cluster).put("node.name", node).build();

        this.client = createClient();
        this.bulkProcessorName = DEFAULT_BULK_PROCESSOR_NAME;
    }

    @PostConstruct
    public void init() {
        dataUtils.setPort(port);
        dataUtils.setHost(host);
        dataUtils.setClusterName(cluster);
        dataUtils.setNodeName(node);
        dataUtils.setTransportSettings(cluster, node);
        dataUtils.setClient(client);
        dataUtils.setIndices(initializeIndexes());
        dataUtils.setAliases(initializeAliases());
        dataUtils.setTypes(initializeTypes());
        dataUtils.setBulkSettings();

        this.bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info("Execute bulk " + executionId);
                    }

                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        if (response.hasFailures()) {
                            log.info(response.buildFailureMessage());
                        } else {
                            log.info("success");
                        }
                    }

                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

                    }
                })
                .setName(bulkProcessorName)
                .setBulkActions(dataUtils.getBulkSettings().get("bulkActions"))
                .setBulkSize(new ByteSizeValue(dataUtils.getBulkSettings().get("bulkSize"), ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(dataUtils.getBulkSettings().get("flushInterval")))
                .setConcurrentRequests(dataUtils.getBulkSettings().get("concurrent"))
                .build();
        log.info("init() - Everything is initialized.");
        log.info("init() - Connected to cluster " + cluster + " with node " + node + ".");

        log.info("Indices in the cluster:");
        for (String index : dataUtils.getIndices()) {
            log.info(index);
        }
        log.info("Types in the cluster:");
        for (String type : dataUtils.getTypes()) {
            log.info(type);
        }
        log.info("Aliases in the cluster:");
        for (String alias : dataUtils.getAliases()) {
            log.info(alias);
        }
    }

    /**
     * Retrieve the indexes present in the ElasticSearch cluster.
     * @return Set of String containing the names of the indexes.
     */
    public Set<String> initializeIndexes() {
        Set<String> set = new HashSet<>();

        List<String> indices = getIndices();
        set.addAll(indices);

        return set;
    }

    /**
     * Retrieve the aliases present in the ElasticSearch cluster.
     * @return Set of String containing the names of the aliases.
     */
    public Set<String> initializeAliases() {
        Set<String> set = new HashSet<>();

        List<String> elements = getAliases();
        set.addAll(elements);

        return set;
    }

    /**
     * Retrieve the types present in the ElasticSearch cluster.
     * @return Set of String containing the names of the types.
     */
    public Set<String> initializeTypes() {
        Set<String> set = new HashSet<>();

        List<String> elements = getTypes();
        set.addAll(elements);

        return set;
    }

    /**
     * Called by each request to handle it: need a transportClient to communicate with the ElasticSearch cluster.
     * @return Client to communicate with the ElasticSearch cluster.
     */
    public Client createClient() {
        try {
            Client builder = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
            log.info("Created client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
            return builder;
        } catch(UnknownHostException e) {
            log.error("createTransportClient() - Error whilst connecting to the ElasticSearch cluster (host=" + host + ", port=" + port + ").", e);
        }
        return null;
    }

    /**
     * Handles a query for the ElasticSearch cluster.
     * @param query type of query.
     * @return List of retrieved responses according to the query.
     */
    @Override
    public List<String> handleQuery(Query query) {
        List<String> queryResponse = new ArrayList<>();

        if (client == null) {
            log.error("handleQuery() - Error: the client value is " + client + ".");
            return null;
        }

        if (query instanceof QueryIndices) {
            log.info("Handling queryIndices.");
            queryResponse.addAll(((QueryIndices) query).getListOfAnswer());
        } else if (query instanceof QueryAliases) {
            log.info("Handling queryAliases.");
            queryResponse.addAll(((QueryAliases) query).getListOfAnswer());
        } else if (query instanceof QueryTypes) {
            log.info("Handling queryTypes.");
            queryResponse.addAll(((QueryTypes) query).getListOfAnswer());
        } else {
            log.error("handleQuery() - Unhandled query type.");
            return null;
        }

        return queryResponse;
    }

    @Override
    public boolean handleIndexQuery(Query query, String indexName, Settings.Builder settings, String type, String mapping) {
        boolean queryResponse = false;

        if (client == null) {
            log.error("handleIndexQuery() - Error: the client value is " + client + ".");
            return queryResponse;
        }

        if (query instanceof QueryIndexBuilder) {
            queryResponse = ((QueryIndexBuilder) query).indexNew(indexName, settings, type, mapping);
            dataUtils.addType(type);
            dataUtils.addIndex(indexName);
        } else {
            log.error("handleIndexQuery() - Unhandled query type.");
        }
        return queryResponse;
    }

    @Override
    public void handleAliasQuery(Query query, String indexMonth, String aliasName) {
        if (client == null) {
            log.error("handleAliasQuery() - Error: the client value is " + client + ".");
            return;
        }

        if (query instanceof QueryAliases) {
            ((QueryAliases) query).addAlias(indexMonth, aliasName);
            dataUtils.addAlias(aliasName);
        } else {
            log.warn("handleAliasQuery() - Unhandled query type.");
        }
    }

    /**
     * Add 1 TagES to index to the ElasticSearch cluster thanks to the BulkProcessor.
     * @param tag to index.
     */
    public void indexTag(TagES tag) {
        try {
            String tagJson = tag.build();
            String indexMonth = dataUtils.getIndex(tag);
            String type = dataUtils.getType(tag);
            String aliasName = dataUtils.generateAliasName(tag.getTagId());
            log.info("Index a new tag.");
            log.info("Index = " + indexMonth);
            log.info("Type = " + type);
            log.info("Alias = " + aliasName);

            bulkAdd(indexMonth, type, tagJson, tag);
            dataUtils.addIndex(indexMonth);
            dataUtils.addType(type);

            bulkAddAlias(indexMonth, tag);
            dataUtils.addAlias(aliasName);
        } catch(IOException e) {
            log.warn("indexTag() - IOError whilst indexing a new tag to the cluster (tag = " + tag + ").", e);
        }
    }

    /**
     * Index several tags in the ElasticSearch cluster according to the BulkProcessor parameters.
     * @param tags to index.
     */
    @Override
    public void indexTags(List<TagES> tags) {
        int bulkActions = dataUtils.getBulkSettings().get("bulkActions");
        for (int i = 0; i < bulkActions && i < tags.size(); i++) {
            indexTag(tags.get(i));

            if (i > 0 && i % bulkActions == 0) {
                log.info("Close bulk: " + i + ".");
                closeBulk();
            }
        }
    }

    public void bulkAdd(String index, String type, String json, TagES tag) throws IOException {
        for (String i: dataUtils.getIndices()) {
            log.info(i);
        }

        if (!dataUtils.getIndices().contains(index)) {
            boolean isIndexed = instantiateIndex(tag, index, type);
            if (isIndexed) {
                log.info("Indexed a new tag of type " + type + ".");
                dataUtils.getIndices().add(type);
            }
        }

        bulkProcessor.add(new IndexRequest(index, type).source(json).routing(String.valueOf(tag.getTagId())));
    }

    /**
     * If index does not exist already when adding data as bulk, we address a new indexQuery to the ElasticSearch cluster.
     * @param tag TagES to add to the cluster.
     * @param index index to which add the TagES tag.
     * @param type the type of the TagES tag according to its dataType.
     * @return the boolean status of the Query.
     */
    public boolean instantiateIndex(TagES tag, String index, String type) {
        String mapping = tag.getMapping();
        return handleIndexQuery(new QueryIndexBuilder(client, new String[]{index}, true, new String[]{type}, null, -1, -1, -1, -1), index, dataUtils.getMonthIndexSettings(), type, mapping);
    }

    public boolean bulkAddAlias(String indexMonth, TagES tag) {
        if (!dataUtils.getIndices().contains(indexMonth)) {
            throw new IllegalArgumentException("Error: index must exist to create an alias");
        }

        long id = tag.getTagId();
        String aliasName = dataUtils.generateAliasName(id);

        if (!dataUtils.getAliases().contains(aliasName)) {
            log.info("Add alias: " + aliasName + " for index " + indexMonth);

            handleAliasQuery(new QueryAliases(client, new String[]{indexMonth}, false, null, null, -1, -1, -1, -1), indexMonth, aliasName);

            dataUtils.getAliases().add(aliasName);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Retrieve all the indices in the ElasticSearch cluster.
     * @return List of the names of the indices.
     */
    @Override
    public List<String> getIndices() {
        return handleQuery(new QueryIndices(client, null, false, null, null, -1, -1, -1, -1));
    }

    /**
     * Retrieve all the aliases in the ElasticSearch cluster.
     * @return list of the names of the aliases.
     */
    @Override
    public List<String> getAliases() {
        return handleQuery(new QueryAliases(client, null, false, null, null, -1, -1, -1, -1));
    }

    /**
     * Retrive all the types in the ElasticSearch cluster.
     * @return List of the names of the types.
     */
    @Override
    public List<String> getTypes() {
        return handleQuery(new QueryTypes(client, null, false, null, null, -1, -1, -1, -1));
    }


    /**
     * close the bulk after it sent enough: reached bulkActions, bulkSize or flushInterval.
     */
    public void closeBulk() {
        try {
            bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            log.warn("closeBulk() - Error whilst awaitClose() the bulkProcessor.", e);
        }
    }

    /**
     * Method called to close the newly opened client.
     * @param client transportClient for the cluster.
     */
    @Override
    public void close(Client client) {
        if (client != null) {
            client.close();
        }
    }
}