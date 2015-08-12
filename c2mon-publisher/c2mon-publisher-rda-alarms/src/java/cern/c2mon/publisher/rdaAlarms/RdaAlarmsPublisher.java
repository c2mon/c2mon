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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.cmw.data.Data;
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

    static final Logger LOG = LoggerFactory.getLogger(RdaAlarmsPublisher.class);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY HH:MM:SS");
    
    private SourceManager sm;
    private VCM received;
    private VCM processed;
    private long rejected;
    private int sourceCount;

    private Thread daemonThread;
    private Server server;
    private String serverName;
    private boolean cont = true;
    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public RdaAlarmsPublisher(String serverName, SourceManager sm) {
        this.serverName = serverName;
        this.sm = sm;
    }

    public void setReceived(VCM received) {
        this.received = received;
    }
    
    public void setProcessed(VCM processed) {
        this.processed = processed;
    }
    
    
    //
    // --- JMX -------------------------------------------------------------------------
    //
    @ManagedAttribute
    public String getServerName() {
        return this.serverName;
    }

    @ManagedAttribute
    public long getRejected() {
        return this.rejected;
    }

    @ManagedAttribute
    public int getSourceCount()
    {
        return this.sourceCount;
    }

    @ManagedAttribute
    public String getStartTime()
    {
        return df.format(new Date(server.getServerInfo().getStartTime()));
    }
    
    @ManagedAttribute
    public int getSubscribedProps()
    {
        return server.getSubscriptionLookup().getSubscriptions().size();
    }

    @ManagedAttribute
    public Map<String,String> getSubscribers()
    {
        HashMap<String, String> res = new HashMap<String, String>();
        for (SubscriptionSource subs : server.getSubscriptionLookup().getSubscriptions()) {
            for (String subscriber : subs.getSubscriptions()) {
                res.put(subscriber, subs.getAPName());
            }
        }
        return res;
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
            LOG.info("Created RDA3 server {}, connecting now to C2MON ..." + serverName);

            C2monServiceGateway.startC2monClient();
            C2monConnectionMonitor.start();
            while (!C2monServiceGateway.getSupervisionManager().isServerConnectionWorking() && cont) {
                LOG.info("Awaiting connection ...");
                Thread.sleep(1000);
            }
            LOG.info("Connecting alarm listener ...");
            
            C2monServiceGateway.getTagManager().addAlarmListener(this);
            Collection<AlarmValue> activeAlarms = C2monServiceGateway.getTagManager().getAllActiveAlarms();
            LOG.info("... now listening to incoming alarms.");
            
            sm.initialize(activeAlarms);    // load all sources declared at startup
            for (AlarmValue av : activeAlarms) {
                this.onAlarmUpdate(av);
            }
            LOG.info("Started with initial selection of " + activeAlarms.size() + " alarms.");
            //
            
            // everything ready, start the RDA server for publishung
            server.start();
            LOG.info("Server now on");
        } catch (Exception e) {
            LOG.error("A major problem occured while running the RDA server. Stopping publisher!", e);
        }
    }

    public void join() throws InterruptedException {
        this.daemonThread.join();
        sm.close();
    }

    public void shutdown() {
        cont = false;
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

            if (request.getPropertyName().equals("_SOURCES")) {
                request.requestCompleted(sm.getSources());
                LOG.debug("Request completed for property {}", request.getPropertyName());
            } else {
                RdaAlarmProperty property = sm.findProperty(request.getPropertyName());

                if (null == property) {
                    LOG.warn("Property {} unknown.", request.getPropertyName());
                    request.requestFailed(new ServerException("Property '" + request.getPropertyName() + "' not found"));
                } else {
                    LOG.debug("Request completed for property {}", request.getPropertyName());
                    Data filters = request.getContext().getFilters();
                    request.requestCompleted(property.getValue(filters));
                }
            }
        }

        @Override
        public void set(SetRequest request) {
            request.requestFailed(new ServerException("SET is not supported by this server"));
        }

    }

    private class SubCallback implements SubscriptionCallback {

        @Override
        public void subscribe(SubscriptionRequest request) {
            RdaAlarmProperty property = sm.findProperty(request.getPropertyName());
            if (property != null) {
                LOG.debug("subscribe: {}", request.getId());
                SubscriptionCreator creator = request.accept();
                
                Data filters = request.getContext().getFilters();
                creator.firstUpdate(property.getValue(filters));
    
                creator.startPublishing();
            } else {
                LOG.error("Subscribe(): Cannot subscribe to non-existing property {}", request.getPropertyName());
                request.reject(new ServerException("No property " + request.getPropertyName()));
            }
        }

        @Override
        public void unsubscribe(Request request) {
            LOG.info("Unsubscribed: " + request.getHeader().getId());
        }

        @Override
        public void subscriptionSourceAdded(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceAdded: {}", subscription.getId());
            RdaAlarmProperty property = sm.findProperty(subscription.getPropertyName());
            if (property != null) {
                property.setSubscriptionSource(subscription);
                LOG.info("Subscription to {} accepted", subscription.getPropertyName());
            } else {
                LOG.error("Cannot subscribe to non-existing property {}", subscription.getPropertyName());
                subscription.notify(new ServerException("No property " + subscription.getPropertyName()));
            }

        }

        @Override
        public void subscriptionSourceRemoved(SubscriptionSource subscription) {
            LOG.debug("subscriptionSourceRemoved: {}", subscription.getId());
            RdaAlarmProperty property = sm.findProperty(subscription.getPropertyName());
            if (property != null) {
                property.removeSubscriptionSource();
            }
        }
    }

    /**
     * Updates the corresponding {@link RdaAlarmProperty} instance about the value update. In case of a new (yet)
     * unknown tag a new {@link RdaAlarmProperty} instance is first of all created.
     */
    @Override
    public void onAlarmUpdate(AlarmValue av) {

        String alarmId = getAlarmId(av);
        LOG.debug(" RECEIVED    > " + alarmId + " is active:" + av.isActive());
        received.increment();

        RdaAlarmProperty sourceProp = sm.findProp(alarmId);
        if (sourceProp == null) {
            rejected++;
            LOG.warn("Alarm " + alarmId + " discarded, could not find a source for it");
        } else {
            try {
                sourceProp.onUpdate(av);
                processed.increment();
            } catch (Exception e) {
                LOG.error("Failed to publish " + alarmId, e);
            }
        }
        LOG.debug(" PROCESSED    > " + alarmId);

    }

    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    static String getAlarmId(AlarmValue av) {
        return av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
    }

}
