/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.c2mon.publisher.rda;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.publisher.core.Gateway;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.exception.RdaException;
import cern.cmw.rda3.common.request.Request;
import cern.cmw.rda3.server.core.GetRequest;
import cern.cmw.rda3.server.core.RequestReplyCallback;
import cern.cmw.rda3.server.core.Server;
import cern.cmw.rda3.server.core.SetRequest;
import cern.cmw.rda3.server.service.ServerBuilder;
import cern.cmw.rda3.server.subscription.SubscriptionCallback;
import cern.cmw.rda3.server.subscription.SubscriptionCreator;
import cern.cmw.rda3.server.subscription.SubscriptionRequest;
import cern.cmw.rda3.server.subscription.SubscriptionSource;

/*
 * import cern.cmw.BadParameter; import cern.cmw.Data; import cern.cmw.IOError; import cern.cmw.InternalException;
 * import cern.cmw.rda.demo.SimpleServer; import cern.cmw.rda.server.DeviceServerBase; import
 * cern.cmw.rda.server.IOPoint; import cern.cmw.rda.server.ValueChangeListener;
 */

/**
 * This class is based on the {@link SimpleServer} that is provided with the RDA package. It creates and registeres a
 * new RDA server and is able to publish data tags as RDA data. The property name corresponds the name of the data tag.
 *
 * @author Matthias Braeger
 */
@Service
public final class RdaPublisher implements Publisher {

    /** slf4j logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(RdaPublisher.class);

    /**
     * Maps the RDA3 properties to the tag names. For each tag subscription there is exactly one entry registered in
     * this map.
     */
    private final Map<String, SimpleProperty> properties = new HashMap<String, SimpleProperty>();

    private final Server server;
    private Thread srvThread;

    /**
     * Default constructor
     * 
     * @param serverName The device name that is used for creating the RDA3 server
     * @throws RdaException In case of a problem at creation time of the RDA3 server
     */
    @Autowired
    public RdaPublisher(@Value("${c2mon.publisher.rda.server.name}") final String serverName) throws RdaException {

        ServerBuilder builder = ServerBuilder.newInstance();
        builder.setServerName(serverName);
        builder.setRequestReplyCallback(new RRCallback());
        builder.setSubscriptionCallback(new SubCallback());

        LOG.info("Creating RDA3 server {}", serverName);
        server = builder.build();
    }

    /**
     * This method has to be called in order to start the RDA3 publisher
     */
    @PostConstruct
    public void start() {
        srvThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("Starting RDA3 server");
                    server.start();
                } catch (Exception e) {
                    LOG.error("A major problem occured while running the RDA server. Stopping publisher!", e);
                    System.exit(1);
                }
            }
        });
        srvThread.start();
    }

    /**
     * Private method to find a property by its property name
     * 
     * @param name The name of the device which corresponds to the data tag name
     * @return A reference to the device
     * @throws BadParameter In case the device is not known.
     */
    private SimpleProperty findProperty(final String name) /* throws BadParameter */{
        SimpleProperty property = properties.get(name);
        if (property == null) {
            // throw new BadParameter("Property '" + name + "' not found");
        }
        return property;
    }

    private static class RRCallback implements RequestReplyCallback {

        @Override
        public void get(GetRequest request) {
            Data data = DataFactory.createData();
            data.append("value", "Get from RDA3 Java server");
            request.requestCompleted(new AcquiredData(data));
        }

        // @Override
        // public Data get(final IOPoint iop, final Data context) throws BadParameter, IOError {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("GET - " + iop.getDeviceName() + "/" + iop.getPropertyName());
        // }
        // SimpleProperty property = findProperty(iop.getPropertyName());
        // return property.get(iop, context);
        // }

        @Override
        public void set(SetRequest request) {
            System.out.println("New SET: " + request.getAPName());
            System.out.println("New SET context: " + request.getContext());
            System.out.println("New SET data: " + request.getData());

            request.requestCompleted();
        }

        // @Override
        // public void set(final IOPoint iop, final Data value, final Data context) throws BadParameter, IOError {
        // if (LOG.isDebugEnabled()) {
        // LOG.debug("SET - " + iop.getDeviceName() + "/" + iop.getPropertyName());
        // }
        // SimpleProperty property = findProperty(iop.getPropertyName());
        // property.set(iop, value, context);
        // }

    }

    private static class SubCallback implements SubscriptionCallback {
        @Override
        public void subscribe(SubscriptionRequest request) {
            System.out.println("subscribe: " + request.getId());

            SubscriptionCreator creator = request.accept();

            Data data = DataFactory.createData();
            data.append("field", "First update from RDA3 Java server");
            creator.firstUpdate(new AcquiredData(data));

            creator.startPublishing();
        }

        // @Override
        // public void monitorOn(final IOPoint iop, final ValueChangeListener listener) throws BadParameter {
        // SimpleProperty property = findProperty(iop.getPropertyName());
        // property.monitorOn(iop, listener);
        // }

        @Override
        public void unsubscribe(Request request) {
            System.out.println("unsubscribed: " + request.getHeader().getId());
        }

        // @Override
        // public void monitorOff(final IOPoint iop, final ValueChangeListener listener) {
        // try {
        // SimpleProperty property = findProperty(iop.getPropertyName());
        // property.monitorOff(iop, listener);
        // }
        // catch (Exception ex) {
        // ex.printStackTrace();
        // }
        // }

        @Override
        public void subscriptionSourceAdded(SubscriptionSource subscription) {
            System.out.println("subscriptionSourceAdded: " + subscription.getId());
        }

        @Override
        public void subscriptionSourceRemoved(SubscriptionSource subscription) {
            System.out.println("subscriptionSourceRemoved: " + subscription.getId());
        }
    }

    /**
     * Updates the corresponding {@link SimpleProperty} instance about the value update. In case of a new (yet) unknown
     * tag a new {@link SimpleProperty} instance is first of all created.
     * 
     * @param cdt An new tag update received by the {@link Gateway}
     * @param cdtConfig The tag configuration which is belonging to this tag update
     */
    @Override
    public void onUpdate(final ClientDataTagValue cdt, final TagConfig cdtConfig) {
        // Saves the received value into a separate file
        Logger logger = LoggerFactory.getLogger("ClientDataTagLogger");
        logger.debug(cdt.toString());

        if (cdt.getDataTagQuality().isExistingTag() && cdtConfig != null) {
            try {
                String propertyName = getRdaProperty(cdtConfig.getJapcPublication());
                if (!properties.containsKey(propertyName)) {
                    LOG.info("Adding for tag " + cdt.getId() + " new RDA publication property: " + propertyName);
                    SimpleProperty property = new SimpleProperty(propertyName);
                    properties.put(propertyName, property);
                }

                properties.get(propertyName).onUpdate(cdt);
            } catch (IllegalArgumentException iae) {
                LOG.warn("Error while parsing JAPC address for updating tag " + cdt.getId()
                        + " ==> No RDA property update possible! Reason: " + iae.getMessage());
            }
        } else {
            LOG.warn("Got value update for not existing tag " + cdt.getId() + " ==> No RDA property update possible!");
        }
    }

    /**
     * Internal helper method for parsing the japc publication string and
     * 
     * @param japcPublication the japc publication address as it is defined by the
     *            {@link TagConfig#getJapcPublication()} method
     * @return The property name without the leading device name
     * @throws IllegalArgumentException In case of an empty or badly defined publication address.
     */
    private static String getRdaProperty(final String japcPublication) throws IllegalArgumentException {
        if (japcPublication == null || japcPublication.equalsIgnoreCase("") || !japcPublication.contains("/")
                || japcPublication.split("/").length != 2) {
            throw new IllegalArgumentException(
                    "JAPC publication address has wrong format! Expected <device>/<property>");
        }

        return japcPublication.split("/")[1];
    }

    @Override
    public void shutdown() {
        server.shutdown();
        try {
            srvThread.join();
        } catch (InterruptedException e) {
            LOG.warn("InterruptedException caught", e);
        }
    }
}
