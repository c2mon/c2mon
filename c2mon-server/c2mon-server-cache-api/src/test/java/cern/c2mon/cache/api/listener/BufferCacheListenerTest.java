package cern.c2mon.cache.api.listener;

import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.Cacheable;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;

public class BufferCacheListenerTest {
  public static final int DEFAULT_MAX_LISTENER_SIZE = new CacheListenerProperties().getBatchSize();
  private AtomicReference<Consumer<Set<DataTag>>> expectations = new AtomicReference<>();

  private BatchCacheListener<DataTag> bufferedCacheListener = new BatchCacheListener<>(
    cacheables -> expectations.get().accept(cacheables)
  );

  @Test
  public void collectsThings() {
    DataTag dataTag = new DataTagCacheObject(0L);

    bufferedCacheListener.apply(dataTag);
    expectations.set(results -> {
      assertEquals(1, results.size());
      assertEquals(dataTag, results.iterator().next());
    });

    bufferedCacheListener.run();
  }

  @Test
  public void collectsManyThings() {
    List<DataTag> dataTags = LongStream.range(0, DEFAULT_MAX_LISTENER_SIZE).mapToObj(DataTagCacheObject::new).collect(Collectors.toList());

    dataTags.forEach(bufferedCacheListener::apply);
    expectations.set(results -> {
      List<DataTag> sortedResults = results.stream().sorted(Comparator.comparing(Cacheable::getId)).collect(Collectors.toList());

      assertEquals(dataTags.size(), sortedResults.size());

      for (int i = 0; i < dataTags.size(); i++) {
        assertEquals(dataTags.get(i), sortedResults.get(i));
      }
    });

    bufferedCacheListener.run();
  }

  @Test
  public void eliminatesEqualDuplicates() {
    List<DataTag> dataTags = LongStream.range(0, DEFAULT_MAX_LISTENER_SIZE).mapToObj(__ -> new DataTagCacheObject(123L)).collect(Collectors.toList());

    dataTags.forEach(bufferedCacheListener::apply);
    expectations.set(results -> {
      assertEquals(1, results.size());
      assertEquals(dataTags.get(dataTags.size() - 1), results.iterator().next());
    });

    bufferedCacheListener.run();
  }

  @Test
  public void eliminatesSameDuplicates() {
    DataTagCacheObject dataTag = new DataTagCacheObject(123L);
    List<DataTag> dataTags = LongStream.range(0, DEFAULT_MAX_LISTENER_SIZE).mapToObj(__ -> dataTag).collect(Collectors.toList());

    dataTags.forEach(bufferedCacheListener::apply);
    expectations.set(results -> {
      assertEquals(1, results.size());
      assertEquals(dataTags.get(dataTags.size() - 1), results.iterator().next());
    });

    bufferedCacheListener.run();
  }
}
