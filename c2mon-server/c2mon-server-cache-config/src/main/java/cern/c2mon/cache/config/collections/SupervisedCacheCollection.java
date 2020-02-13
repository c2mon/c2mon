package cern.c2mon.cache.config.collections;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.set.CacheCollection;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.supervision.Supervised;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Aggregates the [Equipment, SubEquipment, Process] caches
 *
 * @author Alexandros Papageorgiou Koufidis
 */
@Named
@Singleton
public class SupervisedCacheCollection extends CacheCollection<Supervised> {

  @Inject
  public SupervisedCacheCollection(final C2monCache<Process> processCache,
                                   final C2monCache<Equipment> equipmentCache,
                                   final C2monCache<SubEquipment> subEquipmentCache) {
    super(processCache, equipmentCache, subEquipmentCache);
  }
}
