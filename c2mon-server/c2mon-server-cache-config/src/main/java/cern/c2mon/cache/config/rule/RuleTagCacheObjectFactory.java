package cern.c2mon.cache.config.rule;

import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.cache.config.tag.AbstractTagCacheObjectFactory;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.daq.config.Change;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

@Named
public class RuleTagCacheObjectFactory extends AbstractTagCacheObjectFactory<RuleTag> {

  private TagCacheCollection tagCacheCollection;

  @Inject
  public RuleTagCacheObjectFactory(TagCacheCollection tagCacheCollection) {
    this.tagCacheCollection = tagCacheCollection;
  }

  @Override
  public RuleTag createCacheObject(Long id) {
    return new RuleTagCacheObject(id);
  }

  @Override
  public Change configureCacheObject(RuleTag ruleTag, Properties properties) {
    super.configureCacheObject(ruleTag, properties);

    RuleTagCacheObject ruleTagCacheObject = (RuleTagCacheObject) ruleTag;

    new PropertiesAccessor(properties)
      .getString("ruleText").ifPresent(ruleText -> {
      ruleTagCacheObject.setRuleText(ruleText);
        // Also, reset supervision ids
      ruleTag.getEquipmentIds().clear();
      ruleTag.getSubEquipmentIds().clear();
      ruleTag.getProcessIds().clear();
    });

    // Set new supervision ids, if any exist
    if (!ruleTag.getRuleInputTagIds().isEmpty()) {
      addSupervisionIdsBasedOnRuleInput(ruleTag);
    }

    return null;
  }

  private void addSupervisionIdsBasedOnRuleInput(RuleTag ruleTag) {
    tagCacheCollection
      .getAll(ruleTag.getRuleInputTagIds())
      .values()
      .forEach(tag -> {
          ruleTag.getEquipmentIds().addAll(tag.getEquipmentIds());
          ruleTag.getSubEquipmentIds().addAll(tag.getSubEquipmentIds());
          ruleTag.getProcessIds().addAll(tag.getProcessIds());
        }
      );
  }
}
