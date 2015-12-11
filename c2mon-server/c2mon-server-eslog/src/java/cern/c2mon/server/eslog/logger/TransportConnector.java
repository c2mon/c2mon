package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.config.ElasticSearchConfig;
import cern.c2mon.server.eslog.structure.mappings.Mapping;
import cern.c2mon.server.eslog.structure.queries.*;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Allows to connect to the cluster via a transport client.
 * Handles all the queries with a bulkProcessor to the ElasticSearch cluster.
 * This is very light for the cluster to be connected this way.
 * @author Alban Marguet.
 */
@Service
@Slf4j
@Data
public class TransportConnector implements Connector {
    private final String DEFAULT_BULK_PROCESSOR_NAME = "BulkProcessor-javaClient";
    private HashMap<String, Integer> bulkSettings;

    private final int LOCAL_PORT = 1;

    private final String INDEX_PREFIX = "c2mon_";
    private final String TAG_PREFIX = "tag_";
    private final String FIRST_INDEX = INDEX_PREFIX + "1970-01";

    private Client client;
    private Set<String> indices;
    private Set<String> types;
    private Set<String> aliases;

    @Autowired
    private ElasticSearchConfig elasticsearchConfig;

    @Value("${es.port:9300}")
    private int port;

    @Value("${es.host:localhost}")
    private String host ;

    @Value("${es.cluster:elasticsearch}")
    private String cluster;

    @Value("${es.node:TransportNode-javaClient}")
    private String node;

    private boolean isLocal;
    private Settings settings;

    private BulkProcessor bulkProcessor;
    private String bulkProcessorName;

    public TransportConnector() {
        this.bulkProcessorName = DEFAULT_BULK_PROCESSOR_NAME;
        this.bulkSettings = new HashMap<>();
        this.isLocal = false;
        this.indices = new HashSet<>();
        this.types = new HashSet<>();
        this.aliases = new HashSet<>();
    }


    /*****************************************************************************

                                 INITIALIZATION

     ****************************************************************************/


    @PostConstruct
    public void init() {
        this.client = createClient();

        if (initTestPass()) {
            log.info("init() - initial test passed: Transport client is connected to the cluster.");
            updateLists();
            initBulkSettings();

            log.info("init() - Everything is initialized.");
            log.info("init() - Connected to cluster " + cluster + " with node " + node + ".");

            log.info("Indices in the cluster:");
            for (String index : indices) {
                log.info(index);
            }
            log.info("Types in the cluster:");
            for (String type : types) {
                log.info(type);
            }
            log.info("Aliases in the cluster:");
            for (String alias : aliases) {
                log.info(alias);
            }
        } else {
            log.error("Cluster is not initialized.");
        }
    }

    public void initBulkSettings() {
        bulkSettings.put("bulkActions", BulkSettings.BULK_ACTIONS.getSetting());
        bulkSettings.put("bulkSize", BulkSettings.BULK_SIZE.getSetting());
        bulkSettings.put("flushInterval", BulkSettings.FLUSH_INTERVAL.getSetting());
        bulkSettings.put("concurrent", BulkSettings.CONCURRENT.getSetting());

        this.bulkProcessor = BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info("Going to execute new bulk composed of {} actions", request.numberOfActions());
                    }

                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        log.info("Executed bulk composed of {} actions", request.numberOfActions());
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
    }

    /**
     * Called by each request to handle it: need a transportClient to communicate with the ElasticSearch cluster.
     * @return Client to communicate with the ElasticSearch cluster.
     */
    public Client createClient() {
        if (isLocal) {
            this.settings = Settings.settingsBuilder().put("node.local", true).put("node.name", node).put("cluster.name", cluster).build();
            setPort(LOCAL_PORT);
            Client builder = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new LocalTransportAddress(String.valueOf(port)));
            log.info("Created local client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
            return builder;
        } else {
            this.settings = Settings.settingsBuilder().put("cluster.name", cluster).put("node.name", node).build();
            setPort(9300);
            try {
                Client builder = TransportClient.builder().settings(settings).build()
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
                log.info("Created client on host " + host + " and port " + port + " with name " + node + ", in cluster " + cluster + ".");
                return builder;
            } catch (UnknownHostException e) {
                e.printStackTrace();
                log.error("createTransportClient() - Error whilst connecting to the ElasticSearch cluster (host=" + host + ", port=" + port + ").", e);
            }
        }
        return null;
    }

    public boolean initTestPass() {
        if (client != null) {
            return new QueryIndices(client, null, false, null, null, -1, -1, -1, -1).initTest();
        } else {
            log.warn("initTestPass() - client for the ElasticSearch cluster seems to have null value.");
            return false;
        }
    }


    /*****************************************************************************

                                 INDEXING

     ****************************************************************************/


    /**
     * Add 1 TagES to index to the ElasticSearch cluster thanks to the BulkProcessor.
     * @param tag to index.
     */
    public void indexTag(TagES tag) {
        try {
            String tagJson = tag.build();
            String indexMonth = generateIndex(tag.getTagServerTime());
            String type = generateType(tag.getDataType());
            log.info("Index a new tag.");
            log.info("Index = " + indexMonth);
            log.info("Type = " + type);

            bulkAdd(indexMonth, type, tagJson, tag);
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
        Map<String, TagES> aliases = new HashMap<>();
        int bulkActions = bulkSettings.get("bulkActions");
        for (int i = 0; i < tags.size(); i++) {
            TagES tag = tags.get(i);
            indexTag(tag);
            aliases.put(generateAliasName(tag.getTagId()), tag);

        }
        log.info("Close bulk.");
        closeBulk();

        for (String alias : aliases.keySet()) {
            //bulkAddAlias(generateIndex(aliases.get(alias).getTagServerTime()), aliases.get(alias));
        }
        client.admin().indices().prepareRefresh().execute().actionGet();
        client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
    }


    /*****************************************************************************

                             UTILITY FOR INDEXING

     ****************************************************************************/


    /**
     * Handles a query for the ElasticSearch cluster.
     * This method only handles the queries for listing admin values (indices, types and aliases)
     * @param query type of query.
     * @return List of retrieved responses according to the query.
     */
    @Override
    public List<String> handleListingQuery(Query query) {
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

    /**
     * Handles an indexing query for the ElasticSearch cluster.
     * This method allows to add a new index/type if needed directly to the cluster in addition to adding the data itself.
     * @param query the query to handle.
     * @param indexName of the index to add the data to.
     * @param settings of the index in the cluster.
     * @param type of the document to add to the cluster.
     * @param mapping contains the information for the indexing: routing, fields, fields types...
     * @return true if the client has been acked.
     */
    @Override
    public boolean handleIndexQuery(Query query, String indexName, Settings settings, String type, String mapping) {
        boolean isAcked = false;

        if (client == null) {
            log.error("handleIndexQuery() - Error: the client value is " + client + ".");
            return isAcked;
        }

        if (query instanceof QueryIndexBuilder && query.isParametersSet()) {
            isAcked = ((QueryIndexBuilder) query).indexNew(indexName, settings, type, mapping);
            if (isAcked) {
                addType(type);
                addIndex(indexName);
            }
        } else {
            log.warn("handleIndexQuery() - Unhandled query type.");
        }
        return isAcked;
    }

    /**
     * Handles an alias addition query for the ElasticSearch cluster.
     * @param query query type.
     * @param indexMonth name of the index to which to add the alias.
     * @param aliasName alias to give to the name; must follow the format "tag_tagId".
     * @return if the query has been acked.
     */
    @Override
    public boolean handleAliasQuery(Query query, String indexMonth, String aliasName) {
        boolean isAcked = false;
        if (client == null || !checkIndex(indexMonth) || !checkAlias(aliasName)) {
            log.error("handleAliasQuery() - Error: required values are not set (client=" + client + ", indexname= " + indexMonth + ", aliasname= " + aliasName + ".");
            return isAcked;
        }

        if (query instanceof QueryAliases) {
            isAcked = ((QueryAliases) query).addAlias(indexMonth, aliasName);
            if (isAcked) {
                addAlias(aliasName);
            }
        } else {
            log.warn("handleAliasQuery() - Unhandled query type.");
        }

        return isAcked;
    }

    /**
     * Add an indexing request to the bulkProcessor to be added by batches.
     * @param index for the data.
     * @param type of the document.
     * @param json request for indexing.
     * @param tag TagES to be indexed.
     * @throws IOException
     */
    public boolean bulkAdd(String index, String type, String json, TagES tag) throws IOException {
        for (String i: indices) {
            log.info(i);
        }

        if (tag == null || index == null || type == null || !checkIndex(index) || !checkType(type)) {
            log.warn("bulkAdd() - Error while indexing data. Bad index or type values: " + index + ", " + type + ".");
            return false;
        } else {
            if (!indices.contains(index)) {
                boolean isIndexed = instantiateIndex(tag, index, type);
                if (isIndexed) {
                    addType(type);
                    addIndex(index);
                    log.info("Indexed a new tag of type " + type + ".");
                }
            }
            bulkProcessor.add(new IndexRequest(index, type).source(json).routing(String.valueOf(tag.getTagId())));
            return true;
        }
    }

    /**
     * Add an alias query when the data is written by batches.
     * @param indexMonth to add the data into.
     * @param tag to be added to the cluster.
     * @return if the alias is in the aliases list.
     */
    public boolean bulkAddAlias(String indexMonth, TagES tag) {
        if (tag == null || !indices.contains(indexMonth) || !checkIndex(indexMonth)) {
            throw new IllegalArgumentException("bulkAddAlias() - IllegalArgument (tag = " + tag + ", index = " + indexMonth + ").");
        }

        long id = tag.getTagId();
        String aliasName = generateAliasName(id);

        if (!aliases.contains(aliasName)) {
            boolean isAcked = handleAliasQuery(new QueryAliases(client, Arrays.asList(indexMonth), false, null, null, -1, -1, -1, -1), indexMonth, aliasName);
            if (isAcked) {
                addAlias(aliasName);
                log.info("Add alias: " + aliasName + " for index " + indexMonth);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * If index does not exist already when adding data as bulk, we address a new indexQuery to the ElasticSearch cluster.
     * @param tag TagES to add to the cluster.
     * @param index index to which add the TagES tag.
     * @param type the type of the TagES tag according to its dataType.
     * @return the boolean status of the Query.
     */
    public boolean instantiateIndex(TagES tag, String index, String type) {
        if (indices.contains(index)) {
            return false;
        }

        String mapping = null;
        if (!types.contains(type)) {
            mapping = tag.getMapping();
        }

        updateLists();

        return handleIndexQuery(new QueryIndexBuilder(client, Arrays.asList(index), true, Arrays.asList(type), null, -1, -1, -1, -1), index, getMonthIndexSettings(), type, mapping);
    }

    public void updateLists() {
        indices.addAll(handleListingQuery(new QueryIndices(client)));
        types.addAll(handleListingQuery(new QueryTypes(client)));
        aliases.addAll(handleListingQuery(new QueryAliases(client)));
    }

    /**
     * Utility method.
     * Aliases have the following format: "tag_tagId".
     * @param tagId
     * @return name of the alias for a given tagId.
     */
    public String generateAliasName(long tagId) {
        return TAG_PREFIX + tagId;
    }

    /**
     * Type in ElasticSearch.
     * @param dataType TagES's dataType.
     * @return String of the form "tag_type"
     */
    public String generateType(String dataType) {
        return TAG_PREFIX + dataType.toLowerCase();
    }

    /**
     * Index where a TagES is stored in the ElasticSearch cluster.
     * @param tagServerTime TagES's tagServerTime (milliseconds since Epoch).
     * @return name of the index of tag.
     */
    public String generateIndex(long tagServerTime) {
        return INDEX_PREFIX + millisecondsToYearMonth(tagServerTime);
    }

    /**
     * Utility method used by getIndex().
     * @param millis timestamp in ElasticSearch (milliseconds since Epoch)
     * @return String containing the corresponding "yyyy-MM".
     */
    public String millisecondsToYearMonth(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        Date date = new Date(millis);
        String timestamp = sdf.format(date);
        return timestamp.substring(0,7);
    }

    /**
     * Close the bulk after it sent enough: reached bulkActions, bulkSize or flushInterval.
     * And create a new one for further requests.
     */
    public void closeBulk() {
        try {
            bulkProcessor.awaitClose(1, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
            log.warn("closeBulk() - Error whilst awaitClose() the bulkProcessor.", e);
        }
        initBulkSettings();
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

    public void addIndex(String indexName) {
        if (checkIndex(indexName)) {
            indices.add(indexName);
        } else {
            throw new IllegalArgumentException("Indices must follow the format \"c2mon_YYYY_MM\".");
        }
    }

    public void addAlias(String aliasName) {
        if (checkAlias(aliasName)) {
            aliases.add(aliasName);
        } else {
            throw new IllegalArgumentException("Aliases must follow the format \"tag_tagId\".");
        }
    }

    public void addType(String typeName) {
        if (checkType(typeName)) {
            types.add(typeName);
        } else {
            throw new IllegalArgumentException("Types must follow the format \"tag_dataType\".");
        }
    }

    public boolean checkIndex(String index) {
        return index.matches("^" + INDEX_PREFIX + "\\d\\d\\d\\d-\\d\\d$");
    }

    public boolean checkAlias(String alias) {
        return alias.matches("^" + TAG_PREFIX + "\\d+$");
    }

    public boolean checkType(String type) {
        String dataType = type.substring(TAG_PREFIX.length());
        return type.matches("^" + TAG_PREFIX + ".+$") &&
                (dataType.matches(Mapping.boolType) ||
                        dataType.matches(Mapping.doubleType) ||
                        dataType.matches(Mapping.intType) ||
                        dataType.matches(Mapping.longType) ||
                        dataType.matches(Mapping.stringType));
    }

    /**
     * Settings for the index/Month: 10 shards and 0 replica.
     * @return Settings.Builder to attach to an IndexRequest.
     */
    public Settings getMonthIndexSettings() {
        return Settings.settingsBuilder().put("number_of_shards", IndexMonthSettings.SHARDS.getSetting())
                .put("number_of_replicas", IndexMonthSettings.REPLICA.getSetting()).build();
    }
}