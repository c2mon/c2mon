/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.monit.listener.MonitListenerIntf;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * TODO run locally a monit agent and send notifications to the new DAQ (data analysis)
 * 
 * TODO adapt configuration files
 * TODO create deployment project
 * TODO install application on cs-ccr-dmnt1
 * 
 * TODO extend hardware address and implement polling when needed
 *      
 * @author mbuttner
 */
public class MonitMessageHandler extends EquipmentMessageHandler 
    implements IDataTagChanger, IEquipmentConfigurationChanger {

    public static final String VERSION = "1.0";
    
    private static final Logger LOG = LoggerFactory.getLogger(MonitMessageHandler.class);
    private static ClassPathXmlApplicationContext ctx;

    private MonitListenerIntf listener;
    
    private Thread procThr;
    private MonitEventProcessor proc;
    
    public static String profile = "PRO";

    //
    // --- PUBLIC METHODS --------------------------------------------------------------------------------
    //
    /**
     * @return <code>SpectrumEventProcess</code> used by unit tests.
     */
    public MonitEventProcessor getProcessor()
    {
        return this.proc;
    }

    //
    // --- CONNECT / DISCONNECT --------------------------------------------------------------------------
    //
    @Override
    public void connectToDataSource() throws EqIOException {
        
        LOG.info("Connecting data source for SpectrumMessageHandler version [" + VERSION + "]");
        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("classpath:dmn-monit-config.xml");
            ctx.getEnvironment().setDefaultProfiles(profile);
            ctx.refresh();
        }
        
        proc = ctx.getBean("eventProc", MonitEventProcessor.class);
        MonitUpdateEvent.setSender(getEquipmentMessageSender());
        procThr = new Thread(proc);
        
        listener = ctx.getBean("eventListener", MonitListenerIntf.class);
        listener.setProcessor(proc);
        listener.connect();
        
        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            registerTag(tag);
        }        
    
        proc.init();
        procThr.start();
        getEquipmentMessageSender().confirmEquipmentStateOK();
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
        LOG.warn("Received disconnection request ...");
        listener.disconnect();
        proc.shutdown();
        try {
            procThr.join(5 * 1000);
        } catch (Exception e) {
            throw new EqIOException("Failed to stop connection threads for the equipment");
        }
        LOG.info("Disconnection request processed.");
    }
    
    //
    // --- METHODS FOR CONFIGURATION CHANGES --------------------------------------------------------
    //
    /**
     * Apart from the name, there is no parameter specific to the Spectrum equipment configuration
     * since the Spectrum server names and the port used for communication are in the Spring context 
     */
    @Override
    public void onUpdateEquipmentConfiguration(IEquipmentConfiguration equipmentConfiguration,
            IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        LOG.debug("entering onUpdateEquipmentConfiguration()..");

        // without analyzing what has changed in the equipment's configuration
        // we simply call disconnectFromDataSource() and right after - connectToDataSource()
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();

            changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn(ex.getMessage());
        } finally {
            LOG.debug("leaving onUpdateEquipmentConfiguration()");
        }
        
    }

    @Override
    public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        LOG.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        registerTag(sourceDataTag);
        LOG.debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        LOG.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        try {
            unregisterTag(sourceDataTag);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }
        LOG.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));        
    }

    /**
     * The operation is present pas not really used. The only attribute of the tag (apart from its name)
     * is the hostname. IF the hostname changes, the name of the tag will also change as it contains the
     * hostname as part of the "alarm id"-triplet. Changing the hostname is equivalent to simply replace
     * the tag. Therefore, when this method is called, we simply replace the old by the new tag
     */
    @Override
    public void onUpdateDataTag(ISourceDataTag tag, ISourceDataTag oldTag, ChangeReport changes) {
        LOG.debug(format("entering onUpdateDataTag(%d,%d)..", tag.getId(), oldTag.getId()));
        changes.setState(CHANGE_STATE.SUCCESS);
        if (!oldTag.getHardwareAddress().equals(tag.getHardwareAddress())) {
            try {
                LOG.debug(format("calling  unregisterTag(%d)..", oldTag.getId()));
                unregisterTag(oldTag);
            } catch (Exception ex) {
                changes.appendWarn(ex.getMessage());
            }
            LOG.debug(format("calling  registerTag(%d)..", tag.getId()));
            registerTag(tag);
        }
        else {
            changes.appendInfo("No change detected in the tag hardware address. No action effected");
        }
        LOG.debug(format("leaving onUpdateDataTag(%d,%d)", tag.getId(), oldTag.getId()));        
    }


    
    synchronized void registerTag(ISourceDataTag tag) {
        LOG.info("Register tag {} ...", tag.getName());
        try {
            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();
            MonitHardwareAddress addr = MonitHardwareAddress.fromJson(saddr.getAddress().trim());
            proc.add(addr.getHostname(), addr.getMetricName(), tag); 
        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }
    }

    synchronized void unregisterTag(ISourceDataTag tag) throws Exception {
        try {
            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();
            MonitHardwareAddress addr = MonitHardwareAddress.fromJson(saddr.getAddress().trim());
            proc.del(addr.getHostname()); 
        } catch (Exception ex) {
            String err = format("Unable to unregister tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            LOG.error(err);
            throw new Exception(err);
        }
    }
    
    //
    // --- REFRESH OPs --------------------------------------------------------------------------------------
    //
    @Override
    public void refreshAllDataTags() {
        LOG.warn("refreshAllDataTags() method is not implemented by this DAQ ... any problem related to reconfig?");
    }

    @Override
    public void refreshDataTag(long tagId) {
        LOG.info("... refreshing tag " + tagId + " ...");
        ISourceDataTag tag = getEquipmentConfiguration().getSourceDataTags().get(tagId);
        getEquipmentMessageSender().sendTagFiltered(tag, tag.getCurrentValue(), System.currentTimeMillis());
    }

}
