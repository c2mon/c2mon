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

import java.lang.reflect.Field;
import java.util.List;

import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;

/**
 * Helper class with method to check for differences in the hardware address.
 * 
 * @author Andreas Lang
 * 
 */
public abstract class TagChangerHelper {
    /**
     * Checks if all fields are the same.
     * 
     * @param hardwareAddress
     *            The new hardware address.
     * @param oldHardwareAddress
     *            The old hardware address.
     * @return True if a field is not equal with the field in the old address.
     *         If all fields are the same it returns false.
     */
    public static boolean hasHardwareAddressChanged(final HardwareAddress hardwareAddress, final HardwareAddress oldHardwareAddress) {
        SimpleTypeReflectionHandler simpleTypeReflectionHandler = new SimpleTypeReflectionHandler();
        List<Field> sctHardwareAddressFields = simpleTypeReflectionHandler.getNonTransientSimpleFields(hardwareAddress.getClass());
        List<Field> oldSctHardwareAddressFields = simpleTypeReflectionHandler.getNonTransientSimpleFields(oldHardwareAddress.getClass());
        int i = 0;
        for (Field field : sctHardwareAddressFields) {
            try {
                Object fieldValue = field.get(hardwareAddress);
                Object oldFieldValue = oldSctHardwareAddressFields.get(i);
                if (fieldValue == null) {
                    if (oldFieldValue == null) {
                        return true;
                    }
                } else if (!field.get(hardwareAddress).equals(oldSctHardwareAddressFields.get(i))) {
                    return true;
                }
            } catch (Exception e) {
                return true; // if not sure return that they are not equal
            }
            i++;
        }
        return false; // nothing found
    }

}
