/*
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.daq.cmwadmin;

import static java.lang.String.format;

import cern.cmw.adm.ServerAdmin;
import cern.c2mon.daq.cmwadmin.CMWServerHandler.TagType;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.tools.TIMDriverSimpleTypeConverter;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * The thread that connects to the server and queries for status information. As it is a single thread per server, there
 * is a risk of permanently hanging on the same dead connection. On the other, hand this has the advantage that the
 * driver will not flood the CMW server with additional connection requests (which could also fail). This class runs as
 * (almost) endless loop, sleeping a while, querying the standard status data and additional data properties if
 * possible, and than matches the obtained data with the datatags configured for monitoring
 * 
 * @author Mark Buttner, Wojtek Buczak
 */
public class CMWAdminAcquisitionThread extends Thread {

    private CMWServerHandler handler; // the C2MON DAQ API entry class, extends the EquipmentMessageHandler

    private String serviceUrl; // internal shortcut to the server name
    private long pollingTime; // ... the time to wait in between two data acqs
    private EquipmentLogger log; // ... too avoid 100 calls to handler.getEquipmentLogger()

    boolean cont = true; // the flag that allows for smooth exit of the endless loop in run
    long lastContact = 0; // timestamp of the last successful contact we had with the server
    private String lastError = ""; // error message, applies only in case of failure

    // ------------------ CONSTRUCTORS ------------------------------------------------------

    /**
     * Constructor. Stores the handler and extract the data into our shortcuts
     * 
     * @param handler <code>CMWServerHandler</code> the implementation of C2MON's EquipmentMessageHandler
     */
    public CMWAdminAcquisitionThread(final CMWServerHandler handler) {
        this.handler = handler;
        this.serviceUrl = handler.getServiceUrl();
        this.pollingTime = handler.getPollingTime();
        this.log = handler.getEquipmentLogger();
    }

    // ------------------------------------- GETTERS -----------------------------------
    /**
     * This method is sync'd because the connection monitoring thread uses this information
     * 
     * @return <code>long</code> the timestamp of the last successful contact
     */
    public synchronized long getLastContact() {
        return lastContact;
    }

    /**
     * This method is sync'd because the connection monitoring thread uses this information
     * 
     * @return <code>String</code> the message related to the last obeserved error on conn attempt
     */
    public synchronized String getLastError() {
        return this.lastError;
    }

    // ------------------------------------- SETTERS -----------------------------------
    /**
     * Sync'd update of the last contact timestamp (-< the connection monitor might try to read at the same time ...)
     * 
     * @param lastContact <code>long</code>
     */
    private synchronized void setLastContact(long lastContact) {
        this.lastContact = lastContact;
    }

    /**
     * Sync'd update of the last error message (-< the connection monitor might try to read at the same time ...)
     * 
     * @param lastError <code>String</code>
     */
    private synchronized void setLastError(String lastError) {
        this.lastError = lastError;
    }

    /**
     * Request to smoothly exit from the acquisition loop. This simply changes a flag, if the endless while loop is not
     * blocked, it will exit as soon as it wakes up from its sleeping period
     */
    public void shutdown() {
        cont = false;
    }

    // ------------------------------------- IMPLEMENTS RUNNABLE -----------------------------------

    /**
     * The almost endless loop in this method tries to connect to the CMW server described by serviceUrl, and retrieves
     * in one go all admin properties available. It than operates a match "by name" with data tags defined for this
     * equipment in the DAQ configuration. An exception at any stage will lead abandon the whole attempt. The thread
     * than goes to sleep and will retry "pollingTime" millis later.
     */
    @Override
    public void run() {
        log.info("Start acquisition thread ...");

        while (cont) {

            // go through all configured tags and try to match with some data coming from
            // the CMW server
            ISourceDataTag reachableTag = handler.getTag(TagType.REACHABLE);
            ISourceDataTag statusTag = handler.getTag(TagType.STATUS);

            try {
                log.debug(format("Trying to connect to CMW server: %s", handler.getServiceUrl()));

                ServerAdmin server = new ServerAdmin(serviceUrl);
                int status = server.getStatus().value(); // 0 = OK, 1 = WRN, 2 = ERR ?

                this.setLastContact(System.currentTimeMillis());
                if (log.isDebugEnabled())
                    log.debug(format("successfully connected to CMW server: %s", serviceUrl));

                if (reachableTag != null)
                    this.updateValue(reachableTag, null, true);
                if (statusTag != null)
                    this.updateValue(statusTag, null, status);

                // the NoConnectionException is special in the sense that it requires the update
                // of a special datatag which should be present in all configurations
            } catch (cern.cmw.NoConnection nce) {
                if (reachableTag != null)
                    this.updateValue(reachableTag, nce.getMessage(), false);

                // all other exceptions
            } catch (Exception ex) {
                StringBuilder bld = new StringBuilder("Failed to contact CMW server: ");
                bld.append(serviceUrl);
                bld.append(" Exception caught: ");
                bld.append(ex.getMessage());
                handler.getEquipmentLogger().error(bld);
                setLastError(bld.toString());
            }

            try {
                Thread.sleep(this.pollingTime);
            } catch (InterruptedException e) {
                log.warn(e);
            }
        }
        log.info(format("Acquisition thread stopped."));
    }

    /**
     * Send updated tag to the server
     * 
     * @param tag
     * @param userValueDescription
     * @throws Exception
     */
    void updateValue(final ISourceDataTag tag, final String userValueDescription, Object o) /* throws Exception */{
        Object v = null;
        if (o instanceof Number) {
            v = TIMDriverSimpleTypeConverter.convert(tag, (Number) o);
        } else if (o instanceof String) {
            v = TIMDriverSimpleTypeConverter.convert(tag, (String) o);
        } else if (o instanceof Boolean) {
            v = TIMDriverSimpleTypeConverter.convert(tag, (Boolean) o);
        }

        if (null != v) {
            if (log.isDebugEnabled()) {
                log.debug("sending value [" + v + "] for tag [" + tag.getName() + "]");
            }
            handler.getEquipmentMessageSender().sendTagFiltered(tag, v, System.currentTimeMillis(),
                    userValueDescription);
        } else {
            log.info(format("Invalidating tag[%d] with quality CONVERSION_ERROR", tag.getId()));
            handler.getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
        }
    }

}
