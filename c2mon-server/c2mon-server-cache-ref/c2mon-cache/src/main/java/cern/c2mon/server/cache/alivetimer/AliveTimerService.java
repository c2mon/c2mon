package cern.c2mon.server.cache.alivetimer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTimer;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class AliveTimerService {

  C2monCache<Long, AliveTimer> aliveTimerCacheRef;

  @Autowired
  public AliveTimerService(C2monCache<Long, AliveTimer> aliveTimerCacheRef) {
    this.aliveTimerCacheRef = aliveTimerCacheRef;

    log.info("ALIVE TIMER SERVICE WAS CREATED SUCCESSFULLY");
  }
}
