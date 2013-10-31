/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.gm;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.AcquiredParameterValue;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.japc.ValueHeader;
import cern.tim.shared.common.datatag.address.JAPCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * A dedicated JAPC handler for GM devices
 */
public class GmJapcMessageHandler extends GenericJapcMessageHandler {

    @Override
    protected void handleJAPCValue(ISourceDataTag tag, String pParameterName, AcquiredParameterValue pParameterValue) {

        ParameterValue value = pParameterValue.getValue();
        Type type = value.getType();

        ValueHeader header = pParameterValue.getHeader();
        JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): update received for parameter: %s", pParameterName));
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): value of type: %s received", value.getType().toString()));
        }

        if (type != Type.SIMPLE) {
            String errorMsg = String.format("handleJAPCValue() : Type \"%s\" is not supported", type.toString());
            getEquipmentLogger().error("\t" + errorMsg);
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            return;
        }

        SimpleParameterValue svalue = (SimpleParameterValue) value;

        try {

            if (svalue == null) {
                String errMessage = String.format(
                        "The received map does not have field: %s. Please check your configuration", addr
                                .getDataFieldName());
                throw new IndexOutOfBoundsException("Incorrect native address :" + errMessage);
            }

            sendJAPCSValueFromScalar(tag, svalue, null, convertSourceTimestampToMs(header.getAcqStampMillis()));

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
