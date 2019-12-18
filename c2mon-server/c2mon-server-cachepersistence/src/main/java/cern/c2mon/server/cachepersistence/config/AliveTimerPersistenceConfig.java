package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTimer;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class AliveTimerPersistenceConfig extends AbstractPersistenceConfig<AliveTimer> {

  // TODO (Alex) Complete this when control tag refactoring is through

  @Inject
  public AliveTimerPersistenceConfig(final C2monCache<AliveTimer> aliveTimerCache) {
    super(aliveTimerCache, null);
  }
}
