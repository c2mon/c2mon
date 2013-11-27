/*
 * Copyright CERN 2011-2013, All Rights Reserved.
 */
package cern.c2mon.daq.common.vcm.testhandler;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * This class is used for testing purposes only!  The <code>EspMessageHandler<code> is a specialized subclass of the generic 
 * EquipmentMessageHandler. It is used for VCMs (value-change-monitor) j-unit testing.
 */
public class EspMessageHandler extends EquipmentMessageHandler implements Runnable, IDataTagChanger,
        IEquipmentConfigurationChanger {

    public static final int SENDER_THREADS = Integer.parseInt(System.getProperty("dmn2.daq.esp.sender.threads", "2"));
    public static final int SENDER_INTERVAL = Integer.parseInt(System.getProperty("dmn2.daq.esp.interval", "1"));
    
    /*
     * NOTE: logger is initialized in the connectToDataSource() method
     */
    private EquipmentLogger logger;

    /**
     * static executor service for executing periodic update tasks 
     */

    private static ScheduledExecutorService executor;

    /**
     * static map keeping handles for all scheduled update tasks
     */
    private static Map<Long, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<Long, ScheduledFuture<?>>();

    /**
     * reference to the initialization thread
     */
    private Thread initThread;

    private static Map<Long, Boolean> skipMap = new ConcurrentHashMap<Long, Boolean>();

    private static Map<Long, Integer> stepMap = new ConcurrentHashMap<Long, Integer>();

    /**
     * default constructor
     */
    public EspMessageHandler() {
        // create executor
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(SENDER_THREADS);
        }
    }

    /**
     * @param pollingTime
     */
    void startSenderTask(final ISourceDataTag tag, final int senderTime) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering startSenderTask(%d,%d)..", tag.getId(), senderTime));

        scheduledFutures.put(tag.getId(),
                executor.scheduleAtFixedRate(new SenderTask(this, tag), 0, senderTime * 1000, TimeUnit.MILLISECONDS));

        logger.trace("leaving startSenderTask()");
    }

    /**
     * stops poller for given tag
     * 
     * @param tagId
     */
    synchronized void stopSenderTask(final Long tagId) {
        if (logger.isTraceEnabled())
            logger.trace(format("entering startSenderTask(%d)..", tagId));

        ScheduledFuture<?> sf = scheduledFutures.get(tagId);
        if (null != sf) {
            sf.cancel(true);
            scheduledFutures.remove(tagId);
        }

        logger.trace("leaving startSenderTask()");
    }

    synchronized void stopAllSenderTasks() {
        logger.trace("entering stopAllSenderTasks()..");

        for (Long tagId : scheduledFutures.keySet()) {
            this.stopSenderTask(tagId);
        }

        logger.trace("leaving stopAllSenderTasks()");
    }

    @Override
    public void connectToDataSource() {
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
        if (logger != null)
            logger.debug("entering diconnectFromDataSource()..");

        // stop all sender tasks
        this.stopAllSenderTasks();
        
        if (logger != null)
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

    synchronized boolean isTagAlreadyRegistered(final long tagId) {
        boolean result = true;

        if (!scheduledFutures.containsKey(tagId)) {
            result = false;
        }

        return result;
    }

    synchronized void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering registerTag(%d)", tag.getId()));

        // this tag should not be present neither in the notification-tags map nor a scheduler should be present
        if (isTagAlreadyRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is already registered. You must unregister it first!",
                    tag.getId()));
        }

        try {

            this.startSenderTask(tag, SENDER_INTERVAL);

        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving registerTag(%d)", tag.getId()));
        }

    }

    synchronized private void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering unregisterTag(%d)", tag.getId()));

        if (!isTagAlreadyRegistered(tag.getId())) {
            throw new TagOperationException(format("tag: %d is not registered. You must register it first!",
                    tag.getId()));
        }

        try {

            if (isTagAlreadyRegistered(tag.getId())) {
                // stop the poller for that tag (if exists)
                this.stopSenderTask(tag.getId());
            } else {
                throw new TagOperationException(format(
                        "could not unregister tag: %d. Sender task was not registered. You must restart the DAQ!",
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

    /**
     * @param id
     * @return
     */
    public boolean skipUpdate(Long id) {
        if (skipMap.containsKey(id) && skipMap.get(id))
            return true;
        else
            return false;
    }

    public void skip(Long id, boolean flag) {
        skipMap.put(id, flag);
    }

    public void setStep(Long id, Integer step) {
        stepMap.put(id, step);
    }

    /**
     * @param id
     * @return
     */
    public int getStep(Long id) {

        if (!stepMap.containsKey(id)) {
            stepMap.put(id, 1);
        }

        return stepMap.get(id);
    }

}
