/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.spectrum.address.SpectrumHardwareAddress;
import cern.c2mon.daq.spectrum.address.SpectrumHardwareAddressFactory;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerIntf;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * TODO add "active list interface to the DAQ", based on JDK web
 * TODO create unit tests especially for referencing/dereferencing alarms for the same host (based on fake message
 *      added by the unit tests to the event queue and than checking for result on tag)
 * 
 * TODO install and compare results to production status
 *      
 * @author mbuttner
 */
public class SpectrumMessageHandler extends EquipmentMessageHandler 
    implements IDataTagChanger, IEquipmentConfigurationChanger {

    private static final Logger LOG = LoggerFactory.getLogger(SpectrumMessageHandler.class);
    private static ClassPathXmlApplicationContext ctx;

    private Thread listenerThr;
    private SpectrumListenerIntf spectrum;
    
    private Thread procThr;
    private SpectrumEventProcessor proc;
    
    //
    // --- CONNECT / DISCONNECT -------------------------------------------------------------------------------
    //
    @Override
    public void connectToDataSource() throws EqIOException {
        
        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("classpath:resources/dmn-spectrum-config.xml");
            ctx.getEnvironment().setDefaultProfiles("PRO");
            ctx.refresh();
        }
        
        proc = ctx.getBean("eventProc", SpectrumEventProcessor.class);
        proc.setSender(getEquipmentMessageSender());
        procThr = new Thread(proc);

        spectrum = ctx.getBean("eventListener", SpectrumListenerIntf.class);
        spectrum.setProcessor(proc);
        
        
        listenerThr = new Thread(spectrum);

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            registerTag(tag);
        }        
    
        listenerThr.start();
        procThr.start();
        getEquipmentMessageSender().confirmEquipmentStateOK();
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
       spectrum.shutdown();
       proc.shutdown();
       try {
           listenerThr.join(5 * 1000);
           procThr.join(5 * 1000);
       } catch (Exception e) {
           throw new EqIOException("Failed to stop connection threads for the equipment");
       }
    }
    
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

    //
    // --- ADD/REMOVE/UPDATE a tag --------------------------------------------------------------------------
    //
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

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        LOG.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));
        changeReport.setState(CHANGE_STATE.SUCCESS);
        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                LOG.debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (Exception ex) {
                changeReport.appendWarn(ex.getMessage());
            }
            LOG.debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
            registerTag(sourceDataTag);
        }
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }
        LOG.debug(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));        
    }

    @Override
    public void refreshAllDataTags() {
        // WB left this one out in the alarm monitors ...
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // WB left this one out in the alarm monitors ...        
    }

    
    synchronized void registerTag(ISourceDataTag tag) {
        LOG.info("Register tag {} ...", tag.getName());
        try {
            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();
            SpectrumHardwareAddress addr = SpectrumHardwareAddressFactory.fromJson(saddr.getAddress().trim());
            proc.add(addr.getHostname(), tag); 
        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }
    }

    synchronized void unregisterTag(ISourceDataTag tag) throws Exception {
        try {
            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();
            SpectrumHardwareAddress addr = SpectrumHardwareAddressFactory.fromJson(saddr.getAddress().trim());
            proc.del(addr.getHostname()); 
        } catch (Exception ex) {
            String err = format("Unable to unregister tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            LOG.error(err);
            throw new Exception(err);
        }
    }

}
