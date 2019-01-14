package cern.c2mon.server.cache.alarm.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Emiliano Piselli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ConfigurationProperties(prefix = "c2mon.server.alarm.oscillation")
public class OscillationProperties {
  
  
  /** The time range in seconds for the {@link #oscNumbers} threshold */
  private int timeRange = 60;
  
  
  /** The maximum numbers of alarm state changes during the given time range */
  private int oscNumbers = 6;
  
  
  /** time to check if the oscillation is still alive [sec] ; */
  private int timeOscillationAlive = 10;
  
  
}
