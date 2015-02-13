package cern.c2mon.daq.japc.bis;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.ParameterException;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.japc.ValueType;

/**
 * A dedicated JAPC handler for BIS
 */
public class BisJapcMessageHandler extends GenericJapcMessageHandler {

    public static final String NAMES_ARRAY_FIELD = "registerNames";
    public static final String VALUES_ARRAY_FIELD = "registerValues";


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
            if (valuesField == null) {
                throw new ParameterException("field: "+VALUES_ARRAY_FIELD + " not found");
            }

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