/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.ping;

import static cern.c2mon.daq.ping.Configuration.PING_TIMEOUT;
import static cern.c2mon.daq.ping.Configuration.REFRESH_DNS_ADDRESS;
import static java.lang.String.format;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cern.c2mon.shared.daq.datatag.ISourceDataTag;

/**
 * This class implements a polling task. PingTask tasks are executed periodically to check the computer state
 * 
 * @author wbuczak
 */
public class PingTask implements Runnable {

    private PingMessageHandler handler;
    private ISourceDataTag tag;
    private Target target;
    private String hostname;

    private static Map<String, Long> runCountMap = new ConcurrentHashMap<String, Long>();

    private static final Logger log = Logger.getLogger(PingTask.class);

    public PingTask(final PingMessageHandler handler, final ISourceDataTag tag, final Target target) {
        this.handler = handler;
        this.tag = tag;
        this.target = target;
        this.hostname = target.getHostname();
    }

    @Override
    public void run() {

        // check if this host is already registered
        // if not, initialize its counter
        if (!runCountMap.containsKey(target.getHostname())) {
            runCountMap.put(target.getHostname(), 0L);
        } else if (target.getAddress() == null) {
            try {
                target.refreshAddress();
            } catch (UnknownHostException ex) {
                target.failure(ex);
                if (log.isDebugEnabled()) {
                    log.debug("Could not get IP address due to " + ex.getClass().getName() + ": " + ex.getMessage());
                }

                try {
                    handler.getEquipmentMessageSender().sendTagFiltered(tag, target.getCurrentStatus().getCode(),
                            System.currentTimeMillis(),
                            format(target.getCurrentStatus().getDescription(), target.getHostname()));
                } catch (Exception ex2) {
                    log.error("exception caught when trying to send tag update", ex2);
                }

                return;
            }

        }

        long runcount = runCountMap.get(hostname);

        try {
            if (runcount == 0 || (REFRESH_DNS_ADDRESS > 0 && runcount % REFRESH_DNS_ADDRESS == 0)) {
                if (log.isDebugEnabled()) {
                    log.debug("Querying DNS for latest address ..." + target.getHostname());
                }
                target.refreshAddress();

                if (log.isDebugEnabled()) {
                    log.debug(format("received IP address: %s  for host: %s from DNS", target.getAddress()
                            .getHostAddress(), target.getHostname()));
                }
            }

            if (target.getAddress() != null) {
                if (target.getAddress().isReachable(PING_TIMEOUT)) {
                    if (log.isDebugEnabled()) {
                        log.debug("target host: " + target.getHostname() + " replied to PING");
                    }
                    target.success();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("target host: " + target.getHostname() + " did not reply to PING");
                    }
                    target.failure();
                }
            }
        } catch (Exception ex) {
            target.failure(ex);
            // this is not really an error - machine may be switched off etc.. Therefore we log it only in debug
            // mode
            if (log.isDebugEnabled()) {
                log.debug("Ping failed due to " + ex.getClass().getName() + ": " + ex.getMessage());
            }
        }

        // increment the counter
        ++runcount;
        // just for fun ;-)
        if (runcount == Long.MAX_VALUE) {
            runcount = 0;
        }
        runCountMap.put(hostname, runcount);

        try {
            handler.getEquipmentMessageSender().sendTagFiltered(tag, target.getCurrentStatus().getCode(),
                    System.currentTimeMillis(),
                    format(target.getCurrentStatus().getDescription(), target.getHostname()));
        } catch (Exception ex) {
            log.error("exception caught when trying to send tag update", ex);
        }

    }// run

}