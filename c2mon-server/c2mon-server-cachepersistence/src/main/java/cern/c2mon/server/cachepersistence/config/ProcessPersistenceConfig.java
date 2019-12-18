package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.process.Process;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class ProcessPersistenceConfig extends AbstractPersistenceConfig<Process> {

  @Inject
  public ProcessPersistenceConfig(final C2monCache<Process> processCache, final ProcessMapper processMapper) {
    super(processCache, new CachePersistenceDAOImpl<>(processMapper, processCache));
  }
}
