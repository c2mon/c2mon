package cern.c2mon.daq.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.mybatis.spring.support.SqlSessionDaoSupport;

import cern.c2mon.daq.db.Alert;
import cern.c2mon.daq.db.AlertTimeOutException;
import cern.c2mon.shared.common.datatag.address.impl.DBDAQHardwareAdressImpl;


/**
 * This class is an implementation of the IdbDaqDao interface. It provides
 * methods for registering, unregistering and listening for database alerts sent
 * via dbms_alert package.
 * 
 * @author Aleksandra Wardzinska
 * */
public class DbDaqDaoImpl extends SqlSessionDaoSupport implements IDbDaqDao {

    /**
     * The maximum number of elements in a list of values passed to jdbc driver.
     * The real limit is 1000, above it the ORA-01795 exception is thrown.  
     * */
    public static final int MAX_LIST_SIZE = 999;
    
    /**
     * Get the latest values of the alerts (current values of the datatags)
     * @param dataTagsIds     a list of data tags' ids for which the values should be retrieved
     * @return list of alerts that happened before the process started listening
     * */
    @SuppressWarnings("unchecked")
    public List<Alert> getLastAlerts(final List<Long> dataTagsIds) {
        List<Long> shortDataTagsIds;
        List<Alert> alerts = new ArrayList<Alert>();
        Map<String,Object> inputMap = new HashMap<String,Object>();
        for (int k = 0, end = 0; end < dataTagsIds.size(); k++) {
            end = ((k + 1) * MAX_LIST_SIZE < dataTagsIds.size()) ? (k + 1) * MAX_LIST_SIZE : dataTagsIds.size();
            shortDataTagsIds = dataTagsIds.subList(k * MAX_LIST_SIZE, end);
            inputMap.put("ids", shortDataTagsIds); // just make sure that the key is not named "list" 
            alerts.addAll( (List<Alert>) this.getSqlSession().selectList("getLastAlertsById", inputMap) );
        }
        return alerts;
    }
  
    /**
     * Get the latest value of the alert (current value of the given datatag)
     * @param dataTagId     data tag id for which the value should be retrieved
     * @return alert that happened before the process started listening
     * */
    public Alert getLastAlertForDataTagId(final long dataTagId) {
        return (Alert) (this.getSqlSession().selectOne("getLastAlertForDataTagById", dataTagId));
    }

    /**
     * Get all the datatags' ids registered for the equipment (db account)
     * @return list of datatags ids known to the DB DAQ process
     * */
    @SuppressWarnings("unchecked")
    public List<Long> getDataTags() {
        return (List<Long>) this.getSqlSession().selectList("getDataTags");
    }
    
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
    public void insertNewDataTag(final long dataTagId, final String name, final String value, 
                            final String type, final short quality, final String qualityDesc) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tag_id", dataTagId);
        params.put("tag_name", name);
        params.put("tag_value", value);
        params.put("tag_data_type", type);
        params.put("timestamp", new Date());
        params.put("tag_quality", quality);
        params.put("tag_quality_desc", qualityDesc);
        params.put("creation_date", new Date());
        this.getSqlSession().insert("insertDataTag", params);
        
    }
    
    /**
     * Registers an interest in receiveing alerts identified by alertId
     * @param alertId   id of the alert 
     * */
    public void registerForAlert(final String alertId) {
        this.getSqlSession().update("register-for-alert", alertId);
    }

    /**
     * Starts waiting for the given alert
     * @param alertId id of the alert
     * @param timeout in seconds after which the waiting will be interrupted
     * @return received alert from the database 
     * @throws AlertTimeOutException if the wait was timed out
     * */
    public Alert waitForAlert(final String alertId, final int timeout) throws AlertTimeOutException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", alertId);
        params.put("timeout", timeout);
        this.getSqlSession().update("wait-one-alert", params);
        if ((Integer) params.get("status") == Alert.ALERT_OCCURRED)
            return new Alert(alertId, (String) params.get("message"));
        else 
            throw new AlertTimeOutException();
    }

    /**
     * Starts waiting for all alerts for which the process registered 
     * @param timeout in seconds after which the waiting will be interrupted
     * @return alert received
     * @throws AlertTimeOutException if the wait was timed out 
     * */
    public Alert waitForAnyAlert(final int timeout) throws AlertTimeOutException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("timeout", timeout);
        this.getSqlSession().update("wait-any-alert", params);
        if ((Integer) params.get("status") == Alert.ALERT_OCCURRED) 
            return new Alert((String) params.get("name"), (String) params.get("message"));
        else
            throw new AlertTimeOutException();
    }
 
    /**
     * Unregisters the process from receiving the alert
     * @param alertId name of the alert
     * */
    public void unregisterFromAlert(final String alertId) {
        this.getSqlSession().update("unregister-one-alert", alertId);
    }

    /**
     * Unregisters from all alerts
     * */
    public void unregisterFromAll() {
        this.getSqlSession().update("unregister-all-alerts");
    }

    /**
     * Sets the dataSource parameters
     * @param dbUrl     url to the database
     * @param dbUsername    database username
     * @param dbPassword    database password 
     * */
    public void setDataSourceParams(final String dbUrl, final String dbUsername, final String dbPassword) {
        BasicDataSource ds =  (BasicDataSource) this.getSqlSession().getConfiguration().getEnvironment().getDataSource();
        ds.setUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
    }

    /**
     * Gets the dataSource
     * @return basicDataSource 
     * */
    public BasicDataSource getCustomDataSource() {
        BasicDataSource ds =  (BasicDataSource) this.getSqlSession().getConfiguration().getEnvironment().getDataSource();
        return ds;
    }
    
    @Override
    public void updateDataTagItemName(final long dataTagId, final String itemName) {
        DBDAQHardwareAdressImpl dbDAQHardwareAdressImpl = new DBDAQHardwareAdressImpl(dataTagId, itemName, null);
        this.getSqlSession().update("updateDataTagItemName", dbDAQHardwareAdressImpl);
    }
    
    @Override
    public String getItemName(final long dataTagId) {
      return (String) (this.getSqlSession().selectOne("getItemNameForDataTagById", dataTagId));
    }

    @Override
    public void updateDataTagDataType(long dataTagId, String dataType) {
      DBDAQHardwareAdressImpl dbDAQHardwareAdressImpl = new DBDAQHardwareAdressImpl(dataTagId, null, dataType);
      this.getSqlSession().update("updateDataTagDataType", dbDAQHardwareAdressImpl);
    }

    @Override
    public String getDataType(long dataTagId) {
      return (String) (this.getSqlSession().selectOne("getDataTypeForDataTagById", dataTagId));
    }

    @Override
    public DBDAQHardwareAdressImpl getItemNameAndDataType(long dataTagId) {
      return (DBDAQHardwareAdressImpl) (this.getSqlSession().selectOne("getItemNameAndDataTypeById", dataTagId));
    }
  
    
}
