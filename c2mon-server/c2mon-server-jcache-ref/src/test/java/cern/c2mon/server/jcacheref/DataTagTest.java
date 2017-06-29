package cern.c2mon.server.jcacheref;

import java.sql.Timestamp;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.jcacheref.prototype.C2monCacheModule;
import cern.c2mon.server.jcacheref.prototype.datatag.DataTagCacheService;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:c2mon-cache.properties")
@ContextConfiguration(classes = {
        C2monCacheModule.class
})
public class DataTagTest {

  @Autowired
  DataTagCacheService dataTagCacheService;

  Cache<Long, DataTag> dataTagCache;

  @Before
  public void setUp() {
    CachingProvider provider = Caching.getCachingProvider();
    dataTagCache = EasyMock.createMock(Cache.class);
  }

  @Test
  public void updateFromSource() {
    SourceDataTagValue sourceTag = new SourceDataTagValue(2L, "testTag", false); // has null value
    Timestamp newTime = new Timestamp(System.currentTimeMillis() + 1000);
    sourceTag.setTimestamp(newTime);

    assertTrue(sourceTag.getValue() == null);
    assertTrue(sourceTag.isValid());

    DataTagCacheObject dataTag = new DataTagCacheObject(2L, "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    Timestamp oldTime = new Timestamp(System.currentTimeMillis() - 1000);
    dataTag.setSourceTimestamp(oldTime);
    dataTag.setDaqTimestamp(oldTime);
    dataTag.setValue("value not changed");
    dataTag.getDataTagQuality().validate();
    dataTag.setCacheTimestamp(oldTime);

    EasyMock.expect(dataTagCache.get(dataTag.getId())).andReturn(dataTag);


    boolean updated = (boolean) dataTagCacheService.updateFromSource(dataTag.getId(), sourceTag).getReturnValue();

    assertTrue(updated);
  }
}
