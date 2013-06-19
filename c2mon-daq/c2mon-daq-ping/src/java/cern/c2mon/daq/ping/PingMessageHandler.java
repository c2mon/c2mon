/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.ping;

import static java.lang.String.format;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentMessageHandler;
import cern.tim.driver.common.conf.equipment.IDataTagChanger;
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.tim.driver.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.common.datatag.address.SimpleHardwareAddress;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

import static cern.c2mon.daq.ping.Configuration.POLLING_INTERVAL;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler which is responsible for determining accessibility of a computer.
 */
public class PingMessageHandler extends EquipmentMessageHandler implements Runnable, IDataTagChanger,
        IEquipmentConfigurationChanger {

    /*
     * NOTE: logger is initialized in the connectToDataSource() method
     */
    private EquipmentLogger logger;

    /**
     * executor service for periodic threads NOTE: a shared (static) executor is used across all instances of the
     * PingMessageHandler
     */

    private static ScheduledExecutorService executor;

    /**
     * handles for pollers NOTE: a shared (static) map is used across all instances of the PingMessageHandler
     */
    private static Map<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<Long, ScheduledFuture<?>>();

    /**
     * reference to the initialization thread
     */
    private Thread initThread;

    /**
     * default constructor
     */
    public PingMessageHandler() {
        // create executor
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(Configuration.POLLING_THREADS);
        }
    }

    /**
     * @param pollingTime
     */
    void startPingTask(final ISourceDataTag tag, final Target target, final int pollingTime) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering startPingTask(%d,%s, %d)..", tag.getId(), target.getHostname(), pollingTime));

        scheduledFutures.put(tag.getId(), executor.scheduleAtFixedRate(new PingTask(this, tag, target), new Random(
                System.currentTimeMillis()).nextInt(2000), pollingTime * 1000, TimeUnit.MILLISECONDS));

        logger.trace("leaving startPingTask()");
    }

    /**
     * stops poller for given tag
     * 
     * @param tagId
     */
    void stopPingTask(final Long tagId) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering stopPingTask(%d)..", tagId));

        ScheduledFuture<?> sf = scheduledFutures.get(tagId);
        if (null != sf) {
            sf.cancel(true);
            scheduledFutures.remove(tagId);
        }

        logger.trace("leaving stopPingTask()");
    }

    void stopAllPingTasks() {
        logger.trace("entering stopAllPingTasks()..");

        for (Long tagId : scheduledFutures.keySet()) {
            this.stopPingTask(tagId);
        }

        logger.trace("leaving stopAllPingTasks()");
    }

    @Override
    public void connectToDataSource() throws EqIOException {
        logger = getEquipmentLogger();

        logger.debug("entering connectToDataSource()..");

        // register handler as data-tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);

        // set equipment configuration changer
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);

        // create initialization thread
        initThread = new Thread(this);

        // start the initialization thread
        initThread.start();

        logger.debug("leaving connectToDataSource()");
    }

    @Override
    public void run() {

        getEquipmentMessageSender().confirmEquipmentStateOK();

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            try {
                registerTag(tag);
            } catch (TagOperationException ex) {
                getEquipmentLogger().error(ex.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, ex.getMessage());
            }
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void disconnectFromDataSource() throws EqIOException {
        logger.debug("entering diconnectFromDataSource()..");

        // stop all ping tasks
        this.stopAllPingTasks();

        logger.debug("leaving diconnectFromDataSource()");
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub
    }

    @Override
    public void refreshDataTag(@SuppressWarnings("unused") long dataTagId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // register tag
        try {
            registerTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // unregister tag
        try {
            unregisterTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        if (logger.isDebugEnabled())
            logger.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                logger.debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.appendWarn(ex.getMessage());
            }
            try {
                logger.debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
                registerTag(sourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.setState(CHANGE_STATE.FAIL);
                changeReport.appendError(ex.getMessage());
            }
        }// if
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }

        if (logger.isDebugEnabled())
            logger.debug(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    boolean isTagPollerRegistered(final long tagId) {
        boolean result = true;
        if (!scheduledFutures.containsKey(tagId)) {
            result = false;
        }

        return result;
    }

    void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering registerTag(%d)", tag.getId()));

        // check if this tag is not already registered

        // this tag should not be present neither in the notification-tags map nor a scheduler should be present
        if (isTagPollerRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is already registered. You must unregister it first!",
                    tag.getId()));
        }

        try {

            SimpleHardwareAddress addr = (SimpleHardwareAddress) tag.getHardwareAddress();
            Target target = new Target(addr.getAddress());
            this.startPingTask(tag, target, POLLING_INTERVAL);

        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving registerTag(%d)", tag.getId()));
        }

    }

    private void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering unregisterTag(%d)", tag.getId()));

        if (!isTagPollerRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is not registered. You must register it first!",
                    tag.getId()));
        }

        try {

            if (isTagPollerRegistered(tag.getId())) {
                // stop the poller for that tag (if exists)
                this.stopPingTask(tag.getId());
            } else {
                throw new TagOperationException(format(
                        "could not unregister tag: %d. Ping poller task was not registered. You must restart the DAQ!",
                        tag.getId()));
            }

        } catch (Exception ex) {
            String err = format("Unable to unregister tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            logger.error(err);
            throw new TagOperationException(err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving unregisterTag(%d)", tag.getId()));
        }

    }

    @Override
    public void onUpdateEquipmentConfiguration(
            @SuppressWarnings("unused") IEquipmentConfiguration equipmentConfiguration,
            @SuppressWarnings("unused") IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        logger.debug("entering onUpdateEquipmentConfiguration()..");

        // without analyzing what has changed in the equipment's configuration
        // we simply call disconnectFromDataSource() and right after - connectToDataSource()
        try {
            this.disconnectFromDataSource();
            this.connectToDataSource();

            changeReport.setState(CHANGE_STATE.SUCCESS);
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn(ex.getMessage());
        } finally {
            logger.debug("leaving onUpdateEquipmentConfiguration()");
        }

    }

}
