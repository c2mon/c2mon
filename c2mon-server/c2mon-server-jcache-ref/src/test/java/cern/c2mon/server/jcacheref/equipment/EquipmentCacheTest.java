package cern.c2mon.server.jcacheref.equipment;

import javax.cache.Cache;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.jcacheref.IgniteBaseTestingSetup;
import cern.c2mon.server.jcacheref.prototype.equipment.EquipmentCommandCRUD;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheTest extends IgniteBaseTestingSetup {

  @Autowired
  EquipmentCommandCRUD equipmentCommandCRUD;

  @Autowired
  private Cache<Long, Equipment> equipmentTagCache;

  @Before
  public void setup() {

  }

  @Test
  public void addCommandToEquipment() {
    Equipment equipment = new EquipmentCacheObject(1L);
    equipment.getCommandTagIds().add(1L);

    equipmentTagCache.put(equipment.getId(), equipment);
    assertNotNull("Element with an id=1 expected in cache", equipmentTagCache.containsKey(1L));

    equipmentCommandCRUD.addCommandToEquipment(1L, 2L);
    assertEquals("Two items in the collection expected", 2, equipmentTagCache.get(1L).getCommandTagIds().size());
  }

  @Test
  public void removeCommandFromEquipment() {
    Equipment equipment = new EquipmentCacheObject(1L);

    equipment.getCommandTagIds().add(1L);
    equipment.getCommandTagIds().add(2L);
    equipment.getCommandTagIds().add(3L);

    equipmentTagCache.put(equipment.getId(), equipment);
    assertNotNull("Element with an id=1 expected in cache", equipmentTagCache.containsKey(1L));

    equipmentCommandCRUD.removeCommandFromEquipment(1L, 2L);
    assertEquals("Two items in the collection expected", 2, equipmentTagCache.get(1L).getCommandTagIds().size());
  }
}
