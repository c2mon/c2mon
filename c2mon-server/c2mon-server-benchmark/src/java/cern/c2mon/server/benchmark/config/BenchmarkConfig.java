package cern.c2mon.server.benchmark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource("classpath:cern/c2mon/server/benchmark/config/server-benchmark.xml")
public class BenchmarkConfig {}
