package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.SupervisionCacheResetRule;
import cern.c2mon.server.test.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.test.factory.AliveTagCacheObjectFactory;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = SupervisionCacheResetRule.class)
public class AliveTagCascadeSpringTests extends AbstractCacheTest<AliveTag, AliveTag> {

  @Inject
  private C2monCache<AliveTag> aliveTagCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Override
  protected C2monCache<AliveTag> initCache() {
    return aliveTagCache;
  }

  @Override
  protected AbstractCacheObjectFactory<AliveTag> initFactory() {
    return new AliveTagCacheObjectFactory();
  }

  @Test
  public void aliveTagUpdateCascadesToCommFault() throws InterruptedException {
    CountDownLatch commFaultUpdate = new CountDownLatch(1);

    commFaultCache.getCacheListenerManager().registerListener(__ -> commFaultUpdate.countDown(), UPDATE_ACCEPTED);

    apply(factory.sampleBase(),
      aliveTag -> {
        aliveTag.setSourceTimestamp(Timestamp.from(Instant.now()));
        aliveTag.setValue(true);
        aliveTagCache.put(aliveTag.getId(), aliveTag);
      });

    assertTrue(commFaultUpdate.await(100, TimeUnit.MILLISECONDS));
    assertTrue(commFaultCache.get(getSample().getCommFaultTagId()).getValue());
  }

  @Test
  public void aliveTagUpdateCascadeToState() throws InterruptedException {
    CountDownLatch stateTagUpdate = new CountDownLatch(1);

    stateTagCache.getCacheListenerManager().registerListener(__ -> stateTagUpdate.countDown(), UPDATE_ACCEPTED);

    AliveTag processTag = apply(((AliveTagCacheObjectFactory) factory).ofProcess(),
      aliveTag -> {
        aliveTag.setSourceTimestamp(Timestamp.from(Instant.now()));
        aliveTag.setValue(true);
        aliveTagCache.put(aliveTag.getId(), aliveTag);
      });

    assertTrue(stateTagUpdate.await(100, TimeUnit.MILLISECONDS));
    assertEquals(RUNNING, stateTagCache.get(processTag.getStateTagId()).getSupervisionStatus());
  }

  @Test
  public void aliveTagUpdateCascadeThroughCommFault() throws InterruptedException {
    CountDownLatch stateTagUpdate = new CountDownLatch(1);
    CountDownLatch commFaultUpdate = new CountDownLatch(1);
    commFaultCache.getCacheListenerManager().registerListener(__ -> commFaultUpdate.countDown(), UPDATE_ACCEPTED);
    stateTagCache.getCacheListenerManager().registerListener(__ -> stateTagUpdate.countDown(), UPDATE_ACCEPTED);

    apply(factory.sampleBase(),
      aliveTag -> {
        aliveTag.setSourceTimestamp(Timestamp.from(Instant.now()));
        aliveTag.setValue(true);
        aliveTagCache.put(aliveTag.getId(), aliveTag);
      });

    assertTrue(commFaultUpdate.await(100, TimeUnit.MILLISECONDS));
    assertTrue(stateTagUpdate.await(250, TimeUnit.MILLISECONDS));
    assertTrue(commFaultCache.get(getSample().getCommFaultTagId()).getValue());
    assertEquals(RUNNING, stateTagCache.get(getSample().getStateTagId()).getSupervisionStatus());
  }
}
