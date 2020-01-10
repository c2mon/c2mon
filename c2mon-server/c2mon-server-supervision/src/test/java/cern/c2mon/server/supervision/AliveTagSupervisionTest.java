package cern.c2mon.server.supervision;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * Tests for supervision logic related to alive tags and caching
 *
 * @author Alexandros Papageorgiou
 */
public class AliveTagSupervisionTest extends SupervisionCacheTest {

  @Inject
  private C2monCache<AliveTag> aliveTimerCache;

  @Inject
  private C2monCache<Process> processCache;

  @Inject
  private ProcessService processService;

  @Before
  public void initialStatusIsCorrect() {
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    assertNotNull(aliveTimer);
    assertEquals(0, aliveTimer.getLastUpdate());

    Process process = processCache.get(aliveTimer.getRelatedId());
    assertEquals(SupervisionConstants.SupervisionStatus.DOWN, process.getSupervisionStatus());
    assertNull(process.getStatusTime());
    assertNull(process.getStatusDescription());
  }

  /**
   * Tests a process alive tag is correctly processed by the SupervisionManager
   * (alive timer updated; supervision listeners notified, etc).
   *
   * Process is down at start of test, then alive is received.
   *
   * @throws InterruptedException
   */
  @Test(timeout = 1000)
  @DirtiesContext
  public void testProcessAliveTag() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    supervisionNotifier.registerAsListener(supervisionEvent -> latch.countDown());

    long updateTime = System.currentTimeMillis();
    SourceDataTagValue aliveTag = createSampleAliveTag();
    aliveTag.setTimestamp(new Timestamp(updateTime));
    //process control tag
    supervisionManager.processControlTag(aliveTag);

    //check alive is updated
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    assertTrue(aliveTimer.getLastUpdate() > System.currentTimeMillis() - 10000); //account for non-synchronized

    //check process status is changed
    Process process = processCache.get(aliveTimer.getRelatedId());
    assertEquals(SupervisionConstants.SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp processTime = process.getStatusTime();
    assertTrue(processTime.after(new Timestamp(updateTime - 1)));
    assertNotNull(process.getStatusDescription());

    latch.await(); //wait for notification on listener thread
  }

  /**
   * Alives older than 2 minutes are rejected.
   */
  @Test
  @DirtiesContext
  public void testRejectOldAlive() {
    //check alive timer is defined & set last update
    AliveTag aliveTimer = aliveTimerCache.get(1221L);
    aliveTimer.setLastUpdate(System.currentTimeMillis()-1000);
    long aliveTime = aliveTimer.getLastUpdate();
    aliveTimerCache.put(aliveTimer.getId(), aliveTimer);

    //send alive 2 minutes old (should be rejected)
    SourceDataTagValue value = createSampleAliveTag();
    value.setDaqTimestamp(new Timestamp(System.currentTimeMillis() - 130000));

    supervisionManager.processControlTag(value);

    //no update
    assertEquals(aliveTime, aliveTimer.getLastUpdate());
  }

  /**
   * Checks a new process alive has no effect on the state tag or on the process
   * status, since it is already up as running. Only the alive is updated.
   */
  @Test
  @DirtiesContext
  public void testProcessAliveNoEffect() {
    AliveTag aliveTimer = aliveTimerCache.get(1221L);

    // Start the process
    processService.start(aliveTimer.getRelatedId(), new Timestamp(0));
    // Resume so that the status goes to RUNNING
    Process process = processService.resume(aliveTimer.getRelatedId(), new Timestamp(System.currentTimeMillis()), "");
    assertEquals(SupervisionConstants.SupervisionStatus.RUNNING, process.getSupervisionStatus());
    Timestamp originalProcessTime = process.getStatusTime();
    assertNotNull(originalProcessTime);

    long startTimer = aliveTimerCache.get(1221L).getLastUpdate();
    // Parse the control tag
    SourceDataTagValue newerAliveTag = createSampleAliveTag();
    long updatedTimer = startTimer + 1;
    newerAliveTag.setDaqTimestamp(new Timestamp(updatedTimer));
    supervisionManager.processControlTag(newerAliveTag);

    // check alive is updated
    assertTrue(updatedTimer <= aliveTimerCache.get(1221L).getLastUpdate());

    //check process status is not changed & time also
    assertEquals(SupervisionConstants.SupervisionStatus.RUNNING, process.getSupervisionStatus());
    assertEquals(originalProcessTime, process.getStatusTime());
  }

  private SourceDataTagValue createSampleAliveTag() {
    return new SourceDataTagValue(1221L,
      "test alive",
      true,
      0L,
      new SourceDataTagQuality(),
      new Timestamp(System.currentTimeMillis()),
      4,
      false,
      "description",
      10000);
  }
}
