package cern.c2mon.web.restapi.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.ext.history.C2monHistoryGateway;
import cern.c2mon.client.ext.history.C2monHistoryManager;

/**
 * Service wrapper bean around {@link C2monServiceGateway} for accessing C2MON
 * manager singleton beans.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class ServiceGateway {

  private static Logger logger = LoggerFactory.getLogger(ServiceGateway.class);

  /**
   * Reference to the {@link C2monTagManager} instance.
   */
  private C2monTagManager tagManager;

  /**
   * Reference to the {@link C2monCommandManager} instance.
   */
  private C2monCommandManager commandManager;

  /**
   * Reference to the {@link C2monHistoryManager} instance.
   */
  private C2monHistoryManager historyManager;

  /**
   * Called by Spring when the service has been created. Starts the C2MON client
   * and initialises references to the manager beans.
   */
  @PostConstruct
  public void init() {
    logger.info("Starting C2MON service gateway...");
    C2monServiceGateway.startC2monClientSynchronous();

    tagManager = C2monServiceGateway.getTagManager();
    commandManager = C2monServiceGateway.getCommandManager();
    historyManager = C2monHistoryGateway.getHistoryManager();
  }

  /**
   * Retrieve the {@link C2monTagManager} instance.
   *
   * @return the reference to the {@link C2monTagManager}
   */
  public C2monTagManager getTagManager() {
    return tagManager;
  }

  /**
   * Retrieve the {@link C2monCommandManager} instance.
   *
   * @return the reference to the {@link C2monCommandManager}
   */
  public C2monCommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * Retrieve the {@link C2monHistoryManager} instance.
   *
   * @return the reference to the {@link C2monHistoryManager}
   */
  public C2monHistoryManager getHistoryManager() {
    return historyManager;
  }

}
