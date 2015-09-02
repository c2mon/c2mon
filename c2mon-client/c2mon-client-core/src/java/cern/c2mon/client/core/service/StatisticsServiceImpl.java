package cern.c2mon.client.core.service;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.StatisticsService;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class StatisticsServiceImpl implements StatisticsService {
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected StatisticsServiceImpl(final RequestHandler requestHandler) {
    this.clientRequestHandler = requestHandler;
  }
  
  @Override
  public TagStatisticsResponse getTagStatistics() {
    try {
      TagStatisticsResponse response = clientRequestHandler.requestTagStatistics();
      return response;
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }

    return null;
  }
}
