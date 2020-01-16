package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.server.test.cache.SupervisionStateTagFactory;
import cern.c2mon.shared.common.CacheEvent;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.*;

public class StateTagServiceTest extends AbstractCacheTest<SupervisionStateTag> {

  @Inject
  SupervisionStateTagService stateTagService;

  @Inject
  C2monCache<SupervisionStateTag> supervisionStateTagCache;

  private final SupervisionStateTagFactory stateTagFactory = new SupervisionStateTagFactory();

  @Override
  protected SupervisionStateTag getSample() {
    return stateTagFactory.sampleBase();
  }

  @Override
  protected C2monCache<SupervisionStateTag> initCache() {
    return supervisionStateTagCache;
  }

  @Test(expected = CacheElementNotFoundException.class)
  public void getSupervisionStatusThrowsIfNonexistent() {
    stateTagService.getSupervisionEvent(getSample().getId());
  }

  @Test
  public void isRunning() {
    cache.put(getSample().getId(), getSample());
    // Default
    assertFalse(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(STARTUP, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertTrue(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertTrue(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(RUNNING, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertTrue(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(STOPPED, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertFalse(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(DOWN, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertFalse(stateTagService.isRunning(getSample().getId()));

    getSample().setSupervision(UNCERTAIN, "", Timestamp.from(Instant.now()));
    cache.put(getSample().getId(), getSample());
    assertFalse(stateTagService.isRunning(getSample().getId()));
  }

  @Test
  public void isUncertain() {
    getSample().setSupervision(UNCERTAIN, "", new Timestamp(0));

    cache.put(getSample().getId(), getSample());

    assertTrue(SupervisionStateTagEvaluator.isUncertain(stateTagService.getCache().get(getSample().getId())));
  }

  @Test
  public void refreshAndNotifyCurrentSupervisionStatus() {
    // Generates one supervision update event, we don't listen yet
    cache.put(getSample().getId(), getSample());

    final AtomicInteger eventCounter = new AtomicInteger(0);
    final CacheListener<SupervisionStateTag> paramListener = eq -> eventCounter.incrementAndGet();
    cache.getCacheListenerManager().registerListener(paramListener, CacheEvent.SUPERVISION_UPDATE);

    // Should generate exactly one event
    stateTagService.refresh(getSample().getId());

    cache.getCacheListenerManager().close();

    assertEquals(1, eventCounter.get());
  }
}
