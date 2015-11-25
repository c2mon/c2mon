package cern.c2mon.shared.client.configuration.api.equipment;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.tag.*;
import cern.c2mon.shared.client.configuration.api.util.ConfigurationObject;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;


/**
 * POJO class for the ConfigurationObject.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to Equipment.
 * <p>
 * The class is a lombok class which uses the Builder annotation.
 *
 * @author Justin Lewis Salmon
 * @author Franz Ritter
 */
@Data
public class Equipment implements ConfigurationObject {

  /**
   * determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique identifier of the equipment.
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique name of the equipment.
   */
  private String name;

  /**
   * Interval in milliseconds at which the alive tag is expected to change.
   */
  @DefaultValue("60000")
  private Integer aliveInterval;

  /**
   * Free-text description of the equipment.
   */
  private String description;

  /**
   * Fully qualified name of the EquipmentMessageHandler subclass to be used by the DAQ to connect to the equipment.
   */
  // update not allowed
  private String handlerClass;

  /**
   * Address parameters used by the handler class to connect to the equipment.
   */
  // TODO: check if update is possible
  private String address;

  @IgnoreProperty
  private StatusTag stateTag;

  @IgnoreProperty
  private AliveTag aliveTag;

  @IgnoreProperty
  private CommFaultTag commFaultTag;

  @IgnoreProperty
  @Singular
  private List<DataTag<Number>> dataTags = new ArrayList<>();

  @IgnoreProperty
  @Singular
  private List<SubEquipment> subEquipments = new ArrayList<>();

  @IgnoreProperty
  @Singular
  private List<CommandTag> commandTags = new ArrayList<>();

  @Override
  public boolean requiredFieldsGiven() {
    return (id != null) && (name != null) && (description != null) && (stateTag != null) && (commFaultTag != null);
  }

  @Builder
  public Equipment(boolean deleted, Long id, String name, Integer aliveInterval, String description,
                   String handlerClass, String address, @Singular List<SubEquipment> subEquipments, StatusTag stateTag, CommFaultTag commFaultTag, AliveTag aliveTag, @Singular List<DataTag<Number>> dataTags,  @Singular List<CommandTag> commandTags) {
    this.deleted = deleted;
    this.id = id;
    this.name = name;
    this.aliveInterval = aliveInterval;
    this.description = description;
    this.handlerClass = handlerClass;
    this.address = address;

    // metadata of the equipment:
    this.stateTag = stateTag;
    this.commFaultTag = commFaultTag;
    this.aliveTag = aliveTag;

    // Because of lombok the default values of the collections are set here
    this.subEquipments = subEquipments == null ? new ArrayList<SubEquipment>() : subEquipments;
    this.commandTags = commandTags == null ? new ArrayList<CommandTag>() : commandTags;
    this.dataTags = dataTags;
  }


}
