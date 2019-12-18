package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.subequipment.SubEquipment;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class SubEquipmentPersistenceConfig extends AbstractPersistenceConfig<SubEquipment> {

  @Inject
  public SubEquipmentPersistenceConfig(final C2monCache<SubEquipment> subEquipmentCache,
                                       final SubEquipmentMapper subEquipmentMapper) {
    super(subEquipmentCache, new CachePersistenceDAOImpl<>(subEquipmentMapper, subEquipmentCache));
  }
}
