package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.actions.AbstractCacheListenerTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.test.factory.SupervisionStateTagFactory;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.*;

public class StateTagServiceTest extends AbstractCacheListenerTest<SupervisionStateTag, SupervisionStateTag> {

  @Inject
  SupervisionStateTagService stateTagService;

  @Inject
  C2monCache<SupervisionStateTag> supervisionStateTagCache;

  private SupervisionStateTag sample;

  @Override
  protected C2monCache<SupervisionStateTag> initCache() {
    return supervisionStateTagCache;
  }

  @Override
  protected AbstractCacheObjectFactory<SupervisionStateTag> initFactory() {
    return new SupervisionStateTagFactory();
  }

  @Test(expected = CacheElementNotFoundException.class)
  public void getSupervisionStatusThrowsIfNonexistent() {
    stateTagService.getSupervisionEvent(-1L);
  }

  @Override
  public void reload() {
    super.reload();
    sample = getSample();
  }

  @Test
  public void isRunning() {
    cache.put(sample.getId(), sample);
    // Default
    assertFalse(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(STARTUP, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(RUNNING, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertTrue(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(STOPPED, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(DOWN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(stateTagService.isRunning(sample.getId()));

    sample.setSupervision(UNCERTAIN, "", Timestamp.from(Instant.now()));
    cache.put(sample.getId(), sample);
    assertFalse(stateTagService.isRunning(sample.getId()));
  }

  @Test
  public void isUncertain() {
    sample.setSupervision(UNCERTAIN, "", new Timestamp(0));

    cache.put(sample.getId(), sample);

    assertTrue(SupervisionStateTagEvaluator.isUncertain(stateTagService.getCache().get(sample.getId())));
  }

  @Test
  public void refreshAndNotifyCurrentSupervisionStatus() {
    // Generates one supervision update event, we don't listen yet
    cache.put(sample.getId(), sample);

    final AtomicInteger eventCounter = new AtomicInteger(0);
    final CacheListener<SupervisionStateTag> paramListener = eq -> eventCounter.incrementAndGet();
    cache.getCacheListenerManager().registerListener(paramListener, CacheEvent.SUPERVISION_UPDATE);

    // Should generate exactly one event
    stateTagService.refresh(sample.getId());

    cache.getCacheListenerManager().close();

    assertEquals(1, eventCounter.get());
  }
}
