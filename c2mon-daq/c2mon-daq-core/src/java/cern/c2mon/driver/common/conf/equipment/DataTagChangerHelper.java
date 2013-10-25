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


import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Helper class for SourceDataTag changers. It has a some helper methods
 * to simplify checking of changed types. 
 * 
 * @author Andreas Lang
 *
 */
public abstract class DataTagChangerHelper extends TagChangerHelper 
        implements IDataTagChanger {
    
    /**
     * Checks if the value deadband type has changed between the old 
     * and the new data tag.
     * 
     * @param sourceDataTag The new source data tag.
     * @param oldSourceDataTag The old version of the tag.
     * @return True if the data type has changed else false.
     */
    public static boolean hasValueDeadbandTypeChanged(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag) {
        return sourceDataTag.getValueDeadbandType() != oldSourceDataTag.getValueDeadbandType();
    }

    /**
     * Checks if the name has changed between the old and the new data tag.
     * 
     * @param sourceDataTag The new source data tag.
     * @param oldSourceDataTag The old version of the tag.
     * @return True if the data type has changed else false.
     */
    public static boolean hasNameChanged(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag) {
        return !sourceDataTag.getName().equals(oldSourceDataTag.getName());
    }

    /**
     * Checks if the data type has changed between the old and the new data tag.
     * 
     * @param sourceDataTag The new source data tag.
     * @param oldSourceDataTag The old version of the tag.
     * @return True if the name has changed else false.
     */
    public static boolean hasDataTypeChanged(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag) {
        return !sourceDataTag.getDataType().equals(oldSourceDataTag.getDataType());
    }

    /**
     * Checks if the deadband type is an equipment deadband and if it 
     * the deadband value changed.
     * 
     * @param sourceDataTag The actual source data tag.
     * @param oldSourceDataTag The old source data tag.
     * @return Returns true if the deadband type is an equipment deadband type and 
     * the deadband value has changed else false.
     */
    public static boolean hasEquipmentValueDeadbandChanged(final ISourceDataTag sourceDataTag,
            final ISourceDataTag oldSourceDataTag) {
        return sourceDataTag.getValueDeadbandType() == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE 
                || sourceDataTag.getValueDeadbandType() == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE 
                && sourceDataTag.getValueDeadband() != oldSourceDataTag.getValueDeadband();
    }
    
    /**
     * Checks if the timedeadband has changed between the two data tags.
     * 
     * @param sourceDataTag The new data tag.
     * @param oldSourceDataTag The old data tag.
     * @return True if the timedeabband has changed.
     */
    public static boolean hasTimeDeadbandChanged(final ISourceDataTag sourceDataTag, final ISourceDataTag oldSourceDataTag) {
        return sourceDataTag.getTimeDeadband()
                != oldSourceDataTag.getTimeDeadband();
    }
}
