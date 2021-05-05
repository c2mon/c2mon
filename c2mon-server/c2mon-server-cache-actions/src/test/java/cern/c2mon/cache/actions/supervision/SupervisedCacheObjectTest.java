package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.actions.state.SupervisionStateTagService;
import cern.c2mon.cache.api.flow.CacheUpdateFlow;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.impl.configuration.IgniteModule;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.cache.test.factory.SupervisionStateTagFactory;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static cern.c2mon.shared.common.CacheEvent.*;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  CacheLoadingModuleRef.class,
  IgniteModule.class,
  CacheActionsModuleRef.class
})
public class SupervisedCacheObjectTest {

  private static final SupervisionStateTag sample = new SupervisionStateTagFactory().sampleBase();

  @Inject
  private SupervisionStateTagService stateTagService;

  private CacheUpdateFlow<SupervisionStateTag> cacheFlow;

  @Before
  public void before() {
    cacheFlow = stateTagService.getCache().getCacheUpdateFlow();
  }

  @Test
  public void firstInsertionEvents() {
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(INSERTED));
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(SUPERVISION_UPDATE));
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(SUPERVISION_CHANGE));
  }

  @Test
  public void supervisionChangeCreatesEvent() {
    fires(clone -> clone.start(new Timestamp(0)), SUPERVISION_CHANGE, SUPERVISION_UPDATE);
    fires(clone -> clone.stop(new Timestamp(0)), SUPERVISION_UPDATE);
  }

  @Test
  public void supervisionChangeNotFiredWhenEqual() {
    fires(clone -> {}, SUPERVISION_UPDATE);
  }

  @Test
  public void testStatusChanges() {
    // There is no clear definition of these groups besides the implementation of supervised.isRunning
    // The goal is that when that implementation changes, this test should fail and will have to be
    // updated to demonstrate the new functionality.
    List<SupervisionStatus> activeStatuses = asList(STARTUP, RUNNING, RUNNING_LOCAL);
    List<SupervisionStatus> inactiveStatuses = asList(DOWN, STOPPED, UNCERTAIN);
    // The sets are defined statically, so that adding a new status will also cause this test to fail.
    // The developer visiting here must make sure that the supervised.isRunning method is updated to match
    // the new expectations.  Then, proceed to update the lists of statuses as required

    for (SupervisionStatus active : activeStatuses) {
      for (SupervisionStatus other : SupervisionStatus.values()) {
        if (activeStatuses.contains(other))
          changeFires(active, other, SUPERVISION_UPDATE);
        else if (inactiveStatuses.contains(other))
          changeFires(active, other, SUPERVISION_UPDATE, SUPERVISION_CHANGE);
        else
          fail();
      }
    }

    for (SupervisionStatus inactive : inactiveStatuses) {
      for (SupervisionStatus other : SupervisionStatus.values()) {
        if (inactiveStatuses.contains(other))
          changeFires(inactive, other, SUPERVISION_UPDATE);
        else if (activeStatuses.contains(other))
          changeFires(inactive, other, SUPERVISION_UPDATE, SUPERVISION_CHANGE);
        else
          fail();
      }
    }

    for (SupervisionStatus status : SupervisionStatus.values())
      changeFires(status, status, SUPERVISION_UPDATE);
  }

  public void changeFires(SupervisionStatus initialStatus, SupervisionStatus finalStatus, CacheEvent... events) {
    sample.setSupervisionStatus(initialStatus);
    fires(clone -> clone.setSupervisionStatus(finalStatus), events);
  }

  public void fires(Consumer<SupervisionStateTag> mutater, CacheEvent... events) {
    SupervisionStateTag clone = sample.clone();
    mutater.accept(clone);
    Set<CacheEvent> results = cacheFlow.postInsertEvents(clone, sample);

    // Running it twice should produce same results
    assertEquals(results, cacheFlow.postInsertEvents(clone, sample));

    // All events are contained
    for (CacheEvent event : events)
      assertTrue(results.contains(event));
    // and only those events
    assertEquals(events.length, results.size());
  }
}
