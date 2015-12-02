package cern.c2mon.shared.client.configuration.api.tag;

import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.metaData.MetaData;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

/**
 * Special instance of {@link Tag}.
 * ControlTags are tags to maintenance Processes, Equipments or SubEquipments
 *
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class ControlTag extends Tag {

  /**
   * Indicates whether this tag's value changes shall be logged to the
   * short-term log.
   */

  public ControlTag(Long id, String name, String description, TagMode mode,
                    @Singular List<Alarm> alarms, MetaData metaData) {
    super(false, id, name, description, mode, alarms, metaData);
  }

  /**
   * empty default constructor
   */
  public ControlTag(){

  }

}
