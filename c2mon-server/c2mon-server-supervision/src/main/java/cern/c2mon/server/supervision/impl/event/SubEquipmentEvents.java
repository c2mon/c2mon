package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.subequipment.SubEquipmentService;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.subequipment.SubEquipment;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.sql.Timestamp;

@Slf4j
public class SubEquipmentEvents extends SupervisionEventHandler<SubEquipment> {

  @Inject
  public SubEquipmentEvents(SubEquipmentService subEquipmentService) {
    super(subEquipmentService);
  }

  /**
   * This method is called when the subequipment's alivetag or the
   * subequipment's commfault tag (good value) is received. In both cases we
   * assume the equipment is running and we modify its state tag accordingly.
   *
   * @param id          Identifer of the subequipment for which the alivetag/commfaulttag
   *                    was received
   * @param timestamp   Timestamp indicating when it was received
   * @param message     Message explaining which is the cause for the subequipment to be
   *                    considered as being up.
   */
  @Override
  public void onUp(Long id, Timestamp timestamp, String message) {
    super.onUp(id,timestamp, message);

    Long commFaultId = supervised.getCommFaultTagId();
    ControlTag commFaultTag = null; // CommfaultTagCache get?
    setCommFaultTag(commFaultId, true, commFaultTag.getValueDescription(), timestamp);
  }
}
