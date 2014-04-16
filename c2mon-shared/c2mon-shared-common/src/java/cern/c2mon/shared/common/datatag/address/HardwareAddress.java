package cern.c2mon.shared.common.datatag.address;

import java.io.Serializable;

import cern.c2mon.shared.common.ConfigurationException;

/**
 * Parent interface for all hardware addresses used by DAQ modules.
 * All TIM message handlers (e.g. DBMessagHandler, DIPMessageHandler) define
 * their own address format. Therefore, each of them will require a different
 * HardwareAddress sub-interface for accessing address information.
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2007/07/04 12:39:13 $ - $State: Exp $)
 */
public interface HardwareAddress extends Serializable, Cloneable {
  /**
   * Get an XML representation of the HardwareAddress object.
   * @return an XML representation of the HardwareAddress object.
   */
  String toConfigXML();
  
  void validate() throws ConfigurationException;
  
  HardwareAddress clone() throws CloneNotSupportedException;
}
