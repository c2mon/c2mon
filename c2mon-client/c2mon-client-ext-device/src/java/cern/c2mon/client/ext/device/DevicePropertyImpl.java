/**
 *
 */
package cern.c2mon.client.ext.device;

import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * @author Justin Lewis Salmon
 */
public class DevicePropertyImpl implements DeviceProperty {

  /**
   *
   */
  private static final long serialVersionUID = 7758719470170886031L;

  private final Long tagId;

  private ClientDataTagValue tagValue;

  /**
   *
   */
  public DevicePropertyImpl(final Long id) {
    this.tagId = id;
  }

  @Override
  public Long getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClientDataTag getClientDataTagValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClientDataTag clone() throws CloneNotSupportedException {
    return null;

  }

}
