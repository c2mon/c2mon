package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.rule.RuleTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class RuleTagPersistenceConfig extends AbstractPersistenceConfig<RuleTag> {

  @Inject
  public RuleTagPersistenceConfig(final C2monCache<RuleTag> ruleTagCache,
                                       final RuleTagMapper ruleTagMapper) {
    super(ruleTagCache, new CachePersistenceDAOImpl<>(ruleTagMapper, ruleTagCache));
  }
}
