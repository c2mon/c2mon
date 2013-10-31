package cern.c2mon.daq.japc.wie;

/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${2012}, All Rights Reserved.
 */

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.japc.ValueType;
import cern.tim.shared.common.datatag.address.JAPCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * A dedicated JAPC handler for Wiener Fan Traydevices
 */
public class WieJapcMessageHandler extends GenericJapcMessageHandler {

    public static final String DEFAULT_HASHNAMES_FIELD = "names";

    @Override
    protected void handleJAPCValue(ISourceDataTag tag, String pParameterName, AcquiredParameterValue pParameterValue) {

        ParameterValue value = pParameterValue.getValue();

        long timeStamp = System.currentTimeMillis();

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

            SimpleParameterValue svalue = mapValue.get(addr.getDataFieldName());

            ValueType valueType = svalue.getValueType();

            if (valueType.isScalar()) {

                // Simple scalar field

                svalue = mapValue.get(addr.getDataFieldName());

                sendJAPCSValueFromScalar(tag, svalue, null, convertSourceTimestampToMs(timeStamp));

            } else if (valueType.isArray()) {

                int index = addr.getColumnIndex();

                if (addr.getIndexFieldName() != null) {

                    if (mapValue.get(addr.getDataFieldName() + "." + DEFAULT_HASHNAMES_FIELD) == null) {

                        throw new ArrayIndexOutOfBoundsException(addr.getDataFieldName() + "."
                                + DEFAULT_HASHNAMES_FIELD + " is missing");

                    }

                    index = getIndex(mapValue, addr.getDataFieldName() + "." + DEFAULT_HASHNAMES_FIELD,
                            addr.getIndexFieldName());

                }

                sendJAPCSValueFromArray(tag, svalue, valueType, timeStamp, index);
            }

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
