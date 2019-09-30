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

public enum CacheName {

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
  COMMANDTAG(Names.COMMANDTAG),

  /**
   * CommFault cache.
   */
  COMMFAULTTAG(Names.COMMFAULTTAG),

  /**
   * ControlTag cache.
   */
  CONTROLTAG(Names.CONTROLTAG),

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
  RULETAG(Names.RULETAG),

  /**
   * SubEquipment cache.
   */
  SUBEQUIPMENT(Names.SUBEQUIPMENT),

  /**
   * Tag cache - stores DataTag, RuleTag and ControlTag
   */
  TAG(Names.TAG),

  /**
   * Alarm timestamp cache - currently testing
   */
  ALARM_OSCILLATION(Names.ALARM_OSCILLATION);

  @Getter
  private final String label;

  CacheName(final String label) {
    this.label = label;
  }

  public static class Names {
    public static final String ALARM = "alarmCacheRef";
    public static final String ALIVETIMER = "aliveTimerCacheRef";
    public static final String COMMANDTAG = "commandTagCacheRef";
    public static final String COMMFAULTTAG = "commFaultTagCacheRef";
    public static final String CONTROLTAG = "controlTagCacheRef";
    public static final String DATATAG = "dataTagCacheRef";
    public static final String DEVICE = "deviceCacheRef";
    public static final String DEVICECLASS = "deviceClassCacheRef";
    public static final String EQUIPMENT = "equipmentCacheRef";
    public static final String PROCESS = "processCacheRef";
    public static final String RULETAG = "ruleTagCacheRef";
    public static final String SUBEQUIPMENT = "subEquipmentCacheRef";
    public static final String TAG= "tagCacheRef"; // ATTENTION: Not a real cache!
    public static final String ALARM_OSCILLATION = "oscillationCacheRef";
  }
}
