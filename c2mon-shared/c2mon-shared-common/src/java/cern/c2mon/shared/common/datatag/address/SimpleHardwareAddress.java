package cern.c2mon.shared.common.datatag.address;


/**
 * A simple example of a TIM HardwareAddress.
 * This hardware address is not used by any DAQ module but merely serves as an
 * example for implementing real hardware address interfaces and classes.
 * 
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2005/02/01 17:07:16 $ - $State: Exp $)
 */

public interface SimpleHardwareAddress extends HardwareAddress {
  // ---------------------------------------------------------------------------
  // Public constant definitions
  // ---------------------------------------------------------------------------
  /**
   * Get the internal address.
   */
  public String getAddress();
}