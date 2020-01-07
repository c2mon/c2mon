package cern.c2mon.cache.actions.alivetimer;

import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.alive.AliveTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
public class AliveTimerCreationTest {

  private static final Long KEY = 1L;
  private static final Long ALIVE_TAG_ID = 10L;
  private static final Long STATE_TAG_ID = 100L;

  private static final int ALIVE_INTERVAL = 1000;

  private AliveTimerService aliveTimerService;
  private C2monCache<AliveTag> aliveTimerCache;

  @Before
  public void init() {
    aliveTimerCache = new SimpleC2monCache<>("aliveTimerCache");
    aliveTimerService = new AliveTimerService(aliveTimerCache, new CommFaultService(new SimpleC2monCache<>("cFault")));
  }

  @After
  public void cleanup() {
    aliveTimerCache.remove(ALIVE_TAG_ID);
  }

  @Test
  public void generateFromEquipment() {
    EquipmentCacheObject equipmentCacheObject = new EquipmentCacheObject(KEY);
    equipmentCacheObject.setAliveTagId(ALIVE_TAG_ID);
    equipmentCacheObject.setName("equipment");
    equipmentCacheObject.setStateTagId(STATE_TAG_ID);
    equipmentCacheObject.setAliveInterval(ALIVE_INTERVAL);

    aliveTimerService.createAliveTimerFor(equipmentCacheObject);

    AliveTag cachedAliveTimer = aliveTimerCache.get(ALIVE_TAG_ID);

    assertTrue("AliveTimer should have Equipment type set", cachedAliveTimer.isEquipmentAliveType());
    assertEquals("AliveTimer should have interval set", equipmentCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", equipmentCacheObject.getId(), cachedAliveTimer.getRelatedId());
    assertEquals("AliveTimer should have relatedName set", equipmentCacheObject.getName(), cachedAliveTimer.getRelatedName());
    assertEquals("AliveTimer should have stateTagId set", equipmentCacheObject.getStateTagId(), cachedAliveTimer.getRelatedStateTagId());
  }

  @Test
  public void generateFromSubEquipment() {
    SubEquipmentCacheObject subEquipmentCacheObject = new SubEquipmentCacheObject(KEY);
    subEquipmentCacheObject.setAliveTagId(ALIVE_TAG_ID);
    subEquipmentCacheObject.setName("subEquipment");
    subEquipmentCacheObject.setStateTagId(STATE_TAG_ID);
    subEquipmentCacheObject.setAliveInterval(ALIVE_INTERVAL);

    aliveTimerService.createAliveTimerFor(subEquipmentCacheObject);

    AliveTagCacheObject cachedAliveTimer = (AliveTagCacheObject) aliveTimerCache.get(ALIVE_TAG_ID);

    assertEquals("AliveTimer should have SubEquipment type set", AliveTag.ALIVE_TYPE_SUBEQUIPMENT, cachedAliveTimer.getAliveType());
    assertEquals("AliveTimer should have interval set", subEquipmentCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", subEquipmentCacheObject.getId(), cachedAliveTimer.getRelatedId());
    assertEquals("AliveTimer should have relatedName set", subEquipmentCacheObject.getName(), cachedAliveTimer.getRelatedName());
    assertEquals("AliveTimer should have stateTagId set", subEquipmentCacheObject.getStateTagId(), cachedAliveTimer.getRelatedStateTagId());
  }

  @Test
  public void generateFromProcess() {
    ProcessCacheObject processCacheObject = new ProcessCacheObject(KEY);
    processCacheObject.setAliveTagId(ALIVE_TAG_ID);
    processCacheObject.setName("process");
    processCacheObject.setStateTagId(STATE_TAG_ID);
    processCacheObject.setAliveInterval(ALIVE_INTERVAL);

    aliveTimerService.createAliveTimerFor(processCacheObject);

    AliveTag cachedAliveTimer = aliveTimerCache.get(ALIVE_TAG_ID);

    assertTrue("AliveTimer should have Process type set", cachedAliveTimer.isProcessAliveType());
    assertEquals("AliveTimer should have interval set", processCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", processCacheObject.getId(), cachedAliveTimer.getRelatedId());
    assertEquals("AliveTimer should have relatedName set", processCacheObject.getName(), cachedAliveTimer.getRelatedName());
    assertEquals("AliveTimer should have stateTagId set", processCacheObject.getStateTagId(), cachedAliveTimer.getRelatedStateTagId());
  }
}
