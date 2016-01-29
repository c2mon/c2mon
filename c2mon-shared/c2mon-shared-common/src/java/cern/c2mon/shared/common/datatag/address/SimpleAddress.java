package cern.c2mon.shared.common.datatag.address;

import java.util.Map;

/**
 * The HardwareAdress provides a HardwareAddress which can be used in general.
 * That means if a user likes to create an own {@EquipmentMessageHandler}
 * without creating and deploying a new HardwareAddress on the server side he can use
 * this HardwareAddress.
 * The Information for the Server are than all saved in the Map.
 * Therefore the user have to take care of all the logic related to the Map in the DAQ layer.
 *
 *@author Franz Ritter
 */
public interface SimpleAddress extends HardwareAddress {

  /**
   * Return all HardwareAddress information through a Map.
   * @return The Map with the configuration information.
   */
  Map<String, String> getProperties();
}
