package cern.c2mon.server.configuraton.helper;

import static org.junit.Assert.*;

import cern.tim.server.common.alarm.AlarmCacheObject;
import cern.tim.server.common.command.CommandTagCacheObject;
import cern.tim.server.common.datatag.DataTagCacheObject;
import cern.tim.server.common.equipment.AbstractEquipmentCacheObject;
import cern.tim.server.common.equipment.EquipmentCacheObject;
import cern.tim.server.common.process.ProcessCacheObject;
import cern.tim.server.common.rule.RuleTagCacheObject;
import cern.tim.server.common.subequipment.SubEquipmentCacheObject;
import cern.tim.server.common.tag.AbstractTagCacheObject;
import cern.tim.server.common.tag.Tag;

/**
 * Junit helper class for comparing cache objects.
 * @author Mark Brightwell
 *
 */
public class ObjectEqualityComparison {

  public static void assertDataTagValueEquals(DataTagCacheObject expectedObject, DataTagCacheObject object) {
    assertDataTagConfigEquals(expectedObject, object);
    assertEquals(expectedObject.getSourceTimestamp(), object.getSourceTimestamp());
    assertEquals(expectedObject.getDaqTimestamp(), object.getDaqTimestamp());  
  }
  
  public static void assertDataTagConfigEquals(DataTagCacheObject expectedObject, DataTagCacheObject object) {
    assertTagConfigEquals(expectedObject, object);
    assertEquals(expectedObject.getEquipmentId(), object.getEquipmentId());
    assertEquals(expectedObject.getProcessId(), object.getProcessId());    
    assertEquals(expectedObject.getMinValue(), object.getMinValue());
    assertEquals(expectedObject.getMaxValue(), object.getMaxValue());
    if (expectedObject.getAddress() != null) {
      assertEquals(expectedObject.getAddress().toConfigXML(), object.getAddress().toConfigXML());  
    }
        
  }
  
  public static void assertTagConfigEquals(AbstractTagCacheObject expectedObject, AbstractTagCacheObject object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getName(), object.getName());
    assertEquals(expectedObject.getDescription(), object.getDescription());
    assertEquals(expectedObject.getMode(), object.getMode());
    assertEquals(expectedObject.getDataType(), object.getDataType());
    assertEquals(expectedObject.getTopic(), object.getTopic());
    assertEquals(expectedObject.isLogged(), object.isLogged());
    assertEquals(expectedObject.getUnit(), object.getUnit());
    assertEquals(expectedObject.getDipAddress(), object.getDipAddress());
    assertEquals(expectedObject.getJapcAddress(), object.getJapcAddress());    
    assertEquals(expectedObject.isSimulated(), object.isSimulated());
    assertNotNull(object.getValueDictionary());
    //assertEquals(expectedObject.getValueDictionary().toXML(), object.getValueDictionary().toXML()); //compare XML of value dictionary
    assertEquals(expectedObject.getDataTagQuality(), object.getDataTagQuality());  
    assertEquals(expectedObject.getRuleIdsString(), object.getRuleIdsString());
    assertEquals(expectedObject.getRuleIds(), object.getRuleIds());
    assertEquals(((Tag)expectedObject).getProcessIds(), ((Tag)object).getProcessIds());
    assertEquals(((Tag)expectedObject).getEquipmentIds(), ((Tag)object).getEquipmentIds());
  }
  
  public static void assertTagValueEquals(AbstractTagCacheObject expectedObject, AbstractTagCacheObject object) {
    assertTagConfigEquals(expectedObject, object);
    assertEquals(expectedObject.getCacheTimestamp(), object.getCacheTimestamp());
    assertEquals(expectedObject.getValue(), object.getValue());
    assertEquals(expectedObject.getValueDescription(), object.getValueDescription());
  }
  
  public static void assertCommandTagEquals(CommandTagCacheObject expectedObject, CommandTagCacheObject object) {
    assertEquals(expectedObject.getId(), object.getId());
    assertEquals(expectedObject.getName(), object.getName());
    assertEquals(expectedObject.getDescription(), object.getDescription());
    assertEquals(expectedObject.getMode(), object.getMode());
    assertEquals(expectedObject.getDataType(), object.getDataType());
    assertEquals(expectedObject.getAuthorizationDetails().getRbacClass(), object.getAuthorizationDetails().getRbacClass());    
    assertEquals(expectedObject.getAuthorizationDetails().getRbacDevice(), object.getAuthorizationDetails().getRbacDevice());    
    assertEquals(expectedObject.getAuthorizationDetails().getRbacProperty(), object.getAuthorizationDetails().getRbacProperty());    
    assertEquals(expectedObject.getSourceRetries(), object.getSourceRetries());
    assertEquals(expectedObject.getSourceTimeout(), object.getSourceTimeout());
    assertEquals(expectedObject.getExecTimeout(), object.getExecTimeout());
    assertEquals(expectedObject.getClientTimeout(), object.getClientTimeout());
    assertEquals(expectedObject.getEquipmentId(), object.getEquipmentId());
    assertEquals(expectedObject.getHardwareAddress().toConfigXML(), object.getHardwareAddress().toConfigXML());
  }

  public static void assertRuleTagConfigEquals(RuleTagCacheObject expectedObject, RuleTagCacheObject cacheObject) {
    assertTagConfigEquals(expectedObject, cacheObject);
    assertEquals(expectedObject.getRuleText(), cacheObject.getRuleText());    
  }
  
  public static void assertEquipmentEquals(EquipmentCacheObject expectedObject, EquipmentCacheObject actualObject) {
    assertAbstractEquipmentEquals(expectedObject, actualObject);   
    assertEquals(expectedObject.getProcessId(), actualObject.getProcessId());    
    assertEquals(expectedObject.getAddress(), actualObject.getAddress());
    assertEquals(expectedObject.getCommandTagIds(), actualObject.getCommandTagIds());
    assertEquals(expectedObject.getDataTagIds(), actualObject.getDataTagIds());
    assertEquals(expectedObject.getSubEquipmentIds(), actualObject.getSubEquipmentIds());      
  }
  
  public static void assertSubEquipmentEquals(SubEquipmentCacheObject expectedObject, SubEquipmentCacheObject actualObject) {
    assertAbstractEquipmentEquals(expectedObject, actualObject);   
    assertEquals(expectedObject.getParentId(), actualObject.getParentId());
  }
  
  public static void assertAbstractEquipmentEquals(AbstractEquipmentCacheObject expectedObject, AbstractEquipmentCacheObject actualObject) {
    assertEquals(expectedObject.getId(), actualObject.getId());
    assertEquals(expectedObject.getName(), actualObject.getName());
    assertEquals(expectedObject.getDescription(), actualObject.getDescription());
    assertEquals(expectedObject.getHandlerClassName(), actualObject.getHandlerClassName());
    assertEquals(expectedObject.getAliveInterval(), actualObject.getAliveInterval());
    assertEquals(expectedObject.getStateTagId(), actualObject.getStateTagId());
    assertEquals(expectedObject.getAliveTagId(), actualObject.getAliveTagId());    
    assertEquals(expectedObject.getCommFaultTagId(), actualObject.getCommFaultTagId());
    assertEquals(expectedObject.getCommFaultTagValue(), actualObject.getCommFaultTagValue()); //constant at the moment
  }

  public static void assertProcessEquals(ProcessCacheObject expectedObject, ProcessCacheObject cacheObject) {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getName(), cacheObject.getName());
    assertEquals(expectedObject.getDescription(), cacheObject.getDescription());
    assertEquals(expectedObject.getAliveInterval(), cacheObject.getAliveInterval());
    assertEquals(expectedObject.getAliveTagId(), cacheObject.getAliveTagId());
    assertEquals(expectedObject.getMaxMessageDelay(), cacheObject.getMaxMessageDelay());
    assertEquals(expectedObject.getMaxMessageSize(), cacheObject.getMaxMessageSize());
    assertEquals(expectedObject.getStateTagId(), cacheObject.getStateTagId());
    assertEquals(expectedObject.getEquipmentIds(), cacheObject.getEquipmentIds());
    assertEquals(expectedObject.getJmsListenerTopic(), cacheObject.getJmsListenerTopic());
  }
  
  public static void assertAlarmEquals(AlarmCacheObject expectedObject, AlarmCacheObject cacheObject) {
    assertEquals(expectedObject.getId(), cacheObject.getId());
    assertEquals(expectedObject.getFaultFamily(), cacheObject.getFaultFamily());
    assertEquals(expectedObject.getFaultMember(), cacheObject.getFaultMember());
    assertEquals(expectedObject.getFaultCode(), cacheObject.getFaultCode());
    assertEquals(expectedObject.getCondition(), cacheObject.getCondition());    
    assertEquals(expectedObject.getTagId(), cacheObject.getTagId());    
  }
  
}
