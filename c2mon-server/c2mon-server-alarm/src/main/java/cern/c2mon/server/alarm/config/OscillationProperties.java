package cern.c2mon.server.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.alarm.oscillation")
public class OscillationProperties {

  
  private int timeRange = 60;
  private int oscNumbers = 10;
      
      //time to check if the oscillation is still alive [msec] ;
  private int timeOscillationAlive = 2000;
  
  
}
