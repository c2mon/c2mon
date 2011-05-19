package cern.c2mon.server.configuration.handler;

/**
 * Common interface of the ConfigHandlers that
 * manages Tag objects.
 * 
 * @author Mark Brightwell
 *
 * @param <T>
 */
public interface TagConfigHandler<T> {

  /**
   * Adds this Rule to the list of Rules that
   * need evaluating when this tag changes.
   * 
   * @param tagId the Tag that needs to point to the rule
   * @param ruleId the rule that now needs evaluating
   */
  void addRuleToTag(Long tagId, Long ruleId);
  
  /**
   * Removes this Rule from the list of Rules
   * that need evaluating when this Tag changes. 
   * 
   * @param tagId the tag pointing to the rule
   * @param ruleId the rule that no longer needs evaluating
   */
  void removeRuleFromTag(Long tagId, Long ruleId);
  
  void removeAlarmFromTag(Long tagId, Long alarmId);

  /**
   * Adds the alarm to the list of alarms associated to this
   * tag (locks tag).
   * @param tagId the id of the tag
   * @param alarmId the id of the alarm
   */
  void addAlarmToTag(Long tagId, Long alarmId);

  
  
}
