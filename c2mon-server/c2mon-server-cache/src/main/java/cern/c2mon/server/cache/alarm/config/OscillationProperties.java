package cern.c2mon.server.cache.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.alarm.oscillation")
public class OscillationProperties {
  
  
  /** The time range in seconds for the {@link #oscNumbers} threshold */
  private int timeRange = 60;
  
  
  /** The maximum numbers of alarm state changes during the given time range */
  private int oscNumbers = 6;
  
  
  /** time to check if the oscillation is still alive [sec] ; */
  private int timeOscillationAlive = 10;
  
  
}
