/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.c2mon.publisher.rda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.exception.ServerException;
import cern.cmw.rda3.server.core.SetRequest;
import cern.cmw.rda3.server.subscription.SubscriptionSource;

/**
 * This class represents a RDA3 property. For each tag which is published via RDA3 an instance of this class is created.
 * It is responsible of notifying all subscribers about value updates.
 *
 * @author Matthias Braeger, Wojtek Buczak (refactoring for RDA3)
 */
final class SimpleProperty implements BaseTagListener {

    /** Log4j logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(SimpleProperty.class);

    /** The current tag value of the device */
    private Data currentValue = null;

    /** a subscription source reference **/
    private SubscriptionSource subscriptionSource;

    /** The RDA property name for which this class has been instantiated for */
    private final String rdaPropertyName;

    /**
     * Default Constructor
     */
    SimpleProperty(final String pRdaPropertyName) {
        this.rdaPropertyName = pRdaPropertyName;
    }

    synchronized Data get() {
        return currentValue;
    }

    /**
     * @param request 
     * @throws ServerException
     */
    synchronized void set(final SetRequest request) throws ServerException {
        throw new ServerException("SET is not supported by the server");
    }

    /**
     * Creates a CMW Data object of the {@link Tag} object
     * 
     * @param cdt The {@link Tag} object that shall be packed as CMW Data
     * @return The representation of the {@link Tag} object
     */
    private static Data pack(final Tag cdt) {
        Data data = DataFactory.createData();

        switch (cdt.getTypeNumeric()) {
        case TYPE_BOOLEAN:
            data.append("value", (Boolean) cdt.getValue());
            break;
        case TYPE_BYTE:
            data.append("value", (Byte) cdt.getValue());
            break;
        case TYPE_DOUBLE:
            data.append("value", (Double) cdt.getValue());
            break;
        case TYPE_FLOAT:
            data.append("value", (Float) cdt.getValue());
            break;
        case TYPE_INTEGER:
            data.append("value", (Integer) cdt.getValue());
            break;
        case TYPE_LONG:
            data.append("value", (Long) cdt.getValue());
            break;
        case TYPE_SHORT:
            data.append("value", (Short) cdt.getValue());
            break;
        case TYPE_STRING:
            data.append("value", (String) cdt.getValue());
            break;
        default:
            LOG.warn("The data value of tag " + cdt.getId() + " is uninitialized");
            return null;
        }

        data.append("id", cdt.getId());
        data.append("valid", cdt.isValid());
        data.append("simulated", cdt.isSimulated());
        data.append("valueDescription", cdt.getValueDescription());
        data.append("description", cdt.getDescription());
        data.append("unit", cdt.getUnit());
        data.append("quality", cdt.getDataTagQuality().toString());
        data.append("qualityDescription", cdt.getDataTagQuality().getDescription());
        data.append("name", cdt.getName());
        data.append("mode", cdt.getMode().toString());
        data.append("timestamp", Long.valueOf(cdt.getServerTimestamp().getTime()).doubleValue());
        data.append("sourceTimestamp", cdt.getTimestamp().getTime());
        return data;
    }

    /**
     * Generates a new {@link Data} object from the received tag update and propagates it to all the listeners.
     */
    @Override
    public synchronized void onUpdate(final Tag cdt) {
        Data newValue = pack(cdt);
        LOG.debug("Value update received for RDA property {} : {}", rdaPropertyName, newValue);

        // check, because there might not be any RDA3 clients subscribed yet
        if (subscriptionSource != null) {
            subscriptionSource.notify(new AcquiredData(newValue));
        }
        currentValue = newValue;
    }

    /**
     * @param subscription
     */
    public synchronized void setSubscriptionSource(SubscriptionSource subscription) {
        this.subscriptionSource = subscription;
    }

    /**
     * @param subscription
     */
    public synchronized void removeSubscriptionSource() {
        this.subscriptionSource = null;
    }
}
