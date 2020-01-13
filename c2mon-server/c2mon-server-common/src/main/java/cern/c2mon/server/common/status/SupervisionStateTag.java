package cern.c2mon.server.common.status;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.alive.AliveTag;
import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.*;

/**
 * Expresses the current situation of a {@link Supervised} object
 *
 * @author Alexandros Papageorgiou, Brice Copy
 * @apiNote <a href=https://stackoverflow.com/questions/1162816/naming-conventions-state-versus-status>State vs Status discussion for technical jargon naming</a>
 */
@Slf4j
@Data
@AllArgsConstructor
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
     * Supervision status of this object when it was recovered from cache
     */
    final SupervisionStatus supervisionStatus;

    /**
     * Reason/description of the current status, or empty
     */
    final String statusDescription;

    /**STOPPED
     * Time when this supervision status was last confirmed
     */
    final Timestamp statusTime;

    /**
     * Id of the associated {@link AliveTag} used for supervision, or null
     */
    final Long aliveTagId;

    /**
     * Id of the associated {@link CommFaultTag} used for supervision, or null
     */
    final Long commFaultTagId;

    public SupervisionStateTag() {
        this(0L, SupervisionEntity.EQUIPMENT, SupervisionStatus.DOWN, "", new Timestamp(0L), null, null);
    }
}
