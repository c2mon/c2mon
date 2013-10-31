/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.laserproxy;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.tim.shared.common.datatag.address.JAPCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

public class LaserProxyJapcMessageHandler extends GenericJapcMessageHandler {

    public static final String NAMES_ARRAY = "names";
    public static final String PROPERTIES_ARRAY = "properties";
    public static final String TIMESTAMPS_ARRAY = "timestamps";

    @Override
    protected void handleJAPCValue(ISourceDataTag tag, String pParameterName, AcquiredParameterValue pParameterValue) {

        ParameterValue value = pParameterValue.getValue();
        Type type = value.getType();

        JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): update received for parameter: %s", pParameterName));
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): value of type: %s received", value.getType().toString()));
        }

        if (type != Type.MAP) {
            String errorMsg = String.format("handleJAPCValue() : Type \"%s\" is not supported", type.toString());
            getEquipmentLogger().error("\t" + errorMsg);
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            return;
        }

        MapParameterValue mapValue = (MapParameterValue) value;

        try {

            // get the simple value for the map
            SimpleParameterValue svalue = mapValue.get(addr.getDataFieldName());

            // if field is not available - skip it
            if (svalue == null)
                return;

            int index = getIndex(mapValue, NAMES_ARRAY, addr.getDataFieldName());

            // get the properties array
            SimpleParameterValue properties = mapValue.get(PROPERTIES_ARRAY);

            String valueDescription = "";
            try {
                if (properties.getValueType().isArray()) {
                    valueDescription = properties.getString(index);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // do not do anything, but logging the problem - that solves the problem with the
                // maps sending arrays with flexible lengths
                getEquipmentLogger().warn("the searched element wasn't found in the array", ex);
            }

            // get the timestamps array
            SimpleParameterValue timestamps = mapValue.get(TIMESTAMPS_ARRAY);

            long timestamp = 0L;
            try {
                if (timestamps.getValueType().isArray()) {
                    timestamp = timestamps.getLong(index);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // do not do anything, but logging the problem - that solves the problem with the
                // maps sending arrays with flexible lengths
                getEquipmentLogger().warn("the searched element wasn't found in the array", ex);
            }

            sendJAPCSValueFromScalar(tag, svalue, valueDescription, convertSourceTimestampToMs(timestamp));

        } catch (Exception e) {
            getEquipmentLogger().warn(
                    "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                            + tag.getName() + " id : " + tag.getId() + " Problem: " + e.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, e.getMessage());
        }

    }

    @Override
    protected boolean isSelectorOnChangeEnabled() {
        return false;
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Implement this method.
    }

}
