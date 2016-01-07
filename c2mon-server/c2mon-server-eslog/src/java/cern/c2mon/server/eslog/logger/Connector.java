package cern.c2mon.server.eslog.logger;

import cern.c2mon.server.eslog.structure.queries.Query;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import java.util.List;
import java.util.Set;

/**
 * Requirements to connect/query and index data to the ElasticSearch cluster.
 * @author Alban Marguet.
 */
public interface Connector {
  /**
   * @return the Client to communicate with an ElasticSearch cluster.
   */
  Client createClient();

  /**
   * Close th communication with an ElasticSearch cluster.
   * @param client of the ElasticSearch cluster.
   */
  void close(Client client);

  /**
   * Close the BulkProcessor and restart a new one for further data ingestion.
   */
  void closeBulk();

  /**
   * Wait for the good status to be able to fetch data after ingestion.
   */
  void refreshClusterStats();

  /**
   * @return the Client connected to the ElasticSearch cluster.
   */
  Client getClient();

  /**
   * Retrieve the lists of indices, types and aliases from ElasticSearch and
   * update them in memory.
   */
  Set<String> updateIndices();
  Set<String> updateTypes(String index);
  Set<String> updateAliases(String index);

  /**
   * @return true if C2MON is connected to an ElasticSearch cluster. (client can communicate with it)
   * It means that initTestPass() return true.
   */
  boolean isConnected();

  /**
   * Launch a query listing indices or types or aliases against the ElasticSearch cluster.
   * (debugging purpose to verify the indexing of data / creation of indices)
   * @param query to be run.
   * @return list of indices/types/aliases.
   */
  List<String> handleListingQuery(Query query, String index);

  /**
   * Launch an indexing query against the ElasticSearch cluster.
   * Used to insert a new index or to add a new mapping(type, mapping) to an existing index.
   */
  boolean handleIndexQuery(String indexName, Settings settings, String type, String mapping);

  /**
   * Launch an alias query against the ElasticSearch cluster: to fake an index/Tag.
   * It attaches an alias referencing one Tag from an index.
   * @param indexMonth index to which add the alias.
   *                   must be of format "c2mon_YYYY-MM".
   * @param aliasName reference to be used on the client side.
   *                  must of format "tag_tagname".
   * @return response of the query.
   */
  boolean handleAliasQuery(String indexMonth, String aliasName);

  /**
   * Allows to add data by batches to the ElasticSearch cluster thanks to a BulkProcessor.
   * @return response of the BulkProcessor.
   */
  boolean bulkAdd(IndexRequest indexNewTag);

  /**
   * Save data if cluster not available.
   * @param indexNewTag indexRequest awaiting
   */
  void launchFallBackMechanism(IndexRequest indexNewTag);

  /**
   * @return the Index settings: number of shards and number of replica. (lucene settings)
   */
  Settings getIndexSettings(String name);
}