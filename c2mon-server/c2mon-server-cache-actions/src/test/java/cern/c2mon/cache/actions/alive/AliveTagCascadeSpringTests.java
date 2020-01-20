package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.test.cache.AbstractCacheObjectFactory;
import cern.c2mon.server.test.cache.AliveTagCacheObjectFactory;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static org.junit.Assert.assertTrue;

public class AliveTagCascadeSpringTests extends AbstractCacheTest<AliveTag,AliveTag> {

  @Inject
  private C2monCache<AliveTag> aliveTagCache;

  @Inject
  private C2monCache<CommFaultTag> commFaultCache;

  @Before
  public void resetCommFault() {
    commFaultCache.init();
  }

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
        aliveTagCache.put(aliveTag.getId(), aliveTag);
      });

    assertTrue(commFaultUpdate.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void aliveTagUpdateCascadeToState() {

  }
}
