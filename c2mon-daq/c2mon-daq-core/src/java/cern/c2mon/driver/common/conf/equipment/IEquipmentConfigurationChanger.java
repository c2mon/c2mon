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
package cern.c2mon.driver.common.conf.equipment;

import cern.tim.shared.daq.config.ChangeReport;

/**
 * Interface to be implemented from parts of the core which are interested in
 * changes to the EquipmentConfiguration
 * 
 * @author Andreas Lang
 *
 */
public interface IEquipmentConfigurationChanger {
    /**
     * This is called when an equipment unit update occurred. The correct
     * state has to be set in the report.
     * 
     * @param equipmentConfiguration The updated equipment configuration.
     * @param oldEquipmentConfiguration The former equipment configuration.
     * @param changeReport The change report which needs to be filled.
     */
    void onUpdateEquipmentConfiguration(
            final IEquipmentConfiguration equipmentConfiguration,
            final IEquipmentConfiguration oldEquipmentConfiguration,
            final ChangeReport changeReport);
}
