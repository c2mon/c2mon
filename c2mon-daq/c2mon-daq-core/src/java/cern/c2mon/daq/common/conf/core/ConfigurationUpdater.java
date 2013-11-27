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
package cern.c2mon.daq.common.conf.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.config.CommandTagUpdate;
import cern.c2mon.shared.daq.config.ConfigurationJavaConstants;
import cern.c2mon.shared.daq.config.DataTagAddressUpdate;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.config.EquipmentConfigurationUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;
import cern.c2mon.shared.daq.config.ProcessConfigurationUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataTag;

/**
 * Updater for the values in the reconfiguration objects.
 * 
 * @author Andreas Lang
 *
 */
public class ConfigurationUpdater 
            extends SimpleTypeReflectionHandler
            implements ConfigurationJavaConstants {
    
    /**
     * Updates the values of a data tag with the values of an update object.
     * 
     * @param dataTagUpdate The data tag update with all the information of he update.
     * @param sourceDataTag The data tag to update.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateDataTag(final DataTagUpdate dataTagUpdate,
            final SourceDataTag sourceDataTag) throws IllegalAccessException, NoSuchFieldException {
        fillNonNullFields(dataTagUpdate, sourceDataTag,
                CHANGE_ID_FIELD, DATA_TAG_ID_FIELD, EQUIPMENT_ID_FIELD); 
        removeListOfFields(dataTagUpdate.getFieldsToRemove(), sourceDataTag);
        if (dataTagUpdate.getDataTagAddressUpdate() != null) {
            DataTagAddress dataTagAddress = sourceDataTag.getAddress();
            if (dataTagAddress == null) {
                dataTagAddress = new DataTagAddress();
                sourceDataTag.setAddress(dataTagAddress);
            }
            updateDataTagAddress(
                    dataTagUpdate.getDataTagAddressUpdate(), 
                    dataTagAddress
                    );
        }
    }
    
    /**
     * Updates the values of a data tag address with the values of an update object.
     * 
     * @param dataTagAddressUpdate The data tag address update with all the information of he update.
     * @param dataTagAddress The data tag address to update.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateDataTagAddress(
            final DataTagAddressUpdate dataTagAddressUpdate,
            final DataTagAddress dataTagAddress) throws NoSuchFieldException, IllegalAccessException {
        fillNonNullFields(dataTagAddressUpdate, dataTagAddress);
        removeListOfFields(dataTagAddressUpdate.getFieldsToRemove(), dataTagAddress);
        if (dataTagAddressUpdate.getHardwareAddressUpdate() != null) {
            updateHardwareAddress(
                    dataTagAddressUpdate.getHardwareAddressUpdate(),
                    dataTagAddress.getHardwareAddress()
                    );
        }
    }

    /**
     * Updates the values of a hardware address object with the values of an
     * hardware address update object.
     * 
     * @param hardwareAddressUpdate The hardware address update object.
     * @param hardwareAddress The hardware address object to change.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateHardwareAddress(
            final HardwareAddressUpdate hardwareAddressUpdate,
            final HardwareAddress hardwareAddress) throws NoSuchFieldException, IllegalAccessException {
        updateListOfFields(hardwareAddressUpdate.getChangedValues(), hardwareAddress); 
        removeListOfFields(hardwareAddressUpdate.getFieldsToRemove(), hardwareAddress);
    }

    /**
     * Updates a command tag object with the values of a command tag update object.
     * 
     * @param commandTagUpdate The update object to use.
     * @param commandTag The command tag to change.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateCommandTag(final CommandTagUpdate commandTagUpdate,
            final SourceCommandTag commandTag) throws IllegalAccessException, NoSuchFieldException {
        fillNonNullFields(commandTagUpdate, commandTag,
                CHANGE_ID_FIELD, COMMAND_TAG_ID_FIELD, EQUIPMENT_ID_FIELD);
        removeListOfFields(commandTagUpdate.getFieldsToRemove(), commandTag);
        if (commandTagUpdate.getHardwareAddressUpdate() != null) {
            updateHardwareAddress(
                    commandTagUpdate.getHardwareAddressUpdate(),
                    commandTag.getHardwareAddress()
                    );
            }
    }
    
    /**
     * Updates a equipment configuration with the values of an equipment configuration
     * update object.
     * 
     * @param equipmentConfigurationUpdate The update object with the changes.
     * @param equipmentConfiguration The equipment configuration to update.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateEquipmentConfiguration(
            final EquipmentConfigurationUpdate equipmentConfigurationUpdate,
            final IEquipmentConfiguration equipmentConfiguration) throws IllegalAccessException, NoSuchFieldException {
        fillNonNullFields(equipmentConfigurationUpdate, equipmentConfiguration,
                CHANGE_ID_FIELD, EQUIPMENT_ID_FIELD);
        removeListOfFields(
                equipmentConfigurationUpdate.getFieldsToRemove(),
                equipmentConfiguration
                );
    }

    /**
     * Updates the process configuration with the values of a process configuration
     * update object.
     * 
     * @param processConfigurationUpdate The update object with the changes.
     * @param processConfiguration The process configuration to update.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    public void updateProcessConfiguration(
            final ProcessConfigurationUpdate processConfigurationUpdate,
            final ProcessConfiguration processConfiguration) throws IllegalAccessException, NoSuchFieldException {
        fillNonNullFields(processConfigurationUpdate, processConfiguration,
                CHANGE_ID_FIELD, PROCESS_ID_FIELD);
        removeListOfFields(
                processConfigurationUpdate.getFieldsToRemove(),
                processConfiguration
                );
    }
    
    /**
     * Removes the fields with the provided names from the object. Remove in this case
     * means setting it to null.
     * 
     * @param fieldsToRemove The fields which should be removed.
     * @param targetObject The target object where the fields should be removed.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    private void removeListOfFields(final List<String> fieldsToRemove, 
            final Object targetObject) throws NoSuchFieldException, IllegalAccessException {
       // TODO At the moment remove is for me to set it null/0 if there are other examples I have to adjust this.
        for (String fieldName : fieldsToRemove) {
            Field field = getField(targetObject.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field: '" + fieldName + "' not found "
                        + "in " + targetObject.getClass().getName());
            }
            if (isSimpleType(field)) {
                try {
                    Object parsedObject = parse("0", field);
                    setSimpleField(targetObject, fieldName, parsedObject);
                } catch (NoSimpleValueParseException e) {
                    // This should never happen (checked before)
                    e.printStackTrace();
                }
            }
            else {
                setSimpleField(targetObject, fieldName, null);
            }
        }
    }
    
    /**
     * Updates the fields with the provided names from the object.
     * 
     * @param fieldsToUpdate The fields which should be updated.
     * @param targetObject The target object where the fields should be updated.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    private void updateListOfFields(final Map<String, Object> fieldsToUpdate,
            final Object targetObject) throws NoSuchFieldException, IllegalAccessException {
        for (Entry<String, Object> fieldEntry : fieldsToUpdate.entrySet()) {
            String fieldName = fieldEntry.getKey();
            Field field = getField(targetObject.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field: '" + fieldName + "' not found "
                        + "in " + targetObject.getClass().getName());
            }
            setSimpleField(targetObject, fieldName, fieldEntry.getValue());
        }
    }

    /**
     * Sets the fields which are not null in the base object in the target object.
     * 
     * @param baseObject The base object with the values to set.
     * @param targetObject The target object where the field should be set.
     * @param exceptions A list of fields which should be ignored.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    private void fillNonNullFields(final Object baseObject, final Object targetObject,
            final String ... exceptions) throws IllegalAccessException, NoSuchFieldException {
        fillNonNullFields(baseObject, targetObject, Arrays.asList(exceptions));
    }
    
    /**
     * Sets the fields which are not null in the base object in the target object.
     * 
     * @param baseObject The base object with the values to set.
     * @param targetObject The target object where the field should be set.
     * @param exceptions A list of fields which should be ignored.
     * @throws IllegalAccessException May throw a IllegalAccessException.
     * @throws NoSuchFieldException May throw a NoSuchFieldException.
     */
    private void fillNonNullFields(final Object baseObject, final Object targetObject,
            final List<String> exceptions) throws IllegalAccessException, NoSuchFieldException {
        List<Field> fields = getNonTransientSimpleFields(baseObject.getClass());
        for (Field field : fields) {
            if (!exceptions.contains(field.getName())) {
                field.setAccessible(true);
                Object value = field.get(baseObject);
                if (value != null) {
                    setSimpleField(targetObject, field.getName(), value);
                }
            }
        }
    }
    
}
