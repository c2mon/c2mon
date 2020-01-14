package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

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

  @Test(expected = NullPointerException.class)
  public void setNullStatus() {
    sample.setSupervisionStatus(null);
  }

  @Test(expected = NullPointerException.class)
  public void setNullTimestamp() {
    sample.setCacheTimestamp(null);
  }

  @Test(expected = NullPointerException.class)
  public void setNullDescription() {
    sample.setStatusDescription(null);
  }

  @Test
  public void isRunning() {
    // Default
    assertFalse(sample.isRunning());

    sample.setSupervision(SupervisionStatus.STARTUP, "", Timestamp.from(Instant.now()));
    assertTrue(sample.isRunning());

    sample.setSupervision(SupervisionStatus.RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    assertTrue(sample.isRunning());

    sample.setSupervision(SupervisionStatus.RUNNING, "", Timestamp.from(Instant.now()));
    assertTrue(sample.isRunning());

    sample.setSupervision(SupervisionStatus.STOPPED, "", Timestamp.from(Instant.now()));
    assertFalse(sample.isRunning());

    sample.setSupervision(SupervisionStatus.DOWN, "", Timestamp.from(Instant.now()));
    assertFalse(sample.isRunning());

    sample.setSupervision(SupervisionStatus.UNCERTAIN, "", Timestamp.from(Instant.now()));
    assertFalse(sample.isRunning());
  }

  @Test
  public void isUncertain() {
    assertFalse(sample.isUncertain());

    for (SupervisionStatus status : SupervisionStatus.values()) {
      sample.setSupervision(status, "", Timestamp.from(Instant.now()));
      assertEquals(status == SupervisionStatus.UNCERTAIN ,sample.isUncertain());
    }
  }

  @Test
  public void hasNoEvents() {
    assertTrue(sample.hasNoEvents());

    for (SupervisionStatus status : SupervisionStatus.values()) {
      sample.setSupervision(status, "", Timestamp.from(Instant.now()));
      assertFalse(sample.hasNoEvents());
    }
  }

  private void doAndExpect(BiConsumer<Timestamp, String> action, String message, SupervisionStatus expected) {
    Timestamp timestamp = Timestamp.from(Instant.now());
    action.accept(timestamp, message);
    assertEquals(expected, sample.getSupervisionStatus());
    // Uninitialized objects don't have time or description
    if (sample.hasNoEvents())
      return;
    assertEquals(timestamp, sample.getStatusTime());
    if (message != null)
      assertEquals(message, sample.getStatusDescription());
  }
}
