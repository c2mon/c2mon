package cern.c2mon.cache.actions.commfault;

import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.impl.SimpleCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.factory.CommFaultTagCacheObjectFactory;
import cern.c2mon.server.test.factory.SupervisionStateTagFactory;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cern.c2mon.cache.actions.alive.AliveTagCascadeTests.registerLatchListener;
import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.RUNNING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommFaultCascadeTests {
  private final CommFaultTagCacheObjectFactory commFaultFactory = new CommFaultTagCacheObjectFactory();
  private final SupervisionStateTagFactory stateTagFactory = new SupervisionStateTagFactory();

  private SupervisionStateTagService stateTagService = new SupervisionStateTagService(new SimpleCache<>("stateTag"));

  private CommFaultTagUpdateCascader updateCascader;

  @Before
  public void init() {
    updateCascader = new CommFaultTagUpdateCascader(new SimpleCache<>("cFault"), stateTagService);
    updateCascader.register();
  }

  @Test(expected = NullPointerException.class)
  public void nullTag() {
    updateCascader.apply(null);
  }

  @Test
  public void nullValue() {
    updateCascader.apply(
      apply(
        commFaultFactory.sampleBase(),
        it -> it.setValue(null)
      )
    );
  }

  @Test
  public void missingStateTag() {
    updateCascader.apply(
      apply(
        commFaultFactory.sampleBase(),
        it -> it.setStateTagId(null)
      )
    );
  }

  @Test(expected = CacheElementNotFoundException.class)
  public void missingStateTagInCache() {
    // State tag cache is not populated

    updateCascader.apply(commFaultFactory.sampleBase());
  }

  @Test
  public void valid() {
    populateWithData();
    CommFaultTag tagWithCurrentTime = apply(commFaultFactory.sampleBase(),
      it -> {
        it.setValue(true);
        it.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
      });

    updateCascader.apply(tagWithCurrentTime);

    assertEquals(RUNNING, stateTagService.getCache().get(commFaultFactory.sampleBase().getStateTagId()).getSupervisionStatus());
  }

  @Test
  public void validFiresEvents() throws InterruptedException {
    CountDownLatch updateLatch = new CountDownLatch(1);
    registerLatchListener(stateTagService.getCache(), updateLatch);

    valid();

    assertTrue(updateLatch.await(100, TimeUnit.MILLISECONDS));
  }

  @Test
  public void earliestUpdateRejected() {
    long now = System.currentTimeMillis();
    // Put a running state tag
    populateWithData(it -> {
      it.setSupervision(RUNNING, "", new Timestamp(now));
      it.setSourceTimestamp(new Timestamp(now));
    });
    // Prepare a "DOWN" cFault, with previous timestamp
    CommFaultTag tagWithPreviousTime = apply(commFaultFactory.sampleBase(),
      it -> it.setSourceTimestamp(new Timestamp(now - 1)));

    // Should have no effect
    updateCascader.apply(tagWithPreviousTime);
    assertEquals(RUNNING, stateTagService.getCache().get(commFaultFactory.sampleBase().getStateTagId()).getSupervisionStatus());
  }

  private void populateWithData(Consumer<SupervisionStateTag> alsoApply) {
    apply(
      stateTagFactory.sampleBase(),
      it -> stateTagService.getCache().put(it.getId(), apply(it, alsoApply))
    );
  }

  private void populateWithData() {
    populateWithData(__ -> {
      // Do nothing
    });
  }
}
