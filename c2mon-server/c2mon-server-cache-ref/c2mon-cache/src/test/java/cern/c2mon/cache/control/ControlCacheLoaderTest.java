package cern.c2mon.cache.control;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.AbstractCache;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.common.control.ControlTag;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class ControlCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private AbstractCache<Long, ControlTag> controlTagCacheRef;

  @Autowired
  private ControlTagMapper controlTagMapper;

  @Before
  public void init() {
    controlTagCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("ControlTag Cache should not be null", controlTagCacheRef);

    List<ControlTag> dataTagList = controlTagMapper.getAll();

    Set<Long> keySet = dataTagList.stream().map(ControlTag::getId).collect(Collectors.toSet());
    assertTrue("List of control tags should not be empty", dataTagList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", dataTagList.size(), controlTagCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (ControlTag currentTag : dataTagList) {
      //equality of DataTagCacheObjects => currently only compares names
      assertEquals("Cached ControlTag should have the same name as in DB",
              currentTag.getName(), (controlTagCacheRef.get(currentTag.getId())).getName());
    }
  }
}
