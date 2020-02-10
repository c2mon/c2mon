package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl;
import org.junit.Test;

import javax.inject.Inject;

import static org.easymock.EasyMock.verify;
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

    verify(mockManager);
  }
}
