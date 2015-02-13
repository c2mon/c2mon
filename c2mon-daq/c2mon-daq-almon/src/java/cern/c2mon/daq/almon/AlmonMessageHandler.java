/*
 * Copyright CERN 2014, All Rights Reserved.
 */
package cern.c2mon.daq.almon;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.AlmonHardwareAddressFactory;
import cern.c2mon.daq.almon.plsline.PlsLineResolver;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfigurationChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler which is responsible for determining accessibility of a computer.
 */
public class AlmonMessageHandler extends EquipmentMessageHandler implements Runnable, IDataTagChanger,
        IEquipmentConfigurationChanger {

    /*
     * NOTE: LOG is initialized in the connectToDataSource() method
     */
    private EquipmentLogger LOG;

    /**
     * executor service for periodic threads NOTE: a shared (static) executor is used across all instances of the
     * AlmonMessageHandler
     */

    private static ScheduledExecutorService executor;

    // a map keeping JAPC parameters handlers for GM and FESA parameters
    private Map<AlmonHardwareAddress, JapcParameterHandler> almonParameters = new ConcurrentHashMap<>();

    /**
     * reference to the initialization thread
     */
    private Thread initThread;

    private AlmonConfig config;
    private AlmonSender almonSender;
    private PlsLineResolver plsLineResolver;

    private static ClassPathXmlApplicationContext ctx;

    /**
     * default constructor
     */
    public AlmonMessageHandler() {

        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("classpath:resources/dmn-almon-config.xml");
            ctx.getEnvironment().setDefaultProfiles("PRO");
            ctx.refresh();
        }

        config = ctx.getBean(AlmonConfig.class);
        almonSender = ctx.getBean("almonSenderProxy", AlmonSender.class);
        plsLineResolver = ctx.getBean(PlsLineResolver.class);

        // create executor
        if (executor == null) {
            executor = Executors.newScheduledThreadPool(config.getSubcriptionsThreadPoolSize());
        }

    }

    @Override
    public void connectToDataSource() throws EqIOException {
        LOG = getEquipmentLogger();

        if (LOG.isDebugEnabled()) {
            Environment env = ctx.getBean(Environment.class);
            StringBuilder bld = new StringBuilder();
            for (String profile : env.getActiveProfiles())
                bld.append(profile).append(" ");
            LOG.debug("active spring profile(s) : " + bld.toString());
        }

        LOG.debug("entering connectToDataSource()..");

        // register handler as data-tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);

        // set equipment configuration changer
        getEquipmentConfigurationHandler().setEquipmentConfigurationChanger(this);

        // create initialization thread
        initThread = new Thread(this);

        // start the initialization thread
        initThread.start();

        LOG.debug("leaving connectToDataSource()");
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
    public synchronized void disconnectFromDataSource() throws EqIOException {
        LOG.debug("entering diconnectFromDataSource()..");

        for (JapcParameterHandler handler : almonParameters.values()) {
            handler.stopMonitoring();
        }

        almonParameters.clear();
        LOG.debug("leaving diconnectFromDataSource()");
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (LOG.isDebugEnabled())
            LOG.debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // register tag
        try {
            registerTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        if (LOG.isDebugEnabled())
            LOG.debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (LOG.isDebugEnabled())
            LOG.debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // unregister tag
        try {
            unregisterTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(ex.getMessage());
        }

        if (LOG.isDebugEnabled())
            LOG.debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        if (LOG.isDebugEnabled())
            LOG.debug(format("entering onUpdateDataTag(%d,%d)..", sourceDataTag.getId(), oldSourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (!oldSourceDataTag.getHardwareAddress().equals(sourceDataTag.getHardwareAddress())) {
            try {
                LOG.debug(format("calling  unregisterTag(%d)..", oldSourceDataTag.getId()));
                unregisterTag(oldSourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.appendWarn(ex.getMessage());
            }
            try {
                LOG.debug(format("calling  registerTag(%d)..", sourceDataTag.getId()));
                registerTag(sourceDataTag);
            } catch (TagOperationException ex) {
                changeReport.setState(CHANGE_STATE.FAIL);
                changeReport.appendError(ex.getMessage());
            }
        }// if
        else {
            changeReport.appendInfo("No change detected in the tag hardware address. No action effected");
        }

        if (LOG.isDebugEnabled())
            LOG.debug(format("leaving onUpdateDataTag(%d,%d)", sourceDataTag.getId(), oldSourceDataTag.getId()));
    }

    class SubscriptionTask implements Runnable {
        private JapcParameterHandler handler;

        SubscriptionTask(JapcParameterHandler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            // subscribe to the parameter
            handler.startMonitoring();
        }
    }

    synchronized void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (LOG.isTraceEnabled())
            LOG.trace(format("entering registerTag(%d)", tag.getId()));

        try {

            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();

            // wrap it around with almon hardware address
            AlmonHardwareAddress addr = AlmonHardwareAddressFactory.fromJson(saddr.getAddress().trim());

            // initialize the tag

            JapcParameterHandler parameterHandler = null;
            switch (addr.getType()) {
            case GM:
                // check if not yet subscribed to that parameter
                if (!almonParameters.containsKey(addr.getAlarmTriplet())) {

                    LOG.info(format("Registering GmParameter: tag id: %d, alarm-triplet: %s", tag.getId(), addr
                            .getAlarmTriplet().toString()));

                    parameterHandler = new GmJapcParameterHandler(tag, addr, getEquipmentMessageSender(), almonSender);

                }
                break;

            case FESA:
                // check if not yet subscribed to that parameter
                if (!almonParameters.containsKey(addr.getAlarmTriplet())) {

                    LOG.info(format("Registering FesaParameter: tag id: %d, alarm-triplet: %s", tag.getId(), addr
                            .getAlarmTriplet().toString()));

                    parameterHandler = new FesaJapcParameterHandler(tag, addr, getEquipmentMessageSender(),
                            almonSender, plsLineResolver);
                }

                break;

            }

            if (parameterHandler != null) {
                almonParameters.put(addr, parameterHandler);
                executor.execute(new SubscriptionTask(parameterHandler));
            }

        } catch (Exception ex) {
            String err = format("Unable to register tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, err);
        }

        finally {
            if (LOG.isTraceEnabled())
                LOG.trace(format("leaving registerTag(%d)", tag.getId()));
        }

    }

    synchronized void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (LOG.isTraceEnabled())
            LOG.trace(format("entering unregisterTag(%d)", tag.getId()));

        try {

            SimpleHardwareAddress saddr = (SimpleHardwareAddress) tag.getHardwareAddress();

            // wrap it around with almon hardware address
            AlmonHardwareAddress addr = AlmonHardwareAddressFactory.fromJson(saddr.getAddress().trim());

            JapcParameterHandler parameterHandler = almonParameters.get(addr);

            if (parameterHandler != null) {
                parameterHandler.stopMonitoring();
                almonParameters.remove(addr);
            }

        } catch (Exception ex) {
            String err = format("Unable to unregister tag: %d. Problem description: %s", tag.getId(), ex.getMessage());
            LOG.error(err);
            throw new TagOperationException(err);
        }

        finally {
            if (LOG.isTraceEnabled())
                LOG.trace(format("leaving unregisterTag(%d)", tag.getId()));
        }
    }

    @Override
    public void onUpdateEquipmentConfiguration(IEquipmentConfiguration equipmentConfiguration,
            IEquipmentConfiguration oldEquipmentConfiguration, ChangeReport changeReport) {

        LOG.debug("entering onUpdateEquipmentConfiguration()..");

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
            LOG.debug("leaving onUpdateEquipmentConfiguration()");
        }

    }

}
