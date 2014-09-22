package cern.c2mon.daq.db;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.db.dao.IDbDaqDao;
import cern.c2mon.shared.common.datatag.address.DBHardwareAddress;
import cern.c2mon.shared.common.type.TypeConverter;
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
    registerForAlert(sourceDataTag.getId());
    // Refresh DataTag
    refreshDataTag(sourceDataTag.getId());
    
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
    getEquipmentLogger().trace("connection - Connecting with Datatag: " + dataTagId);
    
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
    getEquipmentLogger().trace("disconnection - Disconnecting with Datatag: " + dataTagId);
    
    getEquipmentLogger().trace("disconnection - Remove alerts");
    if (this.alertsSent.remove(dataTagId) == null) {
      if (changeReport != null) {
          getEquipmentLogger().trace("disconnection - DataTag does not exist in Alers map: " + dataTagId);
        changeReport.appendError("disconnection - DataTag does not exist in Alers map: " + dataTagId);
      }
      
      return CHANGE_STATE.FAIL;
    }
    
    getEquipmentLogger().trace("disconnection - Remove invalids" + this.invalidSent.get(dataTagId));
    if (this.invalidSent.remove(dataTagId) == null) {
      if (changeReport != null) {
          getEquipmentLogger().trace("disconnection - DataTag does not exist in Invalids map: " + dataTagId);
        changeReport.appendError("disconnection - DataTag does not exist in Invalids map: " + dataTagId);
      }
      
      return CHANGE_STATE.FAIL;
    }
    
    // Unregister from alert
    getEquipmentLogger().trace("disconnection - Unregister from alert (waiting synchronized)");
//    synchronized (this.dbDaqDao) {
        getEquipmentLogger().trace("disconnection - Unregistering from alert");
      unregisterFromAlert(dataTagId);
//    }
    
    getEquipmentLogger().trace("disconnection - Exiting: " + dataTagId);
    
    return CHANGE_STATE.SUCCESS;      
  }
  
  /**
   * Registers for the given alert
   * 
   * @param alertId 
   * 
   */
  public void registerForAlert(long alertId) {
    getEquipmentLogger().trace("registerForAlert - Registering for Alert: " + alertId);
    this.dbDaqDao.registerForAlert(Long.toString(alertId));
  }
  
  /**
   * Unregisters for the given alert. Closes the
   * connection to the database.
   * 
   * @param alertId 
   */
  public void unregisterFromAlert(long alertId) {
    getEquipmentLogger().trace("unregisterFromAlert - Unregistering from Alert: " + alertId);
    dbDaqDao.unregisterFromAlert(Long.toString(alertId));
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
//      synchronized (getInvalidSent()) {
          int i = getInvalidSent().get(dataTagId);
          getInvalidSent().put(dataTagId, ++i);
//      }
      increaseAllSentDataTags(dataTagId);
  }

  /**
   * Increases the counter of all values sent for a given data tag.
   * 
   * @param dataTagId
   *            id of a datatag
   * */
  private void increaseAllSentDataTags(final long dataTagId) {
//      synchronized (getAlertsSent()) {
          int i = getAlertsSent().get(dataTagId);
          getAlertsSent().put(dataTagId, ++i);
//      }
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
}
