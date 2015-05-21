/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static java.lang.String.format;

import java.lang.management.ManagementFactory;
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
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.LASERHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.diamon.alarms.client.AlarmConsumerInterface;
import cern.diamon.alarms.client.AlarmMessageData;
import cern.diamon.alarms.client.ClientAlarmEvent;
import cern.diamon.alarms.shared.data.AlarmHandle.Descriptor;
import cern.diamon.alarms.source.AlarmMessageBuilder.MessageType;

public class LaserNativeMessageHandler extends EquipmentMessageHandler implements IDataTagChanger,
        IEquipmentConfigurationChanger, AlarmConsumerInterface {

    private static final Logger log = LoggerFactory.getLogger(LaserNativeMessageHandler.class);

    protected EquipmentMonitor mbean;

    private AlarmListener listener;

    private Map<String, ISourceDataTag> alarmToTag = new HashMap<>();

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
        // Nothing
    }

    //
    // --- REGISTERING TAGS ---
    //

    synchronized public void registerTags() throws EqIOException {

        log.info("registering {} tags for equipment {} ...", getEquipmentConfiguration().getSourceDataTags().size(),
                getEquipmentConfiguration().getName());

        // ExecutorService service = Executors.newFixedThreadPool(2);

        for (final ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {

            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {
                addDataTagAndSendUpdate(dataTag);
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

    private void addDataTagAndSendUpdate(ISourceDataTag dataTag) throws EqIOException {
        String alarmId = getAlarmIdFromLaserHardwareAdress(dataTag);

        synchronized (alarmToTag) {
            log.info(format("mapping DataTag (%d) -> %s", dataTag.getId(), alarmId));
            alarmToTag.put(alarmId, dataTag);
        }

        getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
    }

    //
    // --- ADD/REMOVE/UPDATE a tag --------------------------------------------------------------------------
    //

    @Override
    public synchronized void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);

        try {
            addDataTagAndSendUpdate(sourceDataTag);
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

    }

    @Override
    public String getName() {
        return getEquipmentConfiguration().getName();
    }

    @Override
    public void onMessage(AlarmMessageData alarmMessage) {

        if (alarmMessage.getMt().equals(MessageType.BACKUP)) {

            // a backup alarm

            try {
                getEquipmentMessageSender().sendSupervisionAlive();
                synchronizeWithBackup(alarmMessage);
            } catch (Exception e) {
                log.error("Error occured while reading the backup message");
            }

        } else {
            // normal alarm

            for (ClientAlarmEvent event : alarmMessage.getFaults()) {
                onAlarm(event);
            }

        }

    }

    public void onAlarm(ClientAlarmEvent alarm) {

        ISourceDataTag dataTag = findDataTag(alarm.getAlarmId());

        if (dataTag != null) {
            if (alarm.getDescriptor().equals(Descriptor.ACTIVE)) {

                if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(true)) {
                    // NOTHING
                } else {

                    // udpate mbeans in another service asynchronous
                    mbean.setDataTag(dataTag.getId());
                    getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.TRUE, System.currentTimeMillis());

                    mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    log.info(dataTag.getId() + " - " + alarm.getAlarmId() + " - turns to "
                            + dataTag.getCurrentValue().getValue());
                }
            } else if (alarm.getDescriptor().equals(Descriptor.TERMINATE)) {

                if (dataTag.getCurrentValue() != null && dataTag.getCurrentValue().getValue().equals(Boolean.FALSE)) {
                    // NOTHING
                } else {
                    mbean.setDataTag(dataTag.getId());
                    getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
                    mbean.setValue((boolean) dataTag.getCurrentValue().getValue());

                    log.info(dataTag.getId() + " - " + alarm.getAlarmId() + " - turns to "
                            + dataTag.getCurrentValue().getValue());
                }

            }
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
     * @param messageData
     */
    public void synchronizeWithBackup(AlarmMessageData messageData) {

        for (ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {

            boolean found = false;

            if (dataTag.getCurrentValue().getValue().equals(Boolean.TRUE)) {
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
                    getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
                    mbean.setValue((boolean) dataTag.getCurrentValue().getValue());
                    log.info(dataTag.getId() + " TERMINATE");
                }
            }
        }

        // now activate
        for (ClientAlarmEvent alarm : messageData.getFaults()) {
            onAlarm(alarm);
        }

    }

    private ISourceDataTag findDataTag(String alarmId) {
        synchronized (alarmToTag) {
            return alarmToTag.get(alarmId);
        }
    }

    @Override
    public void reset() {
        // Dont' care..

    }

}
