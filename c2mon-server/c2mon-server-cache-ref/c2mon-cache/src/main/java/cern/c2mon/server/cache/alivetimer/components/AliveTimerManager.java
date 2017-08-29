package cern.c2mon.server.cache.alivetimer.components;

import java.io.Serializable;
import java.util.EnumMap;

import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;

import lombok.extern.slf4j.Slf4j;

import cern.c2mon.cache.api.SerializableFunction;
import cern.c2mon.server.common.alive.AliveTimer;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class AliveTimerManager implements EntryProcessor<Long, AliveTimer, Object>, Serializable {

  public final EnumMap<AliveTimerOperation, SerializableFunction<AliveTimer, AliveTimer>> operations = new EnumMap<>(AliveTimerOperation.class);

  public AliveTimerManager() {
    operations.put(AliveTimerOperation.START, this::start);
    operations.put(AliveTimerOperation.UPDATE, this::update);
    operations.put(AliveTimerOperation.STOP, this::stop);
  }

  @Override
  public Object process(MutableEntry<Long, AliveTimer> entry, Object... arguments) throws EntryProcessorException {
    if (AliveTimerOperation.valueOf(arguments[0].toString()).equals(AliveTimerOperation.HAS_EXPIRED)) {
      return hasExpired(entry.getValue());
    }
    else {
      entry.setValue(operations.get(AliveTimerOperation.valueOf(arguments[0].toString())).apply(entry.getValue()));
    }

    return null;
  }

  private AliveTimer start(AliveTimer aliveTimer) {
    log.debug("start() : starting alive for {} {}.", AliveTimer.ALIVE_TYPE_PROCESS, aliveTimer.getRelatedName());

    return changeLastUpdateTime(aliveTimer);
  }

  private AliveTimer update(AliveTimer aliveTimer) {
    log.debug("Updated alive timer for {} {}.", AliveTimer.ALIVE_TYPE_PROCESS, aliveTimer.getRelatedName());

    return changeLastUpdateTime(aliveTimer);
  }

  private AliveTimer changeLastUpdateTime(AliveTimer aliveTimer) {
    aliveTimer.setActive(true);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    return aliveTimer;
  }

  private AliveTimer stop(AliveTimer aliveTimer) {
    log.debug("stop() : stopping alive for {} {}.", aliveTimer.getAliveTypeDescription(), aliveTimer.getRelatedName());

    aliveTimer.setActive(false);
    aliveTimer.setLastUpdate(System.currentTimeMillis());

    return aliveTimer;
  }

  private Boolean hasExpired(AliveTimer aliveTimer) {
    return (System.currentTimeMillis() - aliveTimer.getLastUpdate() > aliveTimer.getAliveInterval() + aliveTimer.getAliveInterval() / 3);
  }
}

