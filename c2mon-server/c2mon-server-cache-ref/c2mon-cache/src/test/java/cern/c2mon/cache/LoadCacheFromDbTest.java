package cern.c2mon.cache;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.equipment.Equipment;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CacheModuleRef.class,
        CommonModule.class
}, loader = AnnotationConfigContextLoader.class)
public class LoadCacheFromDbTest {

  @Autowired
  private C2monCache<Long, Equipment> equipmentCacheRef;

  @Test
  @Ignore
  public void loadCacheFromDb() {
    long id = 1;
//    equipmentCacheRef.loadFromDb(id);

    assertNotNull("Cache should have 1 object", equipmentCacheRef.get(id));
  }

  @Test
  public void preloadCacheFromDb() {

  }
}
