package cern.c2mon.shared.client.configuration.configuration.equipment;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.shared.client.configuration.configuration.tag.DataTag;


/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public class Equipment {

  private final Long id;

  private final Long processId;

  private List<DataTag> dataTags =  new ArrayList<>();

  /**
   *
   */
  public Equipment(Long id, Long processId) {
    this.id = id;
    this.processId = processId;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return the processId
   */
  public Long getProcessId() {
    return processId;
  }

  /**
   * @return the dataTags
   */
  public List<DataTag> getDataTags() {
    return dataTags;
  }

  public void addDataTag(DataTag tag) {
    this.dataTags.add(tag);
  }
}
