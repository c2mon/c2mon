package cern.tim.server.lifecycle;

import java.net.InetAddress;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import com.tc.cluster.DsoCluster;
import com.tc.cluster.DsoClusterEvent;
import com.tc.cluster.DsoClusterListener;
import com.tc.injection.annotations.InjectedDsoInstance;
import com.tcclient.cluster.DsoNode;

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
public class NodeShutdownListener implements DsoClusterListener, ApplicationContextAware {
  
  private static final Logger LOGGER = Logger.getLogger(NodeShutdownListener.class);
  
  private final static Logger SMS_LOGGER = Logger.getLogger("AdminSmsLogger");
  
  @InjectedDsoInstance
  private DsoCluster dsoCluster;
 
  @Autowired
  private LifeCycleController lifeCycleController;
  
  private DsoNode thisNode;
  
  private AbstractApplicationContext context;
  
  @PostConstruct
  public void init() {    
    thisNode = dsoCluster.getCurrentNode();
    dsoCluster.addClusterListener(this);
  }

  @Override
  public void nodeJoined(final DsoClusterEvent arg0) {
    //do nothing
  }

  @Override
  public void nodeLeft(final DsoClusterEvent clusterEvent) {
    LOGGER.info("Detected Terracotta node left event for node " + clusterEvent.getNode().getId());
    if (clusterEvent.getNode().getId().equals(thisNode.getId())) {
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
  }

  @Override
  public void operationsDisabled(final DsoClusterEvent arg0) {
    //do nothing 
  }

  @Override
  public void operationsEnabled(final DsoClusterEvent arg0) {
    //do nothing
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = (AbstractApplicationContext) applicationContext;
  }
  
}
