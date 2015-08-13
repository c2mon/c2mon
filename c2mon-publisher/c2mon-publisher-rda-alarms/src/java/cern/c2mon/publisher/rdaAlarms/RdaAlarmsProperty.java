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
 * This class represents a RDA property. For each tag which is published via RDA an instance of this class is created.
 * It handles the registration of the listeners and is responsible of notifiying all subscribers about value updates.
 *
 * @author Mark Buttner
 */
public class RdaAlarmsProperty {

    // Note: in japc-ext-laser, we had only ACTIVE, TERMINATE, UNKNOWN_STATE (?) and CHANGE (?)
    public enum AlarmState {ACTIVE, TERMINATE, INVALID_A, INVALID_T, UNDEFINED, WRONG_SOURCE}
    
    private static final Logger LOG = LoggerFactory.getLogger(RdaAlarmsProperty.class);

    private Data currentValue = null;
    private SubscriptionSource subscriptionSource;
    private final String rdaPropertyName;

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
    // --- PACKAGE METHODS ------------------------------------------------------------------------
    //
    protected synchronized AcquiredData getValue(Data filters) {
        return getValue(currentValue, filters);
    }

    synchronized Data get() {
        return currentValue;
    }

 
    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    private AcquiredData getValue(Data value, Data filters) {
        if (filters != null && filters.getAllEntriesSize() > 0) {            
            Data filteredValue = DataFactory.createData();

            for (Entry filterEntry : filters.getEntries()) {
                String id = filterEntry.getName();
                if (value.exists(filterEntry.getString())) {
                    filteredValue.append(filterEntry.getString(), value.getString(filterEntry.getString()));
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
