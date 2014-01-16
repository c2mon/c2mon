package cern.c2mon.server.cache.dbaccess.test;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.dbaccess.AlarmMapper;
import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.dbaccess.ControlTagMapper;
import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.EquipmentMapper;
import cern.c2mon.server.cache.dbaccess.ProcessMapper;
import cern.c2mon.server.cache.dbaccess.RuleTagMapper;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.test.CacheObjectCreation;


/**
 * Bean to be inserted into test classes for inserting the test data.
 * Instantiated in test xml.
 * 
 * @author mbrightw
 * @deprecated old helper class for inserting test data into DB; use instead TestDataInserter
 * @see cern.c2mon.server.test.TestDataInserter TestDataInserter
 *
 */
@Deprecated
public class TestDataHelper {
     
  //Mappers for inserting the data.
  @Autowired
  private DataTagMapper dataTagMapper;
  @Autowired
  private ControlTagMapper controlTagMapper;  
  @Autowired
  private RuleTagMapper ruleTagMapper;
  @Autowired
  private ProcessMapper processMapper;
  @Autowired
  private EquipmentMapper equipmentMapper;
  @Autowired
  private SubEquipmentMapper subEquipmentMapper;
  @Autowired
  private CommandTagMapper commandTagMapper;
  @Autowired
  private AlarmMapper alarmMapper;
  
  //fields for test data
  private ControlTagCacheObject processAliveTag;
  private ProcessCacheObject process;
  private EquipmentCacheObject equipment;
  private SubEquipmentCacheObject subEquipment;
  private SubEquipmentCacheObject subEquipment2;
  private ControlTagCacheObject equipmentAliveTag;
  private ControlTagCacheObject subEquipmentAliveTag;
  private DataTagCacheObject dataTag;
  private DataTagCacheObject dataTag2;
  private RuleTagCacheObject ruleTag;
  private CommandTagCacheObject commandTag;
  private AlarmCacheObject alarm1;
  private AlarmCacheObject alarm2;
  private AlarmCacheObject alarm3;
  
  public void createTestData() {
    processAliveTag = CacheObjectCreation.createTestProcessAlive();    
    equipmentAliveTag = CacheObjectCreation.createTestEquipmentAlive();    
    subEquipmentAliveTag = CacheObjectCreation.createTestSubEquipmentAlive();    
    ruleTag = CacheObjectCreation.createTestRuleTag();    
    process = CacheObjectCreation.createTestProcess1();    
    equipment = CacheObjectCreation.createTestEquipment();  
    commandTag = CacheObjectCreation.createTestCommandTag();
    alarm1 = CacheObjectCreation.createTestAlarm1();
    alarm2 = CacheObjectCreation.createTestAlarm2();
    alarm3 = CacheObjectCreation.createTestAlarm3();
    
    //add equipment to list in process
    process.getEquipmentIds().add(equipment.getId());    
    
    subEquipment = CacheObjectCreation.createTestSubEquipment();  
    subEquipment2 = CacheObjectCreation.createTestSubEquipment2();
    
    //add subequipment to list in equipment
    equipment.getSubEquipmentIds().add(subEquipment.getId());    

    dataTag = CacheObjectCreation.createTestDataTag();
    dataTag2 = CacheObjectCreation.createTestDataTag2();

    //add rule to both datatags (only set from string when loaded from DB 
    // - notice the string is not coherently here, but is not used)
    dataTag.addRuleId(ruleTag.getId());
    dataTag2.addRuleId(ruleTag.getId());
    
    //add datatag to list of tags of the equipment
    equipment.getDataTagIds().add(dataTag.getId());
    
    alarm1.setDataTagId(dataTag.getId());
    alarm2.setDataTagId(dataTag.getId());
    alarm3.setDataTagId(dataTag.getId());        
  }
  
  public void insertTestDataIntoDB() {    
    controlTagMapper.insertControlTag(processAliveTag);
    controlTagMapper.insertControlTag(equipmentAliveTag);
    controlTagMapper.insertControlTag(subEquipmentAliveTag);
    ruleTagMapper.insertRuleTag(ruleTag);
    processMapper.insertProcess(process);
    equipmentMapper.insertEquipment(equipment);  
    subEquipmentMapper.insertSubEquipment(subEquipment); 
    subEquipmentMapper.insertSubEquipment(subEquipment2);             
    dataTagMapper.testInsertDataTag(dataTag);
    dataTagMapper.testInsertDataTag(dataTag2);
    commandTagMapper.insertCommandTag(commandTag);
    alarmMapper.insertAlarm(alarm1);
    alarmMapper.insertAlarm(alarm2);
    alarmMapper.insertAlarm(alarm3);
  }

  /**
   * Removes all the test data from the database.
   * Should always run whatever the status of the 
   * DB if a test case fails (or hangs!).
   */
  public void removeTestData() {
    createTestData(); //makes sure all ids are non-null, so method can be run to clean DB    
    alarmMapper.deleteAlarm(alarm2.getId());
    alarmMapper.deleteAlarm(alarm1.getId());
    alarmMapper.deleteAlarm(alarm3.getId());
    dataTagMapper.deleteDataTag(dataTag2.getId());    
    if (dataTag != null) {
      dataTagMapper.deleteDataTag(dataTag.getId());
    }
    if (commandTag != null) {
      commandTagMapper.deleteCommandTag(commandTag.getId());
    }
    if (subEquipment != null) {
      subEquipmentMapper.deleteSubEquipment(subEquipment.getId());
    }
    if (subEquipment2 != null) {
      subEquipmentMapper.deleteSubEquipment(subEquipment2.getId());
    }
    if (equipment != null) {
      equipmentMapper.deleteEquipment(equipment.getId());
    }
    if (process != null) {
      processMapper.deleteProcess(process.getId());
    }
    if (ruleTag != null) {
      ruleTagMapper.deleteRuleTag(ruleTag.getId());
    }
    if (subEquipmentAliveTag != null) {
      controlTagMapper.deleteControlTag(subEquipmentAliveTag.getId());
    }
    if (equipmentAliveTag != null) {
      controlTagMapper.deleteControlTag(equipmentAliveTag.getId());
    }
    if (processAliveTag != null) {
      controlTagMapper.deleteControlTag(processAliveTag.getId());
    }
    
  }

  /**
   * @return the aliveTag
   */
  public ControlTagCacheObject getProcessAliveTag() {
    return processAliveTag;
  }

  /**
   * @return the process
   */
  public ProcessCacheObject getProcess() {
    return process;
  }

  /**
   * @return the equipment
   */
  public EquipmentCacheObject getEquipment() {
    return equipment;
  }

  /**
   * @return the subEquipment
   */
  public SubEquipmentCacheObject getSubEquipment() {
    return subEquipment;
  }

  /**
   * @return the dataTag
   */
  public DataTagCacheObject getDataTag() {
    return dataTag;
  }

  /**
   * @return the ruleTag
   */
  public RuleTagCacheObject getRuleTag() {
    return ruleTag;
  }

  /**
   * @return the equipmentAliveTag
   */
  public ControlTagCacheObject getEquipmentAliveTag() {
    return equipmentAliveTag;
  }

  /**
   * @return the subEquipmentAliveTag
   */
  public ControlTagCacheObject getSubEquipmentAliveTag() {
    return subEquipmentAliveTag;
  }

  /**
   * @return the dataTag2
   */
  public DataTagCacheObject getDataTag2() {
    return dataTag2;
  }

  /**
   * Getter method
   * @return the CommandTag
   */
  public CommandTagCacheObject getCommandTag() {
    return commandTag;
  }

  /**
   * @return the alarm1
   */
  public AlarmCacheObject getAlarm1() {
    return alarm1;
  }

  /**
   * @return the alarm2
   */
  public AlarmCacheObject getAlarm2() {
    return alarm2;
  }
  
  /**
   * @return the alarm3
   */
  public AlarmCacheObject getAlarm3() {
    return alarm3;
  }

  /**
   * @return the subEquipment2
   */
  public SubEquipmentCacheObject getSubEquipment2() {
    return subEquipment2;
  }
  
}
