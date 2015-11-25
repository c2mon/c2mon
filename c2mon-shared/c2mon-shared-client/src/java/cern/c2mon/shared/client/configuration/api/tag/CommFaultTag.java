package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.*;

/**
 * @author Franz Ritter
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommFaultTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  @Setter(AccessLevel.NONE)
  private String address;

  public void setAddress(DataTagAddress privateAlarmCondition) {
    address = privateAlarmCondition.toConfigXML();
  }

  @DefaultValue("Boolean")
  private DataType dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  @Builder
  public CommFaultTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
      Boolean isLogged,  DataTagAddress address) {
    super( id, name, description, mode, alarms);
    this.address = address != null ? address.toConfigXML() : null;
    this.isLogged = isLogged;

  }
}
