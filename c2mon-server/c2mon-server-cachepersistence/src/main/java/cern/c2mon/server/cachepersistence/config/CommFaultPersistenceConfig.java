package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.cachepersistence.impl.ControlTagPersistenceDAOImpl;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.datatag.DataTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Alexandros Papageorgiou
 */
@Named
@Singleton
public class CommFaultPersistenceConfig extends AbstractPersistenceConfig<CommFaultTag> {

  @Inject
  public CommFaultPersistenceConfig(
    final C2monCache<CommFaultTag> commFaultTagCache,
    PersistenceMapper<DataTag> dataTagMapper) {

    super(commFaultTagCache, new ControlTagPersistenceDAOImpl<>(dataTagMapper, commFaultTagCache));
  }
}
