package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou Koufidis
 */
public class AliveTagCreationTest {

  private static final Long KEY = 1L;
  private static final Long ALIVE_TAG_ID = 10L;
  private static final Long STATE_TAG_ID = 100L;

  private static final int ALIVE_INTERVAL = 1000;

  private AliveTagService aliveTimerService;
  private C2monCache<AliveTag> aliveTimerCache;

  @Before
  public void init() {
    aliveTimerCache = new SimpleCache<>("aliveTimerCache");
    aliveTimerService = new AliveTagService(aliveTimerCache);
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

    assertEquals("AliveTimer should have Equipment type set", SupervisionEntity.EQUIPMENT, cachedAliveTimer.getSupervisedEntity());
    assertEquals("AliveTimer should have interval set", equipmentCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", equipmentCacheObject.getId(), cachedAliveTimer.getSupervisedId().longValue());
    assertEquals("AliveTimer should have relatedName set", equipmentCacheObject.getName(), cachedAliveTimer.getSupervisedName());
    assertEquals("AliveTimer should have stateTagId set", equipmentCacheObject.getStateTagId(), cachedAliveTimer.getStateTagId());
  }

  @Test
  public void generateFromSubEquipment() {
    SubEquipmentCacheObject subEquipmentCacheObject = new SubEquipmentCacheObject(KEY);
    subEquipmentCacheObject.setAliveTagId(ALIVE_TAG_ID);
    subEquipmentCacheObject.setName("subEquipment");
    subEquipmentCacheObject.setStateTagId(STATE_TAG_ID);
    subEquipmentCacheObject.setAliveInterval(ALIVE_INTERVAL);

    aliveTimerService.createAliveTimerFor(subEquipmentCacheObject);

    AliveTag cachedAliveTimer = (AliveTag) aliveTimerCache.get(ALIVE_TAG_ID);

    assertEquals("AliveTimer should have SubEquipment type set", SupervisionEntity.SUBEQUIPMENT, cachedAliveTimer.getSupervisedEntity());
    assertEquals("AliveTimer should have interval set", subEquipmentCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", subEquipmentCacheObject.getId(), cachedAliveTimer.getSupervisedId().longValue());
    assertEquals("AliveTimer should have relatedName set", subEquipmentCacheObject.getName(), cachedAliveTimer.getSupervisedName());
    assertEquals("AliveTimer should have stateTagId set", subEquipmentCacheObject.getStateTagId(), cachedAliveTimer.getStateTagId());
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

    assertEquals("AliveTimer should have Process type set", SupervisionEntity.PROCESS, cachedAliveTimer.getSupervisedEntity());
    assertEquals("AliveTimer should have interval set", processCacheObject.getAliveInterval(), cachedAliveTimer.getAliveInterval());
    assertEquals("AliveTimer should have relatedId set", processCacheObject.getId(), cachedAliveTimer.getSupervisedId().longValue());
    assertEquals("AliveTimer should have relatedName set", processCacheObject.getName(), cachedAliveTimer.getSupervisedName());
    assertEquals("AliveTimer should have stateTagId set", processCacheObject.getStateTagId(), cachedAliveTimer.getStateTagId());
  }
}
