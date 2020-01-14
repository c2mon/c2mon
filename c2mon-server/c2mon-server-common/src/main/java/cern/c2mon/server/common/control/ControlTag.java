package cern.c2mon.server.common.control;

import cern.c2mon.server.common.AbstractCacheableImpl;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ControlTag extends AbstractCacheableImpl {

  /**
   * Id of the {@link Supervised} object
   */
  final long supervisedId;
  /**
   * Type of the {@link Supervised} object
   */
  final SupervisionEntity supervisedEntity;

  public ControlTag(@NonNull Long id, @NonNull Long supervisedId, SupervisionEntity supervisedEntity) {
    super(id);
    this.supervisedId = supervisedId;
    this.supervisedEntity = supervisedEntity;
  }
}
