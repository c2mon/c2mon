package cern.c2mon.server.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.TagWithAlarms;
import cern.tim.server.common.rule.RuleTag;
import cern.tim.server.common.rule.RuleTagCacheObject;
import cern.tim.server.common.tag.Tag;

/**
 * Factory class for creating transfer objects for sending to the C2MON client layer  
 *
 * @author Matthias Braeger
 */
public abstract class TransferObjectFactory {
  
  /**
   * Hidden default constructor
   */
  private TransferObjectFactory() {
    // Do nothing
  }

  
  /**
   * Creates a <code>TransferTagImpl</code> object for the given parameters
   * @param tagWithAlarms A tag from the cache
   * @return The resulting <code>TransferTagImpl</code>
   */
  public static TransferTagImpl createTransferTag(final TagWithAlarms tagWithAlarms) {
    Tag tag = tagWithAlarms.getTag();
    TransferTagImpl transferTag = null;
    if (tag != null) {
      transferTag =
        new TransferTagImpl(
            tag.getId(),
            tag.getValue(),
            tag.getDataTagQuality(),
            getTagMode(tag),
            tag.getTimestamp(),
            tag.getCacheTimestamp(),
            tag.getDescription(),
            tag.getName(),
            tag.getTopic());
      
      addAlarmValues(transferTag, tagWithAlarms.getAlarms());
      transferTag.setSimulated(tag.isSimulated());
      transferTag.addEquimpmentIds(tag.getEquipmentIds());
      transferTag.addProcessIds(tag.getProcessIds());

      if (tag instanceof RuleTag) {
        RuleTag ruleTag = (RuleTag) tag;
        transferTag.setRuleExpression(ruleTag.getRuleExpression());
      }
    }
  
    return transferTag;
  }
  
  
  /**
   * Creates a <code>TransferTagValueImpl</code> object for the given parameters
   * @param tagWithAlarms A tag from the cache
   * @return The resulting <code>TransferTagValueImpl</code>
   */
  public static TransferTagValueImpl createTransferTagValue(final TagWithAlarms tagWithAlarms) {
    Tag tag = tagWithAlarms.getTag();
    TransferTagValueImpl tagValue = null;
    if (tag != null) {
      tagValue = 
        new TransferTagValueImpl(
            tag.getId(), 
            tag.getValue(), 
            tag.getDataTagQuality(),
            getTagMode(tag),
            tag.getTimestamp(), 
            tag.getCacheTimestamp(), 
            tag.getDescription());
      
      addAlarmValues(tagValue, tagWithAlarms.getAlarms());
      tagValue.setSimulated(tag.isSimulated());
    }
    
    return tagValue;
  }
  
  /**
   * Inner method to determine the actual tag mode
   * @param tag The tag for which the mode has to be determined
   * @return The tag mode
   */
  private static TagMode getTagMode(final Tag tag) {
    TagMode mode;
    if (tag.isInOperation()) {
      mode = TagMode.OPERATIONAL;
    }
    else if (tag.isInMaintenance()) {
      mode = TagMode.MAINTENANCE;
    }
    else {
      mode = TagMode.TEST;
    }
    
    return mode;
  }
  
  
  /**
   * Private helper method for creating and adding <code>AlarmValueImpl</code> objects to the
   * transfer tag.
   * @param tagValue The tag value to which the alarms will be added
   * @param alarms The alarms from which the <code>AlarmValueImpl</code> are created from
   */
  private static void addAlarmValues(final TransferTagValueImpl tagValue, final Collection<Alarm> alarms) {
    if (alarms != null) {
      List<AlarmValueImpl> alarmValues = new ArrayList<AlarmValueImpl>(alarms.size());
      for (Alarm alarm : alarms) {
        AlarmValueImpl alarmValue = 
          new AlarmValueImpl(
              alarm.getId(),
              alarm.getFaultCode(),
              alarm.getFaultMember(),
              alarm.getFaultFamily(),
              alarm.getInfo(),
              alarm.getTagId(),
              alarm.getTimestamp(),
              alarm.isActive());
        
        alarmValues.add(alarmValue);
      }
      tagValue.addAlarmValues(alarmValues);
    }
  }
}
