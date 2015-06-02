package cern.c2mon.daq.db;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The DBController is used for helping the Message Handler when connecting/disconnecting from source or
 * when doing dynamic reconfiguration of Data Tags
 *
 * @author Nacho Vilches
 *
 */
public class DBController {

  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * The equipment configuration of this handler.
   */
  private IEquipmentConfiguration equipmentConfiguration;

  /**
   * The equipment message sender to send to the server.
   */
  private IEquipmentMessageSender equipmentMessageSender;

  /**
   * A counter indicating the amount of alerts sent to the server
   * */
  private final Map<Long, Integer> alertsSent = Collections.synchronizedMap(new HashMap<Long, Integer>());
  /**
   * A counter indicating the amount of invalid data tags sent to the server
   * */
  private final Map<Long, Integer> invalidSent = Collections.synchronizedMap(new HashMap<Long, Integer>());

  /**
   * An access object for registering/unregistering for alerts and receiving
   * them
   * */
  private IDbDaqDao dbDaqDao;

  /**
   * Flag indicating if the DAQ process is connected to the database
   * */
  private volatile boolean connected = false;

  /**
   * Flag indicating if the DAQ process is running
   * */
  private volatile boolean running = false;

  /**
   * A queue of alerts received from the DB that should be processed
   * */
  private LinkedList<Alert> alertsQueue;

  /**
   * The equipment alive timer
   * */
  private Timer eqAliveTimer;

  /**
   * Timer task for running configurations
   */
  private Timer confTimer;
  
  /**
   * 
   */
  private Timer logTimer = new Timer();

  private Boolean isUnRegistered = Boolean.TRUE;

  /**
   * The frequency with which the amount of processed dataTags is written to
   * the logs
   * */
  private static final long LOGGING_INTERVAL = 60000;

  /**
   * fake Data Tag ID use for waking up the wait_any thread
   * and subscribe again all alerts in the data base
   */
  public static final long RECONFIGURATION_TAG_ID = 0L;

  /**
   * Reconfiguration Data Tag Item name
   */
  public static final String RECONFIGURATION_TAG_ITEM_NAME = "DB.DAQ.RECONFIGURATION:INTERNAL_USE_ONLY";

  /**
   * Constructor
   *
   */
  public DBController(IDbDaqDao dbDaqDao, EquipmentLoggerFactory equipmentLoggerFactory,
      IEquipmentConfiguration equipmentConfiguration, IEquipmentMessageSender equipmentMessageSender) {
    this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
    this.equipmentConfiguration = equipmentConfiguration;
    this.equipmentMessageSender = equipmentMessageSender;
    this.dbDaqDao = dbDaqDao;
    this.running = true;

    if (this.alertsQueue == null) {
        this.alertsQueue = new LinkedList<Alert>();
    }
    else {
        this.alertsQueue.clear();
    }
  }

  /**
   * Connection
   *
   * @param sourceDataTag
   * @param changeReport
   *
   * @return CHANGE_STATE SUCCESS or FAIL
   */
  public CHANGE_STATE connection(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
    try {
		if (connection(sourceDataTag, changeReport, this.dbDaqDao.getDataTags()) == CHANGE_STATE.FAIL) {
		  return CHANGE_STATE.FAIL;
		}
    } catch (EqIOException e) {
    	this.equipmentLogger.error("connection - " + e.getCause().getMessage(), e);

    	return CHANGE_STATE.FAIL;
    }

    // Send reconfiguration Alert (waiting till the end of the reconfiguration process)
//    System.out.println("Alert inserting");
    confTimerTask();

    return CHANGE_STATE.SUCCESS;
  }

  /**
   * Configuration procedure timer task which sends the Reconfiguration Alert after applying it
   * */
  private void confTimerTask() {

      if (this.confTimer != null)
          this.confTimer.cancel();
      this.confTimer = new Timer();

      final long interval = 10000L;
      final long nextExec = 15000L;

      TimerTask task = new TimerTask() {
          @Override
          public void run() {
              equipmentLogger.info("confTimerTask - Reconfiguring Task in progress");
//              System.out.println("confTimerTask - Reconfiguring Task in progress");

              // Send alert for reconfiguration for awaking the Listener Thread
              dbDaqDao.updateDataTagValue(RECONFIGURATION_TAG_ID, "0");

              // Cancel this task
              this.cancel();
          }
      };
      this.confTimer.schedule(task, interval, nextExec);
  }


  /**
   * Connection
   *
   * @param sourceDataTag
   * @param changeReport
   * @param registeredDataTags
   *
   * @return CHANGE_STATE SUCCESS or FAIL
   * @throws EqIOException
   */
  public CHANGE_STATE connection(ISourceDataTag sourceDataTag, ChangeReport changeReport, List<Long> registeredDataTags) throws EqIOException {
	  if (sourceDataTag.getHardwareAddress() instanceof DBHardwareAddress) {
		  try {
			  Long dataTagId = sourceDataTag.getId();
			  getEquipmentLogger().trace("connection - Connecting with Datatag: " + dataTagId);

			  // Init counters for alerts and invalid sends
			  this.alertsSent.put(dataTagId, 0);
			  this.invalidSent.put(dataTagId, 0);

			  // Inserting data data on the data base if it does not exist yet
			  if (!registeredDataTags.contains(dataTagId)) {
				  getEquipmentLogger().trace("connection - Inserting Data Tag: " + dataTagId);
				  insertMissingDataTag(dataTagId, changeReport);
			  }
			  // It exists. Update the Data Tag Hardware Address Item Name and the Data Type on the data base if it is necessary
			  else {
				  ISourceDataTag sdt = getEquipmentConfiguration().getSourceDataTags().get(dataTagId);
				  // Take info from data base
				  getEquipmentLogger().trace("connection - : getItemNameAndDataType " + dataTagId);
				  DBDAQConfigInfo dbDAQConfigInfo = this.dbDaqDao.getItemNameAndDataType(dataTagId);

				  if ((dbDAQConfigInfo != null) && (sdt != null)) {
					  // Compare Item Name from data base with the one on the config
					  String newItemName = ((DBHardwareAddress) sdt.getHardwareAddress()).getDBItemName();
					  String oldItemName = dbDAQConfigInfo.getDBItemName();
					  if (!oldItemName.equals(newItemName)) {
						  getEquipmentLogger().trace("connection - : updateDataTagItemName (" + newItemName + ", " + oldItemName + ")");
						  updateDataTagItemName(dataTagId, changeReport, newItemName, oldItemName);
					  }

					  // Compare Data Type from data base with the one on the config
					  String newDataType = sdt.getDataType();
					  String oldDataType = dbDAQConfigInfo.getDataType();
					  if (!oldDataType.equals(newDataType)) {
						  getEquipmentLogger().trace("connection - : updateDataTagDataType (" + newDataType + ", " + oldDataType + ")");
						  updateDataTagDataType(dataTagId, changeReport, newDataType, oldDataType);
					  }
				  }
			  }
			  getEquipmentLogger().trace("connection - Exiting: " + dataTagId);

		  } catch (DuplicateKeyException de) {
			  // Invalidate
			  String description =  de.getCause().getMessage().replaceAll("\n", "") + ". Manual DB intervention is required."
					  + " Please contact Admin Support.";

			  this.equipmentLogger.error("connection - " + de.getCause().getMessage(), de);
			  getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, description);

			  return CHANGE_STATE.FAIL;
		  } catch (DataAccessException dae) {
			  // Invalidate
			  String description =  dae.getCause().getMessage().replaceAll("\n", "") + ". Unexpected DB exception caught."
					  + " Please contact Admin Support";

			  this.equipmentLogger.error("connection - " + dae.getCause().getMessage().replaceAll("\n", ""), dae);
			  getEquipmentMessageSender().sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, description);

			  return CHANGE_STATE.FAIL;
		  }
	  } else {
		  String errorMsg = "connection - Unsupported HardwareAddress: " + sourceDataTag.getHardwareAddress().getClass();
		  throw new EqIOException(errorMsg);
	  }

	  return CHANGE_STATE.SUCCESS;
  }

  /**
   * Add new datatags to the equipment (insert them in the table).
   *
   * It is use to check if the XML configuration received
   * from the server contains datatags that are not present in the
   * equipment table (db_daq), and if so, inserts them.
   *
   * @param dataTagId The data tag id
   * @param changeReport
   */
  private void insertMissingDataTag(Long dataTagId, ChangeReport changeReport) {
    ISourceDataTag sdt = getEquipmentConfiguration().getSourceDataTags().get(dataTagId);
    String name = ((DBHardwareAddress) sdt.getHardwareAddress()).getDBItemName();
    String type = sdt.getDataType();
    String value = "0";
    getEquipmentLogger().info("insertMissingDataTag - Inserting missing datatag: " + dataTagId + " - " + name);
    if (changeReport != null) {
      changeReport.appendInfo("insertMissingDataTag - Inserting missing datatag: " + dataTagId + " - " + name);
    }
    this.dbDaqDao.insertNewDataTag(dataTagId, name, value, type, SourceDataQuality.OK, null);
  }

  /**
   * Add the Reconfiguration Data Tag to the DDBB
   */
  public void insertReconfigurationDataTag() {
	    getEquipmentLogger().info("insertReconfigurationDataTag - Inserting reconfiguration datatag: " + RECONFIGURATION_TAG_ID + " - " + RECONFIGURATION_TAG_ITEM_NAME);
	    this.dbDaqDao.insertNewDataTag(RECONFIGURATION_TAG_ID, RECONFIGURATION_TAG_ITEM_NAME, "0", "Boolean", SourceDataQuality.OK, null);


	  }

  /**
   * Updates the Data Tag Hardware Adress Item Name on the data base
   *
   * @param dataTagId The data tag id
   * @param changeReport
   * @param newItemName The new Item Name read from the configuration
   * @param oldItemName The old Item Name read from the data base
   */
  private void updateDataTagItemName(Long dataTagId, ChangeReport changeReport, String newItemName, String oldItemName) {
    getEquipmentLogger().info("updateDataTagItemName - Updating datatag Item Name: " + dataTagId + " - " + newItemName + "(new) - "
        + oldItemName + "(old)");
    if (changeReport != null) {
      getEquipmentLogger().info("updateDataTagItemName - Updating datatag Item Name: " + dataTagId + " - " + newItemName + "(new) - "
          + oldItemName + "(old)");
    }
    this.dbDaqDao.updateDataTagItemName(dataTagId, newItemName);
  }

  /**
   * Updates the Data Tag Data Type on the data base
   *
   * @param dataTagId The data tag id
   * @param changeReport
   * @param newDataType The new Data Type read from the configuration
   * @param dolDataType The old Data Type read from the data base
   */
  private void updateDataTagDataType(Long dataTagId, ChangeReport changeReport, String newDataType, String oldDataType) {
    getEquipmentLogger().info("updateDataTagDataType - Updating datatag Data Type: " + dataTagId + " - " + newDataType + "(new) - "
        + oldDataType + "(old)");
    if (changeReport != null) {
      getEquipmentLogger().info("updateDataTagDataType - Updating datatag Data Type: " + dataTagId + " - " + newDataType + "(new) - "
          + oldDataType + "(old)");
    }
    this.dbDaqDao.updateDataTagDataType(dataTagId, newDataType);
  }

  /**
   * Disconnection
   *
   * @param sourceDataTag
   * @param changeReport
   *
   * @return CHANGE_STATE SUCCESS or FAIL
   */
  public CHANGE_STATE disconnection(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
	  Long dataTagId = sourceDataTag.getId();
	  getEquipmentLogger().trace("disconnection - Disconnecting Datatag: " + dataTagId);

	  // remove data tag from DDBB
	  this.dbDaqDao.deleteDataTag(dataTagId);

	  // Send reconfiguration Alert (waiting till the end of the reconfiguration process)
//	  System.out.println("Alert deleting");
	  confTimerTask();

	  getEquipmentLogger().trace("disconnection - Exiting: " + dataTagId);

	  return CHANGE_STATE.SUCCESS;
  }

  /**
   * Processes a single alert. Extracts the name of the alert (==id of the
   * datatag), the value and timestamp, and sends them to TIM server. In case
   * the quality of the datatag is low (SourceDataQuality.UNKNOWN) or the
   * value failed the conversion to its datatype, the datatag is invalidated.
   *
   * @param alert
   *            alert to be sent to the server
   * */
  protected void processAlert(final Alert alert) {
      Long dataTagId = alert.getId();
      if (!getEquipmentConfiguration().getSourceDataTags().containsKey(dataTagId)) {
          this.equipmentLogger.warn("processAlert - An alert was received for a not monitored data tag.");
          return;
      }
      this.equipmentLogger.info("processAlert - Sending datatag: " + alert);
      ISourceDataTag sdt = getEquipmentConfiguration().getSourceDataTags().get(dataTagId);
      if (alert.getQuality() == SourceDataQuality.UNKNOWN) {
          getEquipmentMessageSender().sendInvalidTag(sdt, alert.getQuality(), alert.getQualityDescription(),
                  new Timestamp(alert.getClientTimestamp().getTime()));
          increaseSentInvalidDataTags(dataTagId);
          return;
      }
      Object sdtValue = TypeConverter.cast(alert.getDataTagValue(), sdt.getDataType());
      if (sdtValue == null) {
          this.equipmentLogger.info("processAlert - Conversion error! Got: " + alert.getDataTagValue() + ", expected:" + sdt.getDataType());
          getEquipmentMessageSender().sendInvalidTag(sdt, SourceDataQuality.CONVERSION_ERROR, "", new Timestamp(alert.getClientTimestamp().getTime()));
          increaseSentInvalidDataTags(dataTagId);
          return;
      } else {
          getEquipmentMessageSender().sendTagFiltered(sdt, sdtValue, alert.getTimestamp().getTime());
          increaseAllSentDataTags(dataTagId);
      }
  }

  /**
   * Gets the current value of the given dataTag from the database and sends it to the server.
   * @param dataTagId the id of the data tag
   * */
  protected void refreshDataTag(final long dataTagId) {
      this.equipmentLogger.info("refreshDataTag - Refreshing data tag " + dataTagId);

      Alert alert = dbDaqDao.getLastAlertForDataTagId(dataTagId);
      processAlert(alert);
  }

  /** Methods for loggging of the amount of sent datatags (valid and invalid) **/

  /**
   * Increases the counter of invalid data tag values sent for a given data
   * tag and the counter of all values sent for this data tag.
   *
   * @param dataTagId
   *            id of a datatag
   * */
  private void increaseSentInvalidDataTags(final long dataTagId) {
      int i = getInvalidSent().get(dataTagId);
      getInvalidSent().put(dataTagId, ++i);
      increaseAllSentDataTags(dataTagId);
  }

  /**
   * Increases the counter of all values sent for a given data tag.
   *
   * @param dataTagId
   *            id of a datatag
   * */
  private void increaseAllSentDataTags(final long dataTagId) {
      int i = getAlertsSent().get(dataTagId);
      getAlertsSent().put(dataTagId, ++i);
  }

  /**
   * @return the equipmentConfiguration
   */
  public IEquipmentConfiguration getEquipmentConfiguration() {
    return this.equipmentConfiguration;
  }

  /**
   * @return the equipmentLogger
   */
  public EquipmentLogger getEquipmentLogger() {
    return this.equipmentLogger;
  }

  /**
   *
   */
  public void setEquipmentLogger(final EquipmentLogger equipmentLogger) {
    this.equipmentLogger = equipmentLogger;
  }

  /**
   * @return the equipmentMessageSender
   */
  public IEquipmentMessageSender getEquipmentMessageSender() {
    return this.equipmentMessageSender;
  }

  /**
   *
   * @return the alertsSent
   */
  public Map<Long, Integer> getAlertsSent() {
    return this.alertsSent;
  }

  /**
   *
   * @return the invalidSent
   */
  public Map<Long, Integer> getInvalidSent() {
    return this.invalidSent;
  }

  /**
   *
   */
  public void startAlertPorcessor() {
 // The processor thread is responsible for removing the alerts from the queue and sending them to the server.
    Thread processor = new Thread(new Runnable() {
        @Override
        public void run() {
            //while (true) {
            while (running) {
                while (connected) {
                    synchronized (alertsQueue) {
                        if (alertsQueue.isEmpty()) {
                            //System.out.println("Queue is empty");
                            try {
                                alertsQueue.wait();
                            } catch (InterruptedException e) {
                                equipmentLogger.warn("Wait on alertsQueue interrupted.", e);
                            }
                        }
                        Alert a = alertsQueue.poll();
                        //System.out.println("Taken from the queue");
                        processAlert(a);
                    }
                }
//                System.out.println("startAlertPorcessor - Disconnected");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    equipmentLogger.warn("Waiting for the connection to the database interrupted.", e);
                }
            }
        }
    });
    processor.start();
    this.equipmentLogger.info("Processor thread started");
  }

  /**
   *
   */
  public void startAlertListener() {
    // The connector thread is responsible for listening for new alerts and putting them to the queue.
    Thread connector = new Thread(new Runnable() {
        @Override
        public void run() {

            while (running && !connected) {
                try {
                    registerForAlerts();
                    setConnected();
                    refreshAllDataTags();
                    startLoggingSentDataTags();
                    while (connected) {
                    	try {
//                    		System.out.println("Waiting for any...");
                    		Alert a = dbDaqDao.waitForAnyAlert(Alert.MAX_TIMEOUT);
//                    		System.out.println("Got alert! " + a.getName() + ", " + a.getValue());

                    		if(a.getId() == RECONFIGURATION_TAG_ID) {
                    			// If there is a reconfiguration we subscribe again to all alerts (old+new)
                    			setDisconnected(false);
                                unregisterAlerts();
                                equipmentLogger.info("startAlertListener - Reconfiguration has been done. Registering again ...");
//                            	System.out.println("startAlertListener - Reconfiguration has been done. Registering again ...");
                            } else {
                            	// If is a normal alert we send to the queue for processing
                            	synchronized (alertsQueue) {
                        			alertsQueue.add(a);
                        			alertsQueue.notify();
                        		}
                            }
                        } catch (AlertTimeOutException ex) {
                        	equipmentLogger.warn("startAlertListener - Starting to wait again...", ex);
//                        	System.out.println("Starting to wait again..." + ex);
                        }
                    }
                    equipmentLogger.trace("startAlertListener - Disconnected");
//                    System.out.println("startAlertListener - Disconnected");
                } catch (UncategorizedSQLException e) {
                    setDisconnected();
                    unregisterAlerts();
                    equipmentLogger.error("startAlertListener - SQLException caught. Trying to reconnect to the db.", e);
//                    System.out.println("SQLException caught. Trying to reconnect to the db." + e);
                } catch (Exception e) {
                    setDisconnected();
                    unregisterAlerts();
                    equipmentLogger.error("startAlertListener - Unexpected exception caught. Trying to reconnect to the db.", e);
//                    System.out.println("Unexpected exception caught. Trying to reconnect to the db." + e);
                }

                // Sleep for 5 seconds before trying again to
                // reestablish the connection
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    equipmentLogger.warn("Waiting for reestablishing the connection to the database interrupted.", e);
                    e.printStackTrace();
                }
            }

        }
    });
    connector.start();
    this.equipmentLogger.info("Connector thread started");
  }

  /**
   * Sets the connected flag to true. Resets the counters for the amount of sent datatags.
   * Initiates the equipment alive and confirms the state to the server.
   * */
  public void setConnected() {
    this.connected = true;
    for (Long alertId : this.alertsSent.keySet()) {
      this.alertsSent.put(alertId, 0);
      this.invalidSent.put(alertId, 0);
    }
    initAlive();
    getEquipmentMessageSender().confirmEquipmentStateOK("setConnected - Connected to the database");
  }

  /**
   * Sets the connected flag to false. Stops sending the alive tag and confirms the incorrect state to the server.
   * */
  private void setDisconnected() {
      setDisconnected(true);
  }

  /**
   * Sets the connected flag to false and optionally sends a commfault tag to the server. Used during
   * reconfiguration.
   * */
  private void setDisconnected(boolean sendCommFault) {
      this.connected = false;
      cancelAlive();
      if (sendCommFault) {
        getEquipmentMessageSender().confirmEquipmentStateIncorrect("setDisconnected - The connection to the database is closed.");
      }
  }

  /**
   * Initiates sending of equipment alive tag.
   * */
  private void initAlive() {
	  if (eqAliveTimer != null)
		  eqAliveTimer.cancel();
	  eqAliveTimer = new Timer();

	  final long interval = getEquipmentConfiguration().getAliveTagInterval();

	  TimerTask task = new TimerTask() {
		  @Override
		  public void run() {
			  getEquipmentMessageSender().sendSupervisionAlive();
			  equipmentLogger.info("Equipment alive sent to server.");
		  }
	  };
	  eqAliveTimer.schedule(task, 0, interval);
  }

  /**
   * Cancels the alive tag.
   * */
  private void cancelAlive() {
      eqAliveTimer.cancel();
  }

  /**
   * Registers for all alerts for the monitored datatags.
   * */
  private void registerForAlerts() {
      
      synchronized (isUnRegistered) {
        if (isUnRegistered) {
            this.equipmentLogger.info("registerForAlerts - Registering for alerts (" + getEquipmentConfiguration().getSourceDataTags().size() + ")");
            for (long alertId : getEquipmentConfiguration().getSourceDataTags().keySet()) {
                try {
                  registerForAlert(alertId);
                } catch (DataAccessException dae) {
                    this.equipmentLogger.error("registerForAlerts - " + dae.getCause().getMessage(), dae);
                }
            }
            registerForAlert(RECONFIGURATION_TAG_ID);
            isUnRegistered = Boolean.FALSE;
        } else {
            this.equipmentLogger.error("registerForAlerts - called, but already registered to all alerts. Doing nothing..");
        }
    }
  }

  /**
   * Unregisters all the alerts for the monitored datatags. Closes the
   * connection to the database.
   * */
  private void unregisterAlerts() {
      
    synchronized (isUnRegistered) {
      if (!isUnRegistered) {
        this.equipmentLogger.info("unregisterAlerts - Unregistering alerts (" + getEquipmentConfiguration().getSourceDataTags().size() + ")");
        for (Long alertId : getEquipmentConfiguration().getSourceDataTags().keySet()) {
          try {
            unregisterFromAlert(alertId);
          } catch (DataAccessException dae) {
              this.equipmentLogger.error("unregisterAlerts - " + dae.getCause().getMessage(), dae);
          }
        }
        isUnRegistered = Boolean.TRUE;
      } else {
          this.equipmentLogger.error("unregisterAlerts - called, but already unregistered from all alerts. Doing nothing..");
      }

      unregisterFromAlert(RECONFIGURATION_TAG_ID);
    }
      
  }

  /**
   * Registers for the given alert
   *
   * @param alertId
   *
   */
  private void registerForAlert(long alertId) {
    getEquipmentLogger().trace("registerForAlert - Registering for Alert: " + alertId);
    this.dbDaqDao.registerForAlert(Long.toString(alertId));
  }

  /**
   * Unregisters for the given alert. Closes the
   * connection to the database.
   *
   * @param alertId
   */
  private void unregisterFromAlert(long alertId) {
    getEquipmentLogger().trace("unregisterFromAlert - Unregistering from Alert: " + alertId);
    dbDaqDao.unregisterFromAlert(Long.toString(alertId));
  }

  /**
   * Disconnects from the database and stops the DAQ process
   */
  public void disconnectFromDataSource() {
    this.equipmentLogger.info("disconnectFromDataSource - called.");
    if (this.running && this.connected) {
      unregisterAlerts();
      setDisconnected();
    }
    this.running = false;
    this.equipmentLogger.info("disconnectFromDataSource - Not running");
  }

  /**
   * Gets the current values of all datatags from the database and sends them to the server.
   * */
  public void refreshAllDataTags() {
    List<Long> dataTagIds = new ArrayList<Long>(getEquipmentConfiguration().getSourceDataTags().keySet());
    this.equipmentLogger.info("refreshAllDataTags - Refreshing all data tags ");
    List<Alert> alerts = dbDaqDao.getLastAlerts(dataTagIds);
    synchronized (this.alertsQueue) {
      for (Alert a : alerts) {
        this.alertsQueue.add(a);
      }
      this.alertsQueue.notify();
    }
  }

  /**
   * Starts the logging thread, which writes to the logs the amount of values
   * (valid and invalid) sent for every monitored data tag since the start of
   * the daq process. It writes to the log file every LOGGING_INTERVAL
   * (milliseconds).
   * */
  private void startLoggingSentDataTags() {
      
      logTimer.cancel();
      logTimer = new Timer();
      
      TimerTask task = new TimerTask() {
          @Override
          public void run() {
              showAmountOfProcessedAlerts();
          }
      };
      logTimer.schedule(task, 0, LOGGING_INTERVAL);
  }

  /**
   * Logs the amount of alerts (valid and invalid) that were sent to the
   * server ever since the daq was started
   * */
  private void showAmountOfProcessedAlerts() {
    if (this.equipmentLogger.isTraceEnabled()) {
      int globalTotal = 0;
      StringBuilder msg = new StringBuilder("Processed alerts: [ ");
      for (Entry<Long, Integer> e : this.alertsSent.entrySet()) {
          if (e.getKey() > 0)
              msg.append(e.getKey()).append("=").append(e.getValue()).append("; ");
          globalTotal += e.getValue();
      }
      msg.append(" ]");
      this.equipmentLogger.trace(msg);
      msg = new StringBuilder("Processed invalid: [ ");
      for (Entry<Long, Integer> e : this.invalidSent.entrySet()) {
          if (e.getKey() > 0)
              msg.append(e.getKey()).append("=").append(e.getValue()).append("; ");
          globalTotal += e.getValue();
      }
      msg.append(" ]");
      this.equipmentLogger.trace(msg);
      this.equipmentLogger.trace("Total sent: " + globalTotal);
    }
  }

}
