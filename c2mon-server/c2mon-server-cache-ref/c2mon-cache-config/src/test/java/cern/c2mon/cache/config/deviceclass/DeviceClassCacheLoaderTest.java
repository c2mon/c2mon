package cern.c2mon.cache.config.deviceclass;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.device.DeviceClass;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class DeviceClassCacheLoaderTest extends AbstractCacheLoaderTest<DeviceClass> {

  @Autowired
  private C2monCache<DeviceClass> deviceClassCacheRef;

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Override
  protected LoaderMapper<DeviceClass> getMapper() {
    return deviceClassMapper;
  }

  @Override
  protected void customCompare(List<DeviceClass> mapperList, Map<Long, DeviceClass> cacheList) throws ClassNotFoundException {
    for (DeviceClass currentDeviceClass : mapperList) {
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached DataTag should have the same name as in DB",
        currentDeviceClass.getName(), (cacheList.get(currentDeviceClass.getId()).getName()));
    }
  }

  @Override
  protected DeviceClass getSample() {
    return new DeviceClassCacheObject(0L);
  }

  @Override
  protected Long getExistingKey() {
    return 400L;
  }

  @Override
  protected C2monCache<DeviceClass> initCache() {
    return deviceClassCacheRef;
  }
}
