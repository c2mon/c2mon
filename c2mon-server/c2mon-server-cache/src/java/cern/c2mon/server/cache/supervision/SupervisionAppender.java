package cern.c2mon.server.cache.supervision;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.common.tag.Tag;

/**
 * Helper bean for adding the current supervision status 
 * of Processes and Equipments to Tags.
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionAppender {

  /**
   * Adds the current Process and Equipment
   * Supervision status to the quality of this tag.
   * 
   * <p>Notice NO timestamps of the Tag are modified by this method
   * and will be identical to the previous value.
   * 
   * @param tag for which to add the Supervision info
   * @param <T> the type of the Tag
   */
  <T extends Tag>void addSupervisionQuality(T tagCopy, SupervisionEvent event);   

  

}
