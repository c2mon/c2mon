package cern.c2mon.server.test;

import lombok.extern.slf4j.Slf4j;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
public class HsqlServer {
  private Server server;
  private HsqlProperties properties = new HsqlProperties();

  public HsqlServer(String database, String dbname) {
    properties.setProperty("server.database.0", database);
    properties.setProperty("server.dbname.0", dbname);
  }

  @PostConstruct
  public void start() {
    if (server == null) {
      log.info("Starting HSQL server...");
      server = new Server();
      try {
        server.setProperties(properties);
        server.start();
      } catch (Exception e) {
        log.error("Error starting HSQL server", e);
      }
    }
  }

  @PreDestroy
  public void stop() {
    log.info("Stopping HSQL server...");
    if (server != null) {
      server.stop();
    }
  }
}
