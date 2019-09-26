package cern.c2mon.cache.subequipment;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class SubequipmentCacheLoaderTest extends AbstractCacheLoaderTest<SubEquipment> {

  @Autowired
  private C2monCache<SubEquipment> subEquipmentCacheRef;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Override
  protected LoaderMapper<SubEquipment> getMapper() {
    return subEquipmentMapper;
  }

  @Override
  protected void customCompare(List<SubEquipment> mapperList, Map<Long, SubEquipment> cacheList) throws ClassNotFoundException {
    for (SubEquipment aSubEquipmentList : mapperList) {
      SubEquipmentCacheObject currentSubEquipment = (SubEquipmentCacheObject) aSubEquipmentList;
      //only compares one field so far
      assertEquals("Cached SubEquipment should have the same name as in DB",
        currentSubEquipment.getName(), ((cacheList.get(currentSubEquipment.getId())).getName()));
    }
  }

  @Override
  protected SubEquipment getSample() {
    return new SubEquipmentCacheObject();
  }

  @Override
  protected Long getExistingKey() {
    return 250L;
  }

  @Override
  protected C2monCache<SubEquipment> getCache() {
    return subEquipmentCacheRef;
  }
}
