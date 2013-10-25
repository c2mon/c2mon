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

import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
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
