package cern.c2mon.daq.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * This class represents the DB DAQ. It establishes the connection with the TIM
 * DB DAQ dedicated account and manages the forwarding of the database
 * notifications to the business layer. It gets from the XML configuration the
 * address of the DB account to which it should register for alerts. The
 * incoming alerts contain updates on monitored datatag values. They are
 * propagated to the TIM server.
 * 
 * <p>Synchronization is used. The main thread starts two other threads: the connector and the processor.
 * They synchronize on the alertsQueue and they use the connected flag to monitor the status of the db connection.
 * The connector listens for the alerts and puts them in the queue, the processor polls them from the queue and sends
 * to the server. The connector refreshes also the values of the datatags every time the connection is lost and established again.
 * The refreshDataTags is also called by the main thread in daq core right after the connectToDataSource, and so 
 * the access to dao object dbDaqDao (used for the refresh) also has to be synchronized. 
 * 
 * @author Aleksandra Wardzinska
 * @author Nacho Vilches
 */
public class DBMessageHandler extends EquipmentMessageHandler {
    
    /**
     * The equipment logger of this class.
     */
    private EquipmentLogger equipmentLogger;
    
    /**
     * Separator used between the property name and property value in the
     * equipment (DB) address
     * */
    public static final String VALUE_SEPARATOR = "=";
    /**
     * Separator used between the property/value tokens of the equipment (DB)
     * address
     * */
    public static final String PROPERTY_SEPARATOR = ";";
    /**
     * Name of the property holding the url used to connect to the database
     * */
    public static final String DB_URL = "dbUrl";
    /**
     * Name of the property holding the username of the DB account
     * */
    public static final String DB_USERNAME = "dbUsername";
    /**
     * Name of the property holding the password of the DB account
     * */
    public static final String DB_PASSWORD = "dbPassword";
    /**
     * The frequency with which the amount of processed dataTags is written to
     * the logs
     * */
    private static final long LOGGING_INTERVAL = 60000;
    /**
     * A table with keys for database properties
     * */
    private String[] dbKeys = { DB_URL, DB_USERNAME, DB_PASSWORD };
    /**
     * Contains the database address fields: name, url, username and password
     * */
    private Map<String, String> dbAddress;
    /**
     * The equipment alive timer
     * */
    private Timer timer;
    /**
     * A queue of alerts received from the DB that should be processed
     * */
    private LinkedList<Alert> alertsQueue;
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
     * DB controller
     */
    private DBController dbController;

    /**
     * Setter for dbDaqDao object
     * 
     * @param dbDaqDao
     *            access object for registering/unregistering for alerts
     * */
    public void setDbDaqDao(final IDbDaqDao dbDaqDao) {
        this.dbDaqDao = dbDaqDao;
    }

    /**
     * Getter for dbDaqDao object
     * 
     * @return dbDaqDao object
     * */
    public IDbDaqDao getDbDaqDao() {
        return this.dbDaqDao;
    }

    /**
     * Connects to the data source. Prepares and validates data needed to establish the connection.
     * Inserts the missing datatags in the datatag table on the equipment db account. 
     * Starts the connector thread which takes care of the connection with the db, refreshing of the values
     * of the datatags and receiving of alerts and the processor thread which sends the alerts to the server. 
     * 
     * @throws EqIOException
     *             if unsupported equipment type
     * */
    @Override
    public void connectToDataSource() throws EqIOException {
        this.equipmentLogger = getEquipmentLogger(DBMessageHandler.class);
        
        this.running = true;
        setDBDataSourceAddress();

        if (alertsQueue == null) {
            alertsQueue = new LinkedList<Alert>(); 
        }
        else {
            alertsQueue.clear();
        }
        
        // Controller
        this.dbController = new DBController(this.dbDaqDao, getEquipmentLoggerFactory(), getEquipmentConfiguration(), 
                getEquipmentMessageSender());
        
        // Data Tag Changer
        DBDataTagChanger dataTagChanger = new DBDataTagChanger(this.dbController, getEquipmentLogger(DBDataTagChanger.class));
        getEquipmentConfigurationHandler().setDataTagChanger(dataTagChanger);
        
        if (getEquipmentConfiguration().getSourceDataTags().isEmpty()) {
          String errorMsg = "No datatags found in the configuration xml";
          this.equipmentLogger.error(errorMsg);
          throw new EqIOException(errorMsg);
        }

        // parse HardwareAddress of each registered dataTag
        List<Long> registeredDataTags = this.dbDaqDao.getDataTags();
        for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
            if (dataTag.getHardwareAddress() instanceof DBHardwareAddress) {
              try {
                this.dbController.connection(dataTag, null, registeredDataTags);
              } catch (DuplicateKeyException de) {
                // Invalidate
                String description =  de.getCause().getMessage().replaceAll("\n", "") + ". Manual DB intervention is required."
                    + " Please contact Admin Support.";

                this.equipmentLogger.error("connectToDataSource - " + de.getCause().getMessage(), de);
                getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, description);
              } catch (DataAccessException dae) {
                // Invalidate
                String description =  dae.getCause().getMessage().replaceAll("\n", "") + ". Unexpected DB exception caught." 
                    + " Please contact Admin Support";

                this.equipmentLogger.error("connectToDataSource - " + dae.getCause().getMessage().replaceAll("\n", ""), dae);
                getEquipmentMessageSender().sendInvalidTag(dataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, description);
              }
            } else {
                String errorMsg = "Unsupported HardwareAddress: " + dataTag.getHardwareAddress().getClass();
                throw new EqIOException(errorMsg);
            }
        }
        
        // The processor thread is responsible for removing the alerts from the queue and sending them to the server.
        Thread processor = new Thread(new Runnable() {
            @Override
            public void run() {
                //while (true) {
                while (running) {
                    while (connected) {
                        synchronized (alertsQueue) {
                            //System.out.println("Processor thread" + new Date());
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
                            dbController.processAlert(a);
                        }
                    }
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
                                // System.out.println("Waiting for any...");
//                                synchronized (dbDaqDao) {
                                    Alert a = dbDaqDao.waitForAnyAlert(Alert.MAX_TIMEOUT);
                                    // System.out.println("Got alert! " +
                                    // a.getName() + ", " + a.getValue());
                                    synchronized (alertsQueue) {
                                        alertsQueue.add(a);
                                        alertsQueue.notify();
//                                    }
                                }
                            } catch (AlertTimeOutException ex) {
                                equipmentLogger.warn("Starting to wait again...", ex);
                            }
                        }
                    } catch (UncategorizedSQLException e) {
                        setDisconnected();
                        equipmentLogger.error("SQLException caught. Trying to reconnect to the db.", e);
                    } catch (Exception e) {
                        setDisconnected();
                        equipmentLogger.error("Unexpected exception caught. Trying to reconnect to the db.", e);
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
      for (Long alertId : this.dbController.getAlertsSent().keySet()) {
        this.dbController.getAlertsSent().put(alertId, 0);
        this.dbController.getInvalidSent().put(alertId, 0);
      }
      initAlive();
      getEquipmentMessageSender().confirmEquipmentStateOK("setConnected - Connected to the database");
    }
    
    /**
     * Sets the connected flag to false. Stops sending the alive tag and confirms the incorrect state to the server.
     * */
    private void setDisconnected() {
        this.connected = false;
        cancelAlive();
        getEquipmentMessageSender().confirmEquipmentStateIncorrect("setDisconnected - The connection to the database is closed.");
    }
    
    /**
     * Registers for all alerts for the monitored datatags.
     * */
    private void registerForAlerts() {
        this.equipmentLogger.info("registerForAlerts - Registering for alerts (" + getEquipmentConfiguration().getSourceDataTags().size() + ")");
        for (long alertId : getEquipmentConfiguration().getSourceDataTags().keySet()) {
            try {
                this.dbController.registerForAlert(alertId);
            } catch (DataAccessException dae) {
                this.equipmentLogger.error("registerForAlerts - " + dae.getCause().getMessage(), dae);
            }
        }
    }
    
    /**
     * Initiates sending of equipment alive tag.
     * */
    private void initAlive() {

        if (timer != null)
            timer.cancel();
        timer = new Timer();

        final long interval = getEquipmentConfiguration().getAliveTagInterval();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                getEquipmentMessageSender().sendSupervisionAlive(System.currentTimeMillis());
                equipmentLogger.info("Equipment alive sent to server.");
            }
        };
        timer.schedule(task, 0, interval);
    }

    /**
     * Cancels the alive tag.
     * */
    private void cancelAlive() {
        timer.cancel();
    }

    /**
     * Unregisters all the alerts for the monitored datatags. Closes the
     * connection to the database.
     * */
    private void unregisterAlerts() {
        this.equipmentLogger.info("unregisterAlerts - Unregistering alerts (" + getEquipmentConfiguration().getSourceDataTags().size() + ")");
        for (Long alertId : getEquipmentConfiguration().getSourceDataTags().keySet()) {
            try {
                this.dbController.unregisterFromAlert(alertId);
            } catch (DataAccessException dae) {
                this.equipmentLogger.error("unregisterAlerts - " + dae.getCause().getMessage(), dae);
            }
        }
    }

    /**
     * Sets the dbAddress property of the DBMessageHandler.
     * 
     * @throws EqIOException
     *             if parsing of the address is unsuccessful
     * */
    public void setDBDataSourceAddress() throws EqIOException {
        loadEqConfig();
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:cern/c2mon/daq/db/config/daq-db-config.xml");
        dbDaqDao = (IDbDaqDao) context.getBean("dbDaqDao");
        dbDaqDao.setDataSourceParams(dbAddress.get(DB_URL), dbAddress.get(DB_USERNAME), dbAddress.get(DB_PASSWORD));
    }

    /**
     * Loads the equipment address (the db url for our DB_DAQ table) depending on the location 
     * defined in {@link IEquipmentConfiguration#getAddress()}.
     * <ul>
     * <li>dbUrl=...       : url taken directly from the configuration</li>
     * <li>fileUrl=myconf  : url taken from a file "myconf" on the disk</li>
     * </ul>
     *  
     * @throws EqIOException
     */
    private void loadEqConfig() throws EqIOException {
        String address = super.getEquipmentConfiguration().getAddress();
        
        if (address == null || address.length() == 0) {
            throw new EqIOException("No equipment address in config defined");
        }
        
        if (address.startsWith("dbUrl")) {
            this.equipmentLogger.info("Trying to read database credentials directly from address...");
            parseDBAddress(address);
        } else if(address.startsWith("fileUrl")) {
            File config = new File(address.split("=")[1]);
            loadUrlFromFile(config);
        }
        
        this.equipmentLogger.info("Successfully loaded equipment address.");
    }
    
    /**
     * Searches in the passed {@link Properties} file a property called dbUrl and uses 
     * {@link #parseDBAddress(String)} to load the equipment config.
     *  
     * @see #loadEqConfig()
     * @param configFile The properties file with the dbUrl instruction.
     * @throws EqIOException in case the file or the "dbUrl=" property cannot be found. 
     */
    private void loadUrlFromFile(File configFile) throws EqIOException {
        Properties p = new Properties();
        this.equipmentLogger.info("Trying to read database credentials from file " + configFile + " ...");
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configFile));
            p.load(stream);
        } catch (IOException e) {
            throw new EqIOException("Failed to load from " + configFile + " : " + e.getMessage(), e);
        }
        
        String dbUrl = (String) p.get("dbUrl");
        if (dbUrl == null || dbUrl.length() == 0) {
            throw new EqIOException("Cannot find value for 'dbUrl' in properties file " + configFile);
        }
        this.equipmentLogger.debug("Got file content. Now trying to parse db url .." + configFile + " ...");
        parseDBAddress("dbUrl=" + dbUrl);
        
    }
    
    /**
     * Parses the database address extracted from the equipment configuration
     * section of the XML. Checks for the existence of all expected properties
     * 
     * @throws EqIOException
     *             if the address doesn't contain the required properties or
     *             contains unrecognized ones
     * */
    private void parseDBAddress(String address) throws EqIOException {
        this.dbAddress = new HashMap<String, String>();
        boolean keyFound;
        
        String[] properties = address.split(PROPERTY_SEPARATOR);
        for (int i = 0; i < properties.length; i++) {
            int separatorIndex = properties[i].indexOf(VALUE_SEPARATOR);
            String key = properties[i].substring(0, separatorIndex);
            String value = properties[i].substring(separatorIndex + 1);
            keyFound = false;
            for (int j = 0; j < dbKeys.length; j++) {
                if (key.equalsIgnoreCase(dbKeys[j])) {
                    this.dbAddress.put(dbKeys[j], value);
                    keyFound = true;
                    break;
                }
            }
            if (!keyFound) {
                String explanation = "The 'Address' field of the equipment configuration contains unrecognized key: " + key;
                this.equipmentLogger.fatal(explanation);
                throw new EqIOException(explanation);
            }
        }

        if (!dbAddress.containsKey(DB_URL) || !dbAddress.containsKey(DB_USERNAME) || !dbAddress.containsKey(DB_PASSWORD)) {
            String errorMsq = "The 'Address' field does not contain the required parameters.";
            this.equipmentLogger.fatal(errorMsq);
            throw new EqIOException(errorMsq);
        }
    }

    /**
     * Disconnects from the database and stops the DAQ process
     * */
    @Override
    public void disconnectFromDataSource() {
        if (this.running && this.connected) {
          
            unregisterAlerts();
            setDisconnected();
        }
        this.running = false;
    }

    /**
     * Gets the current values of all datatags from the database and sends them to the server.
     * */
    @Override
    public void refreshAllDataTags() {
        List<Long> dataTagIds = new ArrayList<Long>(getEquipmentConfiguration().getSourceDataTags().keySet());
        this.equipmentLogger.info("refreshAllDataTags - Refreshing all data tags ");
            List<Alert> alerts = dbDaqDao.getLastAlerts(dataTagIds);
            synchronized (alertsQueue) {
                for (Alert a : alerts) {
                    alertsQueue.add(a);
                }
                alertsQueue.notify();
            }
    }

    /**
     * Gets the current value of the given dataTag from the database and sends it to the server.
     * @param dataTagId the id of the data tag
     * */
    @Override
    public void refreshDataTag(final long dataTagId) {
        this.equipmentLogger.info("refreshDataTag - Refreshing data tag " + dataTagId);
            Alert alert = dbDaqDao.getLastAlertForDataTagId(dataTagId);
            this.dbController.processAlert(alert);
    }

    /**
     * Starts the logging thread, which writes to the logs the amount of values
     * (valid and invalid) sent for every monitored data tag since the start of
     * the daq process. It writes to the log file every LOGGING_INTERVAL
     * (milliseconds).
     * */
    private void startLoggingSentDataTags() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                showAmountOfProcessedAlerts();
            }
        };
        timer.schedule(task, 0, LOGGING_INTERVAL);
    }

    /**
     * Logs the amount of alerts (valid and invalid) that were sent to the
     * server ever since the daq was started
     * */
    private void showAmountOfProcessedAlerts() {
      if (this.equipmentLogger.isTraceEnabled()) {
        int globalTotal = 0;
        StringBuilder msg = new StringBuilder("Processed alerts: [ ");
        for (Entry<Long, Integer> e : this.dbController.getAlertsSent().entrySet()) {
            if (e.getKey() > 0)
                msg.append(e.getKey()).append("=").append(e.getValue()).append("; ");
            globalTotal += e.getValue();
        }
        msg.append(" ]");
        this.equipmentLogger.trace(msg);
        msg = new StringBuilder("Processed invalid: [ ");
        for (Entry<Long, Integer> e : this.dbController.getInvalidSent().entrySet()) {
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
