/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.daq.japc.fesa;

import static java.lang.String.format;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cern.c2mon.daq.japc.GenericJapcMessageHandler;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.ParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.Type;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.address.JAPCHardwareAddress;
import cern.c2mon.shared.daq.datatag.ISourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

public class FesaAlarmJapcMessageHandler extends GenericJapcMessageHandler {

    public static final String NAMES_ARRAY = "names";
    public static final String TIMESTAMPS_ARRAY = "timestamps";
    public static final String PREFIXES_ARRAY = "prefixes";
    public static final String SUFFIXES_ARRAY = "suffixes";

    final ConcurrentMap<String, List<ISourceDataTag>> groupedTags = new ConcurrentHashMap<String, List<ISourceDataTag>>();

    @Override
    protected void handleJAPCValue(ISourceDataTag tag, String pParameterName, AcquiredParameterValue pParameterValue) {
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

        if (type != Type.MAP) {
            String errorMsg = String.format("handleJAPCValue() : Type \"%s\" is not supported", type.toString());
            getEquipmentLogger().error("\t" + errorMsg);
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, errorMsg);
            return;
        }

        MapParameterValue mValue = (MapParameterValue) value;

        try {

            int index = getIndex(mValue, NAMES_ARRAY, addr.getDataFieldName());

            if (index < 0) {
                // this alarm needs to be terminated
                getEquipmentMessageSender()
                        .sendTagFiltered(tag, false, System.currentTimeMillis(), "terminating alarm");
            } else {

                // get the timestamps array
                SimpleParameterValue timestamps = mValue.get(TIMESTAMPS_ARRAY);

                long timestamp = 0L;
                try {
                    if (timestamps.getValueType().isArray()) {
                        timestamp = timestamps.getLong(index);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // do not do anything, but logging the problem - that solves the problem with the
                    // maps sending arrays with flexible lengths
                    getEquipmentLogger().warn("the searched element wasn't found in the array", ex);
                }

                // get the prefixes array
                SimpleParameterValue prefixes = mValue.get(PREFIXES_ARRAY);
                // get the suffixes array
                SimpleParameterValue suffixes = mValue.get(SUFFIXES_ARRAY);

                String prefix = "";
                String suffix = "";
                try {
                    if (prefixes.getValueType().isArray()) {
                        prefix = prefixes.getString(index);
                        suffix = suffixes.getString(index);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // do not do anything, but logging the problem - that solves the problem with the
                    // maps sending arrays with flexible lengths
                    getEquipmentLogger().warn("the searched element wasn't found in the array", ex);
                }

                // send the value to the server
                getEquipmentMessageSender().sendTagFiltered(tag, true, timestamp,
                        format("ASI_SUFFIX=%s$$ASI_PREFIX=%s", suffix, prefix));

            }// else

        } catch (Exception e) {
            getEquipmentLogger().warn(
                    "\tInvalidating SourceDataTagValue with quality INCORRECT_NATIVE_ADDRESS, for Tag name : "
                            + tag.getName() + " id : " + tag.getId() + " Problem: " + e.getMessage());
            getEquipmentMessageSender().sendInvalidTag(tag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, e.getMessage());
        }

    }

    @Override
    protected boolean isSelectorOnChangeEnabled() {
        return false;
    }

    /**
     * this code has been commented out due to changes in the GenericJAPCMessageHandler. The thread execution pool has
     * been disabled Date: 14/12/2011
     * 
     * @author wbuczak
     */
    @Override
    public void connectToDataSource() throws EqIOException {
        /*
         * getEquipmentLogger().debug("entering connectToDataSource()..");
         * 
         * // If this is the first time this method is called (on start-up), create // a JAPC parameter factory if
         * (this.parameterFactory == null) { try { this.parameterFactory = ParameterFactory.newInstance(); // We do not
         * really have an Equipment but it indicates at least that // the factory creates went fine.
         * getEquipmentMessageSender().confirmEquipmentStateOK(); } catch (Exception e) {
         * getEquipmentMessageSender().confirmEquipmentStateIncorrect(
         * "Unexpected problem occured when trying to create a JAPC ParameterFactory instance");
         * 
         * getEquipmentLogger() .error(
         * "connectToDataSource() : Unexpected problem occured when trying to create a JAPC ParameterFactory", e); throw
         * new EqIOException("Unexpected problem occured while creating instance of ParameterFactory: " +
         * e.getMessage()); } }
         * 
         * // Build up an internal list of JAPC parameters
         * 
         * JAPCHardwareAddress addr = null; Parameter parameter = null; Selector selector = null; SubscriptionHandle
         * handle = null;
         * 
         * for (ISourceDataTag tag : getEquipmentConfiguration().getSourceDataTags().values()) { // Obtain JAPC address
         * information about the tag addr = (JAPCHardwareAddress) tag.getHardwareAddress();
         * 
         * String key = format("%s:%s", addr.getDeviceName(), addr.getPropertyName());
         * 
         * if (!this.groupedTags.containsKey(key)) { List<ISourceDataTag> sdts = new ArrayList<ISourceDataTag>();
         * sdts.add(tag); this.groupedTags.put(key, sdts); } else { List<ISourceDataTag> sdts =
         * this.groupedTags.get(key); sdts.add(tag); }
         * 
         * }
         * 
         * for (String key : this.groupedTags.keySet()) {
         * 
         * List<ISourceDataTag> tags = this.groupedTags.get(key); // get address of the first tag in the list addr =
         * (JAPCHardwareAddress) tags.get(0).getHardwareAddress();
         * 
         * try {
         * 
         * // make sure protocol and service are correctly specified String protocol =
         * checkProtocol(addr.getProtocol()); String service = checkService(addr.getService());
         * 
         * parameter = this.parameterFactory.newParameter(new ParameterUrlImpl(protocol, service, addr .getDeviceName(),
         * addr.getPropertyName(), null));
         * 
         * // Create a selector for the parameter ( on change ) selector =
         * ParameterValueFactory.newSelector(addr.getCycleSelector(), isSelectorOnChangeEnabled());
         * 
         * getEquipmentLogger().debug( String.format("creating subscription handle for parameter: %s  selector: %s",
         * parameter .getName(), addr.getCycleSelector())); // Create a subscription handle for the parameter with the
         * given selector handle = parameter.createSubscription(selector, new ParameterValueListenerImpl(tags
         * .toArray(new ISourceDataTag[0])));
         * 
         * // Store the reference to the handle so it can't be destroyed by the // garbage collector
         * this.handles.add(handle);
         * 
         * // run the start-monitoring thread executor.execute(new StartMonitoringTask(handle, tags.toArray(new
         * ISourceDataTag[0])));
         * 
         * } catch (Exception e) { // invalidate all tags from this group for (ISourceDataTag sdtag : tags) {
         * StringBuilder bld = new StringBuilder("Unable to create subscription for tag: ").append(
         * sdtag.getId()).append(". Problem description: ").append(e.getMessage()); getEquipmentLogger().error(bld);
         * getEquipmentMessageSender().sendInvalidTag(sdtag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
         * bld.toString()); } parameter = null; }
         * 
         * }// for
         * 
         * executor.shutdown();
         * 
         * getEquipmentLogger().debug("leaving connectToDataSource()");
         */
    }

    @Override
    public void refreshAllDataTags() {
        // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
    }

    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Implement this method.
    }
}
