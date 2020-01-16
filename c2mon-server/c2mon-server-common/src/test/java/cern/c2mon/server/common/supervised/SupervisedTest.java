package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;

public abstract class SupervisedTest<T extends Supervised> {

  protected T sample;

  protected abstract T generateSample();

  @Before
  public void init() {
    sample = generateSample();
  }

  @Test
  public void defaultState() {
    doAndExpect((a, b) -> {
    }, null, SupervisionStatus.DOWN);
  }

  @Test
  public void start() {
    doAndExpect((timestamp, msg) -> sample.start(timestamp), null, SupervisionStatus.STARTUP);
  }

  @Test
  public void stop() {
    doAndExpect((timestamp, msg) -> sample.stop(timestamp), null, SupervisionStatus.DOWN);
  }

  @Test
  public void suspend() {
    doAndExpect((timestamp, msg) -> sample.suspend(timestamp, msg), "", SupervisionStatus.DOWN);
  }

  @Test
  public void resume() {
    doAndExpect((timestamp, msg) -> sample.resume(timestamp, msg), "", SupervisionStatus.RUNNING);
  }

  private void doAndExpect(BiConsumer<Timestamp, String> action, String message, SupervisionStatus expected) {
    Timestamp timestamp = Timestamp.from(Instant.now());
    action.accept(timestamp, message);
    assertEquals(expected, sample.getSupervisionStatus());
    // Uninitialized objects don't have time or description
    if (sample.getStatusTime() == null)
      return;
    assertEquals(timestamp, sample.getStatusTime());
    if (message != null)
      assertEquals(message, sample.getStatusDescription());
  }
}
