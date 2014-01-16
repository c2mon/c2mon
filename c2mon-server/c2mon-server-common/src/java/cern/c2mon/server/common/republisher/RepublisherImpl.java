package cern.c2mon.server.common.republisher;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;


/**
 * Manages re-publication of failed events. Is used in server for
 * JMS re-publication.
 * 
 * <p>To use this class, implement the associated Publisher interface
 * and instantiate a Republisher in your code.
 * 
 * @see TagValuePublisher for an example.
 * 
 * @author Mark Brightwell
 *
 */
@ManagedResource
class RepublisherImpl<T> implements Republisher<T> {

  private static final Logger LOGGER = Logger.getLogger(RepublisherImpl.class);
  
  private int republicationDelay = 10000;
  
  /** For statistics */
  private AtomicLong totalRepublicationAttempts = new AtomicLong(0);
  
  /** Bean that republishes */
  private Publisher<T> publisher;
  
  /** Used to describe events in log */
  private String eventName;
  
  /** For re-publication */
  private Timer timer;
  
  /** For re-publication */
  private PublicationTask publicationTask;
  
  /**
   * Ids of tags that need re-publishing as publication failed (local collection not shared across cluster)
   * (map used as set, with value set as constant).
   */
  private ConcurrentHashMap<T, Long> toBePublished = new ConcurrentHashMap<T, Long>();
  
  /** For scheduling re-publication task */
  private Object republicatonLock = new Object();
  
  /**
   * Constructs a Republisher for the provided Publisher.
   * 
   * @param publisher publisher for which re-publication is needed
   * @param eventName used to describe the events in log
   */
  public RepublisherImpl(Publisher<T> publisher, String eventName) {
    super();
    this.publisher = publisher;
    this.eventName = eventName;
  }

  @Override
  public void publicationFailed(T event) {
    if (isRunning()) {  
      totalRepublicationAttempts.incrementAndGet();
      synchronized (republicatonLock) { //lock required for if logic, to make sure the added publication is picked up in other thread
        toBePublished.put(event, Long.valueOf(1));
        if (publicationTask == null) {
          LOGGER.debug("Unpublished " + eventName + " detected: scheduling new republication task in " + republicationDelay + " milliseconds");
          publicationTask = new PublicationTask();
          timer.schedule(publicationTask, republicationDelay);
        }
      }
    } else {
      throw new IllegalStateException("Event submitted to Republisher before it has been started up!");
    }
        
  }

  @Override
  public boolean isRunning() {
    return timer != null;
  }

  @Override
  public void start() {
    if (!isRunning())
      timer = new Timer(eventName + "-republication-thread");
  }

  @Override
  public void stop() {
    if (isRunning())
      timer.cancel();    
  }

  /**
   * Checks if un-published evemts need publishing. If so, will publish them.
   * 
   * @author Mark Brightwell
   *
   */
  private class PublicationTask extends TimerTask {

    @Override
    public void run() {
      
        LOGGER.debug("Checking for " + eventName + " re-publications");      
        if (!toBePublished.isEmpty()) {
          LOGGER.info("Detected " + eventName +  " events that failed to be published - will attempt republication of these");          
          for (T event : Collections.list(toBePublished.keys())) {  //take copy as these tasks also add to this map if publication fails again
            try {
              publisher.publish(event);
              toBePublished.remove(event);
            } catch (JmsException e) {              
              LOGGER.error("JMS exception caught while attempting re-publication. Will retry shortly.");
              totalRepublicationAttempts.incrementAndGet();
            } catch (Exception e) {
              LOGGER.error("Unexpected exception caught while checking for failed " + eventName + " publications: this event will not be re-published", e);
              totalRepublicationAttempts.incrementAndGet();
              toBePublished.remove(event);
            }
          }                 
        }      
      synchronized (republicatonLock) {
        if (toBePublished.isEmpty()) {
          publicationTask = null;
        } else {          
          LOGGER.debug("Rescheduling " + eventName + " republication task in " + republicationDelay + " milliseconds");          
          timer.schedule(new PublicationTask(), republicationDelay);
        }        
      }
    }    
  }

  /**
   * @param republicationDelay the republication_delay to set
   */
  @Override
  public void setRepublicationDelay(int republicationDelay) {
    this.republicationDelay = republicationDelay;
  }

  @ManagedOperation(description = "Returns the total number of failed publication attempts since the application started")
  @Override
  public long getNumberFailedPublications() {
    return totalRepublicationAttempts.longValue();
  }

  @ManagedOperation(description = "Returns the current number of events awaiting re-publication (should be 0 in normal operation)")
  @Override
  public int getSizeUnpublishedList() {    
    return toBePublished.size();
  }
  
}
