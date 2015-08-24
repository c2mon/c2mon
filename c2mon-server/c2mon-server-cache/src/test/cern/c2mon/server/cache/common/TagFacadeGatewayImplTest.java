package cern.c2mon.server.cache.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.rule.RuleTagCacheTest;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.rule.RuleTag;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration({"classpath:cern/c2mon/server/cache/config/server-cache-gateway-test.xml"})
public class TagFacadeGatewayImplTest {
  
  @Autowired
  private TagFacadeGateway tagFacadeGateway;
  
  /**
   * @see RuleTagCacheTest#testSearchWithNameWildcard()
   */
  @Test
  public void testGetTagsWithAlarms() {
    String regex = "DIAMON_clic_CS-CCR-*";
    Collection<TagWithAlarms> tagsWithAlarms = tagFacadeGateway.getTagsWithAlarms(regex);
    assertNotNull(tagsWithAlarms);
    assertEquals(11, tagsWithAlarms.size());
    for (TagWithAlarms ruleTag : tagsWithAlarms) {
      assertTrue(ruleTag.getTag().getName().toLowerCase().startsWith(regex.substring(0, regex.lastIndexOf('*')).toLowerCase()));
      assertTrue(ruleTag.getTag() instanceof RuleTag);
    }
  }
  
  
}
