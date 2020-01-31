package cern.c2mon.server.configuration.handler.transacted;

import cern.c2mon.server.cache.test.CacheObjectCreation;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.configuration.ConfigurationCacheTest;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.configuration.junit.ConfigRuleChain;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Properties;

import static org.junit.Assert.*;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {
//  GenericApplicationContext.class,
//  ConfigurationModule.class
//})
public class DataTagConfigHandlerTest extends ConfigurationCacheTest {

  @Rule
  @Inject
  public ConfigRuleChain configRuleChain;

  @Inject private DataTagConfigHandler dataTagConfigTransacted;

  @Test
  public void testEmptyUpdateDataTag() {
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());

    // put
//    dataTagLoaderDAO.updateConfig(dataTag);  ??????

    for (ProcessChange processChange : dataTagConfigTransacted.update(dataTag.getId(), new Properties())) {
      assertFalse(processChange.processActionRequired());
    }
  }

  @Test
  public void testNotEmptyUpdateDataTag() {
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setName("new name");

    // put

    for (ProcessChange processChange : dataTagConfigTransacted.update(dataTag.getId(), new Properties())) {
      assertFalse(processChange.processActionRequired());
      assertNull(processChange.getProcessId());
    }
  }

  @Test
  public void testUpdateDAQRelatedPropertiesOfDataTag() {
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();

    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setDataTagAddressUpdate(new DataTagAddressUpdate());

    Properties properties = new Properties();
    properties.put("address", "new address");
    properties.put("dataType", "new data type");
    properties.put("minValue", "new min val");
    properties.put("maxValue", "new max val");

    // put
    for (ProcessChange change : dataTagConfigTransacted.update(dataTag.getId(), properties)) {
      assertTrue(change.processActionRequired());
      assertEquals(Long.valueOf(50), change.getProcessId());
    }

  }

  @Test
  public void testUpdateNonDAQRelatedPropertiesOfDataTag() {
    DataTagCacheObject dataTag = CacheObjectCreation.createTestDataTag();

    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(dataTag.getId());
    update.setEquipmentId(dataTag.getEquipmentId());
    update.setName("new name");

    // Update all properties that do not require DAQ reconfiguration
    Properties properties = new Properties();
    properties.put("id", dataTag.getId());
    properties.put("name", "new name");
    properties.put("description", "new description");
    properties.put("mode", "new mode");
    properties.put("isLogged", "new logged");
    properties.put("unit", "new unit");
    properties.put("equipmentId", dataTag.getEquipmentId());
    properties.put("valueDictionary", "new dict");
    properties.put("japcAddress", "new japc address");
    properties.put("dipAddress", "new dip address");

    // put

    for (ProcessChange change : dataTagConfigTransacted.update(dataTag.getId(), properties)) {
      assertFalse(change.processActionRequired());
    }
  }
}
