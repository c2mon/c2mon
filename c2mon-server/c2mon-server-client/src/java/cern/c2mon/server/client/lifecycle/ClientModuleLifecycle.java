package cern.c2mon.server.client.lifecycle;

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;

import cern.c2mon.server.common.config.ServerConstants;

/**
 * Bean managing lifecycle of client module.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class ClientModuleLifecycle implements SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ClientModuleLifecycle.class);
  
  /**
   * Flag for lifecycle.
   */
  private volatile boolean running = false;
  
  /**
   * JMS client container.
   */
  private DefaultMessageListenerContainer clientJmsContainer;
  
  /**
   * Thread pool used by container.
   */
  private ThreadPoolExecutor clientExecutor;
  
  /**
   * Need to close down the underlying connection.
   */
  private SingleConnectionFactory singleConnectionFactory;
  
  /**
   * Container for admin requests from client.
   */
  private DefaultMessageListenerContainer adminJmsContainer;

  /**
   * Connections for admin messages.
   */
  private SingleConnectionFactory adminConnectionFactory;
  
  /**
   * Constructor.
   * @param clientJmsContainer JMS container used in client module
   * @param clientExecutor thread pool used by container
   * @param singleConnectionFactory client request connection factory
   * 
   */
  @Autowired
  public ClientModuleLifecycle(@Qualifier("clientRequestJmsContainer") final DefaultMessageListenerContainer clientJmsContainer, 
                                    @Qualifier("clientExecutor") final ThreadPoolExecutor clientExecutor,
                                    @Qualifier("clientSingleConnectionFactory") final SingleConnectionFactory singleConnectionFactory,
                                    @Qualifier("adminRequestJmsContainer") final DefaultMessageListenerContainer adminJmsContainer,
                                    @Qualifier("adminSingleConnectionFactory") final SingleConnectionFactory adminSingleConnectionFactory) {
    super();
    this.clientJmsContainer = clientJmsContainer;
    this.clientExecutor = clientExecutor;
    this.singleConnectionFactory = singleConnectionFactory;
    this.adminConnectionFactory = adminSingleConnectionFactory;
    this.adminJmsContainer = adminJmsContainer;
  }

  @Override
  public boolean isAutoStartup() {   
    return false;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public synchronized void start() {
    running = true;
    clientJmsContainer.start();
    adminJmsContainer.start();
  }

  @Override
  public synchronized void stop() {    
    running = false;
    try {   
      LOGGER.info("Shutting down client module JMS connections");
      clientJmsContainer.stop();
      adminJmsContainer.stop();
      clientExecutor.shutdown();
      singleConnectionFactory.destroy(); //closes underlying connection
      adminConnectionFactory.destroy();
      LOGGER.debug("Client module JMS connections stopped");
    } catch (Exception e) {
      LOGGER.error("Exception caught while shutting down Client JMS connection/JMS container", e);
    }
    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_START_LAST - 1;
  }

}
