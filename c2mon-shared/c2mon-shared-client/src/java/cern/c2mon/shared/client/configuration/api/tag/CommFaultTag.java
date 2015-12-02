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
public class CommFaultTag extends ControlTag {


  /**
   * Address configuration of the datatag (if any)
   */

  private DataTagAddress address;

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
                      Boolean isLogged, DataTagAddress address, MetaData metaData) {
    super(id, name, description, mode, alarms, metaData);
    this.address = address;
    this.isLogged = isLogged;
  }

  /**
   * empty default constructor
   */
  public CommFaultTag(){

  }
}
