package cern.c2mon.shared.common.datatag.address;


/**
 * HardwareAddress implementation for the ENSMessageHandler.
 * @author J. Stowisek
 * @version $Revision: 1.2 $ ($Date: 2005/03/22 16:14:22 $ - $State: Exp $) 
 */

public interface ENSHardwareAddress extends HardwareAddress  {
  /**
   * Data type for analogue entities (measures) in the ENS.
   */
  public static final String TYPE_ANALOG = "ANL";

  /**
   * Data type for digital entities in the ENS.
   */
  public static final String TYPE_DIGITAL = "DIG";

  /**
   * Data type for "counters" in the ENS.
   */
  public static final String TYPE_COUNTER = "CNT";

  /**
   * Data type for simple controls (commands) in the ENS.
   */
  public static final String TYPE_CTRL_SIMPLE = "SIMPLE_CTRL";

  /**
   * Data type for set-point controls (commands) in the ENS.
   */
  public static final String TYPE_CTRL_SETPOINT = "SETP_CTRL";

  /**
   * Get the ENS address of the tag
   */
  public String getAddress();

  /**
   * Get the ENS data type of the tag.
   * @see #TYPE_ANALOG
   * @see #TYPE_DIGITAL
   * @return a String representation of the ENS data type for this tag.
   */
  public String getDataType();
  
}