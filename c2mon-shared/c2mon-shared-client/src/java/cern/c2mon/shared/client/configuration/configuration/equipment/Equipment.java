package cern.c2mon.shared.client.configuration.configuration.equipment;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.configuration.tag.DataTag;
import lombok.Data;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
@Data
public class Equipment {

  private Long id;

  private Long processId;

  private List<DataTag> dataTags =  new ArrayList<>();
}
