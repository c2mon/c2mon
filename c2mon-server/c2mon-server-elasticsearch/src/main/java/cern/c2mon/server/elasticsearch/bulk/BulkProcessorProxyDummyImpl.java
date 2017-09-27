package cern.c2mon.server.elasticsearch.bulk;

import org.elasticsearch.action.index.IndexRequest;

public class BulkProcessorProxyDummyImpl implements BulkProcessorProxy {
  @Override
  public void add(IndexRequest request) {

  }

  @Override
  public void flush() {

  }
}
