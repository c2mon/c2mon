package cern.c2mon.server.cache.equipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;

/**
 * Integration test of the EquipmentCache implementation
 * with the cache loading and cache DB access modules.
 * 
 * @author Mark Brightwell
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-equipment-test.xml"})
public class EquipmentCacheTest {
  
  @Autowired
  private EquipmentMapper equipmentMapper;
  
  @Autowired
  private EquipmentCacheImpl equipmentCache;
  
  @Test
  public void testCacheLoading() {
    assertNotNull(equipmentCache);
    
    List<Equipment> equipmentList = equipmentMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...    
    
    //test the cache is the same size as in DB
    assertEquals(equipmentList.size(), equipmentCache.getCache().getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<Equipment> it = equipmentList.iterator();
    while (it.hasNext()) {
      EquipmentCacheObject currentEquipment = (EquipmentCacheObject) it.next();
      //only compares one field so far
      assertEquals(currentEquipment.getName(), (((Equipment) equipmentCache.getCopy(currentEquipment.getId())).getName()));
    }
  }
  
}
