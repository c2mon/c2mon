package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.*;

/**
 *
 * @author Franz Ritter
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AliveTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  @Setter(AccessLevel.NONE)
  private String address;

  public void setAddress(DataTagAddress privateAlarmCondition) {
    address = privateAlarmCondition.toConfigXML();
  }


  // TODO CHECK if a AliveTag DataType is always a LONG
//  @DefaultValue("Long")
//  private final DataType dataType = null;
//
//  @DefaultValue("false")
//  private Boolean isLogged = null;

  @Override
  public boolean requiredFieldsGiven() {
    return (getId() != null) && (getName() != null) && (getDescription() != null);
  }

  @Builder
  public AliveTag( Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,  DataTagAddress address) {
    super(id, name, description, DataType.LONG, mode, false, alarms);
    this.address = address != null ? address.toConfigXML() : null;
  }
}
