package cern.c2mon.daq.clic;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cern.dmn2.agentlib.AgentClient;
import cern.dmn2.agentlib.AgentCommunicationException;
import cern.dmn2.agentlib.AgentContext;
import cern.dmn2.agentlib.AgentListener;
import cern.dmn2.agentlib.AgentMessage;
import cern.dmn2.agentlib.CommandType;
import cern.dmn2.agentlib.CommunicationListener;
import cern.dmn2.agentlib.FieldDataType;
import cern.dmn2.agentlib.MessageBody;
import cern.dmn2.agentlib.MessageHeader;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.driver.common.EquipmentMessageHandler;
import cern.tim.driver.common.ICommandRunner;
import cern.tim.driver.common.conf.equipment.ICommandTagChanger;
import cern.tim.driver.common.conf.equipment.IDataTagChanger;
import cern.tim.driver.tools.TIMDriverSimpleTypeConverter;
import cern.tim.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.common.datatag.address.JAPCHardwareAddress;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for communication with CLIC agents over JMS
 */
public class ClicMessageHandler extends EquipmentMessageHandler implements ICommandRunner, IDataTagChanger,
        ICommandTagChanger, Runnable {

    public static final String DEFAULT_TIMESTAMP_FIELD = "ts";
    public static final String DEFAULT_DETAILS_FIELD = "details";

    public static final String ACQUISITION_PROPERTY = "Acquisition";

    protected static int RECONNECTION_TIME = 30000; // in ms

    // reference to the static agent client instance, shared across all instances of the ClicMessageHandler
    private static AgentClient client;

    /**
     * reference to the initialization thread
     */
    private Thread initThread;

    /**
     * a map keeping for each tag, its AgentListeners
     */
    private Map<Long, ClicAgentListener> clientAgentListenersMap = new ConcurrentHashMap<Long, ClicAgentListener>();

    /**
     * a map keeping list of references to registered Acquisition property AgentListeners , for each of the registered
     * devices
     */
    private Map<String, List<ClicAgentListener>> agentAcquisittionLstenersMap = new ConcurrentHashMap<String, List<ClicAgentListener>>();

    // logger is initialized in the connectToDataSource() method
    private EquipmentLogger logger;

    private CommunicationListener communicationListener = new ClicCommunicationValueListener();

    /**
     * This class implements a JAPC listener. JAPC listener is called when a value of a parameter changes, or a problem
     * (such as disconnection) is detected
     */
    class ClicCommunicationValueListener implements CommunicationListener {

        @Override
        public void connectionLost(String reason) {
            String msg = "connection to JMS broker lost : " + reason;
            logger.warn(msg);
            ClicMessageHandler.this.getEquipmentMessageSender().confirmEquipmentStateIncorrect(msg);
        }

        @Override
        public void connectionEstablished() {
            String msg = "connection to JMS broker recovered";
            getEquipmentMessageSender().confirmEquipmentStateOK(msg);
        }

    }

    class ClicAgentListener implements AgentListener {

        ISourceDataTag tag;
        JAPCHardwareAddress addr;

        public ClicAgentListener(ISourceDataTag tag) {
            this.tag = tag;
            this.addr = (JAPCHardwareAddress) tag.getHardwareAddress();
        }

        @Override
        public void onMessage(AgentMessage msg) {
            if (logger.isTraceEnabled())
                logger.trace("AgentMessage received :" + msg.toString());
            try {

                String timestampFieldName = format("%s.%s", addr.getDataFieldName(), DEFAULT_TIMESTAMP_FIELD);
                String detailsFieldName = format("%s.%s", addr.getDataFieldName(), DEFAULT_DETAILS_FIELD);

                MessageHeader header = msg.getHeader();
                MessageBody body = msg.getBody();

                if (body.size() > 0) {

                    // get the simple value for the map

                    Object value = body.get(addr.getDataFieldName());
                    Object timestampValue = body.get(timestampFieldName);
                    Object detailsValue = body.get(detailsFieldName);

                    if (value == null) {
                        String errMessage = String.format(
                                "Field: %s missing in the map. Please check your configuration.",
                                addr.getDataFieldName());
                        throw new IndexOutOfBoundsException(errMessage);
                    }

                    // if timestamp field is provided - take the timestamp value from the source,
                    // otherwise - take the system's time
                    long timestamp = timestampValue == null ? System.currentTimeMillis() : (Long) timestampValue;

                    if (detailsValue != null) {
                        convertAndSend(tag, value, detailsValue.toString(), timestamp);
                    } else {
                        convertAndSend(tag, value, null, timestamp);
                    }

                }// if body.size() > 0
                else {
                    logger.info(format("received an empty map for parameter: %s (missing initial update?)",
                            header.getAgentDeviceName() + "/" + header.getAgentProperty()));
                }
            } catch (Exception e) {
                getEquipmentLogger().warn(
                        "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                                + tag.getName() + " id : " + tag.getId() + " Problem: " + e.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        e.getMessage());
            }

        }

    }

    @SuppressWarnings("unused")
    @Override
    public void connectToDataSource() throws EqIOException {
        logger = getEquipmentLogger();
        logger.trace("entering connectToDataSource()..");

        // register handler as command runner
        getEquipmentCommandHandler().setCommandRunner(this);
        // register handler as data tag changer
        getEquipmentConfigurationHandler().setDataTagChanger(this);

        // register handler as command tag changer
        getEquipmentConfigurationHandler().setCommandTagChanger(this);

        // initialize the client instance
        client = AgentClient.getInstance();

        // create initialization thread
        initThread = new Thread(this);

        // start the initialization thread
        initThread.start();

        logger.trace("leaving connectToDataSource()");
    }

    @Override
    public void run() {

        boolean connected = false;
        boolean commfaultSent = false;

        while (!connected) {

            try {
                client.connect();
                connected = true;
            } catch (AgentCommunicationException e) {
                connected = false;

                if (!commfaultSent) {
                    getEquipmentMessageSender().confirmEquipmentStateIncorrect(
                            "could not connect to JMS broker : " + e.getMessage());
                }

                try {
                    Thread.sleep(RECONNECTION_TIME);
                } catch (InterruptedException ex) {
                    logger.warn("InterruptedException caught ", ex);
                }
            }
        }

        // so far so good
        getEquipmentMessageSender().confirmEquipmentStateOK();

        // register communication listener
        client.registerCommunicationListener(communicationListener);

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            try {
                registerTag(tag);
            } catch (TagOperationException ex) {
                getEquipmentLogger().error(ex.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, ex.getMessage());
            }
        }

        // get initial update
        getInitialAcquisitionUpdate();
    }// run

    /**
     * Registers new tag. starts subscription etc..
     * 
     * @param tag
     */
    protected void registerTag(ISourceDataTag tag) throws TagOperationException {
        if (logger.isTraceEnabled())
            logger.trace(format("entering registerTag(%d)", tag.getId()));

        // check if there's any listener registered already for that tag
        AgentListener regListener = this.clientAgentListenersMap.get(tag.getId());

        // none of the above should be present
        if (regListener != null) {
            throw new TagOperationException(format("tag: %d is already registered. You must unregister it first!",
                    tag.getId()));
        }

        try {
            JAPCHardwareAddress addr = (JAPCHardwareAddress) tag.getHardwareAddress();

            String device = addr.getDeviceName();
            String property = addr.getPropertyName();

            if (logger.isDebugEnabled())
                logger.debug(format("creating subscription for tag: %d (device/property: %s/%s)", tag.getId(), device,
                        property));

            ClicAgentListener listener = new ClicAgentListener(tag);

            client.subscribe(device, property, listener);

            if (logger.isDebugEnabled())
                logger.debug(format("successfully subscribed to tag: %d (device/property: %s/%s)", tag.getId(), device,
                        property));

            this.clientAgentListenersMap.put(tag.getId(), listener);

            if (property.equals(ACQUISITION_PROPERTY)) {
                if (!this.agentAcquisittionLstenersMap.containsKey(device)) {
                    this.agentAcquisittionLstenersMap.put(device, new ArrayList<ClicAgentListener>());
                }

                List<ClicAgentListener> clist = this.agentAcquisittionLstenersMap.get(device);
                synchronized (clist) {
                    clist.add(listener);
                }
            }

        } catch (Exception ex) {

            String err = format("Problem desc: %s", ex.getMessage());
            getEquipmentLogger().error(ex.getMessage(), ex);

            throw new TagOperationException(tag.getId(), err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving registerTag(%d)", tag.getId()));
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
        ClicAgentListener listener = this.clientAgentListenersMap.get(tag.getId());

        try {
            if (listener != null) {
                client.unregisterLinstener(listener);
            } else {
                logger.warn(format("tag %d is not registerd", tag.getId()));
            }
            this.clientAgentListenersMap.remove(tag.getId());

        } catch (Exception ex) {
            String err = format("Unable to unregister listener tag: %d. Problem description: %s", tag.getId(),
                    ex.getMessage());
            logger.error(err, ex);
            throw new TagOperationException(err);
        }

        finally {
            if (logger.isTraceEnabled())
                logger.trace(format("leaving unregisterTag(%d)", tag.getId()));
        }
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

        this.clientAgentListenersMap.clear();

        client.unregisterCommunicationListener(communicationListener);

        getEquipmentLogger().debug("leaving diconnectFromDataSource()");
    }

    protected final void convertAndSend(final ISourceDataTag tag, final Object sValue, final String valueDescription,
            final long sourceTimestamp) {
        getEquipmentLogger().debug("enetring sendJAPCSValueFromScalar()..");

        Object value4send = null;

        if (sValue instanceof Number) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, (Number) sValue);
        } else {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, sValue.toString());
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

    protected final String sendCommand(SourceCommandTagValue p0) throws EqCommandTagException {
        logger.trace("entering sendCommand()..");

        String result = null;

        ISourceCommandTag sct = getEquipmentConfiguration().getSourceCommandTags().get(p0.getId());

        if (sct == null) {
            throw new EqCommandTagException(String.format(
                    "command #%d is not registered. Please check DAQ configuration", p0.getId()));
        }

        JAPCHardwareAddress addr = (JAPCHardwareAddress) sct.getHardwareAddress();

        String dataField = addr.getDataFieldName();
        if (dataField != null && dataField.length() == 0) {
            dataField = null;
        }

        String device = addr.getDeviceName();
        String property = addr.getPropertyName();

        AgentMessage feedback = null;

        switch (addr.getCommandType()) {

        case SET:
            logger.debug("executing SET command..");
            throw new EqCommandTagException("SET commands are currently NOT supported");

        case GET:

            getEquipmentLogger().debug("executing GET command..");
            try {

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

                    AgentContext context = new AgentContext();

                    for (int i = 0; i < fields.length; i++) {

                        // check if a value is an array or not
                        String v = values[i].trim();

                        String strVal = v;

                        // if value is an array of strings ( this should be indicated by curly brackets ) - get rid of
                        // the brackets
                        if (v.startsWith("{") && v.endsWith("}")) {
                            strVal = v.substring(1, v.length() - 1);
                        }

                        context.add(fields[i], FieldDataType.TYPE_STRING, strVal);
                    }

                    feedback = client.sendCommand(device, property, CommandType.GET, context);

                } else { // no context-field
                    feedback = client.sendCommand(device, property, CommandType.GET);
                }

                if (feedback != null) {
                    // check if the response is correctly set
                    if (feedback.getBody().containsKey(MessageBody.COMMAND_RESPONSE_KEY)) {
                        result = feedback.getBody().get(MessageBody.COMMAND_RESPONSE_KEY).toString();
                    } else {
                        throw new EqCommandTagException("Command reply received from CLIC agent is missing field: "
                                + MessageBody.COMMAND_RESPONSE_KEY.toString());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                throw new EqCommandTagException("command execution failed. Problem: " + e.getMessage());
            }
            break;

        default:

            throw new EqCommandTagException(String.format(
                    "command #%d has unknown type. Only SET and GET commands are supported", p0.getId()));

        }// switch

        logger.trace("leaving sendCommand()");
        return result;
    }

    @Override
    public final String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        return sendCommand(sourceCommandTagValue);
    }

    // public final String runCommand(Long commandId) throws EqCommandTagException {
    // ISourceCommandTag command = getEquipmentConfiguration().getSourceCommandTag(commandId);
    // if (command == null) {
    // return format("command %d is unknown", command);
    // }
    //
    // SourceCommandTagValue ctv = new SourceCommandTagValue(commandId, command.getName(), this
    // .getEquipmentConfiguration().getId(), (short) 0, "dummy", "java.lang.String");
    //
    // return sendCommand(ctv);
    // }

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

    private void getInitialAcquisitionUpdate() {
        if (logger.isTraceEnabled()) {
            logger.trace("entering getInitialAcquisitionUpdate()..");
        }

        for (Map.Entry<String, List<ClicAgentListener>> entry : this.agentAcquisittionLstenersMap.entrySet()) {
            String device = entry.getKey();

            try {
                AgentMessage initValue = client.sendCommand(device, ACQUISITION_PROPERTY, CommandType.GET);
                List<ClicAgentListener> listeners = entry.getValue();
                for (ClicAgentListener listener : listeners) {
                    listener.onMessage(initValue);
                }
            } catch (AgentCommunicationException e) {
                logger.error("could not get initial value for device/parameter: %" + device + "/"
                        + ACQUISITION_PROPERTY);
            }
        }// for

        if (logger.isTraceEnabled()) {
            logger.trace("leaving getInitialAcquisitionUpdate()");
        }
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Auto-generated method stub

    }

    /**
     * @param dataTagId
     */
    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Auto-generated method stub

    }

}