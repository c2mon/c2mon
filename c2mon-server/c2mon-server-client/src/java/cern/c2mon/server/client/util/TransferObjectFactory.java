package cern.c2mon.server.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.tag.Publisher;
import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.TagWithAlarms;
import cern.tim.server.common.control.ControlTag;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.server.common.process.Process;
import cern.tim.server.common.rule.RuleTag;
import cern.tim.server.common.tag.Tag;
import cern.tim.shared.common.datatag.DataTagQualityImpl;

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
                        tag.getValueDescription(),
                        (DataTagQualityImpl) tag.getDataTagQuality(),
                        getTagMode(tag),
                        tag.getTimestamp(),
                        tag instanceof DataTag ? ((DataTag) tag).getDaqTimestamp() : null,
                        tag.getCacheTimestamp(),
                        tag.getDescription(),
                        tag.getName(),
                        tag.getTopic());

            addAlarmValues(transferTag, tagWithAlarms.getAlarms());
            transferTag.setSimulated(tag.isSimulated());
            transferTag.setUnit(tag.getUnit());
            transferTag.addEquimpmentIds(tag.getEquipmentIds());
            transferTag.addProcessIds(tag.getProcessIds());
            if (tag instanceof RuleTag) {
              transferTag.setRuleExpression(((RuleTag) tag).getRuleExpression());
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
                        tag.getValueDescription(),
                        (DataTagQualityImpl) tag.getDataTagQuality(),
                        getTagMode(tag),
                        tag.getTimestamp(), 
                        tag instanceof DataTag ? ((DataTag) tag).getDaqTimestamp() : null,
                        tag.getCacheTimestamp(), 
                        tag.getDescription());

            addAlarmValues(tagValue, tagWithAlarms.getAlarms());
            tagValue.setSimulated(tag.isSimulated());
        }

        return tagValue;
    }  

    /**
     * Creates an <code>AlarmValueImpl</code> object for the given parameters
     * @param alarm An alarm object
     * @return The resulting <code>AlarmValueImpl</code>
     */
    public static AlarmValueImpl createAlarmValue(final Alarm alarm) {
        
        AlarmValueImpl alarmValueImpl = null;       
        
        if (alarm != null) {
            
            alarmValueImpl = new AlarmValueImpl(alarm.getId(), 
                    alarm.getFaultCode(), 
                    alarm.getFaultMember(), 
                    alarm.getFaultFamily(), 
                    alarm.getInfo(), 
                    alarm.getTagId(), 
                    
                    alarm.getTimestamp(), 
                    alarm.isActive());
        }
        return alarmValueImpl;
    }    
    
    /**
     * Creates an <code>AlarmValueImpl</code> object.
     * Also adds Tag Description information.
     * @param alarm An alarm object
     * @param tag A tag object. Used to get Tag Description information.
     * @return The resulting <code>AlarmValueImpl</code>
     */
    public static AlarmValueImpl createAlarmValue(Alarm alarm, Tag tag) {
      
      AlarmValueImpl alarmValueImpl = null;       
      
      if (alarm != null && tag != null) {
          
          alarmValueImpl = new AlarmValueImpl(alarm.getId(), 
                  alarm.getFaultCode(), 
                  alarm.getFaultMember(), 
                  alarm.getFaultFamily(), 
                  alarm.getInfo(), 
                  alarm.getTagId(), 
                  tag.getDescription(),
                  alarm.getTimestamp(), 
                  alarm.isActive());
      }
      return alarmValueImpl;
    }

    /**
     * Creates a <code>TagConfigImpl</code> object for the given parameters
     * @param tagWithAlarms A tag from the cache
     * @param tagProcesses all processes that this tag is attached to (single one for DataTag, multiple for Rules)
     * @return The resulting <code>TransferTagValueImpl</code>
     */
    public static TagConfigImpl createTagConfiguration(final TagWithAlarms tagWithAlarms, final Collection<Process> tagProcesses) {

        Tag tag = tagWithAlarms.getTag();
        TagConfigImpl tagConfig = null;

        if (tag != null) {
            
            tagConfig = new TagConfigImpl(tag.getId());
            tagConfig.setAlarmIds(new ArrayList<Long>(tag.getAlarmIds()));
            
            Boolean controlTag = Boolean.FALSE;
            if (tag instanceof ControlTag) {
                controlTag = Boolean.TRUE;
            }
            tagConfig.setControlTag(controlTag);

            if (tag instanceof DataTag || tag instanceof ControlTag) {
                DataTag dataTag = (DataTag) tag;

                // check if min. value is defined, since it is not mandatory
                if (dataTag.getMinValue() != null)        
                    tagConfig.setMinValue(dataTag.getMinValue().toString());

                // check if max. value is defined, since it is not mandatory
                if (dataTag.getMaxValue() != null)
                    tagConfig.setMaxValue(dataTag.getMaxValue().toString());
                
                if (dataTag.getAddress() != null) {

                    tagConfig.setValueDeadbandType(dataTag.getAddress().getValueDeadbandType());
                    tagConfig.setValueDeadband(dataTag.getAddress().getValueDeadband());
                    tagConfig.setTimeDeadband(dataTag.getAddress().getTimeDeadband());
                    tagConfig.setGuaranteedDelivery(dataTag.getAddress().isGuaranteedDelivery());
                    tagConfig.setHardwareAddress(dataTag.getAddress().getHardwareAddress().toConfigXML());
                    tagConfig.setPriority(dataTag.getAddress().getPriority());
                }
            }

            if (tag instanceof RuleTag) {
                RuleTag ruleTag = (RuleTag) tag;
                tagConfig.setRuleExpressionStr(ruleTag.getRuleText());
            }
            if (!tag.getRuleIds().isEmpty()) {
                tagConfig.addRuleIds(tag.getRuleIds());
            }
            if (tag.getDipAddress() != null) {
                tagConfig.addPublication(Publisher.DIP, tag.getDipAddress());
            }
            if (tag.getJapcAddress() != null) {
                tagConfig.addPublication(Publisher.JAPC, tag.getJapcAddress());
            }
            if (tag.isLogged()) {
                tagConfig.setLogged(Boolean.TRUE);                
            } else {
                tagConfig.setLogged(Boolean.FALSE);
            }
            ArrayList<String> processNames = new ArrayList<String>();
            for (Process process : tagProcesses) {
              processNames.add(process.getName());
            }
            tagConfig.setProcessNames(processNames);
        }
        return tagConfig;
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
