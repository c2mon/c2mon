/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.LASERHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.diamon.alarms.client.AlarmConsumerInterface;
import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.AlarmMessageData.AlarmMessageVisitor;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.shared.data.AlarmHandle.Descriptor;
import cern.diamon.alarms.shared.data.AlarmHandle.SystemProperty;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

public class LaserNativeMessageHandler extends EquipmentMessageHandler implements IDataTagChanger,
        IEquipmentConfigurationChanger, AlarmConsumerInterface {

    private static final Logger log = LoggerFactory.getLogger(LaserNativeMessageHandler.class);
    private static final Logger logOutDatedSources = LoggerFactory.getLogger("OutdatedSources");
    private static final String backupIndicator = "isBackup=true\n";

    protected EquipmentMonitor mbean;

    private AlarmListener listener;
    private IEquipmentMessageSender sender;
    
    private Map<String, ISourceDataTag> alarmToTag = new ConcurrentHashMap<>();
    private HashMap<String, Long> outdatedSources = new HashMap<>();

    
    //
    // --- CONNECT / DISCONNECT DATASOURCE -----------------------------------------------------------------
    //
    @Override
    public synchronized void connectToDataSource() throws EqIOException {
        connectToDataSource(getEquipmentMessageSender());
    }
    
    // Entry point with explicit sender parameter. Used directly by some tests, by the C2MON
    // framework passing the default (production) sender
    @SuppressWarnings("hiding")
    public synchronized void connectToDataSource(IEquipmentMessageSender sender) throws EqIOException {
        this.sender = sender;
        log.debug("connectToDataSource - entering...");

        if (listener == null) {
            listener = AlarmListener.getAlarmListener();
            try {
                listener.startListeningToSource(getName());
            } catch (JMSException e) {
                throw new EqIOException(e);
            }
        }

        // set data tag configuration changer
        super.getEquipmentConfigurationHandler().setDataTagChanger(this);
        // set equipment configuration changer
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);

        initializeMBean();
        registerTags();

        log.trace("connectToDataSource - leaving connectToDataSource()");

    }

    @Override
    public synchronized void disconnectFromDataSource() throws EqIOException {
        log.debug("disconnectFromDataSource - entering ..");

        if (listener != null) {
            log.trace("disconnectFromDataSource Removing Handler from LASER listener..");
            listener.removeHandler(this);
            log.info("disconnectFromDataSource Handler from LASER listener removed.");
        }

        log.trace("disconnectFromDataSource - leaving ..");
    }

    @Override
    public void reset() {
        sender.confirmEquipmentStateOK();

    }
    
    //
    // --- TAG REGISTRATION --------------------------------------------------------------------------------
    //

    @Override
    public void refreshAllDataTags() {
        // Nothing
    }

    @Override
    public void refreshDataTag(long arg0) {
        //
    }

    synchronized public void registerTags() {

        IEquipmentConfiguration econfig = getEquipmentConfiguration();
        log.info("registering {} tags for equipment {} ...", econfig.getSourceDataTags().size(), getName());

        sender.confirmEquipmentStateOK();

        for (final ISourceDataTag dataTag : econfig.getSourceDataTags().values()) {
            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {
                log.info(format("mapping DataTag (%d) -> %s", dataTag.getId(), dataTag.getName()));
                alarmToTag.put(dataTag.getName(), dataTag);
            } else {
                log.warn("Cannot process datatag {}/{} with address type {}", 
                        dataTag.getId(), dataTag.getName(), dataTag.getHardwareAddress().getClass().toString());
            }
        }

        listener.addHandler(this);
        log.info("Tags registered");
    }


    @Override
    public synchronized void onAddDataTag(ISourceDataTag dataTag, ChangeReport changeReport) {
        log.debug(format("entering onAddDataTag(%d)..", dataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);

        try {
            log.info(format("mapping DataTag (%d) -> %s", dataTag.getId(), dataTag.getName()));
            alarmToTag.put(dataTag.getName(), dataTag);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        log.trace(format("leaving onAddDataTag(%d)", dataTag.getId()));
    }

    @Override
    public synchronized void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        alarmToTag.remove(sourceDataTag.getName());
        log.trace(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public synchronized void onUpdateDataTag(ISourceDataTag tag, ISourceDataTag oldTag, ChangeReport report) {        
        log.debug(format("entering onUpdateDataTag(%d,%d)..", tag.getId(), oldTag.getId()));
        report.setState(CHANGE_STATE.SUCCESS);
        alarmToTag.put(tag.getName(), tag);
        log.trace(format("leaving onUpdateDataTag(%d,%d)", tag.getId(), oldTag.getId()));
    }

    @Override
    public synchronized void onUpdateEquipmentConfiguration(IEquipmentConfiguration config,
            IEquipmentConfiguration oldConfig, ChangeReport report) {

        log.debug("entering onUpdateEquipmentConfiguration()..");
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();
            report.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            report.setState(CHANGE_STATE.REBOOT);
            report.appendWarn(ex.getMessage());
        } finally {
            log.trace("leaving onUpdateEquipmentConfiguration()");
        }

    }

    public synchronized void initializeMBean() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objName = null;

            objName = new ObjectName("cern.c2mon.daq.laser.source:type=EquipmentMonitorMBean,name="
                    + getEquipmentConfiguration().getName());
            mbean = new EquipmentMonitor(getEquipmentConfiguration(), this);

            if (mbs.isRegistered(objName)) {
                mbs.unregisterMBean(objName);
                log.info("JMX monitoring for source already registered, removed it ...");
            }

            mbs.registerMBean(mbean, objName);
            log.info("MBean registered");
        } catch (Exception e) {
            log.warn("Cannot register mbean due to " + e.getMessage(), e);
        }

    }

    @Override
    public void onException(JMSException ex) {
        log.error("Got Exception from LASER library : " + ex.getMessage(), ex);
        sender.confirmEquipmentStateIncorrect(ex.getMessage());

    }

    @Override
    public String getName() {
        return getEquipmentConfiguration().getName();
    }

    @Override
    public void onMessage(AlarmMessageData alarmMessage) {

        boolean isBackup = alarmMessage.getMt().equals(MessageType.BACKUP);

        // Check if the source uses an outdated source API. The more recent version of LASER source APIs
        // send an event id string as user property, which allows to sort events without relying on the
        // user timestamp. To request users to update their sources, we log such sources separately
        try {
            ClientAlarmEvent alarm = alarmMessage.getFaults().iterator().next();
            String asiEventIdStr = alarm.getProperty(SystemProperty.ASI_EVENT_ID.toString());

            if (asiEventIdStr == null || asiEventIdStr.isEmpty()) {
                String sourceName = alarmMessage.getSourceId();
                if (!outdatedSources.containsKey(sourceName)) {
                    logOutDatedSources.info("The following source uses an outdated source API ---- " + sourceName);
                }
                outdatedSources.put(sourceName, System.currentTimeMillis());
            }
        }
        catch (Exception e) {
            log.debug("Unable to check source API level (empty message from {})", alarmMessage.getSourceId());
        }
        
        //
        // for backup messages we must terminate all active alarms, which are NOT in the backup message!
        if (isBackup) {
            try {
                sender.sendSupervisionAlive();
                checkTerminatedAlarmsByBackup(alarmMessage);
            } catch (Exception e) {
                log.error("Error occured while reading the backup message: " + e.getMessage(), e);
            }

        }
        
        //
        // process all incoming messages for activations and terminations
        Visitor kv = new Visitor(isBackup);
        alarmMessage.visit(kv);
    }

    /**
     * Synchronize all dataTags values with the alarms values present in the backup. For a given tag,
     * if it is not in the backup message and its current value is null (not initialized) or TRUE 
     * (alarm active) we send an update to the value false in order to terminate the alarm.
     * 
     * @param messageData <code>AlarmMessageData</code>
     */
    public void checkTerminatedAlarmsByBackup(AlarmMessageData messageData) {
        for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {

                String alarmId = dataTag.getName();
                log.debug("Backup init check for " + alarmId + " ... ");
                
                ClientAlarmEvent event = messageData.getFault(alarmId);
                Boolean isActive = null;
                if (dataTag.getCurrentValue() != null) {
                    isActive = (Boolean) dataTag.getCurrentValue().getValue();
                }
                if (event == null && (isActive == null || isActive == Boolean.TRUE)) {                
                    log.debug(dataTag.getId() + " - " + alarmId + " - ACTIVE -> TERM by backup.");
                    long ts = messageData.getSourceTs();
                    sender.sendTagFiltered(dataTag, Boolean.FALSE, ts, backupIndicator);
                    
                    mbean.setDataTag(dataTag.getId());
                    mbean.setValue(Boolean.FALSE);                
                    log.trace(dataTag.getName() + " updated.");
                } 
            }
        }
    }


    private class Visitor implements AlarmMessageVisitor {

        boolean isBackup;

        Visitor(boolean isBackup) {
            this.isBackup = isBackup;
        }

        @Override
        public void onAlarm(ClientAlarmEvent alarm) {
            ISourceDataTag dataTag = alarmToTag.get(alarm.getAlarmId());

            String suffix = isBackup ? " by backup" : "";

            if (alarm.getDescriptor() == Descriptor.UNKNOWN_STATE) {
                log.warn(getEquipmentConfiguration().getName() + " sent alarm " + alarm.getAlarmId()
                        + " with unknown state");
                return;
            }

            if (dataTag == null) {
                log.warn(getEquipmentConfiguration().getName() + " sent unknown alarm " + alarm.getAlarmId());
            } else {

                if (dataTag.getCurrentValue() != null) {
                    Boolean curval = (Boolean) dataTag.getCurrentValue().getValue();
                    if ((alarm.getDescriptor() == Descriptor.ACTIVE && curval.equals(Boolean.TRUE))
                            || (alarm.getDescriptor() == Descriptor.TERMINATE && curval.equals(Boolean.FALSE))) {
                        log.warn(dataTag.getId() + " - " + alarm.getAlarmId() + " : alarm change ignored", suffix);
                        return;
                    }
                }

                Boolean newState = Boolean.FALSE;
                StringBuffer valDescr = new StringBuffer();

                // udpate mbeans in another service asynchronous
                mbean.setDataTag(dataTag.getId());

                if (alarm.getDescriptor() == Descriptor.ACTIVE || alarm.getDescriptor() == Descriptor.CHANGE) {

                    // extract the user properties as value description
                    for (String key : alarm.getUserPropNames()) {
                        valDescr.append(key + "=" + alarm.getProperty(key) + "\n");
                    }

                    if (isBackup) {
                        valDescr.append(backupIndicator);
                    }

                    newState = Boolean.TRUE;
                    log.debug(dataTag.getId() + " - " + alarm.getAlarmId() + " - TERM -> ACTIVE {}.", suffix);
                }

                if (alarm.getDescriptor().equals(Descriptor.TERMINATE)) {
                    log.debug(dataTag.getId() + " - " + alarm.getAlarmId() + " - ACTIVE -> TERM {}.", suffix);
                }

                if (!sender.sendTagFiltered(dataTag, newState, alarm.getUserTs(), valDescr.toString())) {
                    log.warn("sendTagFiltered for {} returned false !?", alarm.getAlarmId());                                        
                }

            }
        }

    }
    
    //
    // --- FOR TESTING ONLY ------------------------------------------------------------------------------
    //
    /**
     * @param listener The new {@link AlarmListener} Only used for testing
     */
    void setAlarmListener(AlarmListener listener) {
        this.listener = listener;
    }

    void setSender(IEquipmentMessageSender sender) {
        this.sender = sender;
    }
    
    ISourceDataTag getTag(long id) {
        return getEquipmentConfiguration().getSourceDataTag(id);
    }

    protected ISourceDataTag getTagByAlarmId(String alarmId) {
        return this.alarmToTag.get(alarmId);
    }
}
