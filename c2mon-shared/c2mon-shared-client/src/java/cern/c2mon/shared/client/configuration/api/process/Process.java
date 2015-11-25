package cern.c2mon.shared.client.configuration.api.process;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.ControlTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * POJO class of a Process.
 * <p>
 * The class is a lombok class which uses the Builder annotation.
 *
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@Data
public class Process implements ConfigurationObject {

  /**
   * determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique identifier of the equipment.
   */
  @IgnoreProperty
  private final Long id;

  /**
   * Unique name of the equipment.
   *
   * Note: Consider that an update of the name is not provided on the server side.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("60000")
  private Integer aliveInterval;

  /**
   * A description of the process.
   */
  private String description;

  /**
   * Max number of updates in a single message from the DAQ process.
   */
  @DefaultValue("100")
  private Integer maxMessageSize;

  /**
   * Max delay between reception of update by a DAQ and sending it to the
   * server.
   */
  @DefaultValue("1000")
  private Integer maxMessageDelay;

  @IgnoreProperty
  private AliveTag aliveTag;

  @IgnoreProperty
  private StatusTag stateTag;

  @IgnoreProperty
  @Singular
  private List<Equipment> equipments = new ArrayList<>();

  public void addEquipment(Equipment equipment) {
    this.equipments.add(equipment);
  }

  @Override
  public boolean requiredFieldsGiven() {
    return (id != null) && (name != null) && (description != null) && (stateTag != null) && (aliveTag != null);
  }

  /**
   * For adding ControlTags use the addControlTag Method
   *
   * @param deleted
   * @param id
   * @param name
   * @param aliveInterval
   * @param description
   * @param maxMessageSize
   * @param maxMessageDelay
   * @param equipments
   */
  @Builder
  public Process(boolean deleted, Long id, String name,  Integer aliveInterval, String description, Integer maxMessageSize,
                 Integer maxMessageDelay, @Singular List<Equipment> equipments, StatusTag stateTag, AliveTag aliveTag) {
    super();
    this.deleted = deleted;
    this.id = id;
    this.name = name;
    this.aliveInterval = aliveInterval;
    this.description = description;
    this.maxMessageSize = maxMessageSize;
    this.maxMessageDelay = maxMessageDelay;
    this.stateTag = stateTag;
    this.aliveTag = aliveTag;
    this.equipments = equipments == null ? new ArrayList<Equipment>() : equipments;
  }
}
