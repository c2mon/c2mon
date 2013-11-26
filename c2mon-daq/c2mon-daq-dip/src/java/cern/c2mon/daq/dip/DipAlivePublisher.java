package cern.c2mon.daq.dip;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import cern.dip.Dip;
import cern.dip.DipData;
import cern.dip.DipException;
import cern.dip.DipFactory;
import cern.dip.DipPublication;
import cern.dip.DipPublicationErrorHandler;
import cern.dip.DipTimestamp;
import cern.dip.TypeMismatch;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.tim.shared.common.datatag.address.DIPHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Publishes the Equipment alive message to DIP, so that it can be read back
 * and sent to the server.
 * 
 * <p>The alive tag must be provided as ISourceDataTag (usually obtained from the EquipmentConfiguration).
 * The publication is made on the DIP publication topic specified in the Tag address. The system
 * timestamp is passed in the "value" field (although this is not currently used on the server for
 * monitoring for alive expiration).
 * 
 * <p>On connection problems, this publisher tries to reconnect to DIP at 5s intervals.
 *  
 * @author Mark Brightwell
 *
 */
public class DipAlivePublisher extends TimerTask implements DipPublicationErrorHandler {

  /**
   * Logger for this DAQ implementation.
   */
  private EquipmentLogger equipmentLogger;
  
  /**
   * Waits between reconnect attempts.
   */
  private static final long CONNECT_RETRY_INTERVAL = 5000;
   
  /**
   * Is the alive timer publisher correctly connected
   * to DIP and publishing the alive. 
   */  
  private volatile AtomicBoolean reconnecting = new AtomicBoolean(false);
  
  /**
   * The interval between alive publications.
   */
  private long aliveInterval;
  
  /**
   * The timer on which the publication tasks are scheduled.
   */
  private Timer aliveTimer;
  
  /**
   * The alive tag.
   */
  private ISourceDataTag aliveTag;
  
  /**
   * The name of the Equipment (used for subscription id).
   */
  private String equipmentName;
  
  /**
   * The factory for creating publications.
   */
  private DipFactory dipFactory;
  
  /**
   * The publication service.
   */
  private DipPublication dipPublication;
  
  /**
   * Constructor.
   * @param equipmentName name of the Equipment (only used in publication id).
   * @param aliveTag the alive tag
   * @param aliveInterval the interval between publications
   * @param equipmentLogger the EquipmentLogger used to log all log messages
   */
  protected DipAlivePublisher(final String equipmentName, final ISourceDataTag aliveTag, 
                                    final long aliveInterval, final EquipmentLoggerFactory equipmentLoggerFactory) {
    this.aliveTag = aliveTag;    
    this.aliveInterval = aliveInterval;
    this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());;
    this.equipmentName = equipmentName;
    if (aliveTag != null) {
      if (aliveTag.getHardwareAddress() instanceof DIPHardwareAddress) {
        initTimer();
        try {
          initDipConnection();
        } catch (DipException e) {
          equipmentLogger.error("Unable to initialize DIP alive connection; will try again in " + CONNECT_RETRY_INTERVAL + "milliseconds.");
          startReconnectThread();
        }     
      } else {
        equipmentLogger.error("Alive Tag has incorrect HardwareAddress type: unable to initialize the DIP Equipment alive publication.");
        aliveTimer = null;
      }
    } else {
      equipmentLogger.warn("No alive tag defined for this DIP Equipment - unable to initialize a DIP Equipment alive publication.");
      aliveTimer = null;
    }
  }
  
  
  /**
   * Assume hardware address is correct type.
   * @throws DipException if thrown by DIP library
   */
  private void initDipConnection() throws DipException {      
    dipFactory = Dip.create(equipmentName + "_ALIVE_" + System.currentTimeMillis());
    dipPublication = dipFactory.createDipPublication(((DIPHardwareAddress) aliveTag.getHardwareAddress()).getItemName(), this);   
  }

  /**
   * Publishes the alive.
   */
  @Override
  public void run() {  
    long millis = System.currentTimeMillis();
    DipTimestamp dipTimestamp = new DipTimestamp(millis);
    DipData dipData = dipFactory.createDipData();    
    try {
      dipData.insert(millis);
    } catch (TypeMismatch e) {
      equipmentLogger.error("DIP Type mismatch on creating DIP Equipment alive update - unable to publish.", e);
    }
    try {
      dipPublication.send(dipData, dipTimestamp);
    } catch (DipException e) {
      equipmentLogger.error("DipException when attempting to publish DIP Equipment alive - resetting alive DIP connection in " 
          + CONNECT_RETRY_INTERVAL + "millseconds.", e);
      stop();
      startReconnectThread();
    }
  }
  
  /**
   * Resets the timer without scheduling any task.
   */
  private void initTimer() {
    aliveTimer = new Timer("DIP alive timer");
  }
  
  /**
   * Starts the alive publication (does nothing if DipAlivePublisher not initialized successfully)
   */
  public void start() {
    if (aliveTimer != null) {
      aliveTimer.scheduleAtFixedRate(this, 0, aliveInterval);
    }
  }
  
  /**
   * Stops the alive publication. Can be restarted with the start method.
   * Does nothing if already stopped or not initialized successfully.
   */
  public void stop() {
    if (aliveTimer != null) {
      aliveTimer.cancel();
    }    
  }

  /**
   * Stops alive mechanism, attempts to reconnect to DIP, restarts alive.
   */
  @Override
  public void handleException(final DipPublication publication, final DipException ex) {
    equipmentLogger.error("Exception caught by DipPublicationErrorHandler - resetting connection.", ex);
    startReconnectThread();
  }

  /**
   * Stops alive mechanism, attempts to reconnect to DIP, restarts alive, all
   * in a separate thread, after a short sleep. Synchronized.
   */
  private synchronized void startReconnectThread() {
    if (reconnecting.compareAndSet(false, true)) {     
      new Thread(new Runnable() {        
        @Override
        public void run() {
          try {
            stop();
            Thread.sleep(CONNECT_RETRY_INTERVAL);            
            initDipConnection();
            initTimer();
            start();            
          } catch (Exception e) {
            equipmentLogger.error("Exception caught in alive publication reconnection thread - "
             + "attempting to restart alive publication in " + CONNECT_RETRY_INTERVAL + "milliseconds");            
          }          
          reconnecting.set(false);
        }
      });
    }
  }
  
}
