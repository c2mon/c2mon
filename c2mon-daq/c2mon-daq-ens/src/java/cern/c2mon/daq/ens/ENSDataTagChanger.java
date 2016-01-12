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
package cern.c2mon.daq.ens;

import cern.c2mon.daq.common.conf.equipment.DataTagChangerHelper;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * The ENS data tag changer. He will perform the necessary changes for the
 * data tags. The current implementation just restarts the ENS message handler.
 * TODO Find a way which not involves restarting for every change.
 *
 * @author Andreas Lang
 *
 */
public class ENSDataTagChanger extends DataTagChangerHelper {

    /**
     * The ENSmessageHandler to do restarts.
     */
    private ENSMessageHandler ensMessageHandler;

    /**
     * Creates a new ENSDataTagChanger.
     * @param ensMessageHandler The ENSMessageHandler to perform restarts/
     */
    public ENSDataTagChanger(final ENSMessageHandler ensMessageHandler) {
        this.ensMessageHandler = ensMessageHandler;
    }

    /**
     * Called when a data tag is added to ENS.
     *
     * @param sourceDataTag The source data tag to add.
     * @param changeReport The change report to fill in. Its state is set to
     * success if nothing goes wrong.
     */
    @Override
    public void onAddDataTag(final ISourceDataTag sourceDataTag,
            final ChangeReport changeReport) {
        ensMessageHandler.restart();
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }

    /**
     * Called when a data tag is removed.
     *
     * @param sourceDataTag The removed data tag.
     * @param changeReport The change report to fill in. Its state is set to
     * success if nothing goes wrong.
     */
    @Override
    public void onRemoveDataTag(final ISourceDataTag sourceDataTag,
            final ChangeReport changeReport) {
        ensMessageHandler.restart();
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }

    /**
     * Called when a source data tag is updated.
     *
     * @param sourceDataTag The source data tag after the update.
     * @param oldSourceDataTag The source data tag object before the updates.
     * @param changeReport The report to fill in. Its state is set to
     * success if nothing goes wrong.
     */
    @Override
    public void onUpdateDataTag(final ISourceDataTag sourceDataTag,
            final ISourceDataTag oldSourceDataTag, final ChangeReport changeReport) {
        if (hasHardwareAddressChanged(sourceDataTag.getHardwareAddress(), oldSourceDataTag.getHardwareAddress())
                || hasDataTypeChanged(sourceDataTag, oldSourceDataTag)
                || hasNameChanged(sourceDataTag, oldSourceDataTag)
                || hasValueDeadbandTypeChanged(sourceDataTag, oldSourceDataTag)
                || hasEquipmentValueDeadbandChanged(sourceDataTag, oldSourceDataTag)) {
            ensMessageHandler.restart();
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }
}
