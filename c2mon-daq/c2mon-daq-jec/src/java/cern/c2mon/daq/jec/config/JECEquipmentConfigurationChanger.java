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
package cern.c2mon.daq.jec.config;
import cern.c2mon.daq.common.conf.equipment.EquipmentConfigurationChangerHelper;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.jec.IJECRestarter;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * JEC implementation for an equipment changer.
 * 
 * @author Andreas Lang
 *
 */
public class JECEquipmentConfigurationChanger implements IEquipmentConfigurationChanger {

    /**
     * Allows to trigger a restart of the Message handler. The restart might be delayed. 
     */
    private IJECRestarter jecRestarter;

    /**
     * Creates a new JECEquipment configuration changer.
     * 
     * @param jecRestarter The JECResarter which allows to trigger a possibly delayed
     * restart of the JEC handler.
     */
    public JECEquipmentConfigurationChanger(final IJECRestarter jecRestarter) {
        this.jecRestarter = jecRestarter;
    }
    
    /**
     * Applies the implementation specific changes to the equipment.
     * 
     * @param equipmentConfiguration The new equipment configuration.
     * @param oldEquipmentConfiguration A copy of the old equipment configuration.
     * @param changeReport The change report to update.
     */
    @Override
    public void onUpdateEquipmentConfiguration(final IEquipmentConfiguration equipmentConfiguration, 
            final IEquipmentConfiguration oldEquipmentConfiguration, final ChangeReport changeReport) {
        if (EquipmentConfigurationChangerHelper
                .hasAddressChanged(equipmentConfiguration, oldEquipmentConfiguration)
            || EquipmentConfigurationChangerHelper
                .hasAliveTagIntervalChanged(equipmentConfiguration, oldEquipmentConfiguration)) {
            jecRestarter.triggerRestart();
            changeReport.appendInfo("Restart for equipment triggered.");
        }
        changeReport.setState(CHANGE_STATE.SUCCESS);
    }

}
