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
   * Write (a.k.a index) a btach of TagES to ElasticSearch.
   * @param tags: batch of TagES.
   */
  void indexTags(Collection<TagES> tags);

  /**
   * Retrieve the lists of indices, types and aliases from ElasticSearch and
   * update them in memory.
   */
  void updateLists();

  /**
   * Launch a query listing indices or types or aliases against the ElasticSearch cluster.
   * (debugging purpose to verify the indexing of data / creation of indices)
   * @param query to be run.
   * @return list of indices/types/aliases.
   */
  List<String> handleListingQuery(Query query);

  /**
   * Launch an indexing query against the ElasticSearch cluster: to write data.
   */
  boolean handleIndexQuery(Query query, String indexName, Settings settings, String type, String mapping);

  /**
   * Launch an alias query against the ElasticSearch cluster: to fake an index/Tag.
   * It attaches an alias referencing one Tag from an index.
   * @param query to be launched.
   * @param indexMonth index to which add the alias.
   *                   must be of format "c2mon_YYYY-MM".
   * @param aliasName reference to be used on the client side.
   *                  must of format "tag_tagname".
   * @return response of the query.
   */
  boolean handleAliasQuery(Query query, String indexMonth, String aliasName);

  /**
   * Allows to add data by batches to the ElasticSearch cluster thanks to a BulkProcessor.
   * @param index to which add the data.
   * @param type of the data in ElasticSearch.
   * @param json json format of a Tag (method build() in TagES).
   * @param tag the TagES to be added.
   * @return response of the BulkProcessor.
   */
  boolean bulkAdd(String index, String type, String json, TagES tag);

  /**
   * Adds an alias of a TagES to the right indices due to a batch indexing of data if the alias does not already exist.
   * Launched after a bulkAdd() for all the data to be indexed to the ElasticSearch cluster..
   * @param indexMonth to which to attach the alias.
   * @param tag TagES to be referenced by an alias.
   * @return response of the query.
   */
  boolean bulkAddAlias(String indexMonth, TagES tag);
}