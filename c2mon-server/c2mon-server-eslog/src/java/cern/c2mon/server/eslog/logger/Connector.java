package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.types.TagES;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import java.util.List;
import java.util.Set;

/**
 * Requirements to connect/query the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public interface Connector {
    Client createClient();
    void close(Client client);
    void indexTags(List<TagES> tags);

    List<String> getIndices();
    List<String> getAliases();
    List<String> getTypes();

    Set<String> initializeIndexes();
    Set<String> initializeTypes();
    Set<String> initializeAliases();

    List<String> handleQuery(Query query);
    boolean handleIndexQuery(Query query, String indexName, Settings.Builder settings, String type, String mapping);
    void handleAliasQuery(Query query, String indexMonth, String aliasName);
}
