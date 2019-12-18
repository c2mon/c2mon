package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.commfault.CommFaultTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class CommFaultPersistenceConfig extends AbstractPersistenceConfig<CommFaultTag> {

  // TODO (Alex) Complete this when control tag refactoring is through

  @Inject
  public CommFaultPersistenceConfig(final C2monCache<CommFaultTag> commFaultTagCache) {
    super(commFaultTagCache, null);
  }
}
