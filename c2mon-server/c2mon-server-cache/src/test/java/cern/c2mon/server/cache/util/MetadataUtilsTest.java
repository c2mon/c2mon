package cern.c2mon.server.cache.util;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.util.MetadataUtils;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class MetadataUtilsTest {

  @Test
  public void testMissingMetadataProperty(){
    Metadata currentMetadata = new Metadata();
    currentMetadata.addMetadata("key", "value");
    Properties properties = new Properties();

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(currentMetadata, result);
  }

  @Test
  public void testEmptyClientMetadata(){
    Metadata currentMetadata = new Metadata();
    currentMetadata.addMetadata("key", "value");
    Properties properties = new Properties();
    properties.put("metadata","");

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(currentMetadata, result);
  }

  @Test
  public void testInitialConfigurationMetadata(){
    Metadata currentMetadata = new Metadata();
    currentMetadata.addMetadata("key1", "value1");
    cern.c2mon.shared.client.metadata.Metadata clientMetadata = new cern.c2mon.shared.client.metadata.Metadata();
    clientMetadata.setUpdate(false);
    clientMetadata.addMetadata("key2", "value2");
    Properties properties = new Properties();
    properties.put("metadata",  cern.c2mon.shared.client.metadata.Metadata.toJSON(clientMetadata));

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(clientMetadata.getMetadata(), result.getMetadata());
  }

  @Test
  public void testUpdateConfigurationMetadata(){
    Metadata currentMetadata = new Metadata();
    String currentKey = "currentKey";
    currentMetadata.addMetadata(currentKey, "value1");
    cern.c2mon.shared.client.metadata.Metadata clientMetadata = new cern.c2mon.shared.client.metadata.Metadata();
    String newClientKey = "newClientKey";
    clientMetadata.addMetadata(newClientKey, "value2");
    clientMetadata.setUpdate(true);
    Properties properties = new Properties();
    properties.put("metadata",  cern.c2mon.shared.client.metadata.Metadata.toJSON(clientMetadata));

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(2, result.getMetadata().size());
    assertTrue(result.getMetadata().containsKey(currentKey));
    assertEquals(currentMetadata.getMetadata().get(currentKey), result.getMetadata().get(currentKey));
    assertTrue(result.getMetadata().containsKey(newClientKey));
    assertEquals(clientMetadata.getMetadata().get(newClientKey), result.getMetadata().get(newClientKey));
  }

  @Test
  public void testUpdateConfigurationMetadataWithRemoveList(){
    Metadata currentMetadata = new Metadata();
    String currentKey1 = "currentKey1";
    currentMetadata.addMetadata(currentKey1, "value1");
    String currentKey2 = "currentKey2";
    currentMetadata.addMetadata(currentKey2, "value2");
    cern.c2mon.shared.client.metadata.Metadata clientMetadata = new cern.c2mon.shared.client.metadata.Metadata();
    String newClientKey = "newClientKey";
    clientMetadata.addMetadata(newClientKey, "value3");
    clientMetadata.setUpdate(true);
    clientMetadata.addToRemoveList(currentKey1);
    Properties properties = new Properties();
    properties.put("metadata",  cern.c2mon.shared.client.metadata.Metadata.toJSON(clientMetadata));

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(2, result.getMetadata().size());
    assertTrue(result.getMetadata().containsKey(currentKey2));
    assertEquals(currentMetadata.getMetadata().get(currentKey2), result.getMetadata().get(currentKey2));
    assertTrue(result.getMetadata().containsKey(newClientKey));
    assertEquals(clientMetadata.getMetadata().get(newClientKey), result.getMetadata().get(newClientKey));
    assertFalse(result.getMetadata().containsKey(currentKey1));
  }

  @Test
  public void testUpdateWithNullMetadataProperty(){
    Metadata currentMetadata = new Metadata();
    String currentKey = "currentKey";
    currentMetadata.addMetadata(currentKey, null);
    cern.c2mon.shared.client.metadata.Metadata clientMetadata = new cern.c2mon.shared.client.metadata.Metadata();
    clientMetadata.setUpdate(true);
    Properties properties = new Properties();
    properties.put("metadata",  cern.c2mon.shared.client.metadata.Metadata.toJSON(clientMetadata));

    Metadata result = MetadataUtils.parseMetadataConfiguration(properties, currentMetadata);

    assertEquals(1, result.getMetadata().size());
    assertTrue(result.getMetadata().containsKey(currentKey));
    assertEquals(currentMetadata.getMetadata().get(currentKey), result.getMetadata().get(currentKey));
  }

}