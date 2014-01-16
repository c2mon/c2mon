package cern.c2mon.server.common.config;

/**
 * Enumeration of all caches used by the C2MON core.
 * 
 * <p>Each class is aware of its name, which can be
 * accessed using the getCacheName() method.
 * 
 * @author Mark Brightwell
 *
 */
public enum C2monCacheName {
  
  /**
   * DataTag cache.
   */
  DATATAG,
  
  /**
   * ControlTag cache.
   */
  CONTROLTAG,
  
  /**
   * RuleTag cache.
   */
  RULETAG,
  
  /**
   * Equipment cache.
   */
  EQUIPMENT,
  
  /**
   * SubEquipment cache.
   */
  SUBEQUIPMENT,
  
  /**
   * Process cache.
   */
  PROCESS,
  
  /**
   * AliveTimer cache.
   */
  ALIVETIMER,
  
  /**
   * Alarm cache.
   */
  ALARM,
  
  /**
   * CommFault cache.
   */
  COMMFAULT,
  
  /**
   * Command cache.
   */
  COMMAND,
  
  /**
   * Cache containing distributed parameters.
   */
  CLUSTER
}
