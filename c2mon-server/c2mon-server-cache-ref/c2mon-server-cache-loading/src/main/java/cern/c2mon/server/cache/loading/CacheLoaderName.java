package cern.c2mon.server.cache.loading;

import lombok.Getter;

/**
 * Enumeration of all caches used by the C2MON core.
 * <p>
 * <p>Each class is aware of its name, which can be
 * accessed using the getCacheName() method.
 *
 * @author Szymon Halastra
 */

public enum CacheLoaderName {

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
  ALARM_TIMESTAMP(Names.ALARM_TIMESTAMP);

  @Getter
  private final String label;

  CacheLoaderName(final String label) {
    this.label = label;
  }

  public static class Names {
    public static final String ALARM = "alarmLoaderDAORef";
    public static final String ALIVETIMER = "aliveTimerLoaderDAORef";
    public static final String COMMANDTAG = "commandTagLoaderDAORef";
    public static final String COMMFAULTTAG = "commFaultTagLoaderDAORef";
    public static final String CONTROLTAG = "controlTagLoaderDAORef";
    public static final String DATATAG = "dataTagLoaderDAORef";
    public static final String DEVICE = "deviceLoaderDAORef";
    public static final String DEVICECLASS = "deviceClassLoaderDAORef";
    public static final String EQUIPMENT = "equipmentLoaderDAORef";
    public static final String PROCESS = "processLoaderDAORef";
    public static final String RULETAG = "ruleTagLoaderDAORef";
    public static final String SUBEQUIPMENT = "subEquipmentLoaderDAORef";
    public static final String TAG= "tagLoaderDAORef";
    public static final String ALARM_TIMESTAMP = "timestampLoaderDAORef";
  }
}
