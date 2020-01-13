package cern.c2mon.server.common.status;

import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;

/**
 * Expresses the current situation of a {@link Supervised} object
 *
 * @author Alexandros Papageorgiou, Brice Copy
 * @apiNote <a href=https://stackoverflow.com/questions/1162816/naming-conventions-state-versus-status>State vs Status discussion for technical jargon naming</a>
 */
public interface SupervisionStateTag extends Cacheable {

  /**
   * Id of the {@link Supervised} object
   */
  long getSupervisedId();

  /**
   * Type of the {@link Supervised} object
   */
  SupervisionConstants.SupervisionEntity getSupervisedEntity();

  /**
   * Supervision status of this object when it was recovered from cache
   */
  SupervisionConstants.SupervisionStatus getSupervisionStatus();

  /**
   * Reason/description of the current status, or empty
   */
  String getStatusDescription();

  /**
   * Time when this supervision status was last confirmed
   */
  Timestamp getStatusTime();

  /**
   * Id of the associated {@link AliveTag} used for supervision, or null
   */
  Long getAliveTagId();

  /**
   * Id of the associated {@link CommFaultTag} used for supervision, or null
   */
  Long getCommFaultTagId();
}
