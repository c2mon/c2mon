package cern.tim.server.lifecycle;

import java.net.InetAddress;

import javax.annotation.PostConstruct;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.cluster.CacheCluster;
import net.sf.ehcache.cluster.ClusterNode;
import net.sf.ehcache.cluster.ClusterScheme;
import net.sf.ehcache.cluster.ClusterTopologyListener;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Whenever this Terracotta node leaves the cluster, this bean
 * ensures it disconnects entirely, preventing this server from
 * consuming any further messages.
 * 
 * <p>The process is not actually killed, so manual intervention
 * may be needed to remove it entirely. However, it should stop
 * consuming messages from clients/DAQs.
 * 
 * @author Mark Brightwell
 *
 */
public class NodeShutdownListener implements ClusterTopologyListener, ApplicationContextAware {
  
  private static final Logger LOGGER = Logger.getLogger(NodeShutdownListener.class);
  
  private final static Logger SMS_LOGGER = Logger.getLogger("AdminSmsLogger");
  
  @Autowired
  public CacheManager cacheManager;
 
  @Autowired
  private LifeCycleController lifeCycleController;
  
  private CacheCluster cacheCluster;
  
  private AbstractApplicationContext context;
  
  @PostConstruct
  public void init() {    
    cacheManager.getCluster(ClusterScheme.TERRACOTTA);
    cacheCluster.addTopologyListener(this);
  }
  
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = (AbstractApplicationContext) applicationContext;
  }

  /**
   * A node has joined the cluster
   *
   * @param node The joining node
   */
  @Override
  public void nodeJoined(final ClusterNode arg0) {
    //do nothing
  }

  /**
   * A node has left the cluster
   *
   * @param node The departing node
   */
  @Override
  public void nodeLeft(final ClusterNode clusterNode) {
    //do nothing        
  }

  /**
   * This node has lost contact (possibly temporarily) with the cluster and cannot execute
   * clustered operations
   *
   * @param node The current node
   */
  @Override
  public void clusterOffline(ClusterNode clusterNode) {
    LOGGER.info("Detected Terracotta cluster offline event for node " + clusterNode.getId());
    LOGGER.info("Initiating server shutdown since this node has left the Terracotta cluster.");
    String hostname;
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      LOGGER.error("Unable to get local hostname", e);
      hostname = "#unknown host#";
    }
    SMS_LOGGER.info("Initiating C2MON server shutdown of " + System.getProperty("c2mon.process.name") + " on " + hostname);
    Thread shutdownThread = new Thread(new Runnable() {               
      public void run() {
        lifeCycleController.prepareForShutdown();
        context.close();          
      }
    }, "Terracotta-node-left-thread");
    shutdownThread.setDaemon(true);
    shutdownThread.start();
  }

  /**
   * This node has established contact with the cluster and can execute clustered operations.
   *
   * @param node The current node
   */
  @Override
  public void clusterOnline(ClusterNode node) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clusterRejoined(ClusterNode arg0, ClusterNode arg1) {
    // TODO Auto-generated method stub
    
  }
}
