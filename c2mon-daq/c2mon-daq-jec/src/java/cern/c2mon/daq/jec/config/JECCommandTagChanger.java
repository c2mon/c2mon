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
package cern.c2mon.daq.jec.config;

import cern.c2mon.daq.common.conf.equipment.CommandTagChangerHelper;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * JEC implementation for a command tag changer.
 * 
 * @author Andreas Lang
 *
 */
public class JECCommandTagChanger implements ICommandTagChanger {

    /**
     * Reconfiguration object.
     */
    private IJECTagConfigurationController plcTagController;
    /**
     * Allows to trigger a restart of the Message handler. The restart might be delayed. 
     */
    private IJECRestarter jecRestarter; 
    
    /**
     * Creates a new JECCommandTagChanger.
     * 
     * @param plcTagController The object to apply the reconfiguration.
     * @param jecRestarter Allows the (possibly delayed) restart of the message handler.
     */
    public JECCommandTagChanger(final IJECTagConfigurationController plcTagController, 
            final IJECRestarter jecRestarter) {
        this.plcTagController = plcTagController;
        this.jecRestarter = jecRestarter;
    }
    
    /**
     * Adds and configures a command tag.
     * 
     * @param sourceCommandTag The command tag to add.
     * @param changeReport The change report object to fill with information about the added command.
     */
    @Override
    public void onAddCommandTag(final ISourceCommandTag sourceCommandTag, 
            final ChangeReport changeReport) {
        PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceCommandTag.getHardwareAddress();
        reconfigureTag(sourceCommandTag, changeReport, plcHardwareAddress);
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }

    /**
     * Removes a command tag from the configuration.
     * 
     * @param sourceCommandTag The source command tag to remove.
     * @param changeReport The change report object to fill with information about the removed command.
     */
    @Override
    public void onRemoveCommandTag(final ISourceCommandTag sourceCommandTag, 
            final ChangeReport changeReport) {
        plcTagController.removeCommandTag(sourceCommandTag);
        changeReport.appendInfo("SourceCommandTag removed in PLC configuration.");
        changeReport.setState(CHANGE_STATE.SUCCESS);

    }

    /**
     * Updates a command tag in the configuration.
     * 
     * @param sourceCommandTag The new source command tag object.
     * @param oldSourceCommandTag The old source command tag to compare for changes.
     * @param changeReport The change report object to fill with information about the updated command.
     */
    @Override
    public void onUpdateCommandTag(final ISourceCommandTag sourceCommandTag,
            final ISourceCommandTag oldSourceCommandTag, final ChangeReport changeReport) {
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceCommandTag.getHardwareAddress();
        PLCHardwareAddress oldHardwareAddress = (PLCHardwareAddress) oldSourceCommandTag.getHardwareAddress();
        if (CommandTagChangerHelper
                .hasHardwareAddressChanged(hardwareAddress, oldHardwareAddress)
                && JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress)) {
            changeReport.appendInfo("SourceCommandTag update might affect address space triggering restart.");
            reconfigureTag(sourceCommandTag, changeReport, hardwareAddress);
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }
    
    /**
     * Reconfigures a command tag which possibly includes the triggering of a restart.
     * 
     * @param sourceCommandTag The source command tag to reconfigure.
     * @param changeReport The change report to update.
     * @param plcHardwareAddress The PLC hardware address of this command tag.
     */
    private void reconfigureTag(final ISourceCommandTag sourceCommandTag, 
            final ChangeReport changeReport, final PLCHardwareAddress plcHardwareAddress) {
        if (plcTagController.isInAddressRange(plcHardwareAddress)) {
            plcTagController.configureCommandTag(sourceCommandTag);
            changeReport.appendInfo("Source Command Tag was within the previous PLC address range.");
        }
        else {
            jecRestarter.triggerRestart();
            changeReport.appendInfo("Source Command Tag was not within the previous PLC address range. Scheduled reconfiguration of PLC.");
        }
    }

}
