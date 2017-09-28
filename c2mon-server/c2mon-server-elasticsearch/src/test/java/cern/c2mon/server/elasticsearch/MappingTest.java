package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientImpl;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.node.NodeValidationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MappingTest {
  protected ElasticsearchClient client;

  protected Indices indices;
  @Before
  public void waitForElasticSearch() throws InterruptedException, ExecutionException, NodeValidationException {
    ElasticsearchProperties properties = new ElasticsearchProperties();
    properties.setEmbedded(false);
    properties.setHost("128.141.159.203");
    properties.setPort(9301);
    this.client = new ElasticsearchClientImpl(properties);
     indices = new Indices(this.client, properties);
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        client.waitForYellowStatus();
        ElasticsearchProperties elasticsearchProperties = this.client.getProperties();
        client.getClient().admin().indices().delete(new DeleteIndexRequest(elasticsearchProperties.getTagConfigIndex()));
        Indices.create(elasticsearchProperties.getTagConfigIndex(), "tag_config", MappingFactory.createTagConfigMapping());
        try {
          //it takes some time for the index to be recreated, should do this properly
          //by waiting for yellow status but it doesn't work?
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      nodeReady.get(120, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      throw new RuntimeException("Timeout when waiting for embedded elasticsearch!");
    }
  }

  public void test() {
    indices.create("test", "type", "{\n" +
        "  \"_routing\": {\n" +
        "    \"required\": true\n" +
        "  },\n" +
        "  \"dynamic_templates\": [\n" +
        "    {\n" +
        "      \"nested_metadata_template\": {\n" +
        "        \"mapping\": {\n" +
        "          \"index\": \"not_analyzed\"\n" +
        "        },\n" +
        "        \"match_mapping_type\": \"string\",\n" +
        "        \"path_match\": \"metadata.*\"\n" +
        "      }\n" +
        "    }\n" +
        "  ],\n" +
        "  \"properties\": {\n" +
        "    \"id\": {\n" +
        "      \"type\": \"long\"\n" +
        "    },\n" +
        "    \"name\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"index\": \"not_analyzed\"\n" +
        "    },\n" +
        "    \"description\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"index\": \"analyzed\"\n" +
        "    },\n" +
        "    \"metadata\": {\n" +
        "      \"type\": \"object\",\n" +
        "      \"dynamic\": \"true\"\n" +
        "    },\n" +
        "    \"unit\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"index\": \"not_analyzed\"\n" +
        "    },\n" +
        "    \"mode\":{\n" +
        "      \"type\": \"string\",\n" +
        "      \"index\": \"not_analyzed\"\n" +
        "    },\n" +
        "    \"c2mon\": {\n" +
        "      \"dynamic\": \"true\",\n" +
        "      \"properties\": {\n" +
        "        \"dataType\": {\n" +
        "          \"type\": \"string\",\n" +
        "          \"index\": \"not_analyzed\"\n" +
        "        },\n" +
        "        \"process\": {\n" +
        "          \"type\": \"string\",\n" +
        "          \"index\": \"not_analyzed\"\n" +
        "        },\n" +
        "        \"equipment\": {\n" +
        "          \"type\": \"string\",\n" +
        "          \"index\": \"not_analyzed\"\n" +
        "        },\n" +
        "        \"subEquipment\": {\n" +
        "          \"type\": \"string\",\n" +
        "          \"index\": \"not_analyzed\"\n" +
        "        },\n" +
        "        \"logged\": {\n" +
        "          \"type\": \"boolean\"\n" +
        "        }\n" +
        "      }\n" +
        "    },\n" +
        "    \"timestamp\": {\n" +
        "      \"type\": \"date\",\n" +
        "      \"format\": \"epoch_millis\"\n" +
        "    }\n" +
        "  }\n" +
        "}");

  }

}
