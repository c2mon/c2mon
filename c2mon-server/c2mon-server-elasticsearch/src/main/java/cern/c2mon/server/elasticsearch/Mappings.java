package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class Mappings {

  @Autowired
  private TransportConnector connector;

  private static Mappings self;

  @PostConstruct
  public void init() {
    self = this;
  }

  public static void create(String indexName, Class<?> klass) {
    String mapping = MappingFactory.createTagMapping(klass.getName());
    create(indexName, Types.of(klass.getName()), mapping);
  }

  private static boolean create(String indexName, String type, String mapping) {
    PutMappingResponse response = null;

    try {
      response = self.connector.getClient().admin().indices().preparePutMapping(indexName).setType(type).setSource(mapping).get();
    } catch (Exception e) {
      log.error("Error creating mapping for indexName={}, type={}, mapping={}", indexName, type, mapping, e);
    }

    return response != null && response.isAcknowledged();
  }
}
