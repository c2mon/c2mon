package cern.c2mon.server.cachepersistence.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cachepersistence.impl.CachePersistenceDAOImpl;
import cern.c2mon.server.common.equipment.Equipment;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Alexandros Papageorgiou
 */
@Service
public class EquipmentPersistenceConfig extends AbstractPersistenceConfig<Equipment> {

  @Inject
  public EquipmentPersistenceConfig(final C2monCache<Equipment> equipmentCache,
                                  final EquipmentMapper equipmentMapper) {
    super(equipmentCache, new CachePersistenceDAOImpl<>(equipmentMapper, equipmentCache));
  }
}