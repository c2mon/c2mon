package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class AlarmCacheBenchmark extends AbstractCacheBenchmarkTest {

  private C2monCache<Alarm> alarmCache;
  private IgniteCache<Long, Alarm> barebonesCache;

  public void initCache() {
    alarmCache = new IgniteC2monCache<>("Test", IgniteFactory.defaultIgniteCacheConfiguration("Test"), Ignition.start());
    barebonesCache = Ignition.ignite().createCache("BarebonesCache");
    alarmCache.init();
  }

  @TearDown
  public void cleanUp() {
    Ignition.stop(true);
  }

  @Benchmark
  public void insertToBarebones(){
    barebonesCache.put(0L, sample);
  }

  @Benchmark
  public void insertElement(Blackhole blackhole) {
    alarmCache.put(0L, sample);
  }
}