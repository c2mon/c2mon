package cern.c2mon.server.eslog.structure.queries;

import com.carrotsearch.hppc.ObjectContainer;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alban Marguet.
 */
@Slf4j
public abstract class Query {
  @Getter @Setter
  protected Client client;
  protected List<Long> tagIds;
  protected List<String> types;
  protected List<String> indices;

  public Query(Client client) {
    this.client = client;
    indices = new ArrayList<>();
    types = new ArrayList<>();
    tagIds = new ArrayList<Long>();
  }

  protected String[] getIndicesFromCluster() {
    return client.admin().indices().prepareGetIndex().get().indices();
  }

  protected CreateIndexRequestBuilder prepareCreateIndexRequestBuilder(String index) {
    return client.admin().indices().prepareCreate(index);
  }

  protected IndicesAliasesRequestBuilder prepareAliases() {
    return client.admin().indices().prepareAliases();
  }

  protected Iterator<ObjectCursor<IndexMetaData>> getIndicesWithMetadata() {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().indices().values().iterator();
  }

  protected ImmutableOpenMap<String, MappingMetaData> getIndexWithMetadata(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getMappings();
  }

  protected ObjectContainer<AliasMetaData> getAliases(String index) {
    return client.admin().cluster().prepareState().execute().actionGet().getState().getMetaData().index(index).getAliases().values();
  }
}