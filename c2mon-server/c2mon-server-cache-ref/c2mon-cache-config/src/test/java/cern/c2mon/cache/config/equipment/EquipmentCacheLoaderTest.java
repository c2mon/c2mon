package cern.c2mon.cache.config.equipment;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class EquipmentCacheLoaderTest extends AbstractCacheLoaderTest<Equipment> {

  @Autowired
  private C2monCache<Equipment> equipmentCacheRef;

  @Autowired
  private EquipmentMapper equipmentMapper;

  @Override
  protected LoaderMapper<Equipment> getMapper() {
    return equipmentMapper;
  }

  @Override
  protected void customCompare(List<Equipment> mapperList, Map<Long, Equipment> cacheList) {
    for (Equipment anEquipmentList : mapperList) {
      EquipmentCacheObject currentEquipment = (EquipmentCacheObject) anEquipmentList;
      //only compares one field so far
      assertEquals("Cached Equipment should have the same name as in DB",
        currentEquipment.getName(), ((cacheList.get(currentEquipment.getId())).getName()));
    }
  }

  @Override
  protected Equipment getSample() {
    return new EquipmentCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 150L;
  }

  @Override
  protected C2monCache<Equipment> getCache() {
    return equipmentCacheRef;
  }
}
