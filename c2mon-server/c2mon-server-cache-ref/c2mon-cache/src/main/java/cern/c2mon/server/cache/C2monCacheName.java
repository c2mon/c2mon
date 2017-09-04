package cern.c2mon.server.cache;

import lombok.Getter;

/**
 * Enumeration of all caches used by the C2MON core.
 * <p>
 * <p>Each class is aware of its name, which can be
 * accessed using the getCacheName() method.
 *
 * @author Szymon Halastra
 */

public enum C2monCacheName {

  /**
   * Alarm cache.
   */
  ALARM(Names.ALARM),

  /**
   * AliveTimer cache.
   */
  ALIVETIMER(Names.ALIVETIMER),

  /**
   * Command cache.
   */
  COMMAND(Names.COMMAND),

  /**
   * CommFault cache.
   */
  COMMFAULT(Names.COMMFAULT),

  /**
   * ControlTag cache.
   */
  CONTROLTAG(Names.CONTROL),

  /**
   * DataTag cache.
   */
  DATATAG(Names.DATATAG),

  /**
   * Device cache.
   */
  DEVICE(Names.DEVICE),

  /**
   * DeviceClass cache.
   */
  DEVICECLASS(Names.DEVICECLASS),

  /**
   * Equipment cache.
   */
  EQUIPMENT(Names.EQUIPMENT),

  /**
   * Process cache.
   */
  PROCESS(Names.PROCESS),

  /**
   * RuleTag cache.
   */
  RULETAG(Names.RULE),

  /**
   * SubEquipment cache.
   */
  SUBEQUIPMENT(Names.SUBEQUIPMENT);

  @Getter
  private final String label;

  C2monCacheName(final String label) {
    this.label = label;
  }

  public static class Names {
    public static final String ALARM = "alarmCacheRef";
    public static final String ALIVETIMER = "aliveTimerCacheRef";
    public static final String COMMAND = "commandTagCacheRef";
    public static final String COMMFAULT = "commFaultTagCacheRef";
    public static final String CONTROL = "controlTagCacheRef";
    public static final String DATATAG = "dataTagCacheRef";
    public static final String DEVICE = "deviceCacheRef";
    public static final String DEVICECLASS = "deviceClassCacheRef";
    public static final String EQUIPMENT = "equipmentCacheRef";
    public static final String PROCESS = "processCacheRef";
    public static final String RULE = "ruleTagCacheRef";
    public static final String SUBEQUIPMENT = "subEquipmentCacheRef";
  }
}
