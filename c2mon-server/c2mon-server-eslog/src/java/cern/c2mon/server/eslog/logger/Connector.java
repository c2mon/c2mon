package cern.c2mon.server.eslog.logger;

import java.util.Collection;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import cern.c2mon.server.eslog.structure.queries.Query;
import cern.c2mon.server.eslog.structure.types.TagES;

/**
 * Requirements to connect/query and index data to the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public interface Connector {
  Client createClient();
  void close(Client client);
  void indexTags(Collection<TagES> tags);

  void updateLists();

  List<String> handleListingQuery(Query query);
  boolean handleIndexQuery(Query query, String indexName, Settings settings, String type, String mapping);
  boolean handleAliasQuery(Query query, String indexMonth, String aliasName);
  boolean bulkAdd(String index, String type, String json, TagES tag);
  boolean bulkAddAlias(String indexMonth, TagES tag);
}