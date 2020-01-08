package cern.c2mon.cache.impl;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.junit.Ignore;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public abstract class AbstractCacheBenchmarkTest {
  protected Alarm sample;
  private Map<Long, Alarm> map;

  @Test
  @Ignore("Turn this on if you want to get some benchmarking on Ignite config")
  public void launchBenchmark() throws RunnerException {
    Options opt = new OptionsBuilder()
      .include("\\." + this.getClass().getSimpleName() + "\\.")
      .forks(2)
      .warmupIterations(3)
      .warmupTime(TimeValue.seconds(1))
      .measurementIterations(1)
      .measurementTime(TimeValue.seconds(1))
      .shouldDoGC(true)
      .shouldFailOnError(true)
      .resultFormat(ResultFormatType.JSON)
      .result("target/result.json") // set this to a valid filename if you want reports
      .shouldFailOnError(true)
      .jvmArgs("-server")
      .build();

    new Runner(opt).run();
  }

  @Setup(Level.Trial)
  public void initBenchmark() {
    sample = new AlarmCacheObject(0L);
    map = new ConcurrentHashMap<>();
    initCache();
  }

  abstract void initCache();

  @Benchmark
  public void insertToHashMap(Blackhole blackhole) {
    map.put(0L, sample);
  }

}
