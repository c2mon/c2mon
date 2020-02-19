package cern.c2mon.server.supervision.alive;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.supervision.SupervisionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled and controlled by {@link AliveTagChecker}, this task
 * will execute every few seconds and check how many processes
 * are still alive.
 *
 * If there is are more than {@link AliveTagCheckerTask#WARNING_THRESHOLD}
 * processes down, SMS messages will be sent out.
 *
 * @see AliveTagChecker
 * @author Alexandros Papageorgiou, Mark Brightwell
 */
@Named
public class AliveTagCheckerTask extends TimerTask {

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AliveTagChecker.class);

  /**
   * SMS logger for warnings.
   */
  private static final Logger SMS_LOGGER = LoggerFactory.getLogger("AdminSmsLogger");

  /**
   * Reference to alive timer facade.
   */
  private final AliveTagService aliveTimerService;

  /**
   * Reference to the SupervisionManager bean.
   */
  private final SupervisionManager supervisionManager;

  /**
   * Threshold of DAQ/Equipment/SubEqu. down when warning is sent to admin.
   */
  private static final short WARNING_THRESHOLD = 50;

  /**
   * Warning has been sent.
   */
  private boolean alarmActive = false;

  /**
   * Count down to alarm switch off.
   */
  private AtomicInteger warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);

  /**
   * 10mins, because we decrement this once per scan, so time value is 60 * SCAN_INTERVAL
   */
  private static final int SWITCH_OFF_COUNTDOWN = 60;

  /**
   * When the last check was performed
   */
  private static long lastCheck = 0L;

  /**
   * @param aliveTimerService the alive timer facade bean
   * @param supervisionManager the supervision manager bean
   */
  @Inject
  public AliveTagCheckerTask(AliveTagService aliveTimerService, SupervisionManager supervisionManager) {
    this.aliveTimerService = aliveTimerService;
    this.supervisionManager = supervisionManager;
  }

  /**
   * Run method of the AliveTimerManager thread.
   */
  @Override
  public void run() {
    if (System.currentTimeMillis() - lastCheck < 9000) {
      LOGGER.debug("Skipping alive check as already performed.");
    }

    LOGGER.debug("run() : checking alive timers ... ");

    try {
      long aliveDownCount = calculateAliveDownAndNotify();

      sendNotificationsIfRequired(aliveDownCount);
    } catch (Exception e) {
      LOGGER.error("Unexpected exception when checking the alive timers", e);
    }
    lastCheck = System.currentTimeMillis();

    LOGGER.debug("run() : finished checking alive timers ... ");
  }

  private long calculateAliveDownAndNotify() {
    return aliveTimerService.getCache().getKeys()
      // Potentially optimizable by switching to single stream
      .parallelStream()
      .filter(this::isExpired)
      .peek(supervisionManager::onAliveTimerExpiration)
      .count();
  }

  private boolean isExpired(long id) {
    boolean aliveExpired = false;

    if (aliveTimerService.getCache().containsKey(id)) {
      try {
        if (aliveTimerService.hasExpired(id)) {
          aliveTimerService.stop(id, System.currentTimeMillis());
          aliveExpired = true;
        }
      } catch (CacheElementNotFoundException notFound) {
        LOGGER.warn("Failed to locate alive timer in cache on expiration check (may happen exceptionally if just removed).", notFound);
      }
    } else {
      aliveExpired = true;
    }

    return aliveExpired;
  }

  private void sendNotificationsIfRequired(long aliveDownCount) {
    if (!alarmActive && aliveDownCount > WARNING_THRESHOLD) {
      alarmActive = true;
      LOGGER.warn("Over {} DAQ/Equipment are currently down.", WARNING_THRESHOLD);
      SMS_LOGGER.warn("Over {} DAQ/Equipment are currently down.", WARNING_THRESHOLD);
    } else if (alarmActive && warningSwitchOffCountDown.decrementAndGet() == 0) {
      SMS_LOGGER.warn("DAQ/Equipment status back to normal ({} detected as down)", aliveDownCount);
      alarmActive = false;
      warningSwitchOffCountDown = new AtomicInteger(SWITCH_OFF_COUNTDOWN);
    }
  }
}
