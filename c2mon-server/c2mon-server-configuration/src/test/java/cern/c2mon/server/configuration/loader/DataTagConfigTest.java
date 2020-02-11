package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil;
import cern.c2mon.server.configuration.util.TestConfigurationProvider;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;

import static org.junit.Assert.*;

public class DataTagConfigTest extends ConfigurationCacheLoaderTest<DataTag> {

  @Inject
  private C2monCache<DataTag> dataTagCache;

  @Inject
  private DataTagService dataTagService;

  @Inject
  private DataTagMapper dataTagMapper;

  @Inject
  private C2monCache<Equipment> equipmentCache;

  @Test
  public void testCreateAndUpdateDataTag() throws ConfigurationException {

    ConfigurationReport report = configurationLoader.applyConfiguration(1);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());

    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(5000000L);

    // corresponds to data inserted using SQL file
    DataTagCacheObject expectedObject = new DataTagCacheObject(5000000L);
    expectedObject.setName("Config_test_datatag"); // non null
    expectedObject.setDescription("test description config datatag");
    expectedObject.setMode(DataTagConstants.MODE_TEST); // non null
    expectedObject.setDataType("Float"); // non null
    expectedObject.setLogged(false); // null allowed
    expectedObject.setUnit("config unit m/sec");
    expectedObject.setDipAddress("testConfigDIPaddress");
    expectedObject.setJapcAddress("testConfigJAPCaddress");
    // expectedObject.setValue(Boolean.TRUE);
    // expectedObject.setValueDescription("test config value description");
    expectedObject.setSimulated(false); // null allowed
    expectedObject.setEquipmentId(150L); // need test equipment
    // inserted
    expectedObject.setProcessId(50L);
    expectedObject.setMinValue(12.2f);
    expectedObject.setMaxValue(23.3f);
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND3")));
    expectedObject.setDataTagQuality(new DataTagQualityImpl());
    // expectedObject.setCacheTimestamp(new
    // Timestamp(System.currentTimeMillis())); //should be set to creation time,
    // so not null
    // expectedObject.setSourceTimestamp(new
    // Timestamp(System.currentTimeMillis()));
    // expectedObject.setRuleIdsString("1234,3456"); //NO: never loaded at
    // reconfiguration of datatag, but only when a new rule is added

    assertEquals(expectedObject, cacheObject);

    Equipment equipment = equipmentCache.get(cacheObject.getEquipmentId());
    // check equipment now has datatag in list
    assertTrue(dataTagService.getDataTagIdsByEquipmentId(cacheObject.getEquipmentId()).contains(5000000L));

    // test update of this datatag
    report = configurationLoader.applyConfiguration(4);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    DataTagCacheObject updatedCacheObject = (DataTagCacheObject) dataTagCache.get(5000000L);

    expectedObject.setJapcAddress("testConfigJAPCaddress2");
    expectedObject.setDipAddress(null); // checks can be set to null also
    expectedObject.setMaxValue(26f);
    expectedObject.setAddress(new DataTagAddress(new OPCHardwareAddressImpl("CW_TEMP_IN_COND4")));

    assertEquals(expectedObject, updatedCacheObject);
    equipment = equipmentCache.get(cacheObject.getEquipmentId());
  }

  @Test
  public void testRemoveDataTag() {
    // check data as expected
    Long tagId = 200001L;
    DataTagCacheObject cacheObject = (DataTagCacheObject) dataTagCache.get(200001L);
    assertNotNull(cacheObject);
    assertNotNull(dataTagMapper.getItem(tagId));

    // run test
    ConfigurationReport report = configurationLoader.applyConfiguration(7);

    // check successful

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertFalse(dataTagCache.containsKey(tagId));
    assertNull(dataTagMapper.getItem(tagId));
    // tag id is no longer in equipment
    assertFalse(dataTagService.getDataTagIdsByEquipmentId(cacheObject.getEquipmentId()).contains(tagId));
  }

  @Test
  public void createEquipmentDataTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    // TEST:
    // Build configuration to add the test DataTag
    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTag = ConfigurationDataTagUtil.buildCreateAllFieldsDataTag(1000L, null);
    dataTag.setEquipmentId(15L);

    Configuration configuration = new Configuration();
    configuration.addEntity(dataTag);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(1000L, dataTag);

    assertEquals(expectedCacheObjectData, cacheObjectData);
    // Check if all caches are updated
    assertTrue(dataTagService.getDataTagIdsByEquipmentId(cacheObjectData.getEquipmentId()).contains(1000L));
  }

  @Test
  public void updateEquipmentDataTag() {
    setUp();

    // TEST:
    // Build configuration to update the test DataTag
    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTagUpdate = cern.c2mon.shared.client.configuration.api.tag.DataTag.update(1000L)
      .description("new description")
      .mode(TagMode.OPERATIONAL)
      .minValue(99)
      .unit("updateUnit").build();
    Configuration configuration = new Configuration();
    configuration.addEntity(dataTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagUpdateCacheObject(cacheObjectData, dataTagUpdate);

    assertEquals(expectedCacheObjectData, cacheObjectData);
  }

  @Test
  public void createSubEquipmentDataTag() {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createSubEquipment = TestConfigurationProvider.createSubEquipment();
    configurationLoader.applyConfiguration(createSubEquipment);

    // TEST:
    // Build configuration to add the test DataTag
    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTag = ConfigurationDataTagUtil.buildCreateAllFieldsDataTag(1000L, null);
    dataTag.setSubEquipmentId(25L);
    dataTag.setEquipmentId(null);

    Configuration configuration = new Configuration();
    configuration.addEntity(dataTag);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagCacheObject(1000L, dataTag);
    expectedCacheObjectData.setSubEquipmentId(dataTag.getSubEquipmentId());
    expectedCacheObjectData.setEquipmentId(null);

    assertEquals(expectedCacheObjectData, cacheObjectData);
    // Check if all caches are updated
    assertTrue(dataTagService.getDataTagIdsBySubEquipmentId(cacheObjectData.getSubEquipmentId()).contains(1000L));
  }

  @Test
  public void updateSubEquipmentDataTag() {
    /// SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration createEquipment = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(createEquipment);
    Configuration createSubEquipment = TestConfigurationProvider.createSubEquipment();
    configurationLoader.applyConfiguration(createSubEquipment);
    Configuration createDataTag = TestConfigurationProvider.createSubEquipmentDataTag(25L);
    configurationLoader.applyConfiguration(createDataTag);

    // TEST:
    // Build configuration to update the test DataTag
    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTagUpdate = cern.c2mon.shared.client.configuration.api.tag.DataTag.update(1000L)
      .description("new description")
      .mode(TagMode.OPERATIONAL)
      .minValue(99)
      .unit("updateUnit").build();
    Configuration configuration = new Configuration();
    configuration.addEntity(dataTagUpdate);

    ///apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    DataTagCacheObject expectedCacheObjectData = cacheObjectFactory.buildDataTagUpdateCacheObject(cacheObjectData, dataTagUpdate);

    assertEquals(expectedCacheObjectData, cacheObjectData);
  }


  @Test
  public void updateRemoveMetadataDataTag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration newEquipmentConfig = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(newEquipmentConfig);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTag = cern.c2mon.shared.client.configuration.api.tag.DataTag.create("DataTag", Integer.class, new DataTagAddress())
      .id(1000L)
      .description("foo")
      .mode(TagMode.OPERATIONAL)
      .isLogged(false)
      .minValue(0)
      .maxValue(10)
      .unit("testUnit")
      .addMetadata("testMetadata1", 11)
      .addMetadata("testMetadata2", 22)
      .addMetadata("testMetadata3", 33)
      .build();
    dataTag.setEquipmentId(15L);

    Configuration configuration = new Configuration();
    configuration.addEntity(dataTag);

    //apply the configuration to the server
    configurationLoader.applyConfiguration(configuration);

    cern.c2mon.shared.client.configuration.api.tag.DataTag updatedDataTag = cern.c2mon.shared.client.configuration.api.tag.DataTag.update(1000L)
      .removeMetadata("testMetadata2")
      .removeMetadata("testMetadata3")
      .build();

    configuration = new Configuration();
    configuration.addEntity(updatedDataTag);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());
    assertEquals(report.getElementReports().get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(report.getElementReports().get(0).getEntity(), ConfigConstants.Entity.DATATAG);

    DataTagCacheObject expectedCacheObjectData = new DataTagCacheObject(0L);
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata1", 11);
    expectedCacheObjectData.setMetadata(metadata);

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    assertEquals(expectedCacheObjectData.getMetadata(), cacheObjectData.getMetadata());
  }

  @Test
  public void createMetadataDatatag() {
    // SETUP:
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration newEquipmentConfig = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(newEquipmentConfig);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTag =
      cern.c2mon.shared.client.configuration.api.tag.DataTag.create("DataTag", Integer.class, new DataTagAddress())
      .id(1000L)
      .description("foo")
      .mode(TagMode.OPERATIONAL)
      .isLogged(false)
      .minValue(0)
      .maxValue(10)
      .unit("testUnit")
      .addMetadata("testMetadata", 11)
      .build();
    dataTag.setEquipmentId(15L);

    Configuration configuration = new Configuration();
    configuration.addEntity(dataTag);

    //apply the configuration to the server
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);

    // check report result
    assertEquals(ConfigConstants.Status.OK, report.getStatus());
    assertTrue(report.getProcessesToReboot().isEmpty());
    assertEquals(1, report.getElementReports().size());
    assertEquals(report.getElementReports().get(0).getAction(), ConfigConstants.Action.CREATE);
    assertEquals(report.getElementReports().get(0).getEntity(), ConfigConstants.Entity.DATATAG);

    // get cacheObject from the cache and compare to the an expected cacheObject
    DataTagCacheObject cacheObjectData = (DataTagCacheObject) dataTagCache.get(1000L);
    DataTagCacheObject expectedCacheObjectData = new DataTagCacheObject(0L);
    Metadata metadata = new Metadata();
    metadata.addMetadata("testMetadata", 11);
    expectedCacheObjectData.setMetadata(metadata);
    assertEquals(expectedCacheObjectData.getMetadata(), cacheObjectData.getMetadata());
  }

  @Test
  public void updateNonExisting() {
    Configuration createProcess = TestConfigurationProvider.createProcess();
    configurationLoader.applyConfiguration(createProcess);
    Configuration newEquipmentConfig = TestConfigurationProvider.createEquipment();
    configurationLoader.applyConfiguration(newEquipmentConfig);
    processService.start(5L, "hostname", new Timestamp(System.currentTimeMillis()));

    cern.c2mon.shared.client.configuration.api.tag.DataTag dataTag = cern.c2mon.shared.client.configuration.api.tag.DataTag.create("DataTag", Integer.class, new DataTagAddress())
      .id(1000L)
      .description("foo")
      .mode(TagMode.OPERATIONAL)
      .isLogged(false)
      .minValue(0)
      .maxValue(10)
      .unit("testUnit")
      .addMetadata("testMetadata1", 11)
      .addMetadata("testMetadata2", 22)
      .addMetadata("testMetadata3", 33)
      .build();
    dataTag.setEquipmentId(15L);

    Configuration configuration = new Configuration();
    configuration.addEntity(dataTag);
    //apply the configuration to the server
    configurationLoader.applyConfiguration(configuration);

    //now add some tags
    cern.c2mon.shared.client.configuration.api.tag.DataTag updatedDataTag = cern.c2mon.shared.client.configuration.api.tag.DataTag.update(1000L)
      .removeMetadata("testMetadata2")
      .removeMetadata("testMetadata3")
      .build();
    configuration = new Configuration();
    configuration.addEntity(updatedDataTag);

    //1010L does not exist
    cern.c2mon.shared.client.configuration.api.tag.DataTag updatedDataTag2 = cern.c2mon.shared.client.configuration.api.tag.DataTag.update(1010L).build();
    configuration.addEntity(updatedDataTag2);
    //apply the configuration to the server
    //should not throw an exception for 1010L
    ConfigurationReport report = configurationLoader.applyConfiguration(configuration);
    assertEquals(2, report.getElementReports().size());
    assertEquals(1010L, (long)report.getElementReports().get(0).getId());
    assertEquals(1000L, (long)report.getElementReports().get(1).getId());
    //the overall report status is WARNING
    assertEquals(ConfigConstants.Status.WARNING, report.getStatus());
    //and the element report status for 1010L is WARNING
    assertEquals(ConfigConstants.Status.WARNING, report.getElementReports().get(0).getStatus());
    assertEquals(ConfigConstants.Status.OK, report.getElementReports().get(1).getStatus());
  }
}
