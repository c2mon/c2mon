package cern.c2mon.daq.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DBDAQHardwareAdressImpl;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

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
  private final Map<Long, Integer> alertsSent = new HashMap<Long, Integer>();
  /**
   * A counter indicating the amount of invalid data tags sent to the server
   * */
  private final Map<Long, Integer> invalidSent = new HashMap<Long, Integer>();
  
  /**
   * An access object for registering/unregistering for alerts and receiving
   * them
   * */
  private IDbDaqDao dbDaqDao;

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
    if (connection(sourceDataTag, changeReport, this.dbDaqDao.getDataTags()) == CHANGE_STATE.FAIL) {
      return CHANGE_STATE.FAIL;
    }
    
    // Register for alert
    synchronized (this.dbDaqDao) {
      registerForAlert(sourceDataTag.getId());
   }
    
    return CHANGE_STATE.SUCCESS;
  }
  
  /**
   * Connection
   * 
   * @param sourceDataTag 
   * @param changeReport 
   * @param registeredDataTags 
   * 
   * @return CHANGE_STATE SUCCESS or FAIL
   */
  public CHANGE_STATE connection(ISourceDataTag sourceDataTag, ChangeReport changeReport, List<Long> registeredDataTags) {
    Long dataTagId = sourceDataTag.getId();
    this.equipmentLogger.trace("connection - Connecting with Datatag: " + dataTagId);
    
    this.alertsSent.put(dataTagId, 0);
    this.invalidSent.put(dataTagId, 0);
    
    // Inserting data data on the data base if it does not exist yet
    if (!registeredDataTags.contains(dataTagId)) {
      insertMissingDataTag(dataTagId, changeReport); 
    }
    // It exists. Update the Data Tag Hardware Address Item Name and the Data Type on the data base if it is necessary
    else {
      ISourceDataTag sdt = getEquipmentConfiguration().getSourceDataTags().get(dataTagId); 
      // Take info from data base
      DBDAQHardwareAdressImpl dbDAQHardwareAdressImpl = this.dbDaqDao.getItemNameAndDataType(dataTagId);
      
      if ((dbDAQHardwareAdressImpl != null) && (sdt != null)) {
        // Compare Item Name from data base with the one on the config
        String newItemName = ((DBHardwareAddress) sdt.getHardwareAddress()).getDBItemName();
        String oldItemName = dbDAQHardwareAdressImpl.getDBItemName();
        if (!oldItemName.equals(newItemName)) {
          updateDataTagItemName(dataTagId, changeReport, newItemName, oldItemName);
        }

        // Compare Data Type from data base with the one on the config
        String newDataType = sdt.getDataType();
        String oldDataType = dbDAQHardwareAdressImpl.getDataType();
        if (!oldDataType.equals(newDataType)) {
          updateDataTagDataType(dataTagId, changeReport, newDataType, oldDataType);
        }
      }
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
    this.equipmentLogger.info("insertMissingDataTag - Inserting missing datatag: " + dataTagId + " - " + name);
    if (changeReport != null) {
      changeReport.appendInfo("insertMissingDataTag - Inserting missing datatag: " + dataTagId + " - " + name);
    }
    this.dbDaqDao.insertNewDataTag(dataTagId, name, value, type, SourceDataQuality.OK, null);
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
    this.equipmentLogger.info("updateDataTagItemName - Updating datatag Item Name: " + dataTagId + " - " + newItemName + "(new) - " 
        + oldItemName + "(old)");
    if (changeReport != null) {
      this.equipmentLogger.info("updateDataTagItemName - Updating datatag Item Name: " + dataTagId + " - " + newItemName + "(new) - " 
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
    this.equipmentLogger.info("updateDataTagDataType - Updating datatag Data Type: " + dataTagId + " - " + newDataType + "(new) - " 
        + oldDataType + "(old)");
    if (changeReport != null) {
      this.equipmentLogger.info("updateDataTagDataType - Updating datatag Data Type: " + dataTagId + " - " + newDataType + "(new) - " 
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
    
    if (this.alertsSent.remove(dataTagId) == null) {
      if (changeReport != null) {
        changeReport.appendError("disconnection - DataTag does not exist in Alers map: " + dataTagId);
      }
      
      return CHANGE_STATE.FAIL;
    }
    if (this.invalidSent.remove(dataTagId) == null) {
      if (changeReport != null) {
        changeReport.appendError("disconnection - DataTag does not exist in Invalids map: " + dataTagId);
      }
      
      return CHANGE_STATE.FAIL;
    }
    
    // Unregister from alert
    synchronized (this.dbDaqDao) {
      unregisterFromAlert(dataTagId);
    }
    
    return CHANGE_STATE.SUCCESS;      
  }
  
  /**
   * Registers for the given alert
   * 
   * @param alertId 
   * 
   */
  public void registerForAlert(long alertId) {
    this.equipmentLogger.trace("registerForAlert - Registering for Alert: " + alertId);
    this.dbDaqDao.registerForAlert(Long.toString(alertId));
  }
  
  /**
   * Unregisters for the given alert. Closes the
   * connection to the database.
   * 
   * @param alertId 
   */
  public void unregisterFromAlert(long alertId) {
    this.equipmentLogger.trace("unregisterFromAlert - Unregistering from Alert: " + alertId);
    dbDaqDao.unregisterFromAlert(Long.toString(alertId));
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
}
