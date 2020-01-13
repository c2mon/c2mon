package cern.c2mon.server.common.status;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;
import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * Expresses the current situation of a {@link Supervised} object
 *
 * @author Alexandros Papageorgiou, Brice Copy
 * @apiNote <a href=https://stackoverflow.com/questions/1162816/naming-conventions-state-versus-status>State vs Status discussion for technical jargon naming</a>
 */
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true)
public class SupervisionStateTag extends AbstractCacheableImpl {

  /**
   * Id of the {@link Supervised} object
   */
  final long supervisedId;

  /**
   * Type of the {@link Supervised} object
   */
  final SupervisionEntity supervisedEntity;
  /**
   * Id of the associated {@link AliveTag} used for supervision, or null
   */
  final Long aliveTagId;
  /**
   * Id of the associated {@link CommFaultTag} used for supervision, or null
   */
  final Long commFaultTagId;
  /**
   * Supervision status of this object when it was recovered from cache
   */
  SupervisionStatus supervisionStatus;
  /**
   * Reason/description of the current status, or empty
   */
  String statusDescription;
  /**
   * Time when this supervision status was last confirmed
   */
  Timestamp statusTime;

  public SupervisionStateTag(Long id, Long supervisedId, String supervisedEntity, Long aliveTagId, Long commFaultTagId) {
    this.id = id;
    this.supervisedId = supervisedId;
    this.supervisedEntity = SupervisionEntity.parse(supervisedEntity);
    this.aliveTagId = aliveTagId;
    this.commFaultTagId = commFaultTagId;
  }
}
