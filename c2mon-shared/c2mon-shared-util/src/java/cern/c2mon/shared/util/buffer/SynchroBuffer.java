package cern.c2mon.shared.util.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/** A buffering utility class.
 * @author F.Calderini
 */
public class SynchroBuffer {


  private static final Logger LOGGER = Logger.getLogger(SynchroBuffer.class.getName());
  
  private long minWindowSize;
  private long maxWindowSize;
  private int windowGrowthFactor;
  private int duplicatePolicy;
  
  /**
   * The maximum number of objects that the synchrobuffer will accept.
   * Once this threshold is reached, FIFO will be applied.
   * (must be set greater than 0)
   */
  private int capacity;

  /**
   * constant indicating infinite capacity of the buffer
   * (can lead to out of memory crashes).
   */
  private static final int INFINITE_CAPACITY = 0;
  
  /**
   * The counter for warning if the capacity is reached.
   * If the maximum capacity is reached, we only log
   * a warning every WARNING_FREQUENCY removals from the buffer.
   */
  private int warningCounter = 0;
  
  /**
   * The frequency of the warnings if the buffer capacity
   * is reached.
   */
  private static final int WARNING_FREQUENCY = 500;
  
  private Thread checkingThread;
  
  private volatile boolean closed = false;
  private volatile boolean firing = false;
  private volatile boolean enabled = false;
    
  private SynchroBufferListener listener = null;
    
  /** The buffer */
  private List buffer = null;
  private Map bufferMap = null;
  
  /** Allows object duplication.
   */
  public static final int DUPLICATE_OK = 1;
  /** Replaces duplicated objects.
   */
  public static final int DUPLICATE_REPLACE = 2;
  /** Discards duplicated objects.
   */
  public static final int DUPLICATE_DISCARD = 3;
  

  /**
   * Constructor. The duplicate policy is set as DUPLICATE_OK, since the maximum capacity feature
   * is only implemented in this case.
   * @param minWindowSize the buffer window min size (msec)
   * @param maxWindowSize the buffer window max size (msec)
   * @param windowGrowthFactor the buffer window growth factor (size = minWindowSize + msg/sec x windowGrowthFactor)
   * @param capacity the maximum size of the buffer (FIFO once this size is reached); must be > 0 (if set
   *          as 0, the capacity will be infinite)
   * @param daemon set as true if the firing thread should be a daemon thread (e.g. if the fire() method could be frozen at shutdown);
   *          in general, non-essential services may with to set the execution thread as a daemon thread; services that are expected
   *          to always close down cleanly should set this to false (default in all other constructors)
   */
  public SynchroBuffer(long minWindowSize, long maxWindowSize, int windowGrowthFactor, int duplicatePolicy, int capacity, boolean daemon) {
    if (duplicatePolicy != SynchroBuffer.DUPLICATE_OK) {
        LOGGER.warn("The maximum capacity of the SynchroBuffer is only supported with the duplicatePolicy set to DUPLICATE_OK...");
        LOGGER.warn("...switching duplicate policy to DUPLICATE_OK");
    }
    init("", minWindowSize, maxWindowSize, windowGrowthFactor, SynchroBuffer.DUPLICATE_OK, capacity, daemon);
}
  
  /**
   * Constructor. The duplicate policy is set as DUPLICATE_OK, since the maximum capacity feature
   * is only implemented in this case.
   * @param minWindowSize the buffer window min size (msec)
   * @param maxWindowSize the buffer window max size (msec)
   * @param windowGrowthFactor the buffer window growth factor (size = minWindowSize + msg/sec x windowGrowthFactor)
   * @param capacity the maximum size of the buffer (FIFO once this size is reached); must be > 0 (if set
   *          as 0, the capacity will be infinite)   
   */
  public SynchroBuffer(long minWindowSize, long maxWindowSize, int windowGrowthFactor, int duplicatePolicy, int capacity) {
      if (duplicatePolicy != SynchroBuffer.DUPLICATE_OK) {
          LOGGER.warn("The maximum capacity of the SynchroBuffer is only supported with the duplicatePolicy set to DUPLICATE_OK...");
          LOGGER.warn("...switching duplicate policy to DUPLICATE_OK");
      }
      init("", minWindowSize, maxWindowSize, windowGrowthFactor, SynchroBuffer.DUPLICATE_OK, capacity, false);
  }
  /** Constructor.
   * @param minWindowSize the buffer window min size (msec)
   * @param maxWindowSize the buffer window max size (msec)
   * @param windowGrowthFactor the buffer window growth factor (size = minWindowSize + msg/sec x windowGrowthFactor)
   * @param duplicatePolicy the buffer object duplication policy
   */
  public SynchroBuffer(long minWindowSize, long maxWindowSize, int windowGrowthFactor, int duplicatePolicy) {
      init("", minWindowSize, maxWindowSize, windowGrowthFactor, duplicatePolicy, SynchroBuffer.INFINITE_CAPACITY, false);
  }
  
  /** Constructor.
   * @param name the name/description of the usage of the buffer
   * @param minWindowSize the buffer window min size (msec)
   * @param maxWindowSize the buffer window max size (msec)
   * @param windowGrowthFactor the buffer window growth factor (size = minWindowSize + msg/sec x windowGrowthFactor)
   * @param duplicatePolicy the buffer object duplication policy
   */
  public SynchroBuffer(String name, long minWindowSize, long maxWindowSize, int windowGrowthFactor, int duplicatePolicy) {
    init(name, minWindowSize, maxWindowSize, windowGrowthFactor, duplicatePolicy, SynchroBuffer.INFINITE_CAPACITY, false);
  }

  /** Default constructor. Initialisation is made via properties.
   * It reads the configuration from the resource config file specified via the system 
   * property <code>syncrobuffer.properties</code>. If not defined, it looks for the default config file 
   * <code>synchrobuffer-config.properties</code>. System properties override the configuration loaded from
   * the properties file. Configuration properties are :
   * <UL>
   * <LI>synchrobuffer.minwindowsize</LI> (msec, default 500)
   * <LI>synchrobuffer.maxwindowsize</LI> (msec, default 5000)
   * <LI>synchrobuffer.windowgrowthfactor</LI> (windowSize = minWindowSize + msg/sec x windowGrowthFactor, default 100)
   * <LI>synchrobuffer.duplicatepolicy</LI> (default SynchroBuffer.DUPLICATES_OK)
   * </UL>
   */
  public SynchroBuffer() {
    Properties properties = SynchroBufferConfig.getProperties(this.getClass().getClassLoader());
    long min_window_size = Long.parseLong(properties.getProperty(SynchroBufferConfig.MIN_WINDOW_SIZE_PROPERTY));
    long max_window_size = Long.parseLong(properties.getProperty(SynchroBufferConfig.MAX_WINDOW_SIZE_PROPERTY));
    int window_growth_factor = Integer.parseInt(properties.getProperty(SynchroBufferConfig.WINDOW_GROWTH_FACTOR_PROPERTY));
    int duplicate_policy = Integer.parseInt(properties.getProperty(SynchroBufferConfig.DUPLICATE_POLICY_PROPERTY));
    init("", min_window_size, max_window_size, window_growth_factor, duplicate_policy, SynchroBuffer.INFINITE_CAPACITY, false);
  }
    
  /**
   * Initializes the SynchroBuffer.
   * @param name
   * @param minSize
   * @param maxSize
   * @param growthFactor
   * @param policy
   * @param capacity        the maxiumum number of elements the sychrobuffer will take (afterwards FIFO is applied)
   */
  private void init(String name, long minSize, long maxSize, int growthFactor, int policy, int capacity, boolean daemon) {
    LOGGER.debug("SynchroBuffer[minWindowSize=" + minSize + ",maxWindowSize=" + maxSize + ",windowGrowthFactor=" + growthFactor
                + ",duplicatePolicy=" + (policy == SynchroBuffer.DUPLICATE_DISCARD ? "DUPLICATE_DISCARD" : (policy == SynchroBuffer.DUPLICATE_REPLACE ? "DUPLICATE_REPLACE" : "DUPLICATE_OK"))
                + ", capacity=" + (capacity == SynchroBuffer.INFINITE_CAPACITY ? "INFINITE_CAPACITY" : Integer.toString(capacity))
                + ", daemon thread=" + daemon + "]");
    if ( (minSize <= 0) || (maxSize <= 0) || (growthFactor <= 0) ) {
      throw(new IllegalArgumentException("arguments must be greater than zero"));
    } else if ( maxSize <= minSize ) {
      throw(new IllegalArgumentException("maximum window size must be greater than minimum window size"));
    } else {
      this.minWindowSize = minSize;
      this.maxWindowSize = maxSize;
      this.windowGrowthFactor = growthFactor;
      this.duplicatePolicy = policy;
      this.capacity = capacity;
      buffer = new ArrayList();
      bufferMap = new LinkedHashMap();
      checkingThread = "".equalsIgnoreCase(name) ? new CheckingThread() : new CheckingThread(name);
      checkingThread.setDaemon(daemon);
      checkingThread.start();
    }
  }
  
  private long fire() {
    //if (buffer.size() == 0)
//          return 0;
    setFiring(true);
    Collection pulled = null;
    synchronized(buffer) {
      pulled = (Collection) ((ArrayList)buffer).clone();
      buffer.clear();
      bufferMap.clear();
    }
    long time_before = System.currentTimeMillis();
    if (listener != null) {
      if (pulled.size() > 0) {
        try {
          listener.pull(new PullEvent(this, pulled));
        } catch (Exception ex) {
          LOGGER.error("Exception caught when calling registered SynchroBuffer listener", ex);
        }
      }
    }
    long time_after = System.currentTimeMillis();
    long time_elapsed = time_after-time_before;
    setFiring(false);
    
    return time_elapsed;
  }
    
  /** Push an object into the buffer. 
   * If the duplicate policy is DUPLICATE_DISCARD the object is discarded if the buffer already contains it. 
   * If the duplicate policy is DUPLICATE_REPLACE the object replaces any previously pushed duplicated instance. 
   * The object is appended otherwise.
   * <code>equals</code> method is used to determine duplications.
   * @param o the object to push
   */
  public void push(Object object) {
    if (isClosed()) {
      LOGGER.debug("synchro isClosed - eXception");
      throw new IllegalArgumentException("buffer closed");
    }
    
    boolean objectAdded = false;
    synchronized(buffer) {
      switch (duplicatePolicy) {
        case SynchroBuffer.DUPLICATE_DISCARD :
          if (!bufferMap.containsKey(object)) {
            buffer.add(object);
            bufferMap.put(object, new Integer(buffer.size()-1));
            objectAdded = true;
          }
          break;
        case SynchroBuffer.DUPLICATE_REPLACE:
          Integer index = (Integer)bufferMap.get(object);
          if (index == null) {
            buffer.add(object);
            index = new Integer(buffer.size()-1);
            bufferMap.put(object, index);
            objectAdded = true;
          } else {
            buffer.set(index.intValue(), object);
          }
          break;
        default :
          buffer.add(object);
          //if the buffer is to large, remove the oldest object
          if (capacity != INFINITE_CAPACITY && buffer.size() > capacity) {
              buffer.remove(0);
              //log capacity reached
              capacityWarn();
          }
          objectAdded = true;
      }
      
    }
    if (LOGGER.isDebugEnabled() && objectAdded && buffer.size() > 100 && buffer.size() % 1000 == 0) {
        LOGGER.debug("buffer reached " + buffer.size() + " cached elements and growing... ");
        LOGGER.debug("if enabled, buffer will keep size below the maximum capacity, which is set at " + capacity);
    }
  }
    
  /**
   * Log a warning message if the capacity is reached
   * (every 500 times the capacity is reached, so as
   * not to overload the logger).
   */
  private void capacityWarn() {
    //warn every so often (according to frequency parameter)
      if (warningCounter == 0) {
          LOGGER.warn("The maximum capacity of the SynchroBuffer was reached (current size is " + buffer.size() + ") - FIFO was applied to the buffer.");
          warningCounter = WARNING_FREQUENCY;
      }
      else {
          warningCounter = warningCounter - 1;
      }
  }
  
  /** Push a collection of objects into the buffer.
   * @param collection the collection of objects to push
   */
  public void push(Collection collection) {
    if (isClosed()) {
      LOGGER.debug("synchrocol isClosed - Exception");
      throw new IllegalArgumentException("buffer closed");
    }
    if ( (collection != null) && (collection.size() != 0) ) {
      synchronized(buffer) {
        if ( (duplicatePolicy != SynchroBuffer.DUPLICATE_DISCARD) && (duplicatePolicy != SynchroBuffer.DUPLICATE_REPLACE) ) {
            buffer.addAll(collection);            
            // if the buffer is too large, remove the same number of old objects as those just added, and log warning
            if (capacity != INFINITE_CAPACITY && buffer.size() > capacity) {
                //always log if collection is too large to add to buffer without overflow
                LOGGER.warn("The maximum capacity of the SynchroBuffer was reached (current size is " + buffer.size() + ") - FIFO was applied to the buffer.");
                while (buffer.size() > capacity) {
                    buffer.remove(0);
                }
            }
        } else {
          Iterator iterator = collection.iterator();
          while (iterator.hasNext()) {
            push(iterator.next());
          }
        }
      }
    }
  }
    
  /** Set the buffer consumer listener.
   * @param listener the listener
   */
  public void setSynchroBufferListener(SynchroBufferListener listener) {
    LOGGER.debug("synchro listener");
    this.listener = listener;
  }
    
  /** Enable the listener. The listener is disabled by default.
   */
  public void enable() {
    LOGGER.debug("enable listener");
    setEnabled(true);
  }

  /** Disable the listener. Pushed object are kept in the buffer and delivered when the listener is enabled.
   */
  public void disable() {
    LOGGER.debug("disable listener");
    setEnabled(false);
  }

  private boolean isClosed() {
    return closed;    
  }
       
  private void setClosed(boolean value) {
    LOGGER.debug("synchro setClosed: " + value);
    closed = value;
  }

  private void setFiring(boolean value) {
    firing = value;    
  }
    
  private boolean isFiring() {    
    return firing;    
  }
    
  private boolean isEmpty() {
    synchronized(buffer) {
      return buffer.isEmpty();
    }
  }
    
  private boolean isEnabled() {   
    return enabled;
  }
    
  private void setEnabled(boolean value) {
    enabled = value;
  }
  
  /**
   * Return the number of objects in the buffer.
   * @return the current size of the buffer
   */
  public final int getSize() {
      synchronized (buffer) {
          return buffer.size();
      }
  }
  
  /**
   * Empties the SynchroBuffer of all it's current content.
   */
  public final void empty() {
      synchronized (buffer) {
          buffer.clear();
          bufferMap.clear();
      }
  }

  /** 
   * Close the buffer and deallocate resources. Waits for the
   * buffer to empty in all cases. Empty the buffer first if
   * the listener may not be able to treat requests.
   * 
   * If the thread is a daemon thread, this method does not wait for the firing
   * thread to finish, as it may be frozen and we wish to release
   * this thread.
   */
  public void close() {
    LOGGER.debug("synchro close");
    setClosed(true);
    while (!isEmpty() || isFiring() && !checkingThread.isDaemon()) {
      try {
        Thread.sleep(minWindowSize);
      } catch (Exception e) {LOGGER.debug("Exception");}
    }
  }
 
  private final class CheckingThread extends Thread {
    
    public CheckingThread() {
      super();
    }
    
    public CheckingThread(String name) {
      super(name);
    }
    
    public void run() {
      float objects_per_sec;
      long calculated_window_size;
      long firing_time = 0;
      long wait_time = minWindowSize;
      LOGGER.debug("synchro checkingThread");
      while ( (!isClosed()) || (!isEmpty() && isEnabled()) ) {
        if (isEnabled()) {
            objects_per_sec = (1000 * buffer.size()) / (wait_time + firing_time);
          calculated_window_size = minWindowSize + ((long)(windowGrowthFactor * objects_per_sec));
          wait_time = ( (calculated_window_size < maxWindowSize) ? calculated_window_size : maxWindowSize );
          firing_time = fire();
          try {
            Thread.sleep(wait_time);
          } catch (InterruptedException ie) {LOGGER.debug("InterruptedException", ie);}
        } else {
          try {
            Thread.sleep(maxWindowSize);
          } catch (InterruptedException ie) {LOGGER.debug("InterruptedException", ie);}
        }
      }
    }
  }
}



