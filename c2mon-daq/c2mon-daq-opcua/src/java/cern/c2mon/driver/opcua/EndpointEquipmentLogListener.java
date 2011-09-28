package cern.c2mon.driver.opcua;

import cern.c2mon.driver.opcua.connection.common.IOPCEndpointListener;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.tools.TIMDriverSimpleTypeConverter;
import cern.tim.shared.common.datatag.address.OPCHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * Listener for endpoint events. Makes sure all important events are logged.
 * 
 * @author Andreas Lang
 * 
 */
public class EndpointEquipmentLogListener implements IOPCEndpointListener {
    /**
     * The equipment logger used to log the events.
     */
    private final EquipmentLogger logger;

    /**
     * Creates a new endpoint log listener.
     * 
     * @param logger
     *            The logger to use to log the events.
     */
    public EndpointEquipmentLogListener(final EquipmentLogger logger) {
        this.logger = logger;
    }

    /**
     * Logs an onNewTagValue event. The log level is debug.
     * 
     * @param dataTag
     *            The tag which has a value update.
     * @param timestamp
     *            The timestamp when the tag changed.
     * @param tagValue
     *            The updated value of the tag.
     */
    @Override
    public void onNewTagValue(final ISourceDataTag dataTag, final long timestamp, final Object tagValue) {
        Object convertedValue;
        if (tagValue == null)
            convertedValue = tagValue;
        else if (tagValue instanceof Number)
            convertedValue = TIMDriverSimpleTypeConverter.convert(
                        dataTag, Double.valueOf(tagValue.toString()));
        else
            convertedValue = TIMDriverSimpleTypeConverter.convert(
                    dataTag, tagValue.toString());
        if (logger.isDebugEnabled()) {
            logger.debug("Original value: '" + (tagValue != null ? tagValue.toString() : "null") + "', Tag type: '" + dataTag.getDataType() + "', Original type: '"
                    + (tagValue != null ? tagValue.getClass().getSimpleName() : "null") + "'");
            logger.debug("New tag value (ID: '" + dataTag.getId() + "'," + " converted value: '" + convertedValue + "', converted type: '"
                    + (convertedValue != null ? convertedValue.getClass().getSimpleName() : "null") + "', Timestamp: '" + timestamp + "').");
        }
    }

    /**
     * Logs an error subscription exception.
     * 
     * @param cause
     *            The cause of the subscription failing.
     */
    @Override
    public void onSubscriptionException(final Throwable cause) {
        logger.error("Exception in OPC subscription.", cause);
    }

    /**
     * Logs a warning for an invalid tag.
     * 
     * @param dataTag
     *            The invalid tag.
     * @param cause
     *            The cause of the tag to be invalid.
     */
    @Override
    public void onTagInvalidException(final ISourceDataTag dataTag, final Throwable cause) {
        logger.warn("Tag with id '" + dataTag.getId() + "' caused exception. " + "Check configuration. Address: " 
        		+ ((OPCHardwareAddress)dataTag.getHardwareAddress()).getOPCItemName(), cause);
    }

}
