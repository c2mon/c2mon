package cern.c2mon.server.history.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.history")
public class HistoryProperties {

  private String tagFallbackFile = "/tmp/tag-fallback.txt";

  private String alarmFallbackFile = "/tmp/alarm-fallback.txt";

  private String commandFallbackFile = "/tmp/command-fallback.txt";

  private Jdbc jdbc = new Jdbc();

  @Data
  public class Jdbc {

    private String url = "jdbc:hsqldb:mem:history;sql.syntax_ora=true";

    private String username = "sa";

    private String password = "";

    // TODO: add other JDBC properties
  }
}
