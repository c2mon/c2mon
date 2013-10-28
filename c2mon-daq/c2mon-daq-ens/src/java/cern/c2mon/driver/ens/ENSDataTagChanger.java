/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.ens;

import cern.c2mon.driver.common.conf.equipment.DataTagChangerHelper;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;

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
