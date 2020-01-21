package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.cache.AbstractCacheObjectFactory;
import cern.c2mon.server.test.cache.CommFaultTagCacheObjectFactory;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.CacheEvent.UPDATE_ACCEPTED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommFaultCascadeSpringTest extends AbstractCacheTest<CommFaultTag, CommFaultTag> {

  @Inject
  private C2monCache<CommFaultTag> commFaultCache;

  @Inject
  private C2monCache<SupervisionStateTag> stateTagCache;

  @Before
  public void resetCaches() {
    commFaultCache.clear();
    commFaultCache.init();
    stateTagCache.clear();
    stateTagCache.init();
  }

  @Override
  protected C2monCache<CommFaultTag> initCache() {
    return commFaultCache;
  }

  @Override
  protected AbstractCacheObjectFactory<CommFaultTag> initFactory() {
    return new CommFaultTagCacheObjectFactory();
  }

  @Test
  @Ignore("This test is failing in Maven runs")
  public void cascadeToState() throws InterruptedException {
    CountDownLatch stateTagUpdate = new CountDownLatch(1);

    stateTagCache.getCacheListenerManager().registerListener(__ -> stateTagUpdate.countDown(), UPDATE_ACCEPTED);

    apply(factory.sampleBase(),
      commFaultTag -> {
        commFaultTag.setSourceTimestamp(Timestamp.from(Instant.now()));
        commFaultTag.setValue(true);
        commFaultCache.put(commFaultTag.getId(), commFaultTag);
      });

    assertTrue(stateTagUpdate.await(200, TimeUnit.MILLISECONDS));
    assertEquals(SupervisionStatus.RUNNING, stateTagCache.get(getSample().getStateTagId()).getSupervisionStatus());
  }
}
