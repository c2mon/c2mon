package cern.c2mon.client.core;

import cern.c2mon.shared.client.statistics.TagStatisticsResponse;

public interface StatisticsService {

  /**
   * Retrieve a {@link TagStatisticsResponse} object containing current
   * statistics about the server, including the current number of configured and
   * invalid tags for the server itself and for each DAQ process.
   *
   * @return the tag statistics object
   */
  TagStatisticsResponse getTagStatistics();
}
