/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.almon.address.AlarmTripplet;
import cern.c2mon.daq.almon.address.AlmonHardwareAddress;
import cern.c2mon.daq.almon.address.UserProperties;
import cern.c2mon.daq.almon.sender.AlmonSender;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
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

    // protected AlarmTripplet activeAlarmTripplet;

    /**
     * Encapsulates information about active device access fault
     */
    static class DeviceAccessFault {
        private final AlarmTripplet devAccessFaultTripplet;

        // properties may be updated
        private UserProperties devAccessFaultProps;

        public DeviceAccessFault(AlarmTripplet tripplet, UserProperties props) {
            this.devAccessFaultTripplet = tripplet;
            this.devAccessFaultProps = props;
        }

        public AlarmTripplet getDevAccessFaultTripplet() {
            return devAccessFaultTripplet;
        }

        public void setDevAccessProps(UserProperties props) {
            this.devAccessFaultProps = props;
        }

        public UserProperties getDevAccessFaultProps() {
            return devAccessFaultProps;
        }
    }

    protected static Map<String, DeviceAccessFault> deviceAccessFaults = new ConcurrentHashMap<String, DeviceAccessFault>();
    protected static Map<String, AtomicInteger> parametersPerDevice = new ConcurrentHashMap<String, AtomicInteger>();

    protected JapcParameterHandler(ISourceDataTag tag, AlmonHardwareAddress hwAddress, IEquipmentMessageSender ems,
            AlmonSender amSender) {
        this.tag = tag;
        this.address = hwAddress;
        this.ems = ems;
        this.amSender = amSender;
    }

    /**
     * The startMonitoring() must be called in order for the parameter to get subscribed
     */
    public void startMonitoring() {
        try {

            String deviceName = address.getAlarmTripplet().getFaultMember();

            // increment
            synchronized (deviceName.intern()) {
                if (!parametersPerDevice.containsKey(deviceName)) {
                    parametersPerDevice.put(deviceName, new AtomicInteger(1));
                } else {
                    parametersPerDevice.get(deviceName).incrementAndGet();
                }
            }// synchronized

            Parameter p = ParameterFactory.newInstance().newParameter(address.getDevice(), address.getProperty());

            Selector selector = null;

            if (address.hasCycle()) {
                selector = ParameterValueFactory.newSelector(address.getCycle());
            }
            shandle = p.createSubscription(selector, this);
            shandle.startMonitoring();

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
        shandle.stopMonitoring();
        shandle = null;

        // terminate the related alarm, if it was active
        if (inFault) {
            // amSender.terminate(address.getAlarmTripplet(), System.currentTimeMillis());
        }

        // terminate device access fault alarm if
        // it is active && there are no more parameters linked to that device.
        // (i.e. if there are more than 1 parameters belonging to that device and the access fault is active it will NOT
        // be terminated
        String deviceName = address.getDevice();

        synchronized (deviceName.intern()) {
            if (parametersPerDevice.containsKey(deviceName)) {
                if (parametersPerDevice.get(deviceName).get() > 0) {
                    parametersPerDevice.get(deviceName).decrementAndGet();
                }

                if (parametersPerDevice.get(deviceName).get() == 0) {
                    // terminate device access fault - if active
                    terminateDeviceAccessFault();
                }
            }
        }// synchronized

    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException exception) {
        // LOG.info("exception caught for parameter: {}  description: {}  ex-message: {}", parameterName, description,
        // exception.getMessage());
        LOG.debug("exception trace:", exception);

        if (exception instanceof SubscriptionRecoveredException) {
            LOG.debug("SubscriptionRecoveredException caught for parameter: {}", parameterName);
            return;
        }

        String reason = getErrorMessage(exception);

        String deviceName = address.getDevice();
        synchronized (deviceName.intern()) {
            // device access alarm not yet active
            if (!deviceAccessFaults.containsKey(deviceName)) {

                AlarmTripplet deviceAlarmTripplet = new AlarmTripplet(ALARM_MON_FAULT_FAMILY, deviceName,
                        ALARM_MON_FAULT_IN_ERROR);

                UserProperties props = new UserProperties();
                props.setProperty(ASI_PREFIX_PROPERTY, reason);
                props.setProperty(ALMON_FAULT_PROPERTY_TAG, exception.getMessage());
                DeviceAccessFault fault = new DeviceAccessFault(deviceAlarmTripplet, props);

                // send comfault tag, indicating the equipment is down
                this.ems.confirmEquipmentStateIncorrect(reason);
                deviceAccessFaults.put(deviceName, fault);
            } else {

                DeviceAccessFault fault = deviceAccessFaults.get(deviceName);

                UserProperties props = new UserProperties();
                props.setProperty(ASI_PREFIX_PROPERTY, reason);
                props.setProperty(ALMON_FAULT_PROPERTY_TAG, exception.getMessage());

                // update already active alarm if the properties have changed
                if (!props.equals(fault.getDevAccessFaultProps())) {
                    AlarmTripplet tripplet = fault.getDevAccessFaultTripplet();
                    // this.amSender.update(tag, System.currentTimeMillis(), props); // update(tripplet,
                    // System.currentTimeMillis(), props);
                    fault.setDevAccessProps(props);
                } else {
                    LOG.debug("Skipping repeated error for parameter: {}", parameterName);
                }

            }
        } // synchronized
    }

    /**
     * This method is called by the subclasses whenever data is received. Its goal is to cancel the access fault alarm
     * if one was previously activated
     */
    protected void terminateDeviceAccessFault() {

        String deviceName = address.getDevice();
        synchronized (deviceName.intern()) {
            if (deviceAccessFaults.containsKey(deviceName)) {
                AlarmTripplet deviceAlarmTripplet = deviceAccessFaults.get(deviceName).getDevAccessFaultTripplet();

                //this.amSender.terminate(deviceAlarmTripplet, System.currentTimeMillis());
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