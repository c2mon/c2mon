package cern.c2mon.server.cache.loading;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.process.Process;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/loading/config/server-cacheloading-controltag-test.xml"})
public class ControlTagLoaderDAOTest {

  @Autowired
  private ControlTagLoaderDAO controlTagLoaderDAO;
  
  @Test
  public void testGetItem() {
    assertNotNull(controlTagLoaderDAO.getItem(1260L));
  }
  
  /**
   * Check the default property is picked up (should override that set in the
   * cache object itself).
   */
  @Test
  public void testGetItemDoPostDbLoading() {
    ControlTag tag = controlTagLoaderDAO.getItem(1261L);
    assertNotNull(tag);
    assertTopicSetCorrectly(tag);    
  }
  
  @Test
  public void testGetAll() {
    assertNotNull(controlTagLoaderDAO.getAllAsMap());
    assertTrue(controlTagLoaderDAO.getAllAsMap().size() > 10);
  }
  
  @Test
  public void testGetAllDoPostDbLoading() {
    for (Map.Entry<Long, ControlTag> entry : controlTagLoaderDAO.getAllAsMap().entrySet()) {
      assertTopicSetCorrectly(entry.getValue());
    }
  }
  
  private void assertTopicSetCorrectly(ControlTag tag) {
    assertEquals("c2mon.client.controltag", tag.getTopic());
  }
  
}
