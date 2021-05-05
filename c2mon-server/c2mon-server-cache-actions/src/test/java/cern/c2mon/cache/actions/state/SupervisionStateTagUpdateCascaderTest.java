package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.factory.AliveTagCacheObjectFactory;
import cern.c2mon.server.cache.test.factory.CommFaultTagCacheObjectFactory;
import cern.c2mon.server.cache.test.factory.SupervisionStateTagFactory;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheActionsModuleRef.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  IgniteModule.class,
})
public class SupervisionStateTagUpdateCascaderTest {

  @Inject private C2monCache<AliveTag> aliveTagCache;
  @Inject private C2monCache<CommFaultTag> commFaultTagCache;
  @Inject private C2monCache<SupervisionStateTag> stateTagCache;
  @Inject private C2monCache<Equipment> equipmentCache;
  @Inject private C2monCache<SubEquipment> subEquipmentCache;

  private final SupervisionStateTagFactory stateTagFactory = new SupervisionStateTagFactory();
  private final AliveTagCacheObjectFactory aliveTagFactory = new AliveTagCacheObjectFactory();
  private final CommFaultTagCacheObjectFactory commFaultTagFactory = new CommFaultTagCacheObjectFactory();

  @Inject
  private SupervisionStateTagUpdateCascader updateCascader;

  @Test
  public void propagatesRunningStatusToControlTags() {
    SupervisionStateTag stateTag = putAndGet(
      stateTagCache,
      apply(stateTagFactory.sampleBase(), t -> t.setSupervisionStatus(SupervisionStatus.RUNNING))
    );

    AliveTag aliveTag = putAndGet(
      aliveTagCache,
      apply(aliveTagFactory.sampleBase(), t -> t.setValue(false))
    );

    CommFaultTag commFaultTag = putAndGet(
      commFaultTagCache,
      apply(commFaultTagFactory.sampleBase(), t -> t.setValue(false))
    );

    SupervisionStateTag subStateTag = putAndGet(
      stateTagCache,
      apply(stateTagFactory.sampleBase(24247422), t -> {
        t.setValue(false);
        t.setSupervisionStatus(SupervisionStatus.DOWN);
      })
    );

    putAndGet(
      subEquipmentCache,
      apply(new SubEquipmentCacheObject(63964334L), sse -> {
        sse.setName("TestSubEq63964334");
        sse.setParentId(stateTag.getSupervisedId());
        sse.setAliveTagId(aliveTag.getId());
        sse.setCommFaultTagId(commFaultTag.getId());
        sse.setStateTagId(subStateTag.getId());
      })
    );

    updateCascader.apply(stateTag);

    assertTrue(aliveTagCache.get(aliveTag.getId()).getValue());
    assertTrue(commFaultTagCache.get(commFaultTag.getId()).getValue());
    assertTrue(stateTagCache.get(subStateTag.getId()).getValue());
    assertEquals(SupervisionStatus.RUNNING, stateTagCache.get(subStateTag.getId()).getSupervisionStatus());
  }

  private static <C extends C2monCache<T>, T extends Cacheable> T putAndGet(C cache, T thing) {
    cache.put(thing.getId(), thing);
    return cache.get(thing.getId());
  }
}
