/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.daq.spectrum;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerIntf;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * 
 *      
 * @author mbuttner
 */
public class SpectrumMessageHandler extends EquipmentMessageHandler 
    implements IDataTagChanger, IEquipmentConfigurationChanger {

    public static final String VERSION = "1.0";
    
    private static final Logger LOG = LoggerFactory.getLogger(SpectrumMessageHandler.class);
    private static ClassPathXmlApplicationContext ctx;

    private Thread listenerThr;
    private SpectrumListenerIntf spectrum;
    
    private Thread procThr;
    private SpectrumEventProcessor proc;
    
    public static String profile = "PRO";

    //
    // --- PUBLIC METHODS --------------------------------------------------------------------------------
    //
    /**
     * @return <code>SpectrumEventProcess</code> used by unit tests.
     */
    public SpectrumEventProcessor getProcessor()
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
            ctx = new ClassPathXmlApplicationContext("classpath:dmn-spectrum-config.xml");
            ctx.getEnvironment().setDefaultProfiles(profile);
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
    
        proc.init();
        listenerThr.start();
        procThr.start();
        getEquipmentMessageSender().confirmEquipmentStateOK();
    }

    @Override
    public void disconnectFromDataSource() throws EqIOException {
        LOG.warn("Received disconnection request ...");
        spectrum.shutdown();
        proc.shutdown();
        try {
            listenerThr.join(5 * 1000);
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
            SpectrumHardwareAddress addr = SpectrumHardwareAddress.fromJson(saddr.getAddress().trim());
            proc.add(addr.getHostname(), tag); 
        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }
    }

    synchronized void unregisterTag(ISourceDataTag tag) throws Exception {
        try {
            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();
            SpectrumHardwareAddress addr = SpectrumHardwareAddress.fromJson(saddr.getAddress().trim());
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
