package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.CacheEvent;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Set;

class AbstractSupervisedC2monCacheFlow<T extends Supervised> extends DefaultC2monCacheFlow<T> {

  AbstractSupervisedC2monCacheFlow() {
    super((t, t2) -> true);
  }

  @Override
  public Set<CacheEvent> postInsertEvents(@Nullable T older, @NonNull T newer) {
    Set<CacheEvent> events = super.postInsertEvents(older, newer);
    events.add(CacheEvent.SUPERVISION_UPDATE);

    if (older != null) {
      if (older.isRunning() != newer.isRunning())
        events.add(CacheEvent.SUPERVISION_CHANGE);
    } else {
      events.add(CacheEvent.SUPERVISION_CHANGE);
    }
    return events;
  }
}
