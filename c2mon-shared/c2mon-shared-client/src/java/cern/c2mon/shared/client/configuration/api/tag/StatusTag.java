package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.*;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StatusTag extends ControlTag {

  @DefaultValue("String")
  private DataType dataType = null;

  @DefaultValue("true")
  private Boolean isLogged = true;

  @Override
  public boolean requiredFieldsGiven() {
    return super.requiredFieldsGiven();
  }

  @Builder
  public StatusTag(Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms,
                   Boolean isLogged) {
    super(id, name, description, mode, alarms);

    this.isLogged = isLogged;

  }
}
