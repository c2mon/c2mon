package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.types.TagES;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides the knowledge about the ElasticSearch cluster: index, types...
 * @author Alban Marguet.
 */
@Slf4j
@Data
@Service
public class DataUtils {
    private final String INDEX_PREFIX = "c2mon_";
    private final String TAG_PREFIX = "tag_";
    private final String FIRST_INDEX = INDEX_PREFIX + "1970-01";

    private String lastIndex;
    private Set<String> indices;
    private Set<String> aliases;
    private Set<String> types;
    private HashMap<String, Integer> bulkSettings;

    public DataUtils() {
        this.indices = new HashSet<>();
        this.aliases = new HashSet<>();
        this.types = new HashSet<>();
        this.lastIndex = FIRST_INDEX;
        this.bulkSettings = new HashMap<>();
    }

    public void setBulkSettings() {
        bulkSettings.put("bulkActions", BulkSettings.BULK_ACTIONS.getSetting());
        bulkSettings.put("bulkSize", BulkSettings.BULK_SIZE.getSetting());
        bulkSettings.put("flushInterval", BulkSettings.FLUSH_INTERVAL.getSetting());
        bulkSettings.put("concurrent", BulkSettings.CONCURRENT.getSetting());
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
        return type.matches("^" + TAG_PREFIX + ".+$");
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

    /**
     * Settings for the index/Month: 10 shards and 0 replica.
     * @return Settings.Builder to attach to an IndexRequest.
     */
    public Settings getMonthIndexSettings() {
        return Settings.settingsBuilder().put("number_of_shards", IndexMonthSettings.SHARDS.getSetting())
                .put("number_of_replicas", IndexMonthSettings.REPLICA.getSetting()).build();
    }

    /**
     * Get the last inserted index in the ElasticSearch cluster. (the most recent)
     * @return name of the index.
     */
    public String getLastIndex() {
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
     * Index where a TagES is stored in the ElasticSearch cluster.
     * @param tag TagES
     * @return name of the index of tag.
     */
    public String getIndex(TagES tag) {
        return millisecondsToYearMonth(tag.getTagServerTime());
    }

    /**
     * Type in ElasticSearch.
     * @param tag TagES
     * @return String of the form "tag_type"
     */
    public String getType(TagES tag) {
        return TAG_PREFIX + tag.getDataType();
    }
}