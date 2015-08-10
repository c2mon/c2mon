/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.rdaAlarms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.cmw.rda3.common.data.AcquiredData;
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
 */
@ManagedResource(objectName = "cern.c2mon.publisher.rdaAlarms:name=RdaAlarmsPublisher", description = "Rda publisher for DIAMON alarm client")
public final class RdaAlarmsPublisher implements Runnable, AlarmListener {

    /** Log4j logger instance */
    private static final Logger LOG = LoggerFactory.getLogger(RdaAlarmsPublisher.class);

    /**
     * Maps the RDA properties to the tag names. For each tag subscription there is exactly one entry registered in this
     * map.
     */
    private static final Map<String, RdaAlarmProperty> properties = new HashMap<String, RdaAlarmProperty>();
    private ConcurrentHashMap<String, String> alarmEquip = new ConcurrentHashMap<String, String>();
    // private RemoteModuleFactory alarmProviderFactory;
    // private AlarmDataProvider provider;
    private long rejected;

    private Thread daemonThread;
    private Server server;
    private String serverName;
    private PreparedStatement pstmt;

    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public RdaAlarmsPublisher(String serverName, DataSource ds) {
        this.serverName = serverName;
        String sql = "select source_id from alarm_definition where alarm_id=?";
        try {
            pstmt = ds.getConnection().prepareStatement(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //
    // --- DAEMON -----------------------------------------------------------------------
    //
    /**
     * This method has to be called in order to start the RDA publisher
     */
    public void start() {
        daemonThread = new Thread(this);
        daemonThread.start();
    }

    @Override
    public void run() {
        try {
            LOG.info("Starting RDA device server");
            ServerBuilder builder = ServerBuilder.newInstance();
            builder.setServerName(serverName);
            builder.setRequestReplyCallback(new RRCallback());
            builder.setSubscriptionCallback(new SubCallback());
            server = builder.build();
            LOG.info("Creatied RDA3 server " + serverName);

            // alarmProviderFactory = new RemoteModuleFactory();
            // alarmProviderFactory.init();

            // provider = alarmProviderFactory.getDataProvider();

            C2monServiceGateway.startC2monClient();

            C2monConnectionMonitor mon = new C2monConnectionMonitor();
            C2monServiceGateway.getSupervisionManager().addClientHealthListener(mon);
            C2monServiceGateway.getSupervisionManager().addConnectionListener(mon);
            C2monServiceGateway.getSupervisionManager().addHeartbeatListener(mon);

            while (!C2monServiceGateway.getSupervisionManager().isServerConnectionWorking()) {
                LOG.info("Awaiting connection ...");
                Thread.sleep(1000);
            }

            LOG.info("Connecting alarm listener ...");
            C2monServiceGateway.getTagManager().addAlarmListener(this);
            LOG.info("... ready.");

            int count = 0;
            for (AlarmValue av : C2monServiceGateway.getTagManager().getAllActiveAlarms()) {
                count++;
                this.onAlarmUpdate(av);
            }
            LOG.info("Started with initial selection of " + count + " alarms (" + rejected + " rejected)");

            server.start();
            LOG.info("Server now on");
        } catch (Exception e) {
            LOG.error("A major problem occured while running the RDA server. Stopping publisher!", e);
            System.exit(1);
        }
    }

    public void join() throws InterruptedException {
        this.daemonThread.join();
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (Exception e) {
                LOG.warn("Failed to close preparedStatement", e);
            }            
        }
    }

    public void shutdown() {
        LOG.debug("Stopping the C2MON client...");
        try {
            C2monServiceGateway.getTagManager().removeAlarmListener(this);
        } catch (JMSException e) {
            LOG.warn("?", e);
        }
        LOG.info("C2MON client stopped.");
        server.shutdown();
        try {
            daemonThread.join();
        } catch (InterruptedException e) {
            LOG.warn("InterruptedException caught", e);
        }
    }

    //
    // --- PUBLIC METHODS -------------------------------------------------------------------------
    //
    class RRCallback implements RequestReplyCallback {

        @Override
        public void get(GetRequest request) {
            LOG.trace("GET - " + request.getPropertyName());
            RdaAlarmProperty property = findProperty(request.getPropertyName());

            if (null == property) {
                LOG.warn("Property {} unknown.", request.getPropertyName());
                request.requestFailed(new ServerException("Property '" + request.getPropertyName() + "' not found"));
            } else {
                LOG.debug("Request completed for property {}", request.getPropertyName());
                request.requestCompleted(new AcquiredData(property.get()));
            }
        }

        @Override
        public void set(SetRequest request) {

            LOG.debug("SET - " + request.getPropertyName());
            RdaAlarmProperty property = findProperty(request.getPropertyName());

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

    private class SubCallback implements SubscriptionCallback {

        @Override
        public void subscribe(SubscriptionRequest request) {
            RdaAlarmProperty property = findProperty(request.getPropertyName());
            LOG.debug("subscribe: {}", request.getId());
            SubscriptionCreator creator = request.accept();
            creator.firstUpdate(new AcquiredData(property.get()));
            creator.startPublishing();
        }

        @Override
        public void unsubscribe(Request request) {
            LOG.info("unsubscribed: " + request.getHeader().getId());
        }

        @Override
        public void subscriptionSourceAdded(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceAdded: {}", subscription.getId());
            RdaAlarmProperty property = findProperty(subscription.getPropertyName());
            property.setSubscriptionSource(subscription);
        }

        @Override
        public void subscriptionSourceRemoved(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceRemoved: {}", subscription.getId());
            RdaAlarmProperty property = findProperty(subscription.getPropertyName());
            property.removeSubscriptionSource();
        }
    }

    /**
     * Updates the corresponding {@link RdaAlarmProperty} instance about the value update. In case of a new (yet)
     * unknown tag a new {@link RdaAlarmProperty} instance is first of all created.
     */
    @Override
    public void onAlarmUpdate(AlarmValue av) {
        // Saves the received value into a separate file
        Logger logger = LoggerFactory.getLogger("ClientDataTagLogger");
        logger.debug(av.toString());

        String alarmId = av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
        LOG.info(" RECEIVED    > " + alarmId + " is active:" + av.isActive());
        ClientDataTagValue cdt = C2monServiceGateway.getTagManager().getDataTag(av.getTagId());
        if (!cdt.getDataTagQuality().isValid()) {
            LOG.warn(" INVALIDATION > " + alarmId + " is active:" + av.isActive());
        }

        String source = alarmEquip.get(alarmId);
        if (source == null) {
            try {
                // Alarm alarm = provider.getAlarmDefinition(alarmId);
                // if (alarm != null) {
                // source = alarm.getSourceName();
                source = getSource(alarmId);
                alarmEquip.put(alarmId, source);
                // }

            } catch (Exception e) {
                LOG.warn(alarmId + " not found by data provider, ignored. (" + e.getMessage() + ")");
                rejected++;
            }
        }
        if (source != null) {
            if (!properties.containsKey(source)) {
                LOG.info("Adding for tag " + alarmId + " new RDA publication property: " + source);
                RdaAlarmProperty property = new RdaAlarmProperty(source);
                properties.put(source, property);
            }
            try {
                properties.get(source).onUpdate(av);
            } catch (Exception e) {
                logger.error("Failed to publish " + alarmId, e);
            }
        } else {
            LOG.warn("Alarm " + alarmId + " discarded, could not find a source for it");
        }
        LOG.info(" PROCESSED    > " + alarmId);

    }

    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    /**
     * Private method to find a property by its property name
     * 
     * @param name The name of the device which corresponds to the data tag name
     * @return A reference to the device or null if not found
     */
    private static RdaAlarmProperty findProperty(final String name) {
        return properties.get(name);
    }


    private String getSource(String alarmId) throws Exception {
        LOG.info("Query source name for " + alarmId + " ...");
        String sourceId = "?";
        pstmt.setString(1,  alarmId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                sourceId = rs.getString(1);
            }
        } 
        LOG.info("Source name for " + alarmId + " -> " + sourceId);
        return sourceId;
    }
}
