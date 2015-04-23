/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.laser.source;

import static java.lang.String.format;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
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
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.diamon.alarms.client.AlarmConnector;
import cern.diamon.alarms.client.AlarmConnectorFactory;

public class LaserNativeMessageHandler extends EquipmentMessageHandler implements IDataTagChanger,
        IEquipmentConfigurationChanger {

    private static final Logger log = LoggerFactory.getLogger(LaserNativeMessageHandler.class);

    protected EquipmentMonitor mbean;

    private ConcurrentHashMap<IEquipmentConfiguration, Collection<LASERHardwareAddress>> alarmList4Equipement = new ConcurrentHashMap<IEquipmentConfiguration, Collection<LASERHardwareAddress>>();
    private ConcurrentHashMap<String, IEquipmentConfiguration> equipementByName = new ConcurrentHashMap<String, IEquipmentConfiguration>();
    private ConcurrentHashMap<IEquipmentConfiguration, IEquipmentMessageSender> equMessageSenderList = new ConcurrentHashMap<IEquipmentConfiguration, IEquipmentMessageSender>();
    private ConcurrentHashMap<IEquipmentConfiguration, ISourceDataTag> heartbeatTag4Equipment = new ConcurrentHashMap<IEquipmentConfiguration, ISourceDataTag>();
    private ConcurrentHashMap<String, ISourceDataTag> tag4LaserHardwareAddress = new ConcurrentHashMap<String, ISourceDataTag>();
    
    
    private AlarmListener listener;
    
    private AlarmConnector connector;
    
    /**
     * @throws JMSException 
     * 
     */
    public LaserNativeMessageHandler() throws Exception {
        listener = new AlarmListener();
        connector = AlarmConnectorFactory.getConnector("tcp://sljas2:2506,tcp://sljas3:2506");
        connector.addListener(listener);
        connector.setTopicRoot("CMW.ALARM_SYSTEM.ALARMS.SOURCES.");
        connector.addSource("#");
        connector.connect();
    }
    
    public LaserNativeMessageHandler(AlarmListener listener) {
        this.listener= listener;
    }

    @Override
    public void shutdown() throws EqIOException {
        super.shutdown();
        
        if (this.connector != null) {
            connector.disconnect();
        }
    }
    
    @Override
    public synchronized void connectToDataSource() throws EqIOException {

        log.debug("connectToDataSource - entering connectToDataSource(). Creating subscriptions for all registered DataTags..");

        initializeMBean();
        registerTags();

        log.debug("connectToDataSource - leaving connectToDataSource()");

    }

    @Override
    public synchronized void disconnectFromDataSource() throws EqIOException {
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("disconnectFromDataSource - entering diconnectFromDataSource()..");
        }

        for (LASERHardwareAddress lAddress : alarmList4Equipement.get(getEquipmentConfiguration())) {
            if (tag4LaserHardwareAddress.containsKey(lAddress.toString())) {
                tag4LaserHardwareAddress.remove(lAddress.toString());
            }
        }
        heartbeatTag4Equipment.remove(getEquipmentConfiguration());
        equMessageSenderList.remove(getEquipmentConfiguration());
        equipementByName.remove(getEquipmentConfiguration().getName());
        alarmList4Equipement.remove(getEquipmentConfiguration());

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("disconnectFromDataSource - leaving diconnectFromDataSource()..");
        }
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshDataTag(long arg0) {
        // TODO Auto-generated method stub

    }

    //
    // --- REGISTERING TAGS ---
    //

    synchronized public void registerTags() throws EqIOException {

        log.info("registering tags ...");

        // ExecutorService service = Executors.newFixedThreadPool(2);

        final Collection<LASERHardwareAddress> listLaserHaddr = new ArrayList<LASERHardwareAddress>();
        for (final ISourceDataTag dataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
            if (dataTag.getHardwareAddress() instanceof LASERHardwareAddress) {

                // Runnable r = new Runnable() {
                //
                // @Override
                // public void run() {
                LASERHardwareAddress laddr = (LASERHardwareAddress) dataTag.getHardwareAddress();
                getEquipmentMessageSender().sendTagFiltered(dataTag, Boolean.FALSE, System.currentTimeMillis());
                tag4LaserHardwareAddress.put(laddr.toString(), dataTag);
                listLaserHaddr.add(laddr);
                // }
                //
                // };
                //
                // service.submit(r);

                // Using toString because it takes times by using the LASERHarwareAddress type

            } else if (dataTag.getHardwareAddress() instanceof SimpleHardwareAddress) {
                ISourceDataTag heartbeatTag = getEquipmentConfiguration().getSourceDataTag(
                        getEquipmentConfiguration().getAliveTagId());
                heartbeatTag4Equipment.put(getEquipmentConfiguration(), heartbeatTag);
            } else {
                String errorMsg = "Unsupported HardwareAddress: " + dataTag.getHardwareAddress().getClass();
                throw new EqIOException(errorMsg);
            }

        }

        // try {
        // service.shutdown();
        // service.awaitTermination(5, TimeUnit.MINUTES);
        // } catch (InterruptedException e) {
        // log.error("Timeout while waiting for sending initial datatags. Not sure all have been submitted.");
        // }

        equMessageSenderList.put(getEquipmentConfiguration(), getEquipmentMessageSender());
        alarmList4Equipement.put(getEquipmentConfiguration(), listLaserHaddr);
        equipementByName.put(getEquipmentConfiguration().getName(), getEquipmentConfiguration());

        synchronized (AlarmListener.lock) {
            AlarmListener.handler = this;
        }

        log.info("Tags registered");
    }

    synchronized public void registerTag(ISourceDataTag tag) {
        if (tag.getHardwareAddress() instanceof LASERHardwareAddress) {
            LASERHardwareAddress laserHardwareAddress = (LASERHardwareAddress) tag.getHardwareAddress();
            alarmList4Equipement.get(getEquipmentConfiguration()).add(laserHardwareAddress);
            tag4LaserHardwareAddress.put(laserHardwareAddress.toString(), tag);
        } else if (tag.getHardwareAddress() instanceof SimpleHardwareAddress) {
            ISourceDataTag heartbeatTag = getEquipmentConfiguration().getSourceDataTag(
                    getEquipmentConfiguration().getAliveTagId());
            heartbeatTag4Equipment.put(getEquipmentConfiguration(), heartbeatTag);
        }

        synchronized (AlarmListener.lock) {
            AlarmListener.handler = this;
        }
    }

    synchronized public void unregisterTag(ISourceDataTag tag) {
        if (tag.getHardwareAddress() instanceof LASERHardwareAddress) {
            LASERHardwareAddress laserHardwareAddress = (LASERHardwareAddress) tag.getHardwareAddress();
            alarmList4Equipement.get(getEquipmentConfiguration()).remove(laserHardwareAddress);
            tag4LaserHardwareAddress.remove(laserHardwareAddress.toString());
        } else if (tag.getHardwareAddress() instanceof SimpleHardwareAddress) {
            heartbeatTag4Equipment.remove(getEquipmentConfiguration());
        }

        synchronized (AlarmListener.lock) {
            AlarmListener.handler = this;
        }
    }

    //
    // --- GETTING INFORMATIONS WE NEED ---
    //

    public ConcurrentHashMap<IEquipmentConfiguration, Collection<LASERHardwareAddress>> getRegisteredLaserHardwareAddress() {
        return alarmList4Equipement;
    }

    public Collection<LASERHardwareAddress> getHardwareAddresses4Equipement(
            IEquipmentConfiguration equipmentConfiguration) {
        return alarmList4Equipement.get(equipmentConfiguration);
    }

    public ArrayList<String> getEquipementsName() {
        ArrayList<String> nameList = new ArrayList<String>();
        for (IEquipmentConfiguration equipementConfiguration : alarmList4Equipement.keySet()) {
            nameList.add(equipementConfiguration.getName());
        }
        return nameList;
    }

    public IEquipmentConfiguration getEquipementByName(String sourceName) {
        return equipementByName.get(sourceName);
    }

    public IEquipmentMessageSender getEquipementMessageSender(IEquipmentConfiguration equipmentConfiguration) {
        return equMessageSenderList.get(equipmentConfiguration);
    }

    public ISourceDataTag getHeartbeat4Equipement(IEquipmentConfiguration equipmentConfiguration) {
        return heartbeatTag4Equipment.get(equipmentConfiguration);
    }

    public ISourceDataTag getTag4LaserAddress(LASERHardwareAddress laserHardwareAddress) {
        return tag4LaserHardwareAddress.get(laserHardwareAddress.toString());
    }

    //
    // --- ADD/REMOVE/UPDATE a tag --------------------------------------------------------------------------
    //

    @Override
    public synchronized void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        try {
            registerTag(sourceDataTag);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }
        log.debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public synchronized void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        log.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        try {
            unregisterTag(sourceDataTag);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }
        log.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public synchronized void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag,
            ChangeReport changeReport) {
        log.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                log.debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (Exception ex) {
                changeReport.appendWarn(ex.getMessage());
            }
            log.debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
            registerTag(sourceDataTag);
        } else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }
        log.debug(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
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
            log.debug("leaving onUpdateEquipmentConfiguration()");
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

    
}
