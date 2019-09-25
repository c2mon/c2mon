package cern.c2mon.cache.rule;

import java.util.List;
import java.util.Map;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class RuleCacheLoaderTest extends AbstractCacheLoaderTest<RuleTag> {

  @Autowired
  private C2monCache<RuleTag> ruleTagCacheRef;

  @Autowired
  private RuleTagMapper ruleTagMapper;

  @Override
  protected LoaderMapper<RuleTag> getMapper() {
    return ruleTagMapper;
  }

  @Override
  protected void compareLists(List<RuleTag> mapperList, Map<Long, RuleTag> cacheList) throws ClassNotFoundException {
    for (RuleTag aRuleList : mapperList) {
      RuleTagCacheObject currentRule = (RuleTagCacheObject) aRuleList;
      //only compares one field so far (name, which does not change when server is running!)
      assertEquals("Cached RuleTag should have the same name as in DB",
        currentRule.getName(), ((cacheList.get(currentRule.getId())).getName()));
    }
  }

  @Override
  protected RuleTag getSample() {
    return new RuleTagCacheObject();
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
