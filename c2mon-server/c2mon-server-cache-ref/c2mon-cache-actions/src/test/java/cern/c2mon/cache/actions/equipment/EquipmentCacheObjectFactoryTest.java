package cern.c2mon.cache.actions.equipment;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.shared.common.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheObjectFactoryTest {

  private EquipmentCacheObjectFactory factory;

  @Before
  public void init() {
    factory = new EquipmentCacheObjectFactory();
  }

  @Test(expected = NullPointerException.class)
  public void createCacheObjectWithNullProperties() throws IllegalAccessException {
    Equipment equipment = factory.createCacheObject(1L, null);

    assertNotNull("Exception should be thrown since properties are null", equipment);
  }

  @Test(expected = ConfigurationException.class)
  public void createCacheObjectWithEmptyProperties() throws IllegalAccessException {
    Properties properties = new Properties();

    Equipment equipment = factory.createCacheObject(1L, properties);

    assertNotNull("Object should be not created, missing required parameters in properties", equipment);
  }

  //TODO: add more tests with properties
}
