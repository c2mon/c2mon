package cern.c2mon.shared.client.configuration.api.tag;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.*;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StatusTag extends ControlTag {

  @Builder
  public StatusTag(Long id, String name, String description, DataType dataType, TagMode mode, @Singular List<Alarm> alarms,
                   Boolean isLogged) {
    super(id, name, description, dataType, mode, isLogged, alarms);

  }
}
