package cern.c2mon.server.cache.rule;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.tag.AbstractTagObjectFacade;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

@Service
public class RuleTagCacheObjectFacade extends AbstractTagObjectFacade<RuleTag> {

  public void update(final RuleTag ruleTag, final Object value, final String valueDesc, final Timestamp timestamp) {           
    updateValue(ruleTag, value, valueDesc);    
    setTimestamp(ruleTag, timestamp);                  
  }

  /**
   * Add the invalidation flag to this RuleTag together with the associated description.
   */
  public void invalidate(RuleTag ruleTag, TagQualityStatus status, String statusDescription, Timestamp timestamp) {
    updateQuality(ruleTag, status, statusDescription);
    setTimestamp(ruleTag, timestamp);
    
  }
  
  private void setTimestamp(RuleTag ruleTag, Timestamp timestamp) {
    // TODO change this to modifying the timestamps if they are not null, rather than putting a new object
    RuleTagCacheObject ruleTagCacheObject = (RuleTagCacheObject) ruleTag;    
    ruleTagCacheObject.setCacheTimestamp(timestamp);
  }
  
}
