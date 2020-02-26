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

import static cern.c2mon.cache.api.listener.BufferCacheListenerTest.DEFAULT_MAX_LISTENER_SIZE;
import static org.junit.Assert.assertEquals;

public class BufferCacheListenerSchedulerTest {

  private AtomicReference<Consumer<Set<DataTag>>> expectations = new AtomicReference<>();

  private BatchCacheListener<DataTag> bufferedCacheListener = new BatchCacheListener<>(
    results -> expectations.get().accept(results)
  );

  @Test
  public void flushesOutOnOverflow() throws InterruptedException {
    final int MAX_OVERFLOW = 17;

    List<DataTag> dataTags = LongStream.range(0, DEFAULT_MAX_LISTENER_SIZE + MAX_OVERFLOW).mapToObj(DataTagCacheObject::new).collect(Collectors.toList());

    dataTags.forEach(bufferedCacheListener::apply);
    expectations.set(results -> {
      List<DataTag> sortedResults = results.stream().sorted(Comparator.comparing(Cacheable::getId)).collect(Collectors.toList());

      assertEquals(MAX_OVERFLOW, sortedResults.size());
    });

    Thread.sleep(100);

    bufferedCacheListener.run();
  }
}
