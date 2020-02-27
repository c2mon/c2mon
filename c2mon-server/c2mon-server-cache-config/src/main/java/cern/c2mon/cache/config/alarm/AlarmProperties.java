package cern.c2mon.cache.config.alarm;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Szymon Halastra
 */
public class AlarmProperties {

  /**
   * Default max length for fault family
   */
  public static final int MAX_FAULT_FAMILY_LENGTH = 64;

  /**
   * Default max length for fault member
   */
  public static final int MAX_FAULT_MEMBER_LENGTH = 64;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  @Getter
  @Setter
  private int maxFaultFamily = MAX_FAULT_FAMILY_LENGTH;

  /**
   * Used to validate the alarm object at runtime configuration.
   */
  @Getter
  @Setter
  private int maxFaultMemberLength = MAX_FAULT_MEMBER_LENGTH;
}
