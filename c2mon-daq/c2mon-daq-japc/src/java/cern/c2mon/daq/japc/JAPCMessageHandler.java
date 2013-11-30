// TIM. CERN. All rights reserved.
//  
// T Nick: Date: Info:
// -------------------------------------------------------------------------
// D jstowisek --/May/2006 Implementation of the prototype
// P wbuczak 23/May/2006 handler implementation
// P wbuczak 25/Jun/2010  upgrade to newest JAPC. Refactoring.
//                                      Support for various JAPC protocols.
//                                
//
// -------------------------------------------------------------------------

package cern.c2mon.daq.japc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import cern.c2mon.daq.tools.TIMDriverSimpleTypeConverter;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.daq.command.ISourceCommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * This is a specialized subclass of the general EquipmentMessageHandler. The class implements an
 * EquipmentMessageHandler for TIM DAQ for JAPC protocol.
 */
public class JAPCMessageHandler extends EquipmentMessageHandler implements ICommandRunner {

    public static final String DEFAULT_PROTOCOL = "rda";
    public static final String DEFAULT_SERVICE = "rda";

    protected class ParameterValueListenerImpl implements ParameterValueListener {

        private final ISourceDataTag tag;

        public ParameterValueListenerImpl(final ISourceDataTag tag) {
            this.tag = tag;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void exceptionOccured(@SuppressWarnings("unused") String parameterId, String description,
                ParameterException e) {

            if (e instanceof SubscriptionProblemException) {
                handleJAPCException(tag, description);
            }

            if (e instanceof SubscriptionRecoveredException) {
                // don't do anything, just log it - we need to wait for the new values to come anyway
                if (getEquipmentLogger().isDebugEnabled())
                    getEquipmentLogger().debug(
                            String.format(
                                    "SubscriptionRecoveredException received for subscription related to tag: #d", tag
                                            .getId()), e);
            }
        }

        @Override
        public void valueReceived(String parameterId, AcquiredParameterValue parameterValue) {
            handleJAPCValue(tag, parameterId, parameterValue);
        }

    }

    /**
     * JAPC parameter factory instance.
     */
    private ParameterFactory parameterFactory;

    private final List<SubscriptionHandle> handles = new ArrayList<SubscriptionHandle>();

    // private final TIMDriverSimpleTypeConverter converter = new TIMDriverSimpleTypeConverter();

    public JAPCMessageHandler() {
        super();
    }

    @Override
    public void connectToDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering connectToDataSource()..");

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
                        .error(
                                "connectToDataSource() : Unexpected problem occured when trying to create a JAPC ParameterFactory",
                                e);
                throw new EqIOException("Unexpected problem occured while creating instance of ParameterFactory: "
                        + e.getMessage());
            }
        }

        // Build up an internal list of JAPC parameters

        // Enumeration<SourceDataTag> tagEnum = this.sourceDataTags.elements();
        JAPCHardwareAddress addr = null;
        Parameter parameter = null;
        Selector selector = null;
        SubscriptionHandle handle = null;

        for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) {
            // Obtain JAPC address information about the tag

            addr = (JAPCHardwareAddress) tag.getHardwareAddress();

            try {

                // make sure protocol and service are correctly specified
                String protocol = checkProtocol(addr.getProtocol());
                String service = checkService(addr.getService());

                parameter = this.parameterFactory.newParameter(new ParameterUrlImpl(protocol, service, addr
                        .getDeviceName(), addr.getPropertyName(), null));

                // Create a selector for the parameter ( on change )

                // TODO : verify why tests fails if onChange flag is set to true!
                selector = ParameterValueFactory.newSelector(addr.getCycleSelector(), false);

                getEquipmentLogger().debug(
                        String.format("creating subscription handle for parameter: %s  selector: %s", parameter
                                .getName(), addr.getCycleSelector()));
                // Create a subscription handle for the parameter with the given selector
                handle = parameter.createSubscription(selector, new ParameterValueListenerImpl(tag));

                // TODO : if startMonitoring fails (throws exception) - add it to background pool for reconnection in
                // the future (a background task is necessary - in order to periodically try to subscribe)
                handle.startMonitoring();

                // Store the reference to the handle so it can't be destroyed by the
                // garbage collector
                this.handles.add(handle);

            } catch (Exception e) {
                StringBuilder bld = new StringBuilder("Unable to create subscription for tag: ").append(tag.getId())
                        .append(". Problem description: ").append(e.getMessage());
                getEquipmentLogger().error(bld);
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        bld.toString());
                parameter = null;
            }

        }// while

        getEquipmentLogger().debug("leaving connectToDataSource()");
    }

    private static String checkProtocol(final String protocol) {
        if (protocol == null || protocol.length() == 0)
            return DEFAULT_PROTOCOL;
        else
            return protocol;
    }

    private static String checkService(final String service) {
        if (service == null || service.length() == 0)
            return DEFAULT_SERVICE;
        else
            return service;
    }

    @Override
    @SuppressWarnings("unused")
    public void disconnectFromDataSource() throws EqIOException {
        getEquipmentLogger().debug("entering diconnectFromDataSource()..");

        for (SubscriptionHandle handle : this.handles) {
            try {
                handle.stopMonitoring();
            } catch (Exception e) {
                // nothing more than logging to be done here
                getEquipmentLogger().warn("disconnectFromDataSource() : Unexpected problem : " + e.getMessage(), e);
            }
        }// for

        this.handles.clear();

        getEquipmentLogger().debug("leaving diconnectFromDataSource()");
    }

    protected void handleJAPCValue(final ISourceDataTag tag, final String pParameterName,
            final AcquiredParameterValue pParameterValue) {

        ParameterValue value = pParameterValue.getValue();
        Type type = value.getType();

        ValueHeader header = pParameterValue.getHeader();
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
                    sendJAPCSValueFromScalar(tag, simpleValue, valueType, header);
                } catch (Exception ex) {
                    getEquipmentLogger().error("handleJAPCValue() : " + ex.getMessage(), ex);
                    getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNKNOWN, ex.getMessage());
                }
            }

            else if (valueType.isArray2d()) {
                try {
                    sendJAPCSValueFromArray2d(tag, simpleValue, valueType, header, addr.getRowIndex(), addr
                            .getColumnIndex());
                } catch (Exception ex) {
                    getEquipmentLogger().error("handleJAPCValue() : " + ex.getMessage(), ex);
                    getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNKNOWN, ex.getMessage());
                }
            } else if (valueType.isArray()) {
                try {
                    sendJAPCSValueFromArray(tag, simpleValue, valueType, header, addr.getColumnIndex());
                } catch (Exception ex) {
                    getEquipmentLogger().error("handleJAPCValue() : " + ex.getMessage(), ex);
                    getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNKNOWN, ex.getMessage());
                }
            }

            else {
                getEquipmentLogger().warn(
                        "handleJAPCValue() : value type \"" + valueType.toString() + "\" not supported.");
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.UNSUPPORTED_TYPE, null);
            }
        }

        else if (type == Type.MAP) {

            getEquipmentLogger().debug("\tupdate type : MAP");

            MapParameterValue mapValue = (MapParameterValue) value;
            // Initialising the MapParameterValue reader
            MPVReader reader = new MPVReader(mapValue, tag);

            try {
                Object value4send = reader.getValue();

                if (value4send != null) {
                    // send the value to the server
                    getEquipmentMessageSender().sendTagFiltered(tag, value4send,
                            convertSourceTimestampToMs(header.getAcqStampMillis()));
                } else {
                    getEquipmentLogger().warn(
                            "\tInvalidating SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : "
                                    + tag.getName() + " id : " + tag.getId());
                    getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // do not do anything - that solves the problem with the
                // maps sending arrays with flexible lengths
            } catch (Exception e) {
                // If we catch that exception, then because there was no value update in the
                // MAP field for the defined index-field-name. We just ignore this.
                getEquipmentLogger().warn(
                        "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                                + tag.getName() + " id : " + tag.getId() + " Problem: " + e.getMessage());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                        e.getMessage());
            }
        } else {
            getEquipmentLogger().error("\thandleJAPCValue() : Type \"" + type.toString() + "\" not supported");
        }

    }

    protected void handleJAPCException(final ISourceDataTag tag, final String pDescription) {
        getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.DATA_UNAVAILABLE, pDescription);
    }

    protected void sendJAPCSValueFromScalar(final ISourceDataTag tag, final SimpleParameterValue simpleValue,
            final ValueType valueType, final ValueHeader header) {
        getEquipmentLogger().debug("enetring sendJAPCSValueFromScalar()..");

        Object value4send = null;

        if (valueType == ValueType.BOOLEAN) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getBoolean());
        } else if (valueType == ValueType.BYTE) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getByte());
        } else if (valueType == ValueType.INT) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getInt());
        } else if (valueType == ValueType.LONG) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getLong());
        } else if (valueType == ValueType.FLOAT) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getFloat());
        } else if (valueType == ValueType.DOUBLE) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getDouble());
        } else if (valueType == ValueType.STRING) {
            value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getString());
        }

        if (value4send != null) {
            // send the value to the server
            getEquipmentMessageSender().sendTagFiltered(tag, value4send,
                    convertSourceTimestampToMs(header.getAcqStampMillis()));
        } else {
            getEquipmentLogger().info(
                    "\tInvalidating SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : " + tag.getName()
                            + " id : " + tag.getId());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
        }

        getEquipmentLogger().debug("leaving sendJAPCSValueFromScalar()");
    }

    protected void sendJAPCSValueFromArray(final ISourceDataTag tag, final SimpleParameterValue simpleValue,
            final ValueType valueType, final ValueHeader header, final int index) {
        getEquipmentLogger().debug("enetring sendJAPCSValueFromArray()..");

        Object value4send = null;

        try {

            if (valueType == ValueType.BOOLEAN_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getBoolean(index));
            } else if (valueType == ValueType.BYTE_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getByte(index));
            } else if (valueType == ValueType.INT_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getInt(index));
            } else if (valueType == ValueType.LONG_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getLong(index));
            } else if (valueType == ValueType.FLOAT_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getFloat(index));
            } else if (valueType == ValueType.DOUBLE_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getDouble(index));
            } else if (valueType == ValueType.STRING_ARRAY) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getString(index));
            }

            if (value4send != null) {
                // send the value to the server
                getEquipmentMessageSender().sendTagFiltered(tag, value4send,
                        convertSourceTimestampToMs(header.getAcqStampMillis()));
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
                    "incorrect native address : could not read data from array at index : " + index);
        }

        getEquipmentLogger().debug("leaving sendJAPCSValueFromArray()");
    }

    protected void sendJAPCSValueFromArray2d(final ISourceDataTag tag, final SimpleParameterValue simpleValue,
            final ValueType valueType, final ValueHeader header, final int rowIndex, final int colIndex) {

        getEquipmentLogger().debug("enetring sendJAPCSValueFromArray2d()..");

        Object value4send = null;

        try {

            if (valueType == ValueType.BOOLEAN_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getBoolean(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.BYTE_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getByte(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.INT_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getInt(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.LONG_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getLong(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.FLOAT_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getFloat(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.DOUBLE_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getDouble(rowIndex,
                        colIndex));
            } else if (valueType == ValueType.STRING_ARRAY_2D) {
                value4send = TIMDriverSimpleTypeConverter.convert(tag, simpleValue.getArray2D().getString(rowIndex,
                        colIndex));
            }

            if (value4send != null) {
                // send the value to the server
                getEquipmentMessageSender().sendTagFiltered(tag, value4send,
                        convertSourceTimestampToMs(header.getAcqStampMillis()));
            } else {
                getEquipmentLogger().info(
                        "\tInvalidating SourceDataTagValue with quality CONVERSION_ERROR, for Tag name : "
                                + tag.getName() + " id : " + tag.getId());
                getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, null);
            }

        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            getEquipmentLogger().warn(
                    "could not read data from an array at index : [" + rowIndex + "][" + colIndex + "]");
            getEquipmentLogger().info(
                    "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                            + tag.getName() + " id : " + tag.getId());
            getEquipmentMessageSender().sendInvalidTag(
                    tag,
                    SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                    "incorrect native address : could not read data from array at index : : [" + rowIndex + "]["
                            + colIndex + "]");
        }

        getEquipmentLogger().debug("leaving sendJAPCSValueFromArray2d()");
    }

    // @Override
    // @SuppressWarnings("unused")
    // protected void postAddCommandTag(SourceCommandTag pCommandTag) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postAddCommandTag() called for command with id " + pCommandTag.getId());
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void postAddDataTag(SourceDataTag pDataTag) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postAddDataTag() called for data tag with id " + pDataTag.getId());
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void postRemoveDataTag(final Long pId) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postRemoveDataTag() called for id " + pId);
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void postRemoveCommandTag(final Long pId) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postRemoveCommandTag() called for id " + pId);
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void postUpdateCommandTag(SourceCommandTag p0) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postUpdateCommandTag() called.");
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void postUpdateDataTag(SourceDataTag p0) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("postUpdateDataTag() called.");
    // }
    // }
    //
    // /**
    // * Make sure that the CommandTag to be added has a valid JAPCHardwareAddress
    // *
    // * @throws ch.cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException
    // * @param pCommandTag
    // */
    // @Override
    // protected void preAddCommandTag(final SourceCommandTag pCommandTag) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preAddCommandTag() called.");
    // }
    //
    // HardwareAddress addr = pCommandTag.getHardwareAddress();
    // if (addr == null) {
    // throw new EqCommandTagException("CommandTag with id " + pCommandTag.getId()
    // + " has no HardwareAddress (null)");
    // }
    // if (!(addr instanceof JAPCHardwareAddress)) {
    // throw new EqCommandTagException("CommandTag with id " + pCommandTag.getId()
    // + " has no valid JAPCHardwareAddress (" + addr.getClass().getName() + " not allowed.");
    // }
    // }
    //
    // /**
    // * Make sure that the DataTag to be added has a valid JAPCHardwareAddress
    // *
    // * @throws ch.cern.c2mon.daq.tools.equipmentexceptions.EqDataTagException
    // * @param pDataTag
    // */
    // @Override
    // protected void preAddDataTag(final SourceDataTag pDataTag) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preAddDataTag() called.");
    // }
    // HardwareAddress addr = pDataTag.getAddress().getHardwareAddress();
    // if (addr == null) {
    // throw new EqDataTagException("DataTag with id " + pDataTag.getId() + " has no HardwareAddress (null)");
    // }
    // if (!(addr instanceof JAPCHardwareAddress)) {
    // throw new EqDataTagException("DataTag with id " + pDataTag.getId() + " has no valid JAPCHardwareAddress ("
    // + addr.getClass().getName() + " not allowed.");
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void preRemoveCommandTag(Long p0) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preRemoveCommandTag() called.");
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void preRemoveDataTag(Long p0) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preRemoveDataTag() called.");
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void preUpdateCommandTag(SourceCommandTag p0) throws EqCommandTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preUpdateCommandTag() called.");
    // }
    // }
    //
    // @Override
    // @SuppressWarnings("unused")
    // protected void preUpdateDataTag(SourceDataTag p0) throws EqDataTagException {
    // if (getEquipmentLogger().isDebugEnabled()) {
    // getEquipmentLogger().debug("preUpdateDataTag() called.");
    // }
    // }

    // TODO move to implementation of ICommandRunner
    protected void sendCommand(SourceCommandTagValue p0) throws EqCommandTagException {
        getEquipmentLogger().debug("entering sendCommand()..");

        ISourceCommandTag sct = getEquipmentConfiguration().getSourceCommandTags().get(p0.getId());

        if (sct == null) {
            throw new EqCommandTagException(String.format(
                    "command #%d is not registered. Please check DAQ configuration", p0.getId()));
        }

        JAPCHardwareAddress addr = (JAPCHardwareAddress) sct.getHardwareAddress();
        ParameterUrl pUrl = null;
        try {

            // make sure protocol and service are correctly specified
            String protocol = checkProtocol(addr.getProtocol());
            String service = checkService(addr.getService());

            String dataField = addr.getDataFieldName();
            if (dataField != null && dataField.length() == 0) {
                dataField = null;
            }

            pUrl = new ParameterUrlImpl(protocol, service, addr.getDeviceName(), addr.getPropertyName(), dataField);

            Parameter parameter = ParameterFactory.newInstance().newParameter(pUrl);

            // Create a selector for the parameter
            Selector selector = ParameterValueFactory.newSelector(addr.getCycleSelector());
            ParameterValue pv = ParameterValueFactory.newParameterValue(p0.getValue());

            parameter.setValue(selector, pv);

        } catch (Exception e) {
            throw new EqCommandTagException("command execution failed. could not set value: " + p0.getValue()
                    + " for parameter: " + pUrl + " Error: " + e.getMessage());
        }

        getEquipmentLogger().debug("leaving sendCommand()");
    }

    /**
     * this method is a temporary <<hack>> to solve the problem with JAPC source timestamps delivered often in microsec.
     * istead of ns.
     * 
     * @param sTimeStamp
     * @return
     */
    static final long convertSourceTimestampToMs(long sTimeStamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1990, 01, 01);

        Date sourceDate = new Date(sTimeStamp);

        // make sure the provided timestamp is not older than 1990-01-01
        if (sourceDate.before(calendar.getTime())) {
            return sTimeStamp * 1000;
        } else
            return sTimeStamp;
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Implement this method.
    }

    @Override
    public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
        sendCommand(sourceCommandTagValue);
        return null;
    }
}
