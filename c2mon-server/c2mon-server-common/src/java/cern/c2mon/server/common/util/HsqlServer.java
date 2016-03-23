package cern.c2mon.server.common.util;

import lombok.extern.slf4j.Slf4j;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerConstants;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author Justin Lewis Salmon
 */
@Component
@Slf4j
public class HsqlServer {

  private static Server server;
  private static HsqlProperties properties = new HsqlProperties();

  public static void start(String database, String dbname) {
    if (server != null && server.getState() == ServerConstants.SERVER_STATE_ONLINE) {
      log.info("HSQL server is already running");
      return;
    }

    server = new Server();
    properties.setProperty("server.database.0", database);
    properties.setProperty("server.dbname.0", dbname);

    try {
      server.setProperties(properties);
    } catch (Exception e) {
      log.error("Error setting HSQL server properties", e);
    }

    log.info("Starting HSQL server...");
    server.start();
  }

  @PreDestroy
  public void stop() {
    if (server != null && server.getState() == ServerConstants.SERVER_STATE_ONLINE) {
      log.info("Stopping HSQL server...");
      server.stop();
    }
  }
}
