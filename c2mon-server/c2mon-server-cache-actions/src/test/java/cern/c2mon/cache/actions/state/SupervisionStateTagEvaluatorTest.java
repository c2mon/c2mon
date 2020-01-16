package cern.c2mon.cache.actions.state;

import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator.isRunning;
import static cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator.isUncertain;
import static cern.c2mon.shared.common.supervision.SupervisionStatus.*;
import static org.junit.Assert.*;

public class SupervisionStateTagEvaluatorTest {

  private final SupervisionStateTag sample = new SupervisionStateTag(0L, 1L, "EQ", null, null);
  

  @Test
  public void isUncertainTest() {
    assertFalse(isUncertain(sample));

    for (SupervisionStatus status : values()) {
      sample.setSupervision(status, "", Timestamp.from(Instant.now()));
      assertEquals(status == UNCERTAIN, isUncertain(sample));
    }
  }

  @Test
  public void isRunningTest() {
    // Default
    assertFalse(isRunning(sample));

    sample.setSupervision(STARTUP, "", Timestamp.from(Instant.now()));
    assertTrue(isRunning(sample));

    sample.setSupervision(RUNNING_LOCAL, "", Timestamp.from(Instant.now()));
    assertTrue(isRunning(sample));

    sample.setSupervision(RUNNING, "", Timestamp.from(Instant.now()));
    assertTrue(isRunning(sample));

    sample.setSupervision(STOPPED, "", Timestamp.from(Instant.now()));
    assertFalse(isRunning(sample));

    sample.setSupervision(DOWN, "", Timestamp.from(Instant.now()));
    assertFalse(isRunning(sample));

    sample.setSupervision(UNCERTAIN, "", Timestamp.from(Instant.now()));
    assertFalse(isRunning(sample));
  }
}
