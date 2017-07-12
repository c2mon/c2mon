package cern.c2mon.server.jcacheref.prototype.alive;

import java.io.Serializable;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.common.alive.AliveTimer;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class AliveTimerManager implements EntryProcessor<Long, AliveTimer, Object>, Serializable {

  public static final int START = 0;
  public static final int STOP = 1;
  public static final int UPDATE = 2;
  public static final int HAS_EXPIRED = 3;

  @Override
  public Object process(MutableEntry<Long, AliveTimer> entry, Object... arguments) throws EntryProcessorException {
    if (entry.exists()) {
      if (arguments[0].equals(START)) {
        if (!entry.getValue().isActive()) {
          entry.setValue(start(entry.getValue()));
        }
      }

      if (arguments[0].equals(UPDATE)) {
        entry.setValue(update(entry.getValue()));
      }

      if (arguments[0].equals(STOP)) {
        if (entry.getValue().isActive()) {
          entry.setValue(stop(entry.getValue()));
        }
      }

      if (arguments[0].equals(HAS_EXPIRED)) {
        return hasExpired(entry.getValue());
      }
    }

    return null;
  }

  private AliveTimer start(AliveTimer aliveTimer) {
    if (log.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("start() : starting alive for ")
              .append(AliveTimer.ALIVE_TYPE_PROCESS + " ")
              .append(aliveTimer.getRelatedName())
              .append(".");
      log.debug(str.toString());
    }

    return changeLastUpdateTime(aliveTimer);
  }

  private AliveTimer update(AliveTimer aliveTimer) {
    if (log.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("Updated alive timer for ")
              .append(AliveTimer.ALIVE_TYPE_PROCESS + " ")
              .append(aliveTimer.getRelatedName())
              .append(".");
      log.debug(str.toString());
    }
    return changeLastUpdateTime(aliveTimer);
  }

  private AliveTimer changeLastUpdateTime(AliveTimer aliveTimer) {
    aliveTimer.setActive(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    return aliveTimer;
  }

  private AliveTimer stop(AliveTimer aliveTimer) {
    if (log.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("stop() : stopping alive for ")
              .append(aliveTimer.getAliveTypeDescription() + " ")
              .append(aliveTimer.getRelatedName())
              .append(".");
      log.debug(str.toString());
    }
    aliveTimer.setActive(false);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    return aliveTimer;
  }

  private Boolean hasExpired(AliveTimer aliveTimer) {
    return (System.currentTimeMillis() - aliveTimer.getLastUpdate() > aliveTimer.getAliveInterval() + aliveTimer.getAliveInterval() / 3);
  }
}