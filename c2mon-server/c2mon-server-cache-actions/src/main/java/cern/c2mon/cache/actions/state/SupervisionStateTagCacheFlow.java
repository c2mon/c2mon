package cern.c2mon.cache.actions.state;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.CacheEvent;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Set;

import static cern.c2mon.cache.actions.state.SupervisionStateTagEvaluator.isRunning;

class SupervisionStateTagCacheFlow extends DefaultCacheFlow<SupervisionStateTag> {

  SupervisionStateTagCacheFlow() {
    super((older, newer) -> newer.getStatusTime().getTime() >= older.getStatusTime().getTime());
  }

  @Override
  public Set<CacheEvent> postInsertEvents(@Nullable SupervisionStateTag older, @NonNull SupervisionStateTag newer) {
    Set<CacheEvent> events = super.postInsertEvents(older, newer);
    events.add(CacheEvent.SUPERVISION_UPDATE);

    if (older != null) {
      if (isRunning(older) != isRunning(newer))
        events.add(CacheEvent.SUPERVISION_CHANGE);
    } else {
      events.add(CacheEvent.SUPERVISION_CHANGE);
    }
    return events;
  }
}
