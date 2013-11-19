package cern.c2mon.shared.daq.datatag;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.ValueChangeMonitor;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.type.TagDataType;
import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;

/**
 * The SourceDataTag class is the representation of a DataTag on the driver side. It contains all tag-specific
 * information the driver needs to acquired values for the tag it represents and to send them on to the application
 * server.
 * 
 * @author Jan Stowisek
 * @version $Revision: 1.22 $ ($Date: 2009/05/12 13:05:39 $ - $State: Exp $)
 */
public class SourceDataTag implements Serializable, Cloneable, ISourceDataTag {

    // ----------------------------------------------------------------------------
    // PRIVATE STATIC MEMBERS
    // ----------------------------------------------------------------------------
    /**
     * Version number of the class used during serialization/deserialization. This is to ensure that minor changes to
     * the class do not prevent us from reading back DataTagAddress objects we have serialized earlier. If fields are
     * added/removed from the class, the version number needs to change.
     */
    private static final long serialVersionUID = -145678123L;

    /** Unique numeric identifier of the DataTag */
    private Long id;

    /** Unique name of the DataTag */
    private String name;

    /** Flag indicating whether this DataTag transports supervision information */
    private boolean control;

    /** Flag indicating the tag's mode: operational, in test or in maintenance */
    private short mode;

    /**
     * Data type of the tag's values. Values of any other data type will be rejected during the update.
     */
    private String dataType;

    /**
     * The min value as comparable without specified type to avoid splitting up the source data tag in different
     * generics.
     */
    @SuppressWarnings("unchecked")
    private Comparable minValue;

    /**
     * The max value as comparable without specified type to avoid splitting up the source data tag in different
     * generics.
     */
    @SuppressWarnings("unchecked")
    private Comparable maxValue;

    /**
     * Address information, including parameters for deadband filtering, transformations etc.
     */
    private DataTagAddress address;

    /** Current value of the SourceDataTag */
    private SourceDataTagValue currentValue;

    // ----------------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------------

    /**
     * Creates a new SourceDataTag
     * 
     * @param id The id of the new tag.
     * @param name The name of the new tag.
     * @param control True if the data tag should be a control tag.
     */
    public SourceDataTag(final Long id, final String name, final boolean control) {
        this(id, name, control, DataTagConstants.MODE_OPERATIONAL, null, null);
    }

    /**
     * Creates a new SourceDataTag.
     * 
     * @param id The id of the new tag.
     * @param name The name of the new tag.
     * @param controlTag True if the data tag should be a control tag.
     * @param mode The mode (see {@link DataTagConstants}) of this tag.
     * @param dataType The data type of this tag.
     * @param address The DataTagAddress of this tag.
     */
    public SourceDataTag(final Long id, final String name, final boolean controlTag, final short mode,
            final String dataType, final DataTagAddress address) {
        this.id = id;
        this.name = name;
        this.control = controlTag;
        this.mode = mode;
        this.dataType = dataType;
        this.address = address;
    }

    /**
     * Gets the id of the data tag.
     * 
     * @return The id of the data tag.
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Gets the name of the data tag.
     * 
     * @return The name of the data tag.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of this data tag.
     * 
     * @param newName The new name of this data tag.
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Gets the data tag data type.
     * 
     * @return The data tag data type.
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * Sets the data tag type of this data tag and updates also the numeric type.
     * 
     * @param newDataType The new data tag type.
     */
    public void setDataType(final String newDataType) {
        this.dataType = newDataType;
    }

    /**
     * Gets the numeric representation of the data type.
     * 
     * @return The numeric representation of the data type. See {@link TagDataType} constants for details.
     */
    public int getDataTypeNumeric() {
        return TagDataType.getDataTypeNumeric(dataType);
    }

    /**
     * Returns true if this is a control tag.
     * 
     * @return True if it is a control tag else false.
     */
    public boolean isControlTag() {
        return this.control;
    }

    /**
     * Returns the mode of this data tag. See {@link DataTagConstants} for details.
     * 
     * @return The mode of this data tag.
     */
    public short getMode() {
        return this.mode;
    }

    /**
     * Checks if the data tag is in operation mode.
     * 
     * @return True if it is in operation modee else false.
     */
    public boolean isInOperation() {
        return (this.mode == DataTagConstants.MODE_OPERATIONAL);
    }

    /**
     * Checks if the data tag is in test mode.
     * 
     * @return True if the data tag is in test mode else false.
     */
    public boolean isInTest() {
        return (this.mode == DataTagConstants.MODE_TEST);
    }

    /**
     * Checks if the data tag is in maintenance mode.
     * 
     * @return True if the data tag is in maintenance mode else false.
     */
    public boolean isInMaintenance() {
        return (this.mode == DataTagConstants.MODE_MAINTENANCE);
    }

    /**
     * Sets if this tag is a control tag.
     * 
     * @param isControlTag If true the tag will be set as control tag else it will be no control tag.
     */
    public void setControlTag(final boolean isControlTag) {
        this.control = isControlTag;
    }

    /**
     * Gets the data tag address.
     * 
     * @return The data tag address.
     */
    public DataTagAddress getAddress() {
        return this.address;
    }

    /**
     * Sets the data tag address.
     * 
     * @param newAddress The new data tag address.
     */
    public void setAddress(final DataTagAddress newAddress) {
        this.address = newAddress;
    }

    /**
     * Returns the min value of the data tag value as comparable object.
     * 
     * @return The min value.
     */
    public Comparable getMinValue() {
        return this.minValue;
    }

    /**
     * Sets the min value of this tag.
     * 
     * @param pMinValue The new min value of this tag.
     */
    public void setMinValue(final Comparable pMinValue) {
        this.minValue = pMinValue;
    }

    /**
     * Returns the max value of the data tag value as comparable object.
     * 
     * @return The max value.
     */
    public Comparable getMaxValue() {
        return this.maxValue;
    }

    /**
     * Sets the max value of this tag.
     * 
     * @param pMaxValue The new max value.
     */
    public void setMaxValue(final Comparable pMaxValue) {
        this.maxValue = pMaxValue;
    }

    /**
     * Gets the hardware address of this tag.
     * 
     * @return The hardware address of this tag.
     */
    public HardwareAddress getHardwareAddress() {
        if (address == null) {
            return null;
        } else {
            return address.getHardwareAddress();
        }
    }

    /**
     * Gets the current value of this tag (might be null).
     * 
     * @return The current value of this tag.
     */
    public synchronized SourceDataTagValue getCurrentValue() {
        if (currentValue != null)
            return this.currentValue.clone();
        else
            return null;
    }
    
    /**
     * Updates this source data tags value.
     * 
     * @param value The new value. (Should match the right data type)
     * @return A SourceDataTag value object to send to the server.
     */
    public synchronized SourceDataTagValue update(final Object value) {
        return update(new SourceDataQuality(), value, null, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * Updates this source data tags value.
     * 
     * @param value The new value. (Should match the right data type)
     * @param srcTimestamp The source timestamp of the value.
     * @return A SourceDataTag value object to send to the server.
     */
    public synchronized SourceDataTagValue update(final Object value, final Timestamp srcTimestamp) {
        return update(new SourceDataQuality(), value, null, srcTimestamp, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Updates this source data tags value.
     * 
     * @param value The new value. (Should match the right data type)
     * @param valueDescription The description of the new value.
     * @param timestamp The src timestamp of the value (the DAQ timestamp is set to the system time at this point)
     * @return A SourceDataTag value object to send to the server.
     */
    public synchronized SourceDataTagValue update(final Object value, final String valueDescription, final Timestamp srcTimestamp) {
      return update(new SourceDataQuality(), value, valueDescription, srcTimestamp, new Timestamp(System.currentTimeMillis()));  
    }
    
    /**
     * Updates this source data tags value.
     * 
     * @param value The new value. (Should match the right data type)
     * @param sourceDataQuality The quality of the new new SDValue
     * @param valueDescription The description of the new value.
     * @param timestamp The src timestamp of the value (the DAQ timestamp is set to the system time at this point)
     * @param daqTimestamp 
     * 
     * @return A SourceDataTag value object to send to the server.
     */
    private synchronized SourceDataTagValue update(final SourceDataQuality sourceDataQuality, final Object value, 
        final String valueDescription, final Timestamp srcTimestamp, final Timestamp daqTimestamp) {
      if (this.currentValue != null) {
        this.currentValue.setValue(value);
        this.currentValue.setValueDescription(valueDescription);
        this.currentValue.setTimestamp(srcTimestamp);
        this.currentValue.setDaqTimestamp(daqTimestamp);
        this.currentValue.setQuality(sourceDataQuality);
    } else {
        this.currentValue = new SourceDataTagValue(this.id, this.name, this.control, value,
            sourceDataQuality, srcTimestamp, this.address.getPriority(), this.address.isGuaranteedDelivery(),
            valueDescription, this.address.getTimeToLive());
        this.currentValue.setDaqTimestamp(daqTimestamp);
    }

    return this.currentValue.clone();
    }


    /**
     * @param mode the mode to set
     */
    public void setMode(final short mode) {
        this.mode = mode;
    }

    /**
     * Returns a FilteredDataTagValue from the current tag, with adjusted quality fields. Is used to send invalidation
     * messages to the filter queue.
     * 
     * @param sourceDataQuality the quality object for the tag
     * @param timestamp the time when the quality message was received
     * @param dynamicFiltered flag for dynamic filtering
     * @param filterApplied A constant which indicates which filter was applied. See {@link FilteredDataTagValue} for
     *            details to the constants.
     * @return the filtered value object
     */
    public final FilteredDataTagValue makeFilterValue(final SourceDataQuality sourceDataQuality,
            final Timestamp timestamp, final boolean dynamicFiltered, final short filterApplied) {
        SourceDataTagValue currentVal = (SourceDataTagValue) this.getCurrentValue();

        FilteredDataTagValue returnValue = new FilteredDataTagValue(currentVal.getId(), currentVal.getName(),
                currentVal.getValue().toString(), new Short(sourceDataQuality.getQualityCode()), null, timestamp, null,
                this.getDataType(), dynamicFiltered, filterApplied);
        // quality and/or value description are set if not null
        if (sourceDataQuality.getDescription() != null) {
            returnValue.setQualityDescription(sourceDataQuality.getDescription());
        }
        if (currentVal.getValueDescription() == null) {
            returnValue.setValueDescription(currentVal.getValueDescription());
        }
        return returnValue;
    }

    /**
     * Returns a FilteredDataTagValue object for the datatag, with updated value and timestamp.
     * 
     * @param timestamp the timestamp of the new value reading
     * @param tagValue the tag value
     * @param valueDescription the value description
     * @param dynamicFiltered flag indicating the value was filtered out by the dynamic filtering mechanism (not
     *            implemented)
     * @param filterApplied A constant which indicates which filter was applied. See {@link FilteredDataTagValue} for
     *            details to the constants.
     * @return the value object
     */
    public final FilteredDataTagValue makeFilterValue(final Timestamp timestamp, final Object tagValue,
            final String valueDescription, final boolean dynamicFiltered, final short filterApplied) {
        SourceDataTagValue currentVal = (SourceDataTagValue) this.getCurrentValue();
        // In constructor below arguments are:
        // Long pId,
        // String pName,
        // String pValue,
        // Short pQualityCode,
        // String pQualityDescription,
        // Timestamp pTimestamp,
        // String pValueDescription,
        // String pDataType,
        // boolean pDynamicFiltered
        // short filterApplied
        FilteredDataTagValue returnValue = new FilteredDataTagValue(currentVal.getId(), currentVal.getName(),
                tagValue.toString(), new Short(currentVal.getQuality().getQualityCode()), currentVal.getQuality()
                        .getDescription(), timestamp, valueDescription, this.getDataType(), dynamicFiltered,
                filterApplied);
        return returnValue;
    }

    /**
     * Invalidate the current value of a SourceDataTag The invalidate method will always return a SourceDataTagValue
     * object, unless
     * <UL>
     * <LI>the quality object passed as a parameter is null
     * <LI>the quality code of the SourceDataQuality object is OK
     * <LI>the timestamp passed as a parameter is older than the timestamp of the current value
     * </UL>
     * No deadband checks are applied for invalidation.
     * 
     * @param quality The quality of the source data tag value.
     * @return The SourceDataTagValue to send to the server.
     */
    public SourceDataTagValue invalidate(final SourceDataQuality quality) {
        return invalidate(quality, null, null, null);
    }
    
    /**
     * Invalidate the current value of a SourceDataTag The invalidate method will always return a SourceDataTagValue
     * object, unless
     * <UL>
     * <LI>the quality object passed as a parameter is null
     * <LI>the quality code of the SourceDataQuality object is OK
     * <LI>the timestamp passed as a parameter is older than the timestamp of the current value
     * </UL>
     * No deadband checks are applied for invalidation.
     * <p>
     * If the timestamp passed is null, the current time is taken as source invalidation.
     * 
     * @param quality The quality of the source data tag value.
     * @param timestamp Timestamp for the invalidation.
     * @return The SourceDataTagValue to send to the server.
     */
    public synchronized SourceDataTagValue invalidate(final SourceDataQuality quality, final Timestamp timestamp) {
      if (this.currentValue != null) {
        return invalidate(quality, this.currentValue.getValue(), this.currentValue.getValueDescription(), timestamp);
      } else {
        return invalidate(quality, null, null, timestamp);   
      }
    }
    
    /**
     * Invalidate the current value of a SourceDataTag The invalidate method will always return a SourceDataTagValue
     * object, unless
     * <UL>
     * <LI>the quality object passed as a parameter is null
     * <LI>the quality code of the SourceDataQuality object is OK
     * <LI>the timestamp passed as a parameter is older than the timestamp of the current value
     * </UL>
     * No deadband checks are applied for invalidation.
     * 
     * @param quality The quality of the source data tag value.
     * @return The SourceDataTagValue to send to the server.
     */
    public SourceDataTagValue invalidate(final SourceDataQuality quality, final Object value, final String valueDescription, 
        final Timestamp timestamp) {
      if (quality == null || quality.isValid()) {
        return null;
      }
      
      Timestamp daqTimestamp = new Timestamp(System.currentTimeMillis());
      Timestamp srcTimestamp = timestamp == null ? daqTimestamp : timestamp;
      
      return update(quality, value, valueDescription, srcTimestamp, daqTimestamp);
    }

    // ----------------------------------------------------------------------------
    // METHODS FOR XML-IFICATION and DE-XML-IFICATION
    // ----------------------------------------------------------------------------

    /**
     * Create a SourceDataTag object from a &lt;SourceDataTag ...&gt; XML element. This method is used by TIM drivers to
     * create SourceDataTag objects for the configuration XML file received from the server. As the server uses the
     * SourceDataTag.toConfigXML() method to create the configuration XML file, there should never be a problem decoding
     * the XML file on the driver side.
     * 
     * @param domElement The dom element to use. This must be the SourceDataTag element.
     * @return The creates SourceDataTag.
     */
    public static SourceDataTag fromConfigXML(final Element domElement) {

        Long id = Long.valueOf(domElement.getAttribute("id"));
        String name = domElement.getAttribute("name");
        boolean control = domElement.getAttribute("control").equals("true");

        SourceDataTag result = new SourceDataTag(id, name, control);

        NodeList fields = domElement.getChildNodes();
        int fieldsCount = fields.getLength();

        for (int i = 0; i < fieldsCount; i++) {
            Node fieldNode = fields.item(i);
            String fieldName;
            String fieldValueString = null;

            if (fieldNode.getNodeType() == 1) {
                fieldName = fieldNode.getNodeName();
                if (fieldNode.getFirstChild() != null) {
                    fieldValueString = fieldNode.getFirstChild().getNodeValue();
                }

                if (fieldName.equals("data-type")) {
                    result.dataType = fieldValueString;
                } else if (fieldName.equals("mode")) {
                    result.mode = Short.parseShort(fieldValueString);
                } else if (fieldName.equals("DataTagAddress")) {
                    result.address = DataTagAddress.fromConfigXML((Element) fieldNode);
                } else if (fieldName.equals("min-value") || fieldName.equals("max-value")) {
                    String dataType = fieldNode.getAttributes().item(0).getNodeValue();
                    Comparable value = (Comparable) TypeConverter.cast(fieldValueString, dataType);
                    if (fieldName.equals("min-value")) {
                        result.minValue = value;
                    } else {
                        result.maxValue = value;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns an XML representation of the SourceDataTag object. This XML snipplet is used in the configuration XML
     * sent to the driver.
     * 
     * @return XML String of this data tag.
     */
    public String toConfigXML() {
        // <DataTag id="..." name="..." control="...">
        StringBuffer str = new StringBuffer("    <DataTag id=\"");

        str.append(id);
        str.append("\" name=\"");
        str.append(name);
        if (isControlTag()) {
            str.append("\" control=\"true\">\n");
        } else {
            str.append("\" control=\"false\">\n");
        }

        // <mode> ... </mode>
        if (mode != DataTagConstants.MODE_OPERATIONAL) {
            str.append("      <mode>");
            str.append(mode);
            str.append("</mode>\n");
        }

        // <data-type> ... </data-type>
        str.append("      <data-type>");
        str.append(dataType);
        str.append("</data-type>\n");

        if (minValue != null) {
            str.append("        <min-value data-type=\"");
            // str.append(minValue.getClass().getName().substring(10));
            str.append(minValue.getClass().getSimpleName());
            str.append("\">");
            str.append(minValue);
            str.append("</min-value>\n");
        }

        if (maxValue != null) {
            str.append("        <max-value data-type=\"");
            // str.append(maxValue.getClass().getName().substring(10));
            str.append(maxValue.getClass().getSimpleName());
            str.append("\">");
            str.append(maxValue);
            str.append("</max-value>\n");
        }

        // <HardwareAddress> ... </HardwareAddress>
        if (address != null) {
            str.append(getAddress().toConfigXML());
        }

        str.append("    </DataTag>\n");
        return str.toString();
    }

    /**
     * Validates this data tag. At the moment this means only checking if the data tag address is != null and then
     * validating the data tag address.
     * 
     * @throws ConfigurationException If the validation fails a ConfigurationException is thrown.
     */
    public void validate() throws ConfigurationException {
        if (address == null) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "Parameter \"address\" must not be null");
        }
        address.validate();
    }

    /**
     * Does a deep clone of this data tag.
     * 
     * @return The clone of this source data tag.
     */
    @Override
    public SourceDataTag clone() {
        SourceDataTag clonedSourceDataTag = null;
        try {
            clonedSourceDataTag = (SourceDataTag) super.clone();

            DataTagAddress address = getAddress();
            if (address != null)
                clonedSourceDataTag.setAddress(address.clone());
        } catch (CloneNotSupportedException e) {
            // This should not happen if nobody changes the address class
            e.printStackTrace();
        }
        return clonedSourceDataTag;
    }

    /**
     * returns the value deadband
     * 
     * @return The value deadband
     */
    @Override
    public float getValueDeadband() {
        float valueDeadband = 0;
        if (address != null) {
            valueDeadband = address.getValueDeadband();
        }
        return valueDeadband;
    }

    /**
     * returns the value deadband type
     * 
     * @return The value deadband type
     */
    @Override
    public short getValueDeadbandType() {
        short valueDeadbandType = DataTagDeadband.DEADBAND_NONE;
        if (address != null) {
            valueDeadbandType = address.getValueDeadbandType();
        }
        return valueDeadbandType;
    }

    /**
     * Short way to get the timedeadband of this tag. The timedeadband is the shortest time period the DAQ should send
     * updates to the Server.
     * 
     * @return The time deadband value.
     */
    @Override
    public int getTimeDeadband() {
        int timeDeabband = 0;
        if (address != null) {
            timeDeabband = address.getTimeDeadband();
        }
        return timeDeabband;
    }

    /**
     * The two SourceDataTag objects are considered equal if they are of the same type and all their non-static
     * attributes are equal
     */
    @Override
    public boolean equals(final Object copy) {

        boolean result = copy != null && copy instanceof SourceDataTag;

        if (result) {

            Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                // compare non-final, non-static and non-transient fields only
                if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers())) {
                    try {

                        if ((field.get(this) != null && field.get(copy) == null)
                                || (field.get(this) == null && field.get(copy) != null)) {
                            result = false;
                        } else if (field.get(this) != null && field.get(copy) != null) {

                            if (field.getType().isArray()) {

                                if (Object[].class.isAssignableFrom(field.getType())) {
                                    result = Arrays.equals((Object[]) field.get(this), (Object[]) field.get(copy));
                                } else {
                                    result = ArrayUtils.isEquals(field.get(this), field.get(copy));
                                }

                            } else {
                                result = field.get(this).equals(field.get(copy));
                            }
                        }
                    } catch (Exception e) {
                        result = false;
                    }
                }

                if (!result)
                    break;
            }// for
        }

        return result;
    }

    @Override
    public final int hashCode() {

        int result = 0;

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            // compare non-final, non-static and non-transient fields only
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers())) {
                try {

                    // skip arrays
                    if (!field.getType().isArray() && field.get(this) != null)
                        // for string take its length
                        if (field.getType().equals(String.class)) {
                            result ^= ((String) field.get(this)).length();
                        } else if (field.getType().equals(short.class)) {
                            result ^= field.getShort(this);
                        } else if (field.getType().equals(int.class)) {
                            result ^= field.getInt(this);
                        } else if (field.getType().equals(float.class)) {
                            result ^= (int) field.getFloat(this);
                        } else if (field.getType().equals(double.class)) {
                            result ^= (int) field.getDouble(this);
                        } else if (field.getType().equals(long.class)) {
                            result ^= (int) field.getLong(this);
                        } else if (field.getType().equals(byte.class)) {
                            result ^= field.getByte(this);
                        } else if (field.getType().equals(boolean.class)) {
                            result ^= field.getBoolean(this) == Boolean.TRUE ? 1 : 0;
                        }

                } catch (Exception e) {
                    // nothing more to be done
                    e.printStackTrace();
                }
            }

        }// for

        return result;
    }

    public void setDataTypeNumeric(int dataType) {
        this.dataType = TagDataType.getDataTypeString(dataType);
    }

    @Override
    public ValueChangeMonitor getValueCheckMonitor() {
        return address.getValueCheckMonitor();
    }

    @Override
    public boolean hasValueCheckMonitor() {
        return address.hasValueCheckMonitor();
    }

}
