package cern.c2mon.shared.client.configuration.api.equipment;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.api.tag.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

/**
 * POJO class for the ConfigurationObject.
 * Holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to SubEquipment.
 * <p>
 * The class is a lombok class which uses the Builder annotation.
 *
 * @author Franz Ritter
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SubEquipment extends Equipment {

  //Set automatic by the superclass
//  private Long equipmentId;

  @Builder(builderMethodName = "builderSubEquipment")
  public SubEquipment(boolean deleted, Long id, String name, Integer aliveInterval, String description,
                      String handlerClass, StatusTag statusTag, AliveTag aliveTag, CommFaultTag commFaultTag, @Singular List<DataTag<Number>> dataTags) {
    super(deleted, id, name, aliveInterval, description, handlerClass, null, new ArrayList<SubEquipment>(), statusTag, commFaultTag, aliveTag, dataTags, null);
  }
}
