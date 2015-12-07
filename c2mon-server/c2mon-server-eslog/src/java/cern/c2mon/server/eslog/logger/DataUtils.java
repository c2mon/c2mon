package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.indexer.BulkSettings;
import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Provides the knowledge about the ElasticSearch cluster: index, types...
 * @author Alban Marguet.
 */
@Slf4j
@Service
public class DataUtils {
    private final String INDEX_PREFIX = "c2mon_";
    private final String TAG_PREFIX = "tag_";
    private final String FIRST_INDEX = INDEX_PREFIX + "1970-01";

    private String clusterName;
    private String host;
    private int port;
    private String nodeName;
    private Settings transportSettings;
    private Client client;
    private String lastIndex;
    private Set<String> indices;
    private Set<String> aliases;
    private Set<String> types;
    private HashMap<String, Integer> bulkSettings;
    private Connector connector;

    @Autowired
    public DataUtils(Client client, Connector connector, String clusterName, String host, int port, String nodeName) {
        this.host = host;
        this.port = port;
        this.client = client;
        this.nodeName = nodeName;
        this.clusterName = clusterName;
        setTransportSettings(clusterName, nodeName);

        this.indices = initializeIndexes();
        this.aliases = initializeAliases();
        this.types = initializeTypes();
        this.lastIndex = FIRST_INDEX;
        this.bulkSettings = new HashMap<>();
        bulkSettings.put("bulkActions", BulkSettings.BULK_ACTIONS.getSetting());
        bulkSettings.put("bulkSize", BulkSettings.BULK_SIZE.getSetting());
        bulkSettings.put("flushInterval", BulkSettings.FLUSH_INTERVAL.getSetting());
        bulkSettings.put("concurrent", BulkSettings.CONCURRENT.getSetting());

        this.connector = connector;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setTransportSettings(String clusterName, String nodeName) {
        this.transportSettings = Settings.settingsBuilder().put("cluster.name", clusterName).put("node.name", nodeName).build();
    }

    public Settings getTransportSettings() {
        return transportSettings;
    }
    /**
     * Query the ElasticSearch cluster to know the most recent index in the cluster.
     * @return name of the index.
     */
    public String updateLastIndex() {
        String lastIndex = FIRST_INDEX;

        for (String index: indices) {
            if (index.matches("^" + INDEX_PREFIX + "(.*)") && index.substring(INDEX_PREFIX.length(), index.length())
                    .compareTo(lastIndex.substring(INDEX_PREFIX.length(), lastIndex.length())) > 0) {
                lastIndex = index;
            }
        }
        log.info("Updated last index: " + lastIndex);
        return lastIndex;
    }

    /**
     * Get the last inserted index in the ElasticSearch cluster. (the most recent)
     * @return name of the index.
     */
    public String getLastIndex() {
        this.lastIndex = updateLastIndex();
        return lastIndex;
    }

    /**
     * @return set of names of the aliases present in the cluster.
     */
    public Set<String> getAliases() {
        return aliases;
    }

    /**
     * @return set of names of the indexes present in the cluster.
     */
    public Set<String> getIndices() {
        return indices;
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
     * Retrieve the indexes present in the ElasticSearch cluster.
     * @return Set of String containing the names of the indexes.
     */
    public Set<String> initializeIndexes() {
        Set<String> set = new HashSet<>();

        String[] indices = client.admin().indices().prepareGetIndex().get().indices();
        Collections.addAll(set, indices);

        return set;
    }

    /**
     * Retrieve the aliases present in the ElasticSearch cluster.
     * @return Set of String containing the names of the aliases.
     */
    public Set<String> initializeAliases() {
        Set<String> set = new HashSet<>();

        Set<String> elements = client.admin().cluster().prepareState().execute().actionGet().
                getState().getMetaData().getAliasAndIndexLookup().keySet();
        set.addAll(elements);

        return set;
    }

    /**
     * Retrieve the types present in the ElasticSearch cluster.
     * @return Set of String containing the names of the types.
     */
    public Set<String> initializeTypes() {
        Set<String> set = new HashSet<>();

        String[] elements = (String[]) client.admin().cluster().prepareState().execute().actionGet().
                getState().getMetaData().index(getLastIndex()).getMappings().values().toArray();
        Collections.addAll(set, elements);

        return set;
    }

    /**
     * Settings for the index/Month: 10 shards and 0 replica.
     * @return Settings.Builder to attach to an IndexRequest.
     */
    public Settings.Builder getMonthIndexSettings() {
        return Settings.settingsBuilder().put("number_of_shards", IndexMonthSettings.SHARDS.getSetting())
                .put("number_of_replicas", IndexMonthSettings.REPLICA.getSetting());
    }

    /**
     * Type in ElasticSearch.
     * @param tag TagES
     * @return String of the form "tag_type"
     */
    public String getType(TagES tag) {
        return TAG_PREFIX + tag.getDataType();
    }

    /**
     *
     * @return Types present in the ElasticSearch cluster.
     */
    public Set<String> getTypes() {
        return types;
    }

    /**
     * Index where a TagES is stored in the ElasticSearch cluster.
     * @param tag TagES
     * @return name of the index of tag.
     */
    public String getIndex(TagES tag) {
        return millisecondsToYearMonth(tag.getTagServerTime());
    }

    /**
     * BulkSettings to send data in batches.
     * @return
     */
    public HashMap<String, Integer> getBulkSettings() {
        return bulkSettings;
    }

    /**
     * Utility method used by getIndex().
     * @param millis timestamp in ElasticSearch (milliseconds since Epoch)
     * @return String containing the corresponding "yyyy-MM".
     */
    private String millisecondsToYearMonth(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

        Date date = new Date(millis);
        String timestamp = sdf.format(date);
        return timestamp.substring(0,7);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}