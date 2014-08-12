/**
 *
 */
package cern.c2mon.client.ext.device;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.shared.common.Cacheable;

/**
 * @author Justin Lewis Salmon
 */
public interface DeviceProperty extends Cacheable, Cloneable {

  @Override
  public Long getId();

  public ClientDataTag getClientDataTagValue();

  @Override
  ClientDataTag clone() throws CloneNotSupportedException;
}
