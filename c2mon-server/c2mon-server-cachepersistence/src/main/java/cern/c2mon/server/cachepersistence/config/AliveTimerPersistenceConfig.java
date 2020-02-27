package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.cachepersistence.impl.ControlTagPersistenceDAOImpl;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.datatag.DataTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Alexandros Papageorgiou
 */
@Named
@Singleton
public class AliveTimerPersistenceConfig extends AbstractPersistenceConfig<AliveTag> {

  @Inject
  public AliveTimerPersistenceConfig(
    final C2monCache<AliveTag> aliveTimerCache,
    PersistenceMapper<DataTag> dataTagMapper) {

    super(aliveTimerCache, new ControlTagPersistenceDAOImpl<>(dataTagMapper, aliveTimerCache));
  }
}
