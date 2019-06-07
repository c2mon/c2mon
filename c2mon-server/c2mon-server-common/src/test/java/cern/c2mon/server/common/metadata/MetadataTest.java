package cern.c2mon.server.common.metadata;

import org.junit.Assert;
import org.junit.Test;

public class MetadataTest {

  @Test
  public void testClone() throws CloneNotSupportedException {
    Metadata metadata = new Metadata();
    metadata.addMetadata("null", null);
    metadata.addMetadata(null, "null");

    Metadata clone = metadata.clone();
    Assert.assertEquals(2, clone.getMetadata().size());
  }
}
