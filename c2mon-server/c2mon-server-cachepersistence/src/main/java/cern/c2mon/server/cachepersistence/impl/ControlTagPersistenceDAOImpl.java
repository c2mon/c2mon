package cern.c2mon.server.cachepersistence.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.PersistenceMapper;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;

/**
 * Override of the standard cache persistence DAO to allow using the datatag mapper
 * for inserting control tags
 */
public class ControlTagPersistenceDAOImpl<CONTROL extends ControlTag> extends CachePersistenceDAOImpl<CONTROL> {

  /**
   * Constructor required cache and the persistence bean for this cache.
   *
   * @param dataTagMapper the mapper bean for this cache
   * @param cache             the cache that is being persisted
   */
  public ControlTagPersistenceDAOImpl(PersistenceMapper<DataTag> dataTagMapper, C2monCache<CONTROL> cache) {
    super(dataTagMapper, cache);
  }

  @Override
  protected DataTag adaptCacheObject(CONTROL cacheObject) {
    return ControlTag.convertToDataTag(cacheObject);
  }
}
