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
package cern.c2mon.daq.common.conf.equipment;

import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
/**
 * This interface may be implemented and registered by core classes.
 * It uses the whole SourceDataTag and not the restricted interface
 * to inform about changes.
 * 
 * @author Andreas Lang
 *
 */
public interface ICoreDataTagChanger {
    
    /**
     * Called when a data tag is added to the configuration.
     * @param sourceDataTag The added source data tag.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onAddDataTag(
            final SourceDataTag sourceDataTag, 
            final ChangeReport changeReport);
    
    /**
     * Called when a data tag is removed from the configuration.
     * @param sourceDataTag The removed source data tag.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onRemoveDataTag(
            final SourceDataTag sourceDataTag, 
            final ChangeReport changeReport);
    
    /**
     * Called when a data tag is updated in the configuration.
     * @param sourceDataTag The updated source data tag.
     * @param oldSourceDataTag The data tag before the update.
     * @param changeReport The previously created change report which should
     * be filled with additional information.
     */
    void onUpdateDataTag(
            final SourceDataTag sourceDataTag,
            final SourceDataTag oldSourceDataTag,
            final ChangeReport changeReport);
}
