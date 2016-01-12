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
package cern.c2mon.daq.common.conf.equipment;

import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.daq.config.ChangeReport;
/**
 * Specific DAQ implementations should implement and register (or configure via
 * Spring) this interface to react to changes of a command tag.
 * 
 * @author Andreas Lang
 *
 */
public interface ICommandTagChanger {
    
    /**
     * Called when a command tag is added to the configuration. The correct
     * state has to be set in the report.
     * 
     * @param sourceCommandTag The added source command tag.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onAddCommandTag(
            final ISourceCommandTag sourceCommandTag, 
            final ChangeReport changeReport);
    
    /**
     * Called when a command tag is removed from the configuration. The correct
     * state has to be set in the report.
     * @param sourceCommandTag The removed source command tag.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onRemoveCommandTag(
            final ISourceCommandTag sourceCommandTag, 
            final ChangeReport changeReport);
    
    /**
     * Called when a command tag is updated in the configuration. The correct
     * state has to be set in the report.
     * 
     * @param sourceCommandTag The updated source command tag.
     * @param oldSourceCommandTag The command tag before the update.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onUpdateCommandTag(
            final ISourceCommandTag sourceCommandTag,
            final ISourceCommandTag oldSourceCommandTag,
            final ChangeReport changeReport);
}
