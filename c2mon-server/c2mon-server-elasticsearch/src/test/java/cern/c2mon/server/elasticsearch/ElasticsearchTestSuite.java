package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentConverterTests;
import cern.c2mon.server.elasticsearch.alarm.AlarmDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClientTests;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocumentTests;
import cern.c2mon.server.elasticsearch.tag.TagDocumentConverterTests;
import cern.c2mon.server.elasticsearch.tag.TagDocumentIndexerTests;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverterTests;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexerTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.springframework.util.FileSystemUtils;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ElasticsearchModuleIntegrationTest.class,
    IndicesTests.class,
    AlarmDocumentConverterTests.class,
    AlarmDocumentIndexerTests.class,
    ElasticsearchClientTests.class,
    SupervisionEventDocumentIndexerTests.class,
    SupervisionEventDocumentTests.class,
    TagDocumentConverterTests.class,
    TagDocumentIndexerTests.class,
    TagConfigDocumentConverterTests.class,
    TagConfigDocumentIndexerTests.class
})
public class ElasticsearchTestSuite {

  @BeforeClass
  @AfterClass
  public static void cleanup() {
    ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();
    FileSystemUtils.deleteRecursively(new java.io.File(elasticsearchProperties.getEmbeddedStoragePath()));
  }
}
