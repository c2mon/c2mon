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
    public static final String ALARM = "alarmCache";
    public static final String ALIVETIMER = "aliveTimerCache";
    public static final String COMMAND = "commandTagCache";
    public static final String COMMFAULT = "commFaultTagCache";
    public static final String CONTROL = "controlTagCache";
    public static final String DATATAG = "dataTagCache";
    public static final String DEVICE = "deviceCache";
    public static final String DEVICECLASS = "deviceClassCache";
    public static final String EQUIPMENT = "equipmentCache";
    public static final String PROCESS = "processCache";
    public static final String RULE = "ruleTagCache";
    public static final String SUBEQUIPMENT = "subEquipmentCache";
  }
}
