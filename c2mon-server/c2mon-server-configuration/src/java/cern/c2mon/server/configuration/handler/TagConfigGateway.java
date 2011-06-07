package cern.c2mon.server.configuration.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.TagLocationService;
import cern.tim.server.common.control.ControlTag;
import cern.tim.server.common.rule.RuleTag;
import cern.tim.server.common.tag.Tag;

/**
 * Gateway bean used to call the TagConfigHandler methods on the
 * correct ConfigHandlerBean, according to the type of the 
 * passed tag.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class TagConfigGateway implements TagConfigHandler<Tag> {

  private DataTagConfigHandler dataTagConfigHandler;
  
  private ControlTagConfigHandler controlTagConfigHandler;
  
  /**
   * Rule config bean uses the gateway for setting rule ids,
   * so autowire field.
   */
  @Autowired
  private RuleTagConfigHandler ruleTagConfigHandler;
  
  private TagLocationService tagLocationService;
  
  @Autowired
  public TagConfigGateway(DataTagConfigHandler dataTagConfigHandler, ControlTagConfigHandler controlTagConfigHandler,
      TagLocationService tagLocationService) {
    super();
    this.dataTagConfigHandler = dataTagConfigHandler;
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.tagLocationService = tagLocationService;
  }

  /**
   * Returns the required config handler for this type
   * of Tag.
   * 
   * (Cast OK. Must keep ControlTag first as extends DataTag.)
   * @param <T>
   * @param tag
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T extends Tag> TagConfigHandler<T> getFacade(T tag) {
    if (tag instanceof RuleTag) {
      return (TagConfigHandler<T>) ruleTagConfigHandler;
    } else if (tag instanceof ControlTag) {
      return (TagConfigHandler<T>) controlTagConfigHandler;
    } else {       
      return (TagConfigHandler<T>) dataTagConfigHandler;
    }
  }
  
  
  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).addAlarmToTag(tagId, alarmId);    
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).addRuleToTag(tagId, ruleId);      
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).removeAlarmFromTag(tagId, alarmId);    
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    Tag tag = tagLocationService.get(tagId);
    getFacade(tag).removeRuleFromTag(tagId, ruleId);
  }
}
