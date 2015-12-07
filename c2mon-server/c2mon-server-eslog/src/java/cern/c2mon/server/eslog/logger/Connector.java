package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.types.TagES;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import java.util.List;

/**
 * Requirements to connect/query the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public interface Connector {
    void close(Client client);
    void indexTags(List<TagES> tags);
    List<String> getIndices();
    List<String> handleQuery(Query query, String[] indexes, boolean isTypeDefined, String[] types, long[] tagIds, int from, int size, int min, int max);
    boolean handleIndexQuery(Query query, String indexName, Settings.Builder settings, String type, String mapping);
    void handleAliasQuery(Query query, String indexMonth, String aliasName);
    DataUtils getUtils();
    Client getClient();
}
