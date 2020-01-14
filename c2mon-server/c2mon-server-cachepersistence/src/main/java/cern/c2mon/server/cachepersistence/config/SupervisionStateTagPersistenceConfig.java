package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class SupervisionStateTagPersistenceConfig extends AbstractPersistenceConfig<SupervisionStateTag> {

  // TODO (Alex) Complete this when control tag refactoring is through

  @Inject
  public SupervisionStateTagPersistenceConfig(final C2monCache<SupervisionStateTag> supervisionStateTagC2monCache) {
    super(supervisionStateTagC2monCache, null);
  }
}
