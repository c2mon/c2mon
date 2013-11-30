package cern.c2mon.daq.japc;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValue;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.SubscriptionHandle;
import cern.japc.SubscriptionProblemException;
import cern.japc.SubscriptionRecoveredException;
import cern.japc.Type;
import cern.japc.ValueHeader;
import cern.japc.ValueType;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;
import cern.japc.spi.ParameterUrl;
import cern.japc.spi.ParameterUrlImpl;
import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for TIM DAQ for JAPC protocol.
 */
public class GenericJapcMessageHandler extends EquipmentMessageHandler implements ICommandRunner, IDataTagChanger,
        ICommandTagChanger, Runnable {

    public static final String DEFAULT_PROTOCOL = "rda";
    public static final String DEFAULT_SERVICE = "rda";

    public static final String DEFAULT_TIMESTAMP_FIELD = "ts";
    public static final String DEFAULT_DETAILS_FIELD = "details";

    protected static int MIN_RECONNECTION_TIME = 1 * 60; // 5 * 60; // in seconds
    protected static int MAX_RECONNECTION_TIME = 30 * 60; // in seconds
    protected static int RECONNECTION_TIME_STEP = 1 * 60; // 5 * 60; // in seconds;

    // protected static int RECONNECTION_THREAD_POOL_SIZE = 16;

    protected static int RECONNECTION_THREAD_POOL_SIZE = 100;

    /**
     * JAPC parameter factory instance.
     */
    protected ParameterFactory parameterFactory;

    /**
     * a map keeping subscription handles
     */
    private Map<Long, SubscriptionHandle> handles = new ConcurrentHashMap<Long, SubscriptionHandle>();

    /**
     * a map keeping for each tag, its ParameterValueListener
     */
    private Map<Long, ParameterValueListener> pvlistenersMap = new ConcurrentHashMap<Long, ParameterValueListener>();

    private static volatile TagConnectionMonitor tagConnectionMonitor; // = new TagConnectionMonitor();

    class JapcHandlerValueListener implements ParameterValueListener {

        private final ISourceDataTag tag;

        public JapcHandlerValueListener(final ISourceDataTag tag) {
            this.tag = tag;
        }

        @Override
        public void exceptionOccured(String parameterId, String description, ParameterException e) {

            if (getEquipmentLogger().isDebugEnabled())
                getEquipmentLogger().debug(
                        "exceptionOccured() : ParameterException caught: " + e.getClass() + " with message "
                                + e.getMessage());

            if (e instanceof SubscriptionRecoveredException) {
                // don't do anything, just log it - we need to wait for the new values to come anyway
                if (getEquipmentLogger().isDebugEnabled())
                    if (getEquipmentLogger().isDebugEnabled())
                        getEquipmentLogger().debug("SubscriptionRecoveredException caught, connection's back", e);

                // don't do anything here, it should be the japc extension job to resent the latest value
                // after reconnection

            } else {

                // invalidate the tag
                handleJAPCException(tag, description);

                if (e instanceof SubscriptionProblemException) {

                    if (getEquipmentLogger().isDebugEnabled())
                        if (getEquipmentLogger().isDebugEnabled())
                            getEquipmentLogger().debug("SubscriptionProblemException caught, connection's lost", e);
                }
            }
        }

        @Override
        public void valueReceived(String parameterId, AcquiredParameterValue parameterValue) {
            handleJAPCValue(tag, parameterId, parameterValue);
        }
    }

    /**
     * This class implements a mechanism of periodic re-subscription for tags that failed to subscribe correctly at
     * start-up
     */
    class TagConnectionMonitor {

        private ScheduledExecutorService executor = Executors.newScheduledThreadPool(RECONNECTION_THREAD_POOL_SIZE);
        private ConcurrentMap<Long, Future<?>> futures = new ConcurrentHashMap<Long, Future<?>>();

        private ConcurrentMap<Long, Integer> reconnectInMap = new ConcurrentHashMap<Long, Integer>();

        class TagReconnectionTask implements Runnable {

            private ISourceDataTag tag = null;

            public TagReconnectionTask(ISourceDataTag tag) {
                this.tag = tag;
            }

            @Override
            public void run() {

                try {

                    if (getEquipmentLogger().isDebugEnabled())
                        getEquipmentLogger().debug(format("calling registerTag(%d)", tag.getId()));

                    registerTag(tag);

                    // we're connected fine, reset the reconnection time
                    reconnectInMap.put(tag.getId(), MIN_RECONNECTION_TIME);

                } catch (TagOperationException ex) {
                    if (getEquipmentLogger().isDebugEnabled())
                        getEquipmentLogger().debug(format("registerTag(%d) failed", tag.getId()));

                    int reconnectIn = reconnectInMap.get(tag.getId());
                    if (reconnectIn < MAX_RECONNECTION_TIME) {
                        reconnectIn += RECONNECTION_TIME_STEP;
                        reconnectInMap.put(tag.getId(), reconnectIn);
                    }

                    // re-schedule reconnection task for this tag
                    add(tag);

                } catch (Exception e) {
                    getEquipmentLogger().warn(e);
                }
            }// run

        }

        public void add(ISourceDataTag tag) {

            if (getEquipmentLogger().isDebugEnabled()) {

                if (!reconnectInMap.containsKey(tag.getId())) {
                    reconnectInMap.put(tag.getId(), MIN_RECONNECTION_TIME);
                }

                int reconnectIn = reconnectInMap.get(tag.getId());

                if ((reconnectIn / 60) > 1.0f) {
                    getEquipmentLogger()
                            .debug(format(
                                    "scheduling re-connection task for tag %d, name: %s The task will run in %d min %d sec.",
                                    tag.getId(), tag.getName(), Math.round(Math.floor(reconnectIn / 60)),
                                    reconnectIn % 60));
                } else {
                    getEquipmentLogger().debug(
                            format("scheduling re-connection task for tag %d, name: %s The task will run in %d sec.",
                                    tag.getId(), tag.getName(), reconnectIn));
                }
            }

            if (futures.get(tag.getId()) != null) {
                Future<?> f = futures.get(tag.getId());
                if (!f.isDone()) {
                    f.cancel(true);
                }

                futures.remove(tag.getId());
            }

            getEquipmentLogger().info(format("Current futures size: " + futures.size()));

            futures.put(tag.getId(),
                    executor.schedule(new TagReconnectionTask(tag), reconnectInMap.get(tag.getId()), TimeUnit.SECONDS));
        }

        public void remove(ISourceDataTag tag) {
            if (futures.containsKey(tag.getId())) {
                Future<?> f = futures.get(tag.getId());
                if (getEquipmentLogger().isDebugEnabled())
                    getEquipmentLogger().debug(format("cancelling re-connection task for tag %d.", tag.getId()));
                f.cancel(true);
            }
        }

        public synchronized void stop() {
            this.executor.shutdownNow();
        }

    }

    protected GenericJapcMessageHandler(boolean createTagConnectionMonitor) {
        if (createTagConnectionMonitor) {
            if (tagConnectionMonitor == null) {
                tagConnectionMonitor = new TagConnectionMonitor();
            }
        }
    }

    public GenericJapcMessageHandler() {
        if (tagConnectionMonitor == null) {
            tagConnectionMonitor = new TagConnectionMonitor();
        }
    }

    /**
     * this method can be overridden by inheriting classes, if needed Default implementation is empty
     */
    protected void beforeConnectToDataSource() throws EqIOException {

    }

    @Override
    public void connectToDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering connectToDataSource()..");

        this.beforeConnectToDataSource();

        // register handler as command runner
        getEquipmentCommandHandler().setCommandRunner(this);
        // register handler as data tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);

        // register handler as command tag changer
        getEquipmentConfigurationHandler().setCommandTagChanger(this);

        // If this is the first time this method is called (on start-up), create
        // a JAPC parameter factory
        if (this.parameterFactory == null) {
            try {
                this.parameterFactory = ParameterFactory.newInstance();
                // We do not really have an Equipment but it indicates at least that
                // the factory creates went fine.
                getEquipmentMessageSender().confirmEquipmentStateOK();
            } catch (Exception e) {
                getEquipmentMessageSender().confirmEquipmentStateIncorrect(
                        "Unexpected problem occured when trying to create a JAPC ParameterFactory instance");

                getEquipmentLogger()
                        .error("connectToDataSource() : Unexpected problem occured when trying to create a JAPC ParameterFactory",
                                e);
                throw new EqIOException("Unexpected problem occured while creating instance of ParameterFactory: "
                        + e.getMessage());
            }
        }

        new Thread(this).start();

        getEquipmentLogger().debug("leaving connectToDataSource()");
    }

    @Override
    public void run() {
        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            try {
                registerTag(tag);
            } catch (TagOperationException ex) {
                getEquipmentLogger().error(ex.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, ex.getMessage());

                tagConnectionMonitor.add(tag);
            }
        }
    }// run

    /**
     * Registers new tag. starts subscription etc..
     * 
     * @param tag
     */
    protected void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (getEquipmentLogger().isTraceEnabled())
            getEquipmentLogger().trace(format("entering registerTag(%d)", tag.getId()));

        // check if there's any listener registered already for that tag
        ParameterValueListener regListener = this.pvlistenersMap.get(tag.getId());

        // check if there's handle registered for that tag
        SubscriptionHandle regHandle = this.handles.get(tag.getId());

        // none of the above should be present
        if (regListener != null || regHandle != null) {
            getEquipmentLogger().warn(
                    format("tag: %d is already registered. You must unregister it first!", tag.getId()));
            return;
        }

        SubscriptionHandle handle = null;
        try {
            JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();

            // make sure protocol and service are correctly specified
            String protocol = checkProtocol(addr.getProtocol());
            String service = checkService(addr.getService());

            Parameter parameter = this.parameterFactory.newParameter(new ParameterUrlImpl(protocol, service, addr
                    .getDeviceName(), addr.getPropertyName(), null));

            Selector selector = getJapcSelector(tag);

            getEquipmentLogger().debug(
                    String.format("creating subscription handle for parameter: %s  selector: %s", parameter.getName(),
                            selector == null ? "null" : selector.toString()));

            ParameterValueListener pvl = new JapcHandlerValueListener(tag/* , parameter */);

            handle = parameter.createSubscription(selector, pvl);

            handle.startMonitoring();

            if (getEquipmentLogger().isDebugEnabled())
                getEquipmentLogger().debug(format("successfully subscribed to parameter: %s", parameter.getName()));

            this.handles.put(tag.getId(), handle);
            this.pvlistenersMap.put(tag.getId(), pvl);

        } catch (Exception ex) {

            String err = format("Problem desc: %s", ex.getMessage());
            getEquipmentLogger().error(ex.getMessage(), ex);

            throw new TagOperationException(tag.getId(), err);
        }

        finally {
            if (getEquipmentLogger().isTraceEnabled())
                getEquipmentLogger().trace(format("leaving registerTag(%d)", tag.getId()));
        }
    }

    /**
     * @param tag
     * @throws TagOperationException
     */
    protected void unregisterTag(ISourceDataTag tag) throws TagOperationException {
        if (getEquipmentLogger().isTraceEnabled())
            getEquipmentLogger().trace(format("entering unregisterTag(%d)", tag.getId()));

        // check if there's handle registered for that tag
        SubscriptionHandle regHandle = this.handles.get(tag.getId());

        try {
            if (regHandle != null) {
                regHandle.stopMonitoring();
                tagConnectionMonitor.remove(tag);
            } else {
                getEquipmentLogger().warn(format("reg handle for tag: %d was null", tag.getId()));
            }
            this.handles.remove(tag.getId());
            this.pvlistenersMap.remove(tag.getId());

        } catch (Exception ex) {
            String err = format("Unable to stop monitoring for tag: %d. Problem description: %s", tag.getId(),
                    ex.getMessage());
            getEquipmentLogger().error(err, ex);
            throw new TagOperationException(err);
        }

        finally {
            if (getEquipmentLogger().isTraceEnabled())
                getEquipmentLogger().trace(format("leaving unregisterTag(%d)", tag.getId()));
        }
    }

    protected static String checkProtocol(final String protocol) {
        if (protocol == null || protocol.length() == 0)
            return DEFAULT_PROTOCOL;
        else
            return protocol;
    }

    protected static String checkService(final String service) {
        if (service == null || service.length() == 0)
            return DEFAULT_SERVICE;
        else
            return service;
    }

    @Override
    @SuppressWarnings("unused")
    public void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering diconnectFromDataSource()..");

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            try {
                unregisterTag(tag);
            } catch (TagOperationException ex) {
                getEquipmentLogger().warn(ex.getMessage());
            }
        }// for

        this.handles.clear();
        this.pvlistenersMap.clear();

        // if (this.tagConnectionMonitor != null)
        // this.tagConnectionMonitor.stop();

        getEquipmentLogger().debug("leaving diconnectFromDataSource()");
    }

    /**
     * this is the default implementation of the handleJAPCValue method. For more specific JAPC handlers this method
     * should be overwritten
     * 
     * @param tag
     * @param pParameterName
     * @param pParameterValue
     */
    protected void handleJAPCValue(final ISourceDataTag tag, final String pParameterName,
            final AcquiredParameterValue pParameterValue) {
        ParameterValue value = pParameterValue.getValue();
        Type type = value.getType();

        // ValueHeader header = pParameterValue.getHeader();
        JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): update received for parameter: %s", pParameterName));
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug(
                    String.format("handleJAPCValue(): value of type: %s received", value.getType().toString()));
        }

        if (type == Type.SIMPLE) {

            getEquipmentLogger().debug("\tupdate type : SIMPLE");

            SimpleParameterValue simpleValue = (SimpleParameterValue) value;

            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t the value-type: " + simpleValue.getValueType().toString());
            }

            ValueType valueType = simpleValue.getValueType();

            if (valueType.isScalar()) {
                try {
                    sendJAPCSValueFromScalar(tag, simpleValue, "", System.currentTimeMillis());
                } catch (Exception ex) {
                    getEquipmentLogger().error("handleJAPCValue() : " + ex.getMessage(), ex);
                    getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNKNOWN, ex.getMessage());
                }
            }

        } else if (type == Type.MAP) {
            MapParameterValue mapValue = (MapParameterValue) value;

            try {

                String timestampFieldName = format("%s.%s", addr.getDataFieldName(), DEFAULT_TIMESTAMP_FIELD);
                String detailsFieldName = format("%s.%s", addr.getDataFieldName(), DEFAULT_DETAILS_FIELD);

                // get the simple value for the map

                if (mapValue.size() > 0) {

                    SimpleParameterValue svalue = mapValue.get(addr.getDataFieldName());
                    SimpleParameterValue timestampValue = mapValue.get(timestampFieldName);
                    SimpleParameterValue detailsValue = mapValue.get(detailsFieldName);

                    if (svalue == null) {
                        String errMessage = String.format(
                                "Field: %s missing in the map. Please check your configuration.",
                                addr.getDataFieldName());
                        throw new IndexOutOfBoundsException(errMessage);
                    }

                    // if timestamp field is provided - take the timestamp value from it, otherwise - take the
                    // system's time
                    long timestamp = timestampValue == null ? System.currentTimeMillis() : timestampValue.getLong();

                    if (detailsValue != null) {
                        sendJAPCSValueFromScalar(tag, svalue, detailsValue.getString(),
                                convertSourceTimestampToMs(timestamp));
                    } else {
                        sendJAPCSValueFromScalar(tag, svalue, null, convertSourceTimestampToMs(timestamp));
                    }

                }// if mapValue.size() > 0
                else {
                    getEquipmentLogger()
                            .info(format("received an empty map for parameter: %s (missing initial update?)",
                                    pParameterName));
                }
            } catch (Exception e) {
                getEquipmentLogger().warn(
                        "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                                + tag.getName() + " id : " + tag.getId() + " Problem: " + e.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        e.getMessage());
            }

        } else {
            String errorMsg = String.format("Type \"%s\" is not supported", type.toString());
            getEquipmentLogger().error("\t" + errorMsg);
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            return;
        }

    }

    /**
     * // TODO: this method should return true, temporarily returns false because of the bug in the japc-mockito
     * (otherwise the tests would fail)
     * 
     * @return
     */
    protected boolean isSelectorOnChangeEnabled() {
        return false;
    }

    protected final void handleJAPCException(final ISourceDataTag tag, final String pDescription) {
        getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, pDescription);
    }

    protected final void sendJAPCSValueFromScalar(final ISourceDataTag tag, final SimpleParameterValue sValue,
            final String valueDescription, final long sourceTimestamp) {
        getEquipmentLogger().debug("enetring sendJAPCSValueFromScalar()..");

        Object value4send = null;
        ValueType valueT = sValue.getValueType();

        // do not convert - conversion is now done by the daq core!
        if (valueT == ValueType.BOOLEAN) {
            value4send = sValue.getBoolean(); 
        } else if (valueT == ValueType.BYTE) {
            value4send = sValue.getByte(); 
        } else if (valueT == ValueType.INT) {
            value4send = sValue.getInt(); 
        } else if (valueT == ValueType.LONG) {
            value4send = sValue.getLong(); 
        } else if (valueT == ValueType.FLOAT) {
            value4send = sValue.getFloat(); 
        } else if (valueT == ValueType.DOUBLE) {
            value4send = sValue.getDouble(); 
        } else if (valueT == ValueType.STRING) {
            value4send = sValue.getString(); 
        }

        if (value4send != null) {
            // send the value to the server
            getEquipmentMessageSender().sendTagFiltered(tag, value4send, sourceTimestamp, valueDescription);
        } else {
            getEquipmentLogger().info(
                    "\tInvalidating SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + tag.getName()
                            + " id : " + tag.getId());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
        }

        getEquipmentLogger().debug("leaving sendJAPCSValueFromScalar()");
    }

    protected final void sendJAPCSValueFromArray(final ISourceDataTag tag, final SimpleParameterValue simpleValue,
            final ValueType valueType, final ValueHeader header, final int index) {
        this.sendJAPCSValueFromArray(tag, simpleValue, valueType,
                convertSourceTimestampToMs(header.getAcqStampMillis()), index);
    }

    protected final void sendJAPCSValueFromArray(final ISourceDataTag tag, final SimpleParameterValue simpleValue,
            final ValueType valueType, final long timestamp, final int index) {
        getEquipmentLogger().debug("enetring sendJAPCSValueFromArray()..");

        Object value4send = null;

        try {
            
            
            // we don't convert, it is now done by the DAQ core
            if (valueType == ValueType.BOOLEAN_ARRAY) {
                value4send = simpleValue.getBoolean(index);
            } else if (valueType == ValueType.BYTE_ARRAY) {
                value4send = simpleValue.getByte(index);
            } else if (valueType == ValueType.INT_ARRAY) {
                value4send = simpleValue.getInt(index);
            } else if (valueType == ValueType.LONG_ARRAY) {
                value4send = simpleValue.getLong(index);
            } else if (valueType == ValueType.FLOAT_ARRAY) {
                value4send = simpleValue.getFloat(index);
            } else if (valueType == ValueType.DOUBLE_ARRAY) {
                value4send = simpleValue.getDouble(index);
            } else if (valueType == ValueType.STRING_ARRAY) {
                value4send = simpleValue.getString(index);
            }

            if (value4send != null) {
                // send the value to the server
                getEquipmentMessageSender().sendTagFiltered(tag, value4send, timestamp);
            } else {
                getEquipmentLogger().info(
                        "\tInvalidating SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : "
                                + tag.getName() + " id : " + tag.getId());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
            }

        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            getEquipmentLogger().warn("could not read data from an array at index : " + index);
            getEquipmentLogger().info(
                    "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                            + tag.getName() + " id : " + tag.getId());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                    "Could not read data from array at index : " + index);
        }

        getEquipmentLogger().debug("leaving sendJAPCSValueFromArray()");
    }

    protected final String sendCommand(SourceCommandTagValue p0) throws EqCommandTagException {
        getEquipmentLogger().debug("entering sendCommand()..");

        String result = null;

        ISourceCommandTag sct = getEquipmentConfiguration().getSourceCommandTags().get(p0.getId());

        if (sct == null) {
            throw new EqCommandTagException(String.format(
                    "command #%d is not registered. Please check DAQ configuration", p0.getId()));
        }

        JAPCHardwareAddress addr = (JAPCHardwareAddress) sct.getHardwareAddress();
        ParameterUrl pUrl = null;

        // make sure protocol and service are correctly specified
        String protocol = checkProtocol(addr.getProtocol());
        String service = checkService(addr.getService());

        String dataField = addr.getDataFieldName();
        if (dataField != null && dataField.length() == 0) {
            dataField = null;
        }

        pUrl = new ParameterUrlImpl(protocol, service, addr.getDeviceName(), addr.getPropertyName(), dataField);

        Selector selector = null;

        switch (addr.getCommandType()) {

        case SET:

            getEquipmentLogger().debug("executing SET command..");
            try {
                Parameter parameter = ParameterFactory.newInstance().newParameter(pUrl);

                // Create a selector for the parameter
                if (addr.getCycleSelector() != null)
                    selector = ParameterValueFactory.newSelector(addr.getCycleSelector());
                ParameterValue pv = ParameterValueFactory.newParameterValue(p0.getValue());

                if (getEquipmentLogger().isTraceEnabled()) {
                    getEquipmentLogger().trace("before parameter.setValue(selector, pv)");
                }

                parameter.setValue(selector, pv);

                if (getEquipmentLogger().isTraceEnabled()) {
                    getEquipmentLogger().trace("after parameter.setValue(selector, pv)");
                }

            } catch (Exception e) {
                throw new EqCommandTagException("command execution failed. could not set value: " + p0.getValue()
                        + " for parameter: " + pUrl + " Error: " + e.getMessage());
            }

            break;

        case GET:

            getEquipmentLogger().debug("executing GET command..");
            try {

                if (getEquipmentLogger().isTraceEnabled())
                    getEquipmentLogger().debug("before ParameterFactory.newInstance().newParameter()");
                Parameter parameter = ParameterFactory.newInstance().newParameter(pUrl);
                if (getEquipmentLogger().isTraceEnabled())
                    getEquipmentLogger().debug("after ParameterFactory.newInstance().newParameter()");

                AcquiredParameterValue apv = null;

                // if context-field is defined
                if (addr.hasContextField()) {

                    // split the fields passed inside the context
                    // Note: the separator is ; (semicolon), but there may be escape characters present: \;
                    String[] rawfields = addr.getContextField().trim().split("(?<!\\\\);");
                    List<String> fl = new ArrayList<String>();
                    for (String s : rawfields) {
                        fl.add(s.replace("\\;", ";"));
                    }

                    String[] fields = fl.toArray(new String[0]);

                    // split the value passed as an argument of the command
                    // Note: the separator of each values is ; (semicolon), but there may be escape characters present:
                    // \;
                    String[] rawvalues = p0.getValue().toString().trim().split("(?<!\\\\);");
                    List<String> fv = new ArrayList<String>();
                    for (String s : rawvalues) {
                        fv.add(s.replace("\\;", ";"));
                    }

                    String[] values = fv.toArray(new String[0]);

                    // make sure that number of fields matches number of values
                    if (fields.length != values.length)
                        throw new EqCommandTagException(
                                "number of fields in the context does not match number of values for that context");

                    SimpleParameterValue[] spvArray = new SimpleParameterValue[fields.length];

                    for (int i = 0; i < fields.length; i++) {

                        // check if a value is an array or not
                        String v = values[i].trim();

                        // if value is an array of strings ( this should be indicated by curly brackets )
                        if (v.startsWith("{") && v.endsWith("}")) {
                            String[] valsArray = v.substring(1, v.length() - 1).split(",");
                            spvArray[i] = ParameterValueFactory.newParameterValue(valsArray);
                        } else {
                            spvArray[i] = ParameterValueFactory.newParameterValue(values[i]);
                        }
                    }

                    if (getEquipmentLogger().isTraceEnabled())
                        getEquipmentLogger().trace("before creating selector");
                    selector = ParameterValueFactory.newSelector(null,
                            ParameterValueFactory.newParameterValue(fields, spvArray));
                    if (getEquipmentLogger().isTraceEnabled())
                        getEquipmentLogger().trace("after creating selector");

                    if (getEquipmentLogger().isTraceEnabled()) {
                        getEquipmentLogger().trace("before parameter.getValue(selector)");
                    }

                    apv = parameter.getValue(selector);

                    if (getEquipmentLogger().isTraceEnabled()) {
                        getEquipmentLogger().trace("after parameter.getValue(selector)");
                    }

                } else { // no context-field
                    selector = ParameterValueFactory.newSelector(null);
                    apv = parameter.getValue(selector);
                }

                result = apv.getValue().getString();

            } catch (Exception e) {
                throw new EqCommandTagException("command execution failed. could not get value from from parameter: "
                        + pUrl + " Error: " + e.getMessage());
            }
            break;

        default:

            throw new EqCommandTagException(String.format(
                    "command #%d has unknown type. Only SET and GET commands are supported", p0.getId()));

        }// switch

        getEquipmentLogger().debug("leaving sendCommand()");
        return result;
    }

    /**
     * this method is a temporary <<hack>> to solve the problem with JAPC source timestamps delivered often in microsec.
     * istead of ns.
     * 
     * @param sTimeStamp
     * @return
     */
    public static final long convertSourceTimestampToMs(long sTimeStamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1990, 01, 01);

        Date sourceDate = new Date(sTimeStamp);

        // make sure the provided timestamp is not older than 1990-01-01
        if (sourceDate.before(calendar.getTime())) {
            return sTimeStamp * 1000;
        } else
            return sTimeStamp;
    }

    /**
     * This method is used to retrieve the array index for the requested value
     * 
     * @param name the parameter Name
     * @return the array index of the parameter name
     */
    protected final int getIndex(final MapParameterValue mpv, final String parameterName, final String fieldName)
            throws ArrayIndexOutOfBoundsException {
        String[] names = null;
        try {
            names = mpv.getStrings(parameterName);
        } catch (Exception ex) {
            throw new ArrayIndexOutOfBoundsException("field not found");
        }

        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(fieldName)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public final String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        return sendCommand(sourceCommandTagValue);
    }

    public final String runCommand(Long commandId) throws EqCommandTagException {
        ISourceCommandTag command = getEquipmentConfiguration().getSourceCommandTag(commandId);
        if (command == null) {
            return format("command %d is unknown", command);
        }

        SourceCommandTagValue ctv = new SourceCommandTagValue(commandId, command.getName(), this
                .getEquipmentConfiguration().getId(), (short) 0, "dummy", "java.lang.String");

        return sendCommand(ctv);
    }

    /**
     * this method is called when a new DataTag is "injected"
     */
    @Override
    public final void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onAddDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // register tag
        try {
            registerTag(sourceDataTag);
        } catch (TagOperationException ex) {
            // if a problem appears when one wants to add configuration
            changeReport.appendWarn(ex.getMessage());
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onAddDataTag(%d)", sourceDataTag.getId()));
    }

    /**
     * this method is called when a request to remove a DataTag is received
     */
    @Override
    public final void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onRemoveDataTag(%d)..", sourceDataTag.getId()));

        changeReport.setState(CHANGE_STATE.SUCCESS);

        // unregister tag
        try {
            unregisterTag(sourceDataTag);
        } catch (TagOperationException ex) {
            changeReport.appendWarn(ex.getMessage());
        }

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onRemoveDataTag(%d)", sourceDataTag.getId()));
    }

    @Override
    public final void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag,
            ChangeReport changeReport) {
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

    @Override
    public final void onAddCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onAddCommandTag(%d)..", sourceCommandTag.getId()));

        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onAddCommandTag(%d)", sourceCommandTag.getId()));
    }

    @Override
    public final void onRemoveCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("entering onRemoveCommandTag(%d)..", sourceCommandTag.getId()));

        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(format("leaving onRemoveCommandTag(%d)", sourceCommandTag.getId()));
    }

    @Override
    public final void onUpdateCommandTag(ISourceCommandTag sourceCommandTag, ISourceCommandTag oldSourceCommandTag,
            ChangeReport changeReport) {

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("entering onUpdateCommandTag(%d,%d)..", sourceCommandTag.getId(),
                            oldSourceCommandTag.getId()));
        // nothing more to be done here
        changeReport.setState(CHANGE_STATE.SUCCESS);

        if (getEquipmentLogger().isDebugEnabled())
            getEquipmentLogger().debug(
                    format("leaving onUpdateCommandTag(%d,%d)", sourceCommandTag.getId(), oldSourceCommandTag.getId()));

    }

    private Selector getJapcSelector(ISourceDataTag tag) {
        Selector selector = null;
        JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();
        // if filter is defined
        if (addr.hasFilter()) {
            // split the filter ( expected format: key=value )
            String[] filter = addr.getFilter().split("=");
            Map<String, SimpleParameterValue> df = new HashMap<String, SimpleParameterValue>();
            df.put(filter[0].trim(), ParameterValueFactory.newParameterValue(filter[1].trim()));
            ParameterValue dataFilter = ParameterValueFactory.newParameterValue(df);
            selector = ParameterValueFactory.newSelector(null, dataFilter, isSelectorOnChangeEnabled());
        }

        return selector;
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Auto-generated method stub

    }

}