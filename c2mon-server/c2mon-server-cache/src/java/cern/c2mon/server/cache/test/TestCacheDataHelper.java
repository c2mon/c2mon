package cern.c2mon.server.cache.test;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.dbaccess.test.TestDataHelper;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * To be used in a Junit test class, needs persistence TestDataHelper in Spring context.
 * @author mbrightw
 *
 */
public class TestCacheDataHelper {

  @Autowired
  private TestDataHelper testDataHelper;
 
  @Autowired
  private DataTagCache dataTagCache;
  
  @Autowired
  private ControlTagCache controlTagCache;
  
  @Autowired
  private C2monCache<Long, RuleTag> ruleTagCache;
  
  @Autowired
  private ProcessCache processCache;
  
  @Autowired
  private EquipmentCache equipmentCache;
  
  @Autowired
  private SubEquipmentCache subEquipmentCache;
  
  @Autowired
  private AliveTimerCache aliveTimerCache;
  //private CommFaultTagCache commFaultTagCache;
  
  public void insertTestDataIntoCache() {
    dataTagCache.put(testDataHelper.getDataTag().getId(), testDataHelper.getDataTag());
    dataTagCache.put(testDataHelper.getDataTag2().getId(), testDataHelper.getDataTag2());
    ruleTagCache.put(testDataHelper.getRuleTag().getId(), testDataHelper.getRuleTag());
    controlTagCache.put(testDataHelper.getProcessAliveTag().getId(), testDataHelper.getProcessAliveTag());
    controlTagCache.put(testDataHelper.getEquipmentAliveTag().getId(), testDataHelper.getEquipmentAliveTag());
    controlTagCache.put(testDataHelper.getSubEquipmentAliveTag().getId(), testDataHelper.getSubEquipmentAliveTag());
    processCache.put(testDataHelper.getProcess().getId(), testDataHelper.getProcess());
    equipmentCache.put(testDataHelper.getEquipment().getId(), testDataHelper.getEquipment());
    subEquipmentCache.put(testDataHelper.getSubEquipment().getId(), testDataHelper.getSubEquipment());       
  }
  
  public void removeTestDataFromCache() {
    dataTagCache.remove(testDataHelper.getDataTag2().getId());
    dataTagCache.remove(testDataHelper.getDataTag().getId());
    ruleTagCache.remove(testDataHelper.getRuleTag().getId());
    controlTagCache.remove(testDataHelper.getProcessAliveTag().getId());
    controlTagCache.remove(testDataHelper.getEquipmentAliveTag().getId());
    controlTagCache.remove(testDataHelper.getSubEquipmentAliveTag().getId());
    processCache.remove(testDataHelper.getProcess().getId());
    equipmentCache.remove(testDataHelper.getEquipment().getId());
    subEquipmentCache.remove(testDataHelper.getSubEquipment().getId());       
  }
  
}
