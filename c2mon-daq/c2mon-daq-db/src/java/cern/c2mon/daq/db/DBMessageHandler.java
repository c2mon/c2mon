package cern.c2mon.daq.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;

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
     * A table with keys for database properties
     * */
    private String[] dbKeys = { DB_URL, DB_USERNAME, DB_PASSWORD };
    /**
     * Contains the database address fields: name, url, username and password
     * */
    private Map<String, String> dbAddress;
   
    /**
     * An access object for registering/unregistering for alerts and receiving
     * them
     * */
    private IDbDaqDao dbDaqDao;

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

        setDBDataSourceAddress();

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
        
        // If the Reconfiguration Data Tag is not present we add it 
        if (!registeredDataTags.contains(DBController.RECONFIGURATION_TAG_ID)) {
        	this.dbController.insertReconfigurationDataTag();
        }
		 
        // Connect to the rest of Data Tags
        for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
        	this.dbController.connection(dataTag, null, registeredDataTags);
        }
        
        // Start the threads for Processing and Listing the alerts
        this.dbController.startAlertPorcessor();
        this.dbController.startAlertListener();
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
        this.dbController.disconnectFromDataSource();
    }

    /**
     * Gets the current values of all datatags from the database and sends them to the server.
     * */
    @Override
    public void refreshAllDataTags() {
      this.dbController.refreshAllDataTags();
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
}
