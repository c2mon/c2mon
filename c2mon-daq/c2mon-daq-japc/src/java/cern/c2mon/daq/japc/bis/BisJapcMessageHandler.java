package cern.c2mon.daq.japc.bis;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.japc.ValueType;

/**
 * A dedicated JAPC handler for BIS
 */
public class BisJapcMessageHandler extends GenericJapcMessageHandler {

    public static final String NAMES_ARRAY_FIELD = "namesArray";
    public static final String VALUES_ARRAY_FIELD = "valuesArray";

    public static final int INDEX_UNDEFINED = -1;

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
            String errorMsg = String.format("Type \"%s\" is not supported. Expected MAP!", type.toString());
            getEquipmentLogger().error("\t" + errorMsg);
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            return;
        }

        MapParameterValue mapValue = (MapParameterValue) value;

        try {
           
            SimpleParameterValue valuesField = mapValue.get(VALUES_ARRAY_FIELD);

            ValueType valuesFieldType = valuesField.getValueType();

            int index = getIndex(mapValue, NAMES_ARRAY_FIELD, addr.getDataFieldName());
            sendJAPCSValueFromArray(tag, valuesField, valuesFieldType, timeStamp, index);

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