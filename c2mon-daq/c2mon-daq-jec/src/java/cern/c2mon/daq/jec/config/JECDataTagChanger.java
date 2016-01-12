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

import static java.lang.String.format;
import cern.c2mon.daq.common.conf.equipment.DataTagChangerHelper;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The JECDataTagChanger applies all changes necessary for data tags.
 * 
 * @author Andreas Lang
 * 
 */
public class JECDataTagChanger implements IDataTagChanger {
    /**
     * Warn message if the reconfiguration was successful and
     * the address range did not change.
     */
    public static final String WITHIN_RANGE_INFO_MESSAGE = "Source Data Tag was within the previous PLC address range.";
    
    /**
     * Info message if the configuration was successful but the
     * PLC address range changed.
     */
    public static final String OUT_OF_RANGE_WARN_MESSAGE = 
                        "Source Data Tag was not within" 
                        + "the previous PLC address range. Scheduling reconfiguration of PLC. "
                        + "Make sure the bigger address range is really provided "
                        + "from the PLC.";
    /**
     * The PLC tag configuration controller allows to access and modify the
     * configuration.
     */
    private IJECTagConfigurationController plcTagController;
    /**
     * object to restart the equipment handler.
     */
    private IJECRestarter jecRestarter;
    
    /**
     * The equipment logger of this class.
     */
    private EquipmentLogger equipmentLogger;

    /**
     * Creates a new JECDataTagChanger.
     * 
     * @param plcTagController
     *            The JECTagConfigurationController to use.
     * @param jecRestarter
     *            The jec restarter to use.
     */
    public JECDataTagChanger(final IJECTagConfigurationController plcTagController, final IJECRestarter jecRestarter,
            EquipmentLogger equipmentLogger) {
        this.plcTagController = plcTagController;
        this.jecRestarter = jecRestarter;
        this.equipmentLogger = equipmentLogger;
    }

    /**
     * Called when a data tag is added to the configuration. Modifies the
     * configuration to apply the changes on equipment level.
     * 
     * @param sourceDataTag
     *            The source data tag to add.
     * @param changeReport
     *            The change report to update.
     */
    @Override
    public void onAddDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onAddDataTag - entering onAddCommandTag(%d)..", sourceDataTag.getId()));
        }

        reconfigureTag(sourceDataTag, changeReport);
//        changeReport.setState(reconfigureTag(sourceDataTag, changeReport));
        if (changeReport.getState() == CHANGE_STATE.SUCCESS) {
            changeReport.appendInfo("onAddDataTag - SourceDataTag added ...");
        } else {
            changeReport.appendInfo("onAddDataTag - SourceDataTag not added ...");
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onRemoveDataTag - leaving onUpdateDataTag(%d)", sourceDataTag.getId()));
        }
    }

    /**
     * Method to reconfigure a data Tag.
     * 
     * @param sourceDataTag
     *            The tag to reconfigure.
     * @param changeReport
     *            The change report to update.
     */
    private void reconfigureTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
        PLCHardwareAddress plcHardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        int blockType = plcHardwareAddress.getBlockType();
        // It is safer to reconnect to the PLC if it is an analog tag
        if (blockType == PLCHardwareAddress.STRUCT_ANALOG 
                || blockType == PLCHardwareAddress.STRUCT_DIAG_ANALOG) {
            jecRestarter.triggerRestart();
            if (plcTagController.isInAddressRange(plcHardwareAddress)) {
                changeReport.appendInfo(WITHIN_RANGE_INFO_MESSAGE);
            } else {
                changeReport.appendWarn(OUT_OF_RANGE_WARN_MESSAGE);
            }
        } else if (plcTagController.isInAddressRange(plcHardwareAddress)) {
            plcTagController.configureDataTag(sourceDataTag);
            changeReport.appendInfo(WITHIN_RANGE_INFO_MESSAGE);
        } else {
            jecRestarter.triggerRestart();
            changeReport.appendWarn(OUT_OF_RANGE_WARN_MESSAGE);
        }
    }

    /**
     * Called when a data tag should be removed.
     * 
     * @param sourceDataTag
     *            The tag to remove.
     * @param changeReport
     *            The change report to update
     */
    @Override
    public void onRemoveDataTag(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onRemoveDataTag - entering onRemoveDataTag(%d)..", sourceDataTag.getId()));
        }

        plcTagController.removeDataTag(sourceDataTag);
        changeReport.appendInfo("SourceDataTag removed in PLC configuration.");
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onRemoveDataTag - leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
        }
    }

    /**
     * Called when a data tag should be updated.
     * 
     * @param sourceDataTag
     *            The tag to update.
     * @param oldSourceDataTag
     *            The former tag to compare for the changes.
     * @param changeReport
     *            The change report to update
     */
    @Override
    public void onUpdateDataTag(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag, final ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onRemoveDataTag - entering onUpdateDataTag(%d)..", sourceDataTag.getId()));
        }

        if (DataTagChangerHelper.hasValueDeadbandTypeChanged(sourceDataTag, oldSourceDataTag) || DataTagChangerHelper.hasEquipmentValueDeadbandChanged(sourceDataTag, oldSourceDataTag)) {
            jecRestarter.triggerRestart();
            changeReport.appendInfo("Source Data Equipment. Reconnect and reconfigure PLC.");

        }
        PLCHardwareAddress hardwareAddress = (PLCHardwareAddress) sourceDataTag.getHardwareAddress();
        PLCHardwareAddress oldHardwareAddress = (PLCHardwareAddress) oldSourceDataTag.getHardwareAddress();
        if (DataTagChangerHelper.hasHardwareAddressChanged(hardwareAddress, oldHardwareAddress)) {
            if (hardwareAddress.getPhysicalMinVal() != oldHardwareAddress.getPhysicalMinVal() || hardwareAddress.getPhysicalMaxVal() != oldHardwareAddress.getPhysicalMaxVal()
                    || hardwareAddress.getResolutionFactor() != oldHardwareAddress.getResolutionFactor()) {
                jecRestarter.triggerRestart();
            } else if (JECTagChangerHelper.mightAffectPLCAddressSpace(hardwareAddress, oldHardwareAddress)) {
                reconfigureTag(sourceDataTag, changeReport);
            }
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(format("onRemoveDataTag - leaving onUpdateDataTag(%d)", sourceDataTag.getId()));
        }
    }
    
    /**
     * @return the equipmentLogger
     */
    public EquipmentLogger getEquipmentLogger() {
        return this.equipmentLogger;
    }

}
