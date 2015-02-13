/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SubscriptionHandle;
import cern.japc.SubscriptionRecoveredException;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;

/**
 * Base class for GM and FESA JAPC parameter subscriptions. Implements common JAPC parameter subscription + device
 * access fault handling mechanisms.
 *
 * @author wbuczak
 */
public abstract class JapcParameterHandler implements ParameterValueListener {

    public static final String ASI_PREFIX_PROPERTY = "ASI_PREFIX";
    public static final String ASI_SUFFIX_PROPERTY = "ASI_SUFFIX";

    protected static final Logger LOG = LoggerFactory.getLogger(JapcParameterHandler.class);

    protected AlmonHardwareAddress address;

    protected AlmonSender amSender;

    protected IEquipmentMessageSender ems;

    private SubscriptionHandle shandle;

    protected ISourceDataTag tag;

    public static final String ERROR_SERVER_UNREACHABLE = "Server is down or unreachable";
    public static final String ERROR_UNKNOWN_DEVICE = "Unknown device (name server)";

    public static final String ALMON_FAULT_PROPERTY_TAG = "errorDetails";

    public static final String ALARM_MON_FAULT_FAMILY = "CMWALARMMONITOR";

    public static final int ALARM_MON_FAULT_IN_ERROR = 2;

    protected boolean inFault = false;

    // the key: deviceName, value: problem description
    protected static Map<String, String> deviceAccessFaults = new ConcurrentHashMap<>();

    protected static Map<String, AtomicInteger> parametersPerDevice = new ConcurrentHashMap<>();

    protected JapcParameterHandler(ISourceDataTag tag, AlmonHardwareAddress hwAddress, IEquipmentMessageSender ems,
            AlmonSender amSender) {
        this.tag = tag;
        this.address = hwAddress;
        this.ems = ems;
        this.amSender = amSender;

        // initialize tag
        ems.sendTagFiltered(tag, Boolean.FALSE, System.currentTimeMillis());
    }

    /**
     * The startMonitoring() must be called in order for the parameter to get subscribed
     */
    public void startMonitoring() {
        try {

            // initialize the tag
            this.amSender.terminate(tag, ems, address.getAlarmTriplet(), System.currentTimeMillis());

            String deviceName = address.getAlarmTriplet().getFaultMember();

            // increment
            synchronized (deviceName.intern()) {
                if (!parametersPerDevice.containsKey(deviceName)) {
                    parametersPerDevice.put(deviceName, new AtomicInteger(1));
                } else {
                    parametersPerDevice.get(deviceName).incrementAndGet();
                }
            }// synchronized

            String device = address.getDevice();
            String prop = address.getProperty();
            
            // TODO something not properly handled here when device is null ...            
            if (device == null)
            {
                LOG.error("Trying to create JAPC parameter for " + device + "/" + prop);
                throw new ParameterException("Can not subscribe to NULL device");
            }
            else
            {
                Parameter p = ParameterFactory.newInstance().newParameter(device, prop);
    
                Selector selector = null;
    
                if (address.hasCycle()) {
                    selector = ParameterValueFactory.newSelector(address.getCycle());
                }
                shandle = p.createSubscription(selector, this);
    
                shandle.startMonitoring();
            }
        } catch (ParameterException e) {

            LOG.warn("subscribing to parameter: {} failed. Reason: {}", address.getJapcParameterName(), e.getMessage());
            LOG.debug("exception trace:", e);

            exceptionOccured(address.getJapcParameterName(), null, e);
        }
    }

    /**
     * This method is to be called when parameter is no longer needed. In practice alarm-monitor's db polling task
     * (which periodically monitors db for alarm definition changes calls it when a parameter is to be unregistered. See
     * <code>AlmonMain</code> for details
     */
    public void stopMonitoring() {

        // terminate the related alarm, if it was active
        if (inFault) {
            this.amSender.terminate(tag, ems, address.getAlarmTriplet(), System.currentTimeMillis());
        }

        if (shandle != null) {
            shandle.stopMonitoring();
            shandle = null;
        }
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        LOG.debug("ParameterException caught: {} ", exception.getMessage(), exception);

        if (exception instanceof SubscriptionRecoveredException) {
            LOG.debug("SubscriptionRecoveredException caught for parameter: {}", parameterName);
            return;
        }

        String deviceName = address.getDevice();
        String reason = getErrorMessage(exception);

        synchronized (deviceName.intern()) {

            // device access alarm not yet active or active but the reason has changed
            if (!deviceAccessFaults.containsKey(deviceName) || (!deviceAccessFaults.get(deviceName).equals(reason))) {
                // send a note to the business layer, to confirm that the equipment is not properly configured,
                // or connected
                LOG.trace("calling ems.confirmEquipmentStateIncorrect() with description: {}", reason);
                this.ems.confirmEquipmentStateIncorrect(reason);
                deviceAccessFaults.put(deviceName, reason);
            }// if

        } // synchronized

        // invalidate tag
        this.ems.sendInvalidTag(this.tag, SourceDataQuality.DATA_UNAVAILABLE, reason);
    }

    /**
     * This method is called by the subclasses whenever data is received. Its goal is to cancel the access fault alarm
     * if one was previously activated
     */
    protected void terminateDeviceAccessFault() {

        String deviceName = address.getDevice();
        synchronized (deviceName.intern()) {
            if (deviceAccessFaults.containsKey(deviceName)) {
                // send a note to the business layer, to confirm that the equipment is properly configured, connected to
                // its source and running
                ems.confirmEquipmentStateOK();
                deviceAccessFaults.remove(deviceName);
            }
        }
    }

    protected static String getErrorMessage(ParameterException e) {
        String defaultMsg = ERROR_SERVER_UNREACHABLE;
        if (e.getMessage() != null) {
            if (e.getMessage().contains("is not known by naming server")) {
                return ERROR_UNKNOWN_DEVICE;
            }
        }

        return defaultMsg;
    }
}