package cern.c2mon.cache;

import cern.c2mon.cache.actions.supervision.AbstractSupervisedC2monCacheFlow;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus.*;
import static org.junit.Assert.*;

public class AbstractSupervisedCacheObjectTest {

  private Supervised sample = new EquipmentCacheObject();
  private AbstractSupervisedC2monCacheFlow cacheFlow = new AbstractSupervisedC2monCacheFlow() {
  };

  @Test
  public void firstInsertionEvents() {
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(CacheEvent.INSERTED));
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(CacheEvent.SUPERVISION_UPDATE));
    assertTrue(cacheFlow.postInsertEvents(null, sample).contains(CacheEvent.SUPERVISION_CHANGE));
  }

  @Test
  public void supervisionChangeCreatesEvent() throws CloneNotSupportedException {
    fires(clone -> clone.start(new Timestamp(0)), CacheEvent.SUPERVISION_CHANGE, CacheEvent.SUPERVISION_UPDATE);

    fires(clone -> clone.stop(new Timestamp(0)), CacheEvent.SUPERVISION_UPDATE);
  }

  @Test
  public void supervisionChangeNotFiredWhenEqual() throws CloneNotSupportedException {
    fires(clone -> {
    }, CacheEvent.SUPERVISION_UPDATE);
  }

  @Test
  public void testStatusChanges() throws CloneNotSupportedException {
    // There is no clear definition of these groups besides the implementation of supervised.isRunning
    // The goal is that when that implementation changes, this test should fail and will have to be
    // updated to demonstrate the new functionality.
    List<SupervisionConstants.SupervisionStatus> activeStatuses = Arrays.asList(STARTUP, RUNNING, RUNNING_LOCAL);
    List<SupervisionConstants.SupervisionStatus> inactiveStatuses = Arrays.asList(DOWN, STOPPED, UNCERTAIN);
    // The sets are defined statically, so that adding a new status will also cause this test to fail.
    // The developer visiting here must make sure that the supervised.isRunning method is updated to match
    // the new expectations.  Then, proceed to update the lists of statuses as required

    for (SupervisionConstants.SupervisionStatus active : activeStatuses) {
      for (SupervisionConstants.SupervisionStatus other : SupervisionConstants.SupervisionStatus.values()) {
        if (activeStatuses.contains(other))
          changeFires(active, other, CacheEvent.SUPERVISION_UPDATE);
        else if (inactiveStatuses.contains(other))
          changeFires(active, other, CacheEvent.SUPERVISION_UPDATE, CacheEvent.SUPERVISION_CHANGE);
        else
          fail();
      }
    }

    for (SupervisionConstants.SupervisionStatus inactive : inactiveStatuses) {
      for (SupervisionConstants.SupervisionStatus other : SupervisionConstants.SupervisionStatus.values()) {
        if (inactiveStatuses.contains(other))
          changeFires(inactive, other, CacheEvent.SUPERVISION_UPDATE);
        else if (activeStatuses.contains(other))
          changeFires(inactive, other, CacheEvent.SUPERVISION_UPDATE, CacheEvent.SUPERVISION_CHANGE);
        else
          fail();
      }
    }

    for (SupervisionConstants.SupervisionStatus status : SupervisionConstants.SupervisionStatus.values())
      changeFires(status, status, CacheEvent.SUPERVISION_UPDATE);
  }

  public void changeFires(SupervisionConstants.SupervisionStatus initialStatus, SupervisionConstants.SupervisionStatus finalStatus,
                          CacheEvent... events) throws CloneNotSupportedException {
    sample.setSupervisionStatus(initialStatus);
    fires(clone -> clone.setSupervisionStatus(finalStatus), events);
  }

  public void fires(Consumer<Supervised> mutater, CacheEvent... events) throws CloneNotSupportedException {
    Supervised clone = (Supervised) sample.clone();
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
