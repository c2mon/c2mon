package cern.c2mon.server.cache.dbaccess.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.cachedbaccess")
public class CacheDbAccessProperties {

  /**
   * Enable/disable insertion of demo data into the cache backup at startup
   */
  private boolean insertTestData = true;

  /**
   * JDBC properties
   */
  private Jdbc jdbc = new Jdbc();

  @Data
  public class Jdbc {

    /**
     * JDBC URL pointing to a database containing the cache backup schema
     */
    private String url = "jdbc:hsqldb:mem:cache;sql.syntax_ora=true";

    /**
     * Database account username
     */
    private String username = "sa";

    /**
     * Database account password
     */
    private String password = "";

    // TODO: add other JDBC properties (validationQuery, etc.)
  }
}
