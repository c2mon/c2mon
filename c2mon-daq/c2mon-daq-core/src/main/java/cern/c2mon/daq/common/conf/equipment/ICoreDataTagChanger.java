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

import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.daq.config.ChangeReport;
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
