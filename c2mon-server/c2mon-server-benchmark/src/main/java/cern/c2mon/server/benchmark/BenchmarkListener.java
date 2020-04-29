/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.benchmark;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.lifecycle.Lifecycle;

@Slf4j
@Service
public class BenchmarkListener implements C2monCacheListener<Tag>, SmartLifecycle {

  private Logger dataTagLogger = LoggerFactory.getLogger("BenchmarkDataTag");

  private Logger priorityLogger = LoggerFactory.getLogger("BenchmarkPriority");

  private CacheRegistrationService cacheRegistrationService;

  private static final long BENCHMARK_TIME = 1000; //1s
  private static final long MIDDLE_BENCHMARK_TIME = 2000; //1s
  private static final long HIGH_BENCHMARK_TIME = 5000; //1s

  private long maxTagToServer = 0;
  private long maxTagToListener = 0;
  private long maxPriorityToServer = 0;
  private long maxPriorityToListener = 0;

  private long nbAboveBenchmark;
  private long nbAboveMiddleBenchmark;
  private long nbAboveHighBenchmark;

  private long nbAboveBenchmarkPriority;
  private long nbAboveMiddleBenchmarkPriority;
  private long nbAboveHighBenchmarkPriority;

  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  @Autowired
  public BenchmarkListener(CacheRegistrationService cacheRegistrationService) {
    super();
    this.cacheRegistrationService = cacheRegistrationService;
  }

  @Override
  public void notifyElementUpdated(Tag tag) {
    if (!running){
      log.warn("Received notification while component not running - will process anyway");
    }
    if (tag instanceof DataTag) {
      DataTagCacheObject dataTag = (DataTagCacheObject) tag;
      //TODO can remove this once all DAQ updates have DAQ t.s. set
      long daqTime = dataTag.getDaqTimestamp() == null ? dataTag.getTimestamp().getTime() : dataTag.getDaqTimestamp().getTime();
      long serverTime = dataTag.getCacheTimestamp().getTime();
      long currentTime = System.currentTimeMillis();
      long toServer = (serverTime - daqTime);
      long toListener = (currentTime - daqTime);
      if (dataTag.getAddress() != null && dataTag.getAddress().getPriority() == DataTagAddress.PRIORITY_HIGH) {
        priorityLogger.debug("DAQ to server(ms): " + toServer + "; DAQ to listener: " +  toListener + " (Id: " + tag.getId() + ")");
        if (toServer > maxPriorityToServer) {
          maxPriorityToServer = toServer;
          priorityLogger.warn("max DAQ to server: " + maxPriorityToServer);
        }
        if (toListener > maxPriorityToListener) {
          maxPriorityToListener = toListener;
          priorityLogger.warn("max DAQ to listener: " + maxPriorityToListener);
        }
        if (toListener > HIGH_BENCHMARK_TIME) {
          nbAboveHighBenchmarkPriority++;
          priorityLogger.info("Number of update above " + HIGH_BENCHMARK_TIME + ": " + nbAboveHighBenchmarkPriority);
        } else if (toListener > MIDDLE_BENCHMARK_TIME) {
          nbAboveMiddleBenchmarkPriority++;
          priorityLogger.info("Number of update above " + MIDDLE_BENCHMARK_TIME + ": " + nbAboveMiddleBenchmarkPriority);
        } else if (toListener > BENCHMARK_TIME) {
          nbAboveBenchmarkPriority++;
          priorityLogger.info("Number of update above " + BENCHMARK_TIME + ": " + nbAboveBenchmarkPriority);
        }
      } else {
        dataTagLogger.debug("DAQ to server(ms): " + toServer + "; DAQ to listener: " +  toListener + " (Id: " + tag.getId() + ")");
        if (toServer > maxTagToServer) {
          maxTagToServer = toServer;
          dataTagLogger.warn("max DAQ to server: " + maxTagToServer);
        }
        if (toListener > maxTagToListener) {
          maxTagToListener = toListener;
          dataTagLogger.warn("max DAQ to listener: " + maxTagToListener);
        }
        if (toListener > HIGH_BENCHMARK_TIME) {
          nbAboveHighBenchmark++;
          dataTagLogger.info("Number of update above " + HIGH_BENCHMARK_TIME + ": " + nbAboveHighBenchmark);
        } else if (toListener > MIDDLE_BENCHMARK_TIME) {
          nbAboveMiddleBenchmark++;
          dataTagLogger.info("Number of update above " + MIDDLE_BENCHMARK_TIME + ": " + nbAboveMiddleBenchmark);
        } else if (toListener > BENCHMARK_TIME) {
          nbAboveBenchmark++;
          dataTagLogger.info("Number of update above " + BENCHMARK_TIME + ": " + nbAboveBenchmark);
        }
      }
    }
  }

  @PostConstruct
  void init() {
    listenerContainer = cacheRegistrationService.registerToAllTags(this,1);
  }

  @Override
  public void confirmStatus(Tag cacheable) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    log.debug("Stopping Benchmark listener");
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    log.debug("Starting Benchmark listener");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    listenerContainer.stop();
    running = false;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

}
