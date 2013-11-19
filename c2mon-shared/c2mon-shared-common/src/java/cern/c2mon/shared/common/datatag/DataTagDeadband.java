package cern.c2mon.shared.common.datatag;

/**
 * @author J. Stowisek
 * @version $Revision: 1.1 $ ($Date: 2004/11/05 11:15:53 $ - $State: Exp $)
 */

public final class DataTagDeadband {
    /**
     * Constant to be used to disable value-based deadband filtering in a DAQ process.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     */
    public static final short DEADBAND_NONE = 0;

    /**
     * Constant to be used to enable absolute value deadband filtering on the DAQ process level. When absolute value
     * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
     * greater or less than the last known value. Otherwise, the new value will be discarded.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_ABSOLUTE = 1;

    /**
     * Constant to be used to enable relative value deadband filtering on the DAQ process level. When absolute value
     * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
     * per cent (!) greater or less than the last known value. Otherwise, the new value will be discarded.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_RELATIVE = 2;

    /**
     * Constant to be used to enable absolute value deadband filtering on the equipment message handler level. When
     * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
     * least "deadbandValue" greater or less than the last known value. Otherwise, the new value will be discarded. The
     * DAQ process framework will not perform any deadband filtering if this type is set.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_EQUIPMENT_ABSOLUTE = 3;

    /**
     * Constant to be used to enable relative value deadband filtering on the equipment message handler level. When
     * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
     * least "deadbandValue" per cent (!) greater or less than the last known value. Otherwise, the new value will be
     * discarded. The DAQ process framework will not perform any deadband filtering if this type is set.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_EQUIPMENT_RELATIVE = 4;

    /**
     * Constant to be used to enable absolute value deadband filtering on the DAQ process level. As long as value
     * description stays unchanged, it works in exactly the same fashion as DEADBAND_PROCESS_ABSOLUTE_VALUE. If, however
     * value description change is detected, deadband filtering is skipped.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE = 5;

    /**
     * Constant to be used to enable relative value deadband filtering on the DAQ process level. As long as value
     * description stays unchanged, it works in exactly the same fashion as DEADBAND_PROCESS_RELATIVE_VALUE. If, however
     * value description change is detected, deadband filtering is skipped.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE = 6;

    /**
     * @return a String representation of the specified valueDeadbandType
     * @param valueDeadbandType {@link DataTagDeadband}
     */
    public static String toString(final short valueDeadbandType) {

        if (valueDeadbandType == DEADBAND_NONE)
            return "DEADBAND_NONE";
        else if (valueDeadbandType == DEADBAND_PROCESS_ABSOLUTE)
            return "DEADBAND_PROCESS_ABSOLUTE";
        else if (valueDeadbandType == DEADBAND_PROCESS_RELATIVE)
            return "DEADBAND_PROCESS_RELATIVE";
        else if (valueDeadbandType == DEADBAND_EQUIPMENT_ABSOLUTE)
            return "DEADBAND_EQUIPMENT_ABSOLUTE";
        else if (valueDeadbandType == DEADBAND_EQUIPMENT_RELATIVE)
            return "DEADBAND_EQUIPMENT_RELATIVE";
        else if (valueDeadbandType == DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE)
            return "DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE";
        else if (valueDeadbandType == DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE)
            return "DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE";
        else
            return "UNKNOWN";
    }

    /**
     * Check whether a parameter is a valid deadband
     */
    public static final boolean isValidType(final short valueDeadbandType) {
        return valueDeadbandType >= DEADBAND_NONE && valueDeadbandType <= DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE;
    }
}