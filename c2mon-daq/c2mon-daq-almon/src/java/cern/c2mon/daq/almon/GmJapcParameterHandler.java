/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.japc.ValueType;

/**
 * The <code>FesaJapcParameterHandler</code> is a parameter handler implementation for GM alarms
 *
 * @author wbuczak
 */
public class GmJapcParameterHandler extends JapcParameterHandler {

    private static final int GM_NO_ALARM = 0;
    private static final int FAULT_CODE_MASK = 0xffffff;

    private int currentFault = GM_NO_ALARM;

    public GmJapcParameterHandler(ISourceDataTag tag, AlmonHardwareAddress hwAddress, IEquipmentMessageSender ems,
            AlmonSender alarmsSender) {
        super(tag, hwAddress, ems, alarmsSender);
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        LOG.trace("parameter: {} received update value: {}", parameterName, value.getValue().getString());

        // if access fault was activated - terminate it
        super.terminateDeviceAccessFault();

        // If we received data, look at the value. The action we take will depend on our state:
        // - !inFault and value == GM_NO_ALARM , ignore
        // if value != GM_NO_ALARM publish a activate
        // - inFault -> if value != GM_NO_ALARM publish a terminate and activate
        // wit the new alarm
        // if value == GM_NO_ALARM publish terminate

        int fault = 0;

        if (value.getValue().getType() == Type.SIMPLE) {
            SimpleParameterValue svalue = (SimpleParameterValue) value.getValue();
            ValueType valueType = svalue.getValueType();
            if (valueType.isScalar()) {
                fault = svalue.getInt();
            }
        } else if (value.getValue().getType() == Type.MAP) {
            MapParameterValue mvalue = (MapParameterValue) value.getValue();
            ValueType valueType = mvalue.getValueType(address.getField());
            if (valueType.isScalar()) {
                fault = mvalue.getInt(address.getField());
            }
        }

        // ignore alarm repetitions
        if ((!this.inFault && fault == GM_NO_ALARM) || (this.inFault && fault == this.currentFault)) {
            // with periodic sub we will get empty alarms - skip warnings
            LOG.trace("parameter: {} received repetition of alarm value == {}", parameterName, fault);
            return;
        }

        long timestamp = System.currentTimeMillis();

        // If we are in fault, terminate the previous alarm
        if (this.inFault) {
            LOG.debug("parameter: {} received fault = false while inFault = true  : terminating", parameterName);

            this.inFault = false;
            this.currentFault = GM_NO_ALARM;
            this.amSender.terminate(tag, ems, address.getAlarmTriplet(), timestamp);
        } // if inFault

        // if we arrive here we have to activate a new alarm (if it is related to the current tag)
        if (fault != GM_NO_ALARM && (fault & FAULT_CODE_MASK) == address.getAlarmTriplet().getFaultCode()) {
            LOG.debug("parameter: {} received fault = true : activating", parameterName);

            this.inFault = true;
            this.currentFault = fault;

            this.amSender.activate(tag, ems, address.getAlarmTriplet(), timestamp, new UserProperties());
        }
    }

}