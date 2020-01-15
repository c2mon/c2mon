package cern.c2mon.cache.config.rule;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.config.AbstractCacheLoaderTest;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexandros Papageorgiou
 */
public class RuleCacheLoaderTest extends AbstractCacheLoaderTest<RuleTag> {

  @Inject
  private C2monCache<RuleTag> ruleTagCacheRef;

  @Inject
  private RuleTagMapper ruleTagMapper;

  @Override
  protected LoaderMapper<RuleTag> getMapper() {
    return ruleTagMapper;
  }

  @Override
  protected void customCompare(List<RuleTag> mapperList, Map<Long, RuleTag> cacheList) throws ClassNotFoundException {
    for (RuleTag aRuleList : mapperList) {
      RuleTagCacheObject currentRule = (RuleTagCacheObject) aRuleList;
      //only compares one field so far (name, which does not change when server is running!)
      assertEquals("Cached RuleTag should have the same name as in DB",
        currentRule.getName(), ((cacheList.get(currentRule.getId())).getName()));
    }
  }

  @Override
  protected RuleTag getSample() {
    return new RuleTagCacheObject(0L);
  }

  @Override
  protected Long getExistingKey() {
    return 60011L;
  }

  @Override
  protected C2monCache<RuleTag> getCache() {
    return ruleTagCacheRef;
  }
}
