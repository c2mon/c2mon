package cern.c2mon.cache.deviceclass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.common.device.DeviceClassCacheObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.common.device.DeviceClass;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
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
  protected void compareLists(List<DeviceClass> mapperList, Map<Long, DeviceClass> cacheList) throws ClassNotFoundException {
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
  protected C2monCache<DeviceClass> getCache() {
    return deviceClassCacheRef;
  }
}
