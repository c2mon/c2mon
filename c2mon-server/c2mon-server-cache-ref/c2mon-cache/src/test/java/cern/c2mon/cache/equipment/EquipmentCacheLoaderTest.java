package cern.c2mon.cache.equipment;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCache<Long, Equipment> equipmentCacheRef;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Before
  public void init() {
    equipmentCacheRef.init();
  }

  @Test
  public void preloadCache() {
    assertNotNull("Equipment Cache should not be null", equipmentCacheRef);

    List<Equipment> equipmentList = equipmentMapper.getAll();

    assertTrue("List of equipment tags should not be empty", equipmentList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", equipmentList.size(), equipmentCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<Equipment> it = equipmentList.iterator();
    while (it.hasNext()) {
      EquipmentCacheObject currentEquipment = (EquipmentCacheObject) it.next();
      //only compares one field so far
      assertEquals("Cached Equipment should have the same name as in DB",
              currentEquipment.getName(), ((equipmentCacheRef.get(currentEquipment.getId())).getName()));
    }
  }
}
