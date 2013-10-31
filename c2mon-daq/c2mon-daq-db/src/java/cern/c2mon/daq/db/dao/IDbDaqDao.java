package cern.c2mon.daq.db.dao;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import cern.c2mon.daq.db.Alert;
import cern.c2mon.daq.db.AlertTimeOutException;



/**
 * This interface contains the methods necessary for working with database
 * alerts for DB DAQ process.
 * 
 * @author Aleksandra Wardzinska
 * */
public interface IDbDaqDao {

    /**
     * Sets the dataSource parameters
     * @param dbUrl     url to the database
     * @param dbUsername    database username
     * @param dbPassword    database password 
     * */
    void setDataSourceParams(String dbUrl, String dbUsername, String dbPassword);
    
    /**
     * Get the latest values of the alerts (current values of the datatags)
     * @param dataTagsIds     a list of data tags' ids for which the values should be retrieved
     * @return list of alerts that happened before the process started listening
     * */
    List<Alert> getLastAlerts(List<Long> dataTagsIds);

    /**
     * Get the latest value of the alert (current value of the given datatag)
     * @param dataTagId     data tag id for which the value should be retrieved
     * @return alert that happened before the process started listening
     * */
    Alert getLastAlertForDataTagId(long dataTagId);
    
    /**
     * Get all the datatags' ids registered for the equipment (db account)
     * @return list of datatags ids present in the db daq table
     * */
    List<Long> getDataTags();
    
    /**
     * Registers a new datatag for the equipment (db account). The value, quality and qualityDesc don't matter much
     * as this is just an initial registration of the datatag.
     * @param dataTagId     id of the datatag
     * @param name          name of the datatag
     * @param value         value of the datatag
     * @param type          data type of the datatag
     * @param quality       quality of the datatag value
     * @param qualityDesc   description of the quality
     * */
    void insertNewDataTag(final long dataTagId, final String name, final String value, 
            final String type, final short quality, final String qualityDesc);
    
    /**
     * Registers an interest in receiveing alerts identified by alertId
     * @param alertId   id of the alert 
     * */
    void registerForAlert(String alertId);

    /**
     * Starts waiting for the given alert
     * @param alertId id of the alert
     * @param timeout in seconds after which the waiting will be interrupted
     * @return received alert from the database 
     * @throws AlertTimeOutException if the wait was timed out
     * */
    Alert waitForAlert(String alertId, int timeout) throws AlertTimeOutException;

    /**
     * Starts waiting for all alerts for which the process registered 
     * @param timeout in seconds after which the waiting will be interrupted
     * @return alert received
     * @throws AlertTimeOutException if the wait was timed out 
     * */
    Alert waitForAnyAlert(int timeout) throws AlertTimeOutException;
  
    /**
     * Unregisters the process from receiving the alert
     * @param alertId name of the alert
     * */
    void unregisterFromAlert(String alertId);

    /**
     * Unregisters from all alerts
     * */
    void unregisterFromAll();
    
    /**
     * Gets the dataSource
     * @return basicDataSource 
     * */
    BasicDataSource getCustomDataSource();
    
}
