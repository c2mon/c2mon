package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.cachepersistence.impl.ControlTagPersistenceDAOImpl;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.supervision.SupervisionStateTag;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author Alexandros Papageorgiou
 */
@Named
@Singleton
public class SupervisionStateTagPersistenceConfig extends AbstractPersistenceConfig<SupervisionStateTag> {

  @Inject
  public SupervisionStateTagPersistenceConfig(
    final C2monCache<SupervisionStateTag> StateTagCache,
    PersistenceMapper<DataTag> dataTagMapper) {

    super(StateTagCache, new ControlTagPersistenceDAOImpl<>(dataTagMapper, StateTagCache));
  }
}
