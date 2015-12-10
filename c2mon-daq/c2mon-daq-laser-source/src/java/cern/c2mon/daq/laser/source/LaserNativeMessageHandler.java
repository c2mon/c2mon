/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;
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

    protected EquipmentMonitor mbean;

    private AlarmListener listener;

    private Map<String, ISourceDataTag> alarmToTag = new HashMap<>();

    private DataTagValueDictionary valueDictionary = new DataTagValueDictionary();

    private static final String backupIndicator = "isBackup";

    private HashMap<String, Calendar> outdatedSources = new HashMap<String, Calendar>();

    /**
     */
    public LaserNativeMessageHandler() {

    }

    /**
     * @param listener The new {@link AlarmListener} Only used for testing
     */
    void setAlarmListener(AlarmListener listener) {
        this.listener = listener;
    }

    @Override
    public void shutdown() throws EqIOException {
        super.shutdown();
    }

    @Override
    public synchronized void connectToDataSource() throws EqIOException {

        log.debug("connectToDataSource - entering...");

        if (listener == null) {
            listener = AlarmListener.getAlarmListener();
            try {
                listener.startListingToSource(getEquipmentConfiguration().getName());
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
    public void refreshAllDataTags() {
        // Nothing
    }

    @Override
    public void refreshDataTag(long arg0) {
        log.info("Refresh datatag {}", arg0);
    }

    //
    // --- REGISTERING TAGS ---
    //

    synchronized public void registerTags() throws EqIOException {

        log.info("registering {} tags for equipment {} ...", getEquipmentConfiguration().getSourceDataTags().size(),
                getEquipmentConfiguration().getName());

        getEquipmentMessageSender().confirmEquipmentStateOK();

        for (final ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {

            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {
                addDataTagAndSendUpdate(dataTag, false);
            } else {
                log.warn("Cannot process datatag {}: the hardware address is not of type {}", dataTag.getId(),
                        LASERHardwareAddress.class.getName());
            }

        }

        listener.addHandler(this);

        log.info("Tags registered");
    }

    private String getAlarmIdFromLaserHardwareAdress(ISourceDataTag dataTag) throws EqIOException {

        if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {
            LASERHardwareAddress addr = (LASERHardwareAddress) dataTag.getHardwareAddress();

            if (addr == null) {
                throw new EqIOException("The hardware adress for Tag " + dataTag.getId() + "is null");
            }

            return addr.getFaultFamily() + ":" + addr.getFaultMember() + ":" + addr.getFalutCode();
        }

        String errorMsg = "Unsupported HardwareAddress: " + dataTag.getHardwareAddress().getClass();
        throw new EqIOException(errorMsg);

    }

    private void addDataTagAndSendUpdate(ISourceDataTag dataTag, boolean sendUpdate) throws EqIOException {
        String alarmId = getAlarmIdFromLaserHardwareAdress(dataTag);

        synchronized (alarmToTag) {
            log.info(format("mapping DataTag (%d) -> %s", dataTag.getId(), alarmId));
            alarmToTag.put(alarmId, dataTag);
        }

        if (sendUpdate) {
            getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
        } 

    }

    //
    // --- ADD/REMOVE/UPDATE a tag --------------------------------------------------------------------------
    //

    @Override
    public synchronized void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);

        try {
            addDataTagAndSendUpdate(sourceDataTag, true);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        log.trace(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public synchronized void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        try {
            synchronized (alarmToTag) {
                alarmToTag.remove(sourceDataTag.getName());
            }

            // no update send for this datatag

        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        log.trace(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public synchronized void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag,
            ChangeReport changeReport) {
        log.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        synchronized (alarmToTag) {
            alarmToTag.put(sourceDataTag.getName(), sourceDataTag);
        }

        log.trace(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    @Override
    public synchronized void onUpdateEquipmentConfiguration(IEquipmentConfiguration equipmentConfiguration,
            IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        log.debug("entering onUpdateEquipmentConfiguration()..");
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();
            changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn(ex.getMessage());
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
            mbean = new EquipmentMonitor(getEquipmentConfiguration());

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
        getEquipmentMessageSender().confirmEquipmentStateIncorrect(ex.getMessage());

    }

    @Override
    public String getName() {
        return getEquipmentConfiguration().getName();
    }

    @Override
    public void onMessage(AlarmMessageData alarmMessage) {

        boolean isBackup = alarmMessage.getMt().equals(MessageType.BACKUP);

        // Check if the source uses an outdated source API
        try {
            ClientAlarmEvent alarm = alarmMessage.getFaults().iterator().next();
            String asiEventIdStr = alarm.getProperty(SystemProperty.ASI_EVENT_ID.toString());

            if (asiEventIdStr == null || asiEventIdStr.isEmpty()) {
                outdatedSource(alarmMessage.getSourceId());
            }

        }

        catch (Exception e) {
            log.debug("Unable to check if the source uses an outdated source API, no alarms in the message. source :  "
                    + alarmMessage.getSourceId());
        }

        if (isBackup) {
            // a backup alarm
            try {
                getEquipmentMessageSender().sendSupervisionAlive();
                checkTerminatedAlarmsByBackup(alarmMessage);
            } catch (Exception e) {
                log.error("Error occured while reading the backup message: " + e.getMessage(), e);
            }

        }

        Visitor kv = new Visitor(isBackup);
        alarmMessage.visit(kv);
    }

    private void outdatedSource(String sourceName) {

        if (!outdatedSources.containsKey(sourceName)) {
            logOutDatedSources.info("The following source uses an outdated source API ---- " + sourceName);
            outdatedSources.put(sourceName, Calendar.getInstance());
        }

        if ((Calendar.getInstance().get(Calendar.DAY_OF_YEAR) > outdatedSources.get(sourceName).get(
                Calendar.DAY_OF_YEAR) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > outdatedSources.get(
                sourceName).get(Calendar.HOUR_OF_DAY))
                || Calendar.getInstance().get(Calendar.YEAR) > outdatedSources.get(sourceName).get(Calendar.YEAR)) {

            logOutDatedSources.info("The following source uses an outdated source API ---- " + sourceName);
            outdatedSources.get(sourceName).setTime(Calendar.getInstance().getTime());

        }

    }

    /**
     * Synchronize all dataTags values with the alarms values present in the backup
     * <p>
     * 1. loop over active alarms in equipment<br>
     * 1.1 is active alarm in backup ?<br>
     * no: terminate alarm<br>
     * yes : skip<br>
     * 2. call onAlarm for each ClientAlarmEvent in backup<br>
     * </p>
     * 
     * @param messageData <code>AlarmMessageData</code>
     */
    public void checkTerminatedAlarmsByBackup(AlarmMessageData messageData) {

        for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {

            boolean found = false;

            if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(Boolean.TRUE)) {
                // its a currently active alarm
                // check if it is still active in the backup
                for (ClientAlarmEvent alarm : messageData.getFaults()) {
                    if (findDataTag(alarm.getAlarmId()) != null
                            && findDataTag(alarm.getAlarmId()).getId() == dataTag.getId()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Terminate alarm
                    mbean.setDataTag(dataTag.getId());

                    getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, messageData.getSourceTs(),
                            backupIndicator + "=true\n");
                    mbean.setValue(Boolean.FALSE);

                    String alarmID = dataTag.getName();
                    try {
                        alarmID = getAlarmIdFromLaserHardwareAdress(dataTag);
                    } catch (EqIOException e) {
                        // SHOULD NEVER HAPPEN as the validation took place before
                    }

                    log.debug(dataTag.getId() + " - " + alarmID + " - ACTIVE -> TERM by backup.");
                }
            }
        }
    }

    private ISourceDataTag findDataTag(String alarmId) {
        synchronized (alarmToTag) {
            return alarmToTag.get(alarmId);
        }
    }

    @Override
    public void reset() {
        getEquipmentMessageSender().confirmEquipmentStateOK();

    }

    private class Visitor implements AlarmMessageVisitor {

        boolean isBackup;

        Visitor(boolean isBackup) {
            this.isBackup = isBackup;
        }

        @Override
        public void onAlarm(ClientAlarmEvent alarm) {
            ISourceDataTag dataTag = findDataTag(alarm.getAlarmId());

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
                    if (alarm.getDescriptor() == Descriptor.ACTIVE && curval.equals(Boolean.TRUE)
                            || alarm.getDescriptor() == Descriptor.TERMINATE && curval.equals(Boolean.FALSE)) {
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
                        valDescr.append(backupIndicator + "=true\n");
                    }

                    valueDictionary.addDescription(dataTag, valDescr.toString());
                    newState = Boolean.TRUE;
                    log.debug(dataTag.getId() + " - " + alarm.getAlarmId() + " - TERM -> ACTIVE {}.", suffix);
                }

                if (alarm.getDescriptor().equals(Descriptor.TERMINATE)) {
                    log.debug(dataTag.getId() + " - " + alarm.getAlarmId() + " - ACTIVE -> TERM {}.", suffix);
                }

                // DMN-2488 using the userTs in TAG will trigger a discard of the update on the
                // server side. Result: we must pack the user timestamp into the description!
                long now = System.currentTimeMillis();
                valDescr.append("ASI_USER_TS=" + alarm.getUserTs() + "\n");
                if (!getEquipmentMessageSender().sendTagFiltered(dataTag, newState, now, valDescr.toString())) {
                    log.warn("sendTagFiltered for {} returned false !?", alarm.getAlarmId());                                        
                }

            }
        }

    }

}
