package cern.c2mon.shared.client.configuration.api.tag;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.util.DataType;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.tag.TagMode;
import lombok.Data;
import lombok.Singular;

/**
 * Tag class which holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to all Tags.
 * <p>
 * The class is a lombok class which uses the Builder annotation.
 *
 * @author Franz Ritter
 */
@Data
public abstract class Tag implements ConfigurationObject {

  /**
   * determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique datatag identifier (unique across all types of tags: control,
   * datatag and rules).
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique tag name.
   */
  private String name;

  /**
   * Free-text description of the tag
   */
  private String description;

  /**
   * Indicates whether a tag is "in operation", "in maintenance" or "in test".
   */
  @DefaultValue("TEST")
  private TagMode mode = TagMode.TEST;

  @IgnoreProperty
  private List<Alarm> alarms;

  public void addAlarm(Alarm alarm) {
    this.alarms.add(alarm);
  }

  /**
   * Checks if all mandatory fields are set.
   * <p>
   * mode is also a Primary filed. But Because that mode is also a default Value it is not necessary to set it in the POJO
   */
  @Override
  public boolean requiredFieldsGiven() {
    return (getId() != null) && (getName() != null) && (description != null) ;
  }

  public Tag(boolean deleted, Long id, String name, String description, TagMode mode, @Singular List<Alarm> alarms) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.name = name;
    this.description = description;
    this.mode = mode;
    this.alarms = alarms == null ?  new ArrayList<Alarm>() : alarms;

  }

}
