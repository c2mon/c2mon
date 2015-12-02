package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.metaData.MetaData;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.*;

/**
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AliveTag extends ControlTag {

  /**
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address;

  @DefaultValue("Long")
  private DataType dataType = null;

  @DefaultValue("false")
  private Boolean isLogged = null;

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  @Builder
  public AliveTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms, DataTagAddress address, MetaData metaData) {
    super(id, name, description, mode, alarms, metaData);
    this.address = address;
  }

  /**
   * empty default constructor
   */
  public AliveTag() {
  }
}
