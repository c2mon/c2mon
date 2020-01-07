package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class AliveTimerPersistenceConfig extends AbstractPersistenceConfig<AliveTag> {

  // TODO (Alex) Complete this when control tag refactoring is through

  @Inject
  public AliveTimerPersistenceConfig(final C2monCache<AliveTag> aliveTimerCache) {
    super(aliveTimerCache, null);
  }
}
