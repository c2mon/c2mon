package cern.c2mon.cache.rule;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCacheBase;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class RuleCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCacheBase<Long, RuleTag> ruleTagCacheRef;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Before
  public void init() {
    ruleTagCacheRef.init();
  }

  @Test
  @Ignore
  public void preloadCache() {
    assertNotNull("RuleTag Cache should not be null", ruleTagCacheRef);

    List<RuleTag> ruleList = ruleTagMapper.getAll();

    assertTrue("List of rule tags should not be empty", ruleList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", ruleList.size(), ruleTagCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    for (RuleTag aRuleList : ruleList) {
      RuleTagCacheObject currentRule = (RuleTagCacheObject) aRuleList;
      //only compares one field so far (name, which does not change when server is running!)
      assertEquals("Cached RuleTag should have the same name as in DB",
              currentRule.getName(), ((ruleTagCacheRef.get(currentRule.getId())).getName()));
    }
  }
}
