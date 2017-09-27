package cern.c2mon.server.elasticsearch.bulk;

import org.elasticsearch.action.index.IndexRequest;

public interface BulkProcessorProxy {
  void add(IndexRequest request);

  void flush();
}
