/*
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.daq.cmwadmin;

import static java.lang.String.format;

import cern.c2mon.daq.cmwadmin.TagOperationException;
import cern.c2mon.driver.common.EquipmentMessageHandler;
import cern.c2mon.driver.common.conf.equipment.IDataTagChanger;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.common.datatag.address.SimpleHardwareAddress;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for CMW admin. It is a purely polling based data acquisition, the DAQ: - creates one thread
 * per server for data acquisition (see CMWAdminAcquisitionThread) - runs a thread to check that we received regular
 * updates, i.e. the acquisition is not blocked - handles connect/disconnect/execute callbacks (in this class) Notes: -
 * the connection test interval must be greater than the polling time, otherwise the system will constantly consider
 * that the connection timed out. That's why the prog takes the polling time as passed by the configuration, and
 * considers that connection checking should occur once per two pollings. TODO check in and deploy
 * 
 * @author Mark Buttner, Wojtek Buczak
 */
public class CMWServerHandler extends EquipmentMessageHandler implements Runnable, IDataTagChanger, IEquipmentConfigurationChanger {

    // C2MON equipment address parsing parameters. For the moment, it JMX based (which explains the
    // unused fields) to avoid a specialized implementation in the common API
    static final int EQ_ADDRESS_NUMBER_OF_PARAM_EXPECTED = 2;

    static final int SERVICE_URL_INDEX = 0;
    static final int POLLING_TIME_INDEX = 1;

    // The delay we accept to wait on disconnect before the threads stop
    static int DISCONNECT_TIMEOUT = 2; // in seconds!

    // timestamp of last successful connection
    private long lastContact = 0;

    // initially, we are init' and not connected
    boolean init = true;

    // the thread should not stop, flag changed on stop request
    boolean cont = true;

    // thread used for data acquisition
    private CMWAdminAcquisitionThread acquisitionThread;

    // the connection monitoring thread in this class
    private Thread connectionThread;

    // time in between two data acqs, parameter for the acq. thread
    private long pollingTime;

    // URL used to connect to the server
    private String serviceUrl;

    // time to wait in between two connection checks
    private long connTestInterval;

    // flag needed for smart notification avout status changes
    private boolean serviceConnected;

    /**
     * defines the 2 tags, which are mandatory for that type of equipment
     */
    enum TagType {
        STATUS, REACHABLE
    }

    /**
     * reference to the "reachable" tag
     */
    ISourceDataTag reachableTag;

    /**
     * reference to the "state" tag
     */
    ISourceDataTag statusTag;

    // --------------------------------------------- CONSTRUCTORS -------------------------------------
    /**
     * default constructor
     */
    public CMWServerHandler() {
    }

    // ---------------------------------------------- GETTERS -----------------------------------------
    /**
     * @return <code>String</code> the equipment URL-like string allowing to connect
     */
    public String getServiceUrl() {
        return this.serviceUrl;
    }

    /**
     * @return <code>long</code> time in ms to sleep in between two pollings to this server
     */
    public long getPollingTime() {
        return pollingTime;
    }

    // ----------------------------- IMPLEMENTS RUNNABLE ----------------------------------------------
    /**
     * Monitors the connection status to that server. This is done using the timestamp of last successful contact in the
     * data acquisition thread. The loop sleeps for the given delay, checks that the last contact is not too long time
     * ago and notifies the server accordingly.
     */
    public void run() {

        // send the first initial commfault tag
        getEquipmentMessageSender().confirmEquipmentStateOK("initial connection state");
        // set the connection state to OK
        serviceConnected = true;

        while (cont) {
            try {
                getEquipmentLogger().debug(format("Connection monitor sleeping for a while ..."));
                Thread.sleep(connTestInterval);
                getEquipmentLogger().debug(format("Checking connection ..."));
                lastContact = this.acquisitionThread.getLastContact();
                if (lastContact < System.currentTimeMillis() - connTestInterval) {
                    getEquipmentLogger().debug(format("timed out  ..."));
                    if (serviceConnected || init) {
                        getEquipmentLogger().warn(format("Connection to [%s] timed out", this.getServiceUrl()));
                        serviceConnected = false;
                        getEquipmentMessageSender().confirmEquipmentStateIncorrect("connection timed out");
                    }
                } else {
                    getEquipmentLogger().debug(format("is in time  ..."));
                    if (!serviceConnected) {
                        getEquipmentLogger().warn(format("Connection to [%s] is back", this.getServiceUrl()));
                        getEquipmentLogger().info("assuming connection recovered - sending CommfaultTag");
                        serviceConnected = true;
                        getEquipmentMessageSender().confirmEquipmentStateOK();
                    }
                }
                init = false;
            } catch (InterruptedException e) {
                getEquipmentLogger().info(format("Connection test failed due to sleep interruption"));
            }
        }
    }

    // -------------------------- IMPLEMENTS C2MON DAQ interface ---------------------------------------------
    //
    /**
     * On connection: check the equipment address and than start the acquisition and connection monitoring threads
     * 
     * @throws EqIOException an I/O exception for the C2MON equipment
     */
    @Override
    public void connectToDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering connectToDataSource()..");

        parseEquipmentAddress();

        // register handler as data-tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);
        // set equipment configuration changer
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);
       
        verifyTags();        
        
        if (!cern.cmw.rda.client.RDAService.initialized()) {
            cern.cmw.rda.client.RDAService.init();
        }

        acquisitionThread = new CMWAdminAcquisitionThread(this);
        acquisitionThread.start();

        connectionThread = new Thread(this);
        connectionThread.start(); // monitor connection

        getEquipmentLogger().debug("leaving connectToDataSource()");
    }

    /**
     * parses the equipment address
     * 
     * @throws EqIOException
     */
    private void parseEquipmentAddress() throws EqIOException {
        getEquipmentLogger().debug("entering validateEquipmentAddress()..");

        String address = this.getEquipmentConfiguration().getAddress();
        if (null == address) {
            throw new EqIOException("equipment address must NOT be null. Check DAQ configuration!");
        }

        String[] tokens = address.trim().split(";");

        if (tokens.length < EQ_ADDRESS_NUMBER_OF_PARAM_EXPECTED) {
            throw new EqIOException(format(
                    "Equipment address requires %d parameters, but %d were found. Check DAQ configuration!",
                    EQ_ADDRESS_NUMBER_OF_PARAM_EXPECTED, tokens.length));
        }

        this.serviceUrl = tokens[SERVICE_URL_INDEX].trim();
        try {
            pollingTime = Integer.parseInt(tokens[POLLING_TIME_INDEX].trim());
            if (pollingTime <= 0) {
                throw new EqIOException(
                        "Polling time in the equmpent address must NOT be <= 0. Check DAQ configuration!");
            }
            this.connTestInterval = pollingTime * 2;

        } catch (NumberFormatException ex) {
            throw new EqIOException(
                    "polling time in the equmpent address must be an integer >= 0. Check DAQ configuration!");
        }

        getEquipmentLogger().debug("leaving validateEquipmentAddress()");
    }

    private void verifyTags() {
        getEquipmentLogger().debug("entering verifyTags()..");

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            try {
                registerTag(tag);
            } catch (TagOperationException ex) {
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        "unsupported tag type");
            }
        }

        getEquipmentLogger().debug("leaving verifyTags()");
    }

    // ---------------------------------------------------------------------------------

    // on disconnect, stop all the submitted tasks
    @Override
    public void disconnectFromDataSource() {
        getEquipmentLogger().info("entering diconnectFromDataSource()..");
        cont = false;

        if (acquisitionThread != null) {
            acquisitionThread.shutdown();
            try {
                acquisitionThread.join(DISCONNECT_TIMEOUT * 1000);                
                acquisitionThread.interrupt();
            } catch (InterruptedException ie) {
                getEquipmentLogger().warn("Acquisition thread did not stop within delay, skipping");
            }
        }
        try {
            if (connectionThread != null) {
                connectionThread.join(DISCONNECT_TIMEOUT * 1000);
                connectionThread.interrupt();
            }
        } catch (InterruptedException ie) {
            getEquipmentLogger().warn("Connection monitoring thread did not stop within delay, skipping");
        }
        getEquipmentLogger().debug("leaving diconnectFromDataSource()");
    }

    public synchronized ISourceDataTag getTag(TagType type) {
        ISourceDataTag reference = null;
        switch (type) {
        case REACHABLE:
            reference = reachableTag;
            break;
        case STATUS:
            reference = statusTag;
            break;
        }

        return reference;
    }

    private synchronized void setTag(TagType type, ISourceDataTag tag) {
        if (type != null) {
            switch (type) {
            case REACHABLE:
                this.reachableTag = tag;
                break;
            case STATUS:
                this.statusTag = tag;
                break;
            }// switch
        }// if
    }

    // not used because in a poller DAQ we are sure to have regular updates
    @Override
    public void refreshAllDataTags() {
    }

    @Override
    public void refreshDataTag(@SuppressWarnings("unused") long dataTagId) {
    }

    @Override
    public void onAddDataTag(ISourceDataTag sdt, ChangeReport rep) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onAddDataTag(%d)..", sdt.getId()));

        rep.setState(CHANGE_STATE.SUCCESS);

        // register tag
        try {
            registerTag(sdt);
        } catch (TagOperationException ex) {
            rep.setState(CHANGE_STATE.FAIL);
            rep.appendError(ex.getMessage());
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onAddDataTag(%d)", sdt.getId()));

    }

    @Override
    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport rep) {

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        rep.setState(CHANGE_STATE.SUCCESS);

        // unregister tag
        try {
            unregisterTag(sourceDataTag);
        } catch (TagOperationException ex) {
            rep.setState(CHANGE_STATE.FAIL);
            rep.appendError(ex.getMessage());
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));

    }

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                getEquipmentLogger().debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.appendWarn(ex.getMessage());
            }
            try {
                getEquipmentLogger().debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
                registerTag(sourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.setState(CHANGE_STATE.FAIL);
                changeReport.appendError(ex.getMessage());
            }
        }// if
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (getEquipmentLogger().isTraceEnabled())
            getEquipmentLogger().trace(format("entering registerTag(%d)", tag.getId()));

        // check if this tag is not already registered
        if (isTagAlreadyRegistered(tag.getId())) {
            getEquipmentLogger().warn(format("tag: %d is already registered. You must unregister it first!", tag.getId()));
            return;
        }

        SimpleHardwareAddress addr = (SimpleHardwareAddress) tag.getHardwareAddress();
        TagType type = TagType.valueOf(addr.getAddress().toUpperCase().trim());

        if (type == null) {
            throw new TagOperationException(format(
                    "the type of tag: %d is unsupported. only REACHABLE and STATUS tags are supported. ", tag.getId()));
        }

        this.setTag(type, tag);

        if (getEquipmentLogger().isTraceEnabled())
            getEquipmentLogger().trace(format("leaving registerTag(%d)", tag.getId()));
    }

    private void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (getEquipmentLogger().isTraceEnabled())
            getEquipmentLogger().trace(format("entering unregisterTag(%d)", tag.getId()));

        if (!isTagAlreadyRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is not registered. You must register it first!",
                    tag.getId()));
        }

        try {

            if (tag.getId() == reachableTag.getId()) {
                setTag(TagType.REACHABLE, null);
            } else if (tag.getId() == statusTag.getId()) {
                setTag(TagType.STATUS, null);
            }

        } catch (Exception ex) {
            String err = format("Unable to unregister tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentLogger().error(err);
            throw new TagOperationException(err);
        }

        finally {
            if (getEquipmentLogger().isTraceEnabled())
                getEquipmentLogger().trace(format("leaving unregisterTag(%d)", tag.getId()));
        }
    }

    boolean isTagAlreadyRegistered(final long tagId) {
        boolean result = false;
        if (this.reachableTag != null && this.reachableTag.getId() == tagId) {
            return true;
        }
        if (this.statusTag != null && this.statusTag.getId() == tagId) {
            return true;
        }

        return result;
    }
    
    
    @Override
    public void onUpdateEquipmentConfiguration(
            @SuppressWarnings("unused") IEquipmentConfiguration equipmentConfiguration,
            @SuppressWarnings("unused") IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        getEquipmentLogger().debug("entering onUpdateEquipmentConfiguration()..");

        // without analyzing what has changed in the equipment's configuration
        // we simply call disconnect and right after - connect
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();

            changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn(ex.getMessage());
        } finally {
            getEquipmentLogger().debug("leaving onUpdateEquipmentConfiguration()");
        }

    }    

}