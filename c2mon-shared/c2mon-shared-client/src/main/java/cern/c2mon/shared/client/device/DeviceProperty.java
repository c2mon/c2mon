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
package cern.c2mon.shared.client.device;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;
import org.simpleframework.xml.ElementList;

/**
 * Simple XML mapper bean representing a device property. Used when serializing and deserialising device properties
 * during configuration.
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
public class DeviceProperty extends DeviceElement {

    private static final long serialVersionUID = 889764022662498560L;
    /**
     * The list of nested fields of this property.
     */
    @ElementList(required = false, name = "PropertyFields")
    private List<DeviceProperty> fields = new ArrayList<>();


    /**
     * Creates a device property with the category "tagId". The result type of device property defaults to String.
     *
     * @param id    the unique ID of the parent command.
     * @param name  the unique name of the parent property.
     * @param value the ID of the Tag references by this device property
     * @return the newly created device property of category "tagId".
     */
    public static DeviceProperty forTagId(Long id, String name, Long value) {
        return new DeviceProperty(id, name, String.valueOf(value), Category.TAG_ID, null);
    }

    /**
     * Create a new device property with the category "constantValue" of type "String".
     *
     * @param id    unique ID of the parent property. Can be null during configuration requests, in which case the server
     *              will assign the appropriate ID by name.
     * @param name  the name of the parent property.
     * @param value the real constant value string.
     * @return a new device property of category "constantValue".
     */
    public static DeviceProperty forConstantValue(Long id, String name, String value) {
        return new DeviceProperty(id, name, value, Category.CONSTANT_VALUE, null);
    }

    /**
     * Create a new device property with the category "constantValue".
     *
     * @param id         unique ID of the parent property. Can be null during configuration requests, in which case the server
     *                   will assign the appropriate ID by name.
     * @param name       the name of the parent property.
     * @param value      the real constant value
     * @param resultType the result type of the constant value
     * @param <T>        the java class corresponding to the result type.
     * @return a new device property of category "constantValue".
     */
    public static <T> DeviceProperty forConstantValue(Long id, String name, T value, ResultType resultType) {
        return new DeviceProperty(id, name, String.valueOf(value), Category.CONSTANT_VALUE, resultType);
    }

    /**
     * Create a new device property with the category "clientRule".
     *
     * @param id         unique ID of the parent property. Can be null during configuration requests, in which case the server
     *                   will assign the appropriate ID by name.
     * @param name       the name of the parent property.
     * @param value      the real value of the client rule
     * @param resultType the result type of the client rule
     * @return a new device property of category "clientRule".
     */
    public static DeviceProperty forClientRule(Long id, String name, String value, ResultType resultType) {
        return new DeviceProperty(id, name, value, Category.CLIENT_RULE, resultType);
    }

    /**
     * Create a new device property with the category "mappedProperty" and the default result type 'String'.
     *
     * @param id     unique ID of the parent property. Can be null during configuration requests, in which case the server
     *               will assign the appropriate ID by name.
     * @param name   the name of the parent property.
     * @param fields An optional single-depth collections of nested properties which will be added as fields to the
     *               mapped property.
     * @return a new device property of category "mappedProperty".
     */
    public static DeviceProperty forMappedProperty(Long id, String name, List<DeviceProperty> fields) {
        return new DeviceProperty(id, name, fields);
    }

    private DeviceProperty(final Long id, final String name, final String value, final String category, final ResultType resultType) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.category = category;
        if (resultType != null) {
            this.resultType = resultType;
        }
    }

    private DeviceProperty(final Long id, final String name, final List<DeviceProperty> fields) {
        this(id, name, null, Category.MAPPED_PROPERTY, null);
        if (fields != null) {
            this.fields = fields;
        }
    }

    /**
     * Attempts to convert the string representation of the result type into a
     * class object of the corresponding type.
     *
     * @return the class of the result type
     */
    public Class<?> getResultTypeClass() throws ClassNotFoundException {
        // TODO: remove exception
        return resultType.resultClass;
    }

    /**
     * Retrieve the raw property fields of this device property (if they exist).
     *
     * @return the property fields if they exist
     */
    public Map<String, DeviceProperty> getFields() {
        if (this.fields == null) {
            return null;
        }

        Map<String, DeviceProperty> fieldMap = new ConcurrentHashMap<>();

        for (DeviceProperty field : this.fields) {
            fieldMap.put(field.getName(), field);
        }

        return fieldMap;
    }

    /**
     * Retrieve the raw fields of this property
     *
     * @return the property fields
     */
    public List<DeviceProperty> getFieldList() {
        return fields;
    }

    /**
     * Set a field of this property.
     *
     * @param field the field to set
     */
    public void setFields(DeviceProperty field) {
        if (field != null && (field.getName() != null || field.getId() != null)) {
            this.fields.add(field);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceProperty)) {
            return false;
        }
        DeviceProperty that = (DeviceProperty) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getValue(), that.getValue()) &&
                Objects.equals(getCategory(), that.getCategory()) &&
                Objects.equals(getResultType(), that.getResultType()) &&
                Objects.equals(getFields(), that.getFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getValue(), getCategory(), getResultType(), getFields());
    }

}
