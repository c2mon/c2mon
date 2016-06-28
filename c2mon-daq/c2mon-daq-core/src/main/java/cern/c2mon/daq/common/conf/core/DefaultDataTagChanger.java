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
package cern.c2mon.daq.common.conf.core;

import cern.c2mon.daq.common.conf.equipment.DataTagChangerHelper;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
/**
 * This is a default implementation. of the data tag changer.
 * It returns for every change which might affect the implementation
 * layer the status REBOOT. You might use it also to do partial 
 * implementations of the reconfiguration. Just inherit then from this
 * class and overwrite the parts you want to implement.
 * 
 * @author Andreas Lang
 *
 */
public class DefaultDataTagChanger extends DataTagChangerHelper {

    /**
     * Called when a command tag is added. Every call of this method would
     * require equipment specific reaction. Therefore it will
     * always set the REBOOT state in the report.
     * @param sourceDataTag The command tag to add.
     * @param changeReport The report to fill.
     */
    @Override
    public void onAddDataTag(final ISourceDataTag sourceDataTag, 
            final ChangeReport changeReport) {
        changeReport.appendError("The equipment has no implementation to"
                + " add data tags. You have to restart the DAQ.");
        changeReport.setState(CHANGE_STATE.REBOOT);
    }

    /**
     * Called when a command tag is removed. Every call of this method would
     * require equipment specific reaction. Therefore it will
     * always set the REBOOT state in the report.
     * @param sourceDataTag The command tag to remove.
     * @param changeReport The report to fill.
     */
    @Override
    public void onRemoveDataTag(final ISourceDataTag sourceDataTag, 
            final ChangeReport changeReport) {
        changeReport.appendError("The equipment has no implementation to"
                + " remove data tags. You have to restart the DAQ.");
        changeReport.setState(CHANGE_STATE.REBOOT);
    }

    /**
     * Called when a command tag is updated if the change might affect
     * the equipment specific part it will set the REBOOT state in the
     * report.
     * @param sourceDataTag The updated source command tag.
     * @param oldSourceDataTag The source command tag before the update.
     * @param changeReport The report to fill.
     */
    @Override
    public void onUpdateDataTag(final ISourceDataTag sourceDataTag,
            final ISourceDataTag oldSourceDataTag, 
            final ChangeReport changeReport) {
        // TODO If the source timeout for the implementations has moved to the hardware address remove that.
        if (hasDataTypeChanged(sourceDataTag, oldSourceDataTag)) {
            changeReport.appendError("Data type changes might affect "
                    + "the equipment. But there is no implementation. That "
                    + "means you have to restart the DAQ.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (hasTimeDeadbandChanged(sourceDataTag, oldSourceDataTag)) {
            changeReport.appendError("Timedeadband changes might affect "
                    + "the equipment. But there is no implementation. That "
                    + "means you have to restart the DAQ.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (hasEquipmentValueDeadbandChanged(sourceDataTag, oldSourceDataTag)) {
            changeReport.appendError("Value deadband changes might affect "
                    + "the equipment. But there is no implementation. That "
                    + "means you have to restart the DAQ.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (hasValueDeadbandTypeChanged(sourceDataTag, oldSourceDataTag)) {
            changeReport.appendError("Value deadband type changes might affect "
                    + "the equipment. But there is no implementation. That "
                    + "means you have to restart the DAQ.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (hasHardwareAddressChanged(sourceDataTag.getHardwareAddress(), oldSourceDataTag.getHardwareAddress())) {
            changeReport.appendError("Changes to the hardware address might affect "
                    + "the equipment. But there is no implementation. That "
                    + "means you have to restart the DAQ.");
            changeReport.setState(CHANGE_STATE.REBOOT);
        }
        if (!changeReport.getState().equals(CHANGE_STATE.REBOOT)) {
            changeReport.setState(CHANGE_STATE.SUCCESS);
        }
    }

}
