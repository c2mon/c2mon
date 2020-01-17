package cern.c2mon.cache.actions.alive;

import cern.c2mon.cache.actions.commfault.CommFaultService;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.impl.SimpleC2monCache;
import cern.c2mon.server.test.cache.AliveTagCacheObjectFactory;
import cern.c2mon.server.test.cache.CommFaultTagCacheObjectFactory;
import cern.c2mon.server.test.cache.SupervisionStateTagFactory;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static org.junit.Assert.assertTrue;

public class AliveTagCascadeTests {

  private final AliveTagCacheObjectFactory factory = new AliveTagCacheObjectFactory();
  private final CommFaultTagCacheObjectFactory commFaultFactory = new CommFaultTagCacheObjectFactory();
  private final SupervisionStateTagFactory stateTagFactory = new SupervisionStateTagFactory();
  private CommFaultService commFaultService = new CommFaultService(new SimpleC2monCache<>("commFault"));
  private SupervisionStateTagService stateTagService = new SupervisionStateTagService(new SimpleC2monCache<>("stateTag"));

  private AliveTagUpdateCascader updateCascader =
    new AliveTagUpdateCascader(new SimpleC2monCache<>("aliveTag"), commFaultService, stateTagService);

  @Before
  public void setUp() {
    updateCascader.register();
  }

  private void populateWithData() {
    apply(commFaultFactory.sampleBase(), it -> commFaultService.getCache().put(it.getId(), it));
  }

  @Test(expected = NullPointerException.class)
  public void nullTag() {
    updateCascader.apply(null);
  }

  @Test
  public void nullValue() {
    updateCascader.apply(apply(factory.sampleBase(), it -> it.setValue(null)));
  }

  @Test
  public void missingCfault() {
    updateCascader.apply(apply(factory.sampleBase(), alive -> alive.setCommFaultTagId(null)));
  }

  @Test(expected = CacheElementNotFoundException.class)
  public void missingCfaultInCache() {
    populateWithData();

    updateCascader.apply(apply(factory.sampleBase(), aliveTag -> aliveTag.setCommFaultTagId(-1L)));
  }

  @Test
  public void missingStateTag() {
    populateWithData();

    updateCascader.apply(apply(factory.sampleBase(), alive -> alive.setStateTagId(null)));
  }

  @Test
  public void missingStateTagInCache() {
    populateWithData();

    updateCascader.apply(apply(factory.sampleBase(), aliveTag -> aliveTag.setStateTagId(-1L)));
  }

  @Test(expected = CacheElementNotFoundException.class)
  public void missingStateTagInCacheAndProcess() {
    populateWithData();

    updateCascader.apply(apply(factory.ofProcess(), aliveTag -> aliveTag.setStateTagId(-1L)));
  }

  @Test
  public void valid() throws InterruptedException {
    populateWithData();
    CountDownLatch commFaultUpdate = new CountDownLatch(1);
    registerLatchListener(commFaultService.getCache(), commFaultUpdate);

    updateCascader.apply(apply(factory.sampleBase(),
      aliveTag -> aliveTag.setSourceTimestamp(Timestamp.from(Instant.now()))));

    assertTrue(commFaultUpdate.await(100, TimeUnit.MILLISECONDS));
  }

  private void registerLatchListener(C2monCache<?> cache, CountDownLatch latch) {
    cache.getCacheListenerManager().registerListener(item -> latch.countDown(), CacheEvent.UPDATE_ACCEPTED);
  }
}
