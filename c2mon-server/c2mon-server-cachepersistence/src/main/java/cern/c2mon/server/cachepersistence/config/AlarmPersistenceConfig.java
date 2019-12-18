package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.alarm.Alarm;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class AlarmPersistenceConfig extends AbstractPersistenceConfig<Alarm> {

  @Inject
  public AlarmPersistenceConfig(final C2monCache<Alarm> alarmCache, final AlarmMapper alarmMapper) {
    super(alarmCache, new CachePersistenceDAOImpl<>(alarmMapper, alarmCache));
  }
}