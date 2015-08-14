package cern.c2mon.publisher.rdaAlarms;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.publisher.rdaAlarms.C2monConnectionIntf.Quality;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.data.Entry;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.server.subscription.SubscriptionSource;

/**
 * This class represents a RDA property. For each alarm source, an instance of this class is created
 * and holds entries for all alarms sent by this source (in the for of String ALARM_ID = String STATUS).
 * 
 * To query the RDA publisher, one needs to:
 * - subscribe to the property with the name SOURCE_NAME
 * - find the entry with name ALARM_ID
 * - process the value following the values of AlarmState enum in this class
 * 
 * It is possible to find the list of known sources by a GET call to the special property "_SOURCES"
 * 
 * The class handles the registration of the listeners and is responsible of notifying all subscribers 
 * about value updates.
 *
 * @author Mark Buttner
 */
public class RdaAlarmsProperty {

    // Note: in japc-ext-laser, we had only ACTIVE, TERMINATE, UNKNOWN_STATE (?) and CHANGE (?)
    public enum AlarmState {ACTIVE, TERMINATE, INVALID_A, INVALID_T, UNDEFINED, WRONG_SOURCE}
    
    private static final Logger LOG = LoggerFactory.getLogger(RdaAlarmsProperty.class);

    private final String rdaPropertyName;
    private Data currentValue = null;
    private SubscriptionSource subscriptionSource;

    private ConcurrentHashMap<String,Long> updates = new ConcurrentHashMap<String, Long>();
    
    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------
    //
    RdaAlarmsProperty(final String pRdaPropertyName) {
        this.rdaPropertyName = pRdaPropertyName;
        currentValue = DataFactory.createData();
    }

    //
    // --- PUBLIC METHODS --------------------------------------------------------------------------
    //
    public synchronized void setSubscriptionSource(SubscriptionSource subscription) {
        this.subscriptionSource = subscription;
    }

    public synchronized void removeSubscriptionSource() {
        this.subscriptionSource = null;
    }

    /**
     * Get without filtering. The whole content is returned, i.e. all alarms
     * @return <code>Data</code> the list of all known alarms for the source with their status
     */
    public synchronized Data get() {
        return currentValue;
    }
    
    /**
     * Get with filters. Only the entries matching the filter entries are returned and
     * translated (the id in the returned structure is no longer the alarm id, but the filter name
     * provided by the caller).
     * @param filters the list of elements to return, if present
     * @return <code>AcquiredData</code> the list of alarms matching the filter entries
     */
    public synchronized AcquiredData getValue(Data filters) {
        return getValue(currentValue, filters);
    }

    /**
     * When a new value for an alarm linked to this property is received:
     * - if the update is older than the previous known, it is discarded
     * - if the alarm is known, we remove its entry (updates are not supported by RDA Data objects)
     * - in case the underlying datatag does not exist anymore, we stop there (the alarm is
     *      no longer in the data of the property)
     * - otherwise, the status is computed out of the alarm and datatag information, 
     *      added to the internal storage, subscribers are notified.
     * 
     * @param av <code>AlarmValue</code> the new value for an alarm coming from the source
     */
    public synchronized void onUpdate(AlarmValue av) {
        String alarmId = RdaAlarmsPublisher.getAlarmId(av);

        // 1. make sure we do not override with older stuff!
        Long updateTs = updates.get(alarmId);
        if (updateTs == null) {
            updates.put(alarmId, av.getTimestamp().getTime());
        } else {
            if (updateTs.longValue() > av.getTimestamp().getTime()) {
                return;
            }
            updateTs = av.getTimestamp().getTime();
        }
            
        
        // 2. we can not update. If an existing value must be updated, first remove it
        //    and add it with its new value later on.
        if (currentValue.exists(alarmId)) {
            currentValue.remove(alarmId);
        }

        // 3. merge alarm state and C2MON data quality into string provided by the device
        C2monConnectionIntf c2mon = RdaAlarmsPublisher.getPublisher().getC2mon();
        int qual = c2mon.getQuality(av.getTagId());
        if ((qual & Quality.EXISTING) != Quality.EXISTING) {
            LOG.info(" TAG_DELETED  > " + alarmId);
        } else {
            AlarmState status = AlarmState.TERMINATE;
            if ((qual & Quality.VALID) != Quality.VALID) {
                status = AlarmState.INVALID_T;
            }
            if (av.isActive()) {
                status = AlarmState.ACTIVE;
                if ((qual & Quality.VALID) != Quality.VALID) {
                    status = AlarmState.INVALID_A;
                }
            }
            currentValue.append(alarmId, status.toString());
            LOG.debug("Value update received for RDA property " + rdaPropertyName + " " + currentValue.size());
        }
        
        // 4. if we have subscribers, tell them about the update
        if (subscriptionSource != null) {
            Data filters = subscriptionSource.getContext().getFilters();
            subscriptionSource.notify(getValue(currentValue, filters));
        }
    }
 
    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    /**
     * Internal method to build the result for a GET with filter. The simplest case is when the
     * requested alarm is present in the cache, it is just added to the result with the filtername
     * as key (instead of alarm id GETs without filters). 
     * 
     * If the alarm is not present, we must first ask the dataprovider to check if it belongs to
     * the source represented by this property. If yes, the request is valid, we simply did not
     * receive any activation since startup. Otherwise, the filter is added to the result set with
     * a status value explaining what is wrong.
     * 
     * @param value <code>Data</code> the current value of the properts
     * @param filters   <code>Data</code> the list of filter elements
     * @return <code>AcquiredData</code> the result set
     */
    private AcquiredData getValue(Data value, Data filters) {
        if (filters != null && filters.getAllEntriesSize() > 0) {            
            Data filteredValue = DataFactory.createData();

            for (Entry filterEntry : filters.getEntries()) {
                String id = filterEntry.getName();
                if (value.exists(filterEntry.getString())) {
                    filteredValue.append(id, value.getString(filterEntry.getString()));
                } else {
                    try {
                        String source = RdaAlarmsPublisher.getPublisher().getSourceMgr().getSourceNameForAlarm(filterEntry.getString());
                        if (source == null) {
                            filteredValue.append(id, AlarmState.UNDEFINED.toString());                    
                        } else {
                            if (source.equals(this.rdaPropertyName)) {
                                filteredValue.append(id, AlarmState.TERMINATE.toString());                                            
                            } else {
                                filteredValue.append(id, AlarmState.WRONG_SOURCE.toString() + "->" + source);                                                                        
                            }
                        }
                    } catch (Exception e) {
                        filteredValue.append(id, AlarmState.UNDEFINED.toString());                    
                        LOG.warn("Failed to retrieve data for alarm " + filterEntry.getString(), e);
                    }
                }
                
            }
            return new AcquiredData(filteredValue);
        }
        return new AcquiredData(value);
    }

}
