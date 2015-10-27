/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.c2mon.publisher.rda;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.or.ObjectRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.ClientDataTagValueRenderer;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.publisher.core.Gateway;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.exception.RdaException;
import cern.cmw.rda3.common.exception.ServerException;
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

/**
 * This class is based on the SimpleServer that is provided with the RDA package. It creates and registeres a
 * new RDA3 server and is able to publish data tags as RDA3 data. The property name corresponds the name of the data
 * tag.
 *
 * @author Matthias Braeger, Wojtek Buczak (refactoring for RDA3)
 */
@Service
public final class RdaPublisher implements Publisher {

    /** slf4j logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(RdaPublisher.class);

    /**
     * Maps the RDA3 properties to the tag names. For each tag subscription there is exactly one entry registered in
     * this map.
     */
    private static final Map<String, SimpleProperty> properties = new ConcurrentHashMap<String, SimpleProperty>();

    private final Server server;
    private Thread srvThread;

    /** used to render the {@link ClientDataTagValue} objects for log4j */
    private final ObjectRenderer log4jObjectRenderer;

    /**
     * Default constructor
     *
     * @param serverName The device name that is used for creating the RDA3 server
     * @throws RdaException In case of a problem at creation time of the RDA3 server
     */
    @Autowired
    public RdaPublisher(@Value("${c2mon.publisher.rda.server.name}") final String serverName) throws RdaException {
        log4jObjectRenderer = new ClientDataTagValueRenderer();

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
     * @return A reference to the device or null if not found
     */
    private static SimpleProperty findProperty(final String name) {
        return properties.get(name);
    }

    private static class RRCallback implements RequestReplyCallback {

        @Override
        public void get(GetRequest request) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("GET - {}", request.getPropertyName());
            }
            SimpleProperty property = findProperty(request.getPropertyName());

            if (null == property) {
                request.requestFailed(new ServerException("Property '" + request.getPropertyName() + "' not found"));
            } else {
                request.requestCompleted(new AcquiredData(property.get()));
            }
        }

        @Override
        public void set(SetRequest request) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("SET - {}", request.getPropertyName());
            }
            SimpleProperty property = findProperty(request.getPropertyName());

            if (null == property) {
                request.requestFailed(new ServerException("Property '" + request.getPropertyName() + "' not found"));
            } else {

                try {
                    property.set(request);
                } catch (ServerException e) {
                    request.requestFailed(e);
                }

                request.requestCompleted();
            }
        }

    }

    private static class SubCallback implements SubscriptionCallback {

        @Override
        public void subscribe(SubscriptionRequest request) {
            SimpleProperty property = findProperty(request.getPropertyName());

            LOG.debug("subscribe: {}", request.getId());

            SubscriptionCreator creator = request.accept();

            creator.firstUpdate(new AcquiredData(property.get()));

            creator.startPublishing();
        }

        @Override
        public void unsubscribe(Request request) {
            System.out.println("unsubscribed: " + request.getHeader().getId());
        }

        @Override
        public void subscriptionSourceAdded(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceAdded: {}", subscription.getId());

            SimpleProperty property = findProperty(subscription.getPropertyName());
            property.setSubscriptionSource(subscription);
        }

        @Override
        public void subscriptionSourceRemoved(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceRemoved: {}", subscription.getId());
            SimpleProperty property = findProperty(subscription.getPropertyName());
            property.removeSubscriptionSource();
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
        logger.debug(log4jObjectRenderer.doRender(cdt));

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
            throw new IllegalArgumentException("Publication address has wrong format! Expected <device>/<property>");
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
