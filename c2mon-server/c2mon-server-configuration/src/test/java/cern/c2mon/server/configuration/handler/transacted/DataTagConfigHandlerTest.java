package cern.c2mon.server.configuration.handler.transacted;

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

public class DataTagConfigHandlerTest extends ConfigurationCacheTest {

  @Rule
  @Inject
  public ConfigRuleChain configRuleChain;

  @Inject private DataTagConfigHandler dataTagConfigTransacted;
  
  private static final long VALID_ID = 200000;
  private static final long VALID_EQ_ID = 150;

  @Test
  public void testEmptyUpdateDataTag() {
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(VALID_ID);
    update.setEquipmentId(VALID_EQ_ID);

    // put
//    dataTagLoaderDAO.updateConfig(dataTag);  ??????

    for (ProcessChange processChange : dataTagConfigTransacted.update(VALID_ID, new Properties())) {
      assertFalse(processChange.processActionRequired());
    }
  }

  @Test
  public void testNotEmptyUpdateDataTag() {
    // mimic the actions of the datatag facade
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(VALID_ID);
    update.setEquipmentId(VALID_EQ_ID);
    update.setName("new name");

    // put

    for (ProcessChange processChange : dataTagConfigTransacted.update(VALID_ID, new Properties())) {
      assertFalse(processChange.processActionRequired());
      assertNull(processChange.getProcessId());
    }
  }

  @Test
  public void testUpdateDAQRelatedPropertiesOfDataTag() {
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(VALID_ID);
    update.setEquipmentId(VALID_EQ_ID);
    update.setDataTagAddressUpdate(new DataTagAddressUpdate());

    Properties properties = new Properties();
    properties.put("address", "new address");
    properties.put("dataType", "new data type");
    properties.put("minValue", "new min val");
    properties.put("maxValue", "new max val");

    // put
    for (ProcessChange change : dataTagConfigTransacted.update(VALID_ID, properties)) {
      assertTrue(change.processActionRequired());
      assertEquals(Long.valueOf(50), change.getProcessId());
    }

  }

  @Test
  public void testUpdateNonDAQRelatedPropertiesOfDataTag() {
    DataTagUpdate update = new DataTagUpdate();
    update.setDataTagId(VALID_ID);
    update.setEquipmentId(VALID_EQ_ID);
    update.setName("new name");

    // Update all properties that do not require DAQ reconfiguration
    Properties properties = new Properties();
    properties.put("id", VALID_ID);
    properties.put("name", "new name");
    properties.put("description", "new description");
    properties.put("mode", 1);
    properties.put("isLogged", false);
    properties.put("unit", "new unit");
    properties.put("equipmentId", VALID_EQ_ID);
    properties.put("valueDictionary", "new dict");
    properties.put("japcAddress", "new japc address");
    properties.put("dipAddress", "new dip address");

    // put

    for (ProcessChange change : dataTagConfigTransacted.update(VALID_ID, properties)) {
      assertFalse(change.processActionRequired());
    }
  }
}
