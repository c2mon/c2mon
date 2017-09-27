package cern.c2mon.server.elasticsearch.client;

public class ElasticsearchClientNotAvailable extends RuntimeException {
  public ElasticsearchClientNotAvailable() {
    super("Elasticsearch client not available");
  }
}
