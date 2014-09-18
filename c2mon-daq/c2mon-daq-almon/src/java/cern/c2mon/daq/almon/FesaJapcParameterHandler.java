/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import java.util.Properties;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.plsline.PlsLineResolver;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Type;
import cern.japc.ValueConversionException;

/**
 * The <code>FesaJapcParameterHandler</code> is a parameter handler implementation for FESA
 * 
 * @author wbuczak
 */
public class FesaJapcParameterHandler extends JapcParameterHandler {

    public static final String NAMES_ARRAY_FIELD = "names";
    public static final String CYCLE_NAME_FIELD = "cycleName";
    public static final String PREFIXES_ARRAY_FIELD = "prefixes";
    public static final String SUFFIXES_ARRAY_FIELD = "suffixes";
    public static final String TIMESTAMPS_ARRAY_FIELD = "timestamps";

    public static final String FESA_CYCLE_NAME_USER_PROPERTY = "fesa.cycleName";
    public static final String FESA_PLS_LINE_USER_PROPERTY = "fesa.plsLine";

    private PlsLineResolver plsLineResolver;
    private Properties userProperties;

    /**
     * @param amParameter
     * @param amSender
     */
    protected FesaJapcParameterHandler(ISourceDataTag tag, AlmonHardwareAddress hwAddress, IEquipmentMessageSender ems,
            AlmonSender amSender, PlsLineResolver plsLineResolver) {
        super(tag, hwAddress, ems, amSender);
        this.plsLineResolver = plsLineResolver;
    }

    /**
     * When we receive an update we have to activate all faults in the names array and terminate all faults which were
     * active but are not in the names array.
     */
    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue value) {
        LOG.debug("parameter: {} received value: {}", parameterName, value.getValue().getString());

        // if access fault was activated - terminate it
        super.terminateDeviceAccessFault();

        if (!(value.getValue().getType() == Type.MAP)) {
            LOG.warn("MAP parameter value expected for parameter {}", parameterName);
            return;
        }

        MapParameterValue mvalue = (MapParameterValue) value.getValue();

        String[] names = null;
        String[] prefixes = null;
        String[] suffixes = null;
        long[] timestamps = null;

        try {
            names = mvalue.getStrings(NAMES_ARRAY_FIELD);
            prefixes = mvalue.getStrings(PREFIXES_ARRAY_FIELD);
            suffixes = mvalue.getStrings(SUFFIXES_ARRAY_FIELD);
            timestamps = mvalue.getLongs(TIMESTAMPS_ARRAY_FIELD);
        } catch (ValueConversionException ex) {
            LOG.error("Expected/mandatory field not found!", ex);
            return;
        }

        String cycleName = null;
        try {
            cycleName = mvalue.getString(CYCLE_NAME_FIELD);
        } catch (ValueConversionException ex) {
            // nothing to do,cycle name may not be present
        }

        int plsLine = PlsLineResolver.PLS_LINE_UNDEFINED;
        if (!(null == cycleName) && !cycleName.isEmpty()) {
            // try to extract the pls-line
            plsLine = plsLineResolver.resolve(cycleName);
        }// if !cycleName.isEmpty()

        // iterate through the names array
        boolean keepActive = false;
        for (int i = 0; i < names.length; i++) {

            String field = names[i];

            // check that field for the alarm-to-be-activated is present
            if (field.equals(address.getField())) {

                // fill in the user properties
                UserProperties props = new UserProperties();

                if (prefixes != null && i < prefixes.length && prefixes[i].length() != 0)
                    props.setProperty(ASI_PREFIX_PROPERTY, prefixes[i]);
                if (suffixes != null && i < suffixes.length && suffixes[i].length() != 0)
                    props.setProperty(ASI_SUFFIX_PROPERTY, suffixes[i]);

                if (!(null == cycleName) && !cycleName.isEmpty()) {
                    // LSR-1466: Store cycle name for alarms collected from FESA devices
                    props.setProperty(FESA_CYCLE_NAME_USER_PROPERTY, cycleName);
                    if (plsLine > PlsLineResolver.PLS_LINE_UNDEFINED) {
                        props.setProperty(FESA_PLS_LINE_USER_PROPERTY, Integer.toString(plsLine));
                    }
                }

                if (!inFault) {
                    this.amSender.activate(tag, ems, address.getAlarmTripplet(), timestamps[i] / 1000000, props);
                    this.inFault = true;
                    // this.activeAlarmTripplet = address.getAlarmTripplet();
                    keepActive = true;

                } else { // we are already in fault - check if update is needed
                    if (!userProperties.equals(props)) {
                        // activateAlarm = true;
                        this.amSender.update(tag, ems, address.getAlarmTripplet(), timestamps[i] / 1000000, props);
                    }

                    keepActive = true;
                }

                // remember user properties of the active alarm
                userProperties = props;

                break; // match was found - no need to process further, exit the loop
            } // if

        }// for

        // if we are in fault, and the field was not present
        if (inFault && !keepActive) {
            this.amSender.terminate(tag, ems, address.getAlarmTripplet(), System.currentTimeMillis());
            this.inFault = true;
            // this.activeAlarmTripplet = null;
        }
    }
}