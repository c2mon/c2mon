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

package cern.c2mon.daq.japc;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.japc.MapParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.ValueType;

/**
 * This is a wrapper class for the MapParameterValue objects. It helps reading the value that is specified by a
 * SourceDataTag and JAPCHardwareAddress object.
 *
 * @author Matthias Braeger
 */
public class MPVReader {

    private final MapParameterValue mpv;
    private final JAPCHardwareAddress addr;
    private final ISourceDataTag tag;

    /**
     * Log4j Logger instance used by this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(MPVReader.class);

    protected static final String DEFAULT_FIELD_NAME = "value";

    /**
     * Default Constructor
     *
     * @param mpv The MapParameterValue object
     * @param addr The JAPCHardwareAddress
     * @param logger The EquipmentLogger for log4j
     */
    public MPVReader(final MapParameterValue mpv, final ISourceDataTag tag) {
        this.mpv = mpv;
        this.addr = (JAPCHardwareAddress) tag.getHardwareAddress();
        this.tag = tag;
    }

    /**
     * Returns the value that has been requested within the JAPCHardwareAddress object.
     *
     * @return The actual value of the related data tag
     * @throws IndexOutOfBoundsException In case of an incorrect native address
     */
    public Object getValue() throws IndexOutOfBoundsException {
        // The SimpleParameterValue representation of the related data Field

        SimpleParameterValue simpleValue = null;

        // if no data-field has been provided, use the default one (named "value")
        if (addr.getDataFieldName() == null || addr.getDataFieldName().length() == 0) {
            simpleValue = mpv.get(DEFAULT_FIELD_NAME);
        } else {
            simpleValue = mpv.get(addr.getDataFieldName());
        }

        if (simpleValue == null) {
            String errMessage = String.format(
                    "The received map does not have field: %s. Please check your configuration", addr
                            .getDataFieldName());
            throw new IndexOutOfBoundsException("Incorrect native address :" + errMessage);
        }

        // the value type of the SimpleParameter Value
        ValueType valueType = simpleValue.getValueType();

        Object value4send = null;

        if (valueType.isArray()) {
            // The array index
            int index = getIndex(addr.getIndexName(), addr.getIndexFieldName());

            if (LOG.isDebugEnabled())
                LOG.debug("enetring getValue()..");

            try {
              value4send = simpleValue.getObject(index);
            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {

                String errMessage = "Could not determine the array index position. Field name index-field-name ["
                        + addr.getIndexName() + "] could not be found in data-field-name array ["
                        + addr.getIndexFieldName() + "] = " + Arrays.toString(mpv.getStrings(addr.getIndexFieldName()));

                if (LOG.isDebugEnabled())
                    LOG.debug("getValue() - tag #" + tag.getId() + ". " + errMessage);

                throw new ArrayIndexOutOfBoundsException(errMessage);
            }

        } else if (valueType.isScalar()) {
          value4send = simpleValue.getObject();
        }

        if (LOG.isDebugEnabled())
            LOG.debug("leaving getValue()");

        return value4send;
    }

    /**
     * Private method which is used to retrieve the array index for the requested value
     *
     * @param name the parameter Name
     * @return the array index of the parameter name
     */
    private int getIndex(final String parameterName, final String fieldName) throws ArrayIndexOutOfBoundsException {
        String[] names = null;
        try {
            names = mpv.getStrings(fieldName);
        } catch (Exception ex) {
            throw new ArrayIndexOutOfBoundsException("field not found");
        }

        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(parameterName)) {
                    return i;
                }
            }
        }

        return -1;
    }
}
