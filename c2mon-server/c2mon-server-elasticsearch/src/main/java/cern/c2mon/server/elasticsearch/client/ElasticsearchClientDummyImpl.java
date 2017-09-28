package cern.c2mon.server.elasticsearch.client;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.NodeValidationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class ElasticsearchClientDummyImpl implements ElasticsearchClient {

  @Autowired
  private ElasticsearchProperties properties;

  @Override
  public void waitForYellowStatus() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public ClusterHealthResponse getClusterHealth() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void startEmbeddedNode() throws NodeValidationException {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void close() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public void closeEmbeddedNode() throws IOException {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public ElasticsearchProperties getProperties() {
    return this.properties;
  }

  @Override
  public Client getClient() {
    throw new ElasticsearchClientNotAvailable();
  }

  @Override
  public boolean isClusterYellow() {
    throw new ElasticsearchClientNotAvailable();
  }
}
