package cern.c2mon.cache.deviceclass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.common.device.DeviceClass;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class DeviceClassCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCacheBase<DeviceClass> deviceClassCacheRef;

  @Autowired
  private DeviceClassMapper deviceClassMapper;

  @Before
  public void init() {
    deviceClassCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("DataTag Cache should not be null", deviceClassCacheRef);

    List<DeviceClass> deviceClassList = deviceClassMapper.getAll();

    Set<Long> keySet = deviceClassList.stream().map(DeviceClass::getId).collect(Collectors.toSet());
    assertTrue("List of DeviceClass tags should not be empty", deviceClassList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", deviceClassList.size(), deviceClassCacheRef.getKeys().size());
    // Compare all the objects from the cache and buffer
    for (DeviceClass currentDeviceClass : deviceClassList) {
      // Equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached DataTag should have the same name as in DB",
              currentDeviceClass.getName(), (deviceClassCacheRef.get(currentDeviceClass.getId()).getName()));
    }
  }
}
