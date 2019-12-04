package cern.c2mon.cache.actions.supervision;

import cern.c2mon.cache.api.flow.DefaultCacheFlow;
import cern.c2mon.server.common.supervision.Supervised;
import cern.c2mon.shared.common.CacheEvent;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Set;

public class AbstractSupervisedCacheFlow<T extends Supervised> extends DefaultCacheFlow<T> {

  public AbstractSupervisedCacheFlow() {
    super((older, newer) -> (older.getStatusTime() == null || newer.getStatusTime() == null)
      || newer.getStatusTime().getTime() >= older.getStatusTime().getTime());
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
