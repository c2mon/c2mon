package cern.c2mon.server.elasticsearch;

import org.junit.Test;

import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.tag.TagDocument;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Justin Lewis Salmon
 */
public class IndicesTests extends BaseElasticsearchIntegrationTest {

  @Test
  public void monthlyIndex() {
    Indices.getProperties().setIndexType("M");

    TagDocument document = new TagDocument();
    document.put("timestamp", 1448928000000L);

    String index = Indices.indexFor(document);
    assertEquals("c2mon-tag_2015-12", index);
  }

  @Test
  public void weeklyIndex() {
    Indices.getProperties().setIndexType("W");

    TagDocument document = new TagDocument();
    document.put("timestamp", 1448928000000L);

    String index = Indices.indexFor(document);
    assertEquals("c2mon-tag_2015-W49", index);
  }

  @Test
  public void dailyIndex() {
    Indices.getProperties().setIndexType("D");

    TagDocument document = new TagDocument();
    document.put("timestamp", 1448928000000L);

    String index = Indices.indexFor(document);
    assertEquals("c2mon-tag_2015-12-01", index);
  }
}
