package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;

public class SupervisionStateTagTest {

  private static SupervisionStateTag sample = new SupervisionStateTag(0L, 1L, "EQ", null, null);

  @Test(expected = NullPointerException.class)
  public void setNullStatus() {
    sample.setSupervision(null,"", new Timestamp(1L));
  }

  @Test(expected = NullPointerException.class)
  public void setNullDescription() {
    sample.setSupervision(SupervisionStatus.RUNNING,null, new Timestamp(1L));
  }

  @Test(expected = NullPointerException.class)
  public void setNullTimestamp() {
    sample.setSupervision(SupervisionStatus.RUNNING,"", null);
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
