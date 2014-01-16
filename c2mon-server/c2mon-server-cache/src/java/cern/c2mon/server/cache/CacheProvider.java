package cern.c2mon.server.cache;

/**
 * Allows retrieving the references to the different caches.
 * 
 * @author Matthias Braeger
 */
public interface CacheProvider {

  /**
   * @return Reference to the alarm cache
   */
  AlarmCache getAlarmCache();
  
  /**
   * @return Reference to the alive timer cache
   */
  AliveTimerCache getAliveTimerCache();
  
  /**
   * @return Reference to the cluster cache
   */
  ClusterCache getClusterCache();
  
  /**
   * @return Reference to the command tag cache
   */
  CommandTagCache getCommandTagCache();
  
  /**
   * @return Reference to the communication fault cache
   */
  CommFaultTagCache getCommFaultTagCache();
  
  /**
   * @return Reference to the control tag cache
   */
  ControlTagCache getControlTagCache();
  
  /**
   * @return Reference to the data tag cache
   */
  DataTagCache getDataTagCache();
  
  /**
   * @return Reference to the equipment cache
   */
  EquipmentCache getEquipmentCache();
  
  /**
   * @return Reference to the process cache
   */
  ProcessCache getProcessCache();
  
  /**
   * @return Reference to the rule tag cache
   */
  RuleTagCache getRuleTagCache();
  
  /**
   * @return Reference to the sub-equipment cache
   */
  SubEquipmentCache getSubEquipmentCache();
}
