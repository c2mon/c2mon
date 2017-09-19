package cern.c2mon.cache.process;

import java.util.List;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.CacheModuleRef;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.shared.common.Cacheable;

import static org.junit.Assert.assertNotNull;

/**
 * This is an integration test for loading cache from DB using embedded cache
 *
 * @author Szymon Halastra
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CommonModule.class,
        CacheModuleRef.class,
        CacheDbAccessModule.class,
        CacheLoaderModuleRef.class,
})
public class ProcessCacheLoaderTest {

  @Autowired
  private DataSource cacheDataSource;

  @Autowired
  private ProcessMapper processMapper;

  @Autowired
  private C2monCache<Long, Process> processCacheRef;

  @Before
  public void init() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
            new ClassPathResource("sql/cache-data-remove.sql"),
            new ClassPathResource("sql/cache-data-insert.sql")
    );
    DatabasePopulatorUtils.execute(populator, cacheDataSource);
  }

  @Test
  @Ignore
  public void loadCacheFromDb() {
    //TODO: 1. get test data from sql
    assertNotNull("Checks if processCache is not null", processCacheRef);
    //TODO: 2. use mapper for mapping it to object
    //TODO: 3. load cache from DAO
    List<Cacheable> processList = processMapper.getAll();
    //TODO: 4. check if cache is loaded properly

    IgniteCache<Long, Process> cache = Ignition.ignite().getOrCreateCache("cache");


//    assertEquals("Checks if all objects were loaded", processList.size(), processCacheRef.getKeys().size());

    log.info("test");
  }
}
