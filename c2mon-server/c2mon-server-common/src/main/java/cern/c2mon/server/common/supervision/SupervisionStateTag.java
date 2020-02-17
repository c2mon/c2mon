package cern.c2mon.server.common.supervision;

import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import lombok.*;

import java.sql.Timestamp;

import static cern.c2mon.server.common.util.KotlinAPIs.orElse;

/**
 * Expresses the current situation of a {@link Supervised} object
 *
 * @author Alexandros Papageorgiou, Brice Copy
 * @apiNote <a href=https://stackoverflow.com/questions/1162816/naming-conventions-state-versus-status>State vs Status discussion for technical jargon naming</a>
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SupervisionStateTag extends ControlTag {
  /**
   * Id of the associated {@link AliveTag} used for supervision, or null
   */
  Long aliveTagId;
  /**
   * Id of the associated {@link CommFaultTag} used for supervision, or null
   */
  Long commFaultTagId;
  /**
   * Supervision status of this object when it was recovered from cache
   */
  SupervisionStatus supervisionStatus = SupervisionStatus.DOWN;
  /**
   * Reason/description of the current status, or empty
   */
  String statusDescription = "";
  /**
   * Time when this supervision status was last confirmed
   */
  Timestamp statusTime = DEFAULT_TIMESTAMP;

  /**
   * Primary Ctor, also used by MyBatis (hence the Long instead of long)
   */
  public SupervisionStateTag(Long id, @NonNull Long supervisedId, String supervisedEntity, Long aliveTagId, Long commFaultTagId) {
    super(id, supervisedId, SupervisionEntity.parse(supervisedEntity));
    this.aliveTagId = aliveTagId;
    this.commFaultTagId = commFaultTagId;
  }

  public SupervisionStateTag(Long id) {
    super(id, null, null);
  }

  /**
   * Sets the supervision information for the supervised object, including
   * status, description and time
   *
   * @param supervisionStatus the new status
   * @param statusDescription a reason for the current status
   * @param statusTime        time of the supervision event
   */
  public void setSupervision(@NonNull SupervisionStatus supervisionStatus,
                             String statusDescription,
                             @NonNull Timestamp statusTime) {
    this.supervisionStatus = supervisionStatus;
    this.statusDescription = orElse(statusDescription, "");
    this.statusTime = (Timestamp) statusTime.clone();
  }

  @Override
  public SupervisionStateTag clone() {
    SupervisionStateTag clone = (SupervisionStateTag) super.clone();
    if (statusTime != null)
      clone.statusTime = new Timestamp(statusTime.getTime());
    return clone;
  }

  /**
   * Sets the status of this Supervised object to STARTUP,
   * with associated message.
   * <p>
   * Starts the alive timer if not already running.
   * <p>
   * Careful, this does NOT update the cache entry. You need to explicitly {@code put} for that
   */
  public void start(final Timestamp timestamp) {
    setSupervision(SupervisionStatus.STARTUP, getSupervisedEntity() + " " + getName() + " was started", timestamp);
  }

  public void stop(final Timestamp timestamp) {
    setSupervision(SupervisionStatus.DOWN, getSupervisedEntity() + " " + getName() + " was stopped", timestamp);
  }

  public void resume(final Timestamp timestamp, final String message) {
    setSupervision(SupervisionStatus.RUNNING, message, timestamp);
  }

  public void suspend(final Timestamp timestamp, final String message) {
    setSupervision(SupervisionStatus.DOWN, message, timestamp);
  }
}
