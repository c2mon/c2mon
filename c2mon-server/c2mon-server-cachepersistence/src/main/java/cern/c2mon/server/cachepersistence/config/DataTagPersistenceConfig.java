package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.datatag.DataTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class DataTagPersistenceConfig extends AbstractPersistenceConfig<DataTag> {

  @Inject
  public DataTagPersistenceConfig(final C2monCache<DataTag> dataTagCache,
                                  final DataTagMapper dataTagMapper) {
    super(dataTagCache, new CachePersistenceDAOImpl<>(dataTagMapper, dataTagCache));
  }
}
