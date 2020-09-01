/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.common.datatag;

import java.sql.Timestamp;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * The SourceDataTag class is the representation of a DataTag on the driver side. It contains all tag-specific
 * information the driver needs to acquired values for the tag it represents and to send them on to the application
 * server.
 *
 * @author Jan Stowisek
 */
@Data
@Slf4j
@NoArgsConstructor
public class SourceDataTag implements Cloneable, ISourceDataTag {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /** Unique numeric identifier of the DataTag */
    @Attribute
    private Long id;

    /** Unique name of the DataTag */
    @Attribute
    private String name;

    /** Flag indicating whether this DataTag transports supervision information */
    @Attribute
    private boolean control;

    /** Flag indicating the tag's mode: operational, in test or in maintenance */
    @Element(required = false)
    private short mode;

    /**
     * Data type of the tag's values. Values of any other data type will be rejected during the update.
     */
    @Element(name = "data-type")
    private String dataType;

    /**
     * The min value as comparable without specified type to avoid splitting up the source data tag in different
     * generics.
     */
    @Element(name = "min-value", required = false, type = Double.class) // Double is defined to make the serialization to Number work
    private Number minValue;

    /**
     * The max value as comparable without specified type to avoid splitting up the source data tag in different
     * generics.
     */
    @Element(name = "max-value", required = false, type = Double.class) // Double is defined to make the serialization to Number work
    private Number maxValue;

    /**
     * Address information, including parameters for deadband filtering, transformations etc.
     */
    @Element(name = "DataTagAddress")
    private DataTagAddress address;

    /** Current value of the SourceDataTag */
    private SourceDataTagValue currentValue;

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
        adjustJmsPriority();
    }

    /**
     * Checks if the data tag is in operation mode.
     *
     * @return True if it is in operation modee else false.
     */
    @JsonIgnore
    public boolean isInOperation() {
        return (this.mode == DataTagConstants.MODE_OPERATIONAL);
    }

    /**
     * Checks if the data tag is in test mode.
     *
     * @return True if the data tag is in test mode else false.
     */
    @JsonIgnore
    public boolean isInTest() {
        return (this.mode == DataTagConstants.MODE_TEST);
    }

    /**
     * Checks if the data tag is in maintenance mode.
     *
     * @return True if the data tag is in maintenance mode else false.
     */
    @JsonIgnore
    public boolean isInMaintenance() {
        return (this.mode == DataTagConstants.MODE_MAINTENANCE);
    }

    /**
     * Gets the hardware address of this tag.
     *
     * @return The hardware address of this tag.
     */
    @Override
    public HardwareAddress getHardwareAddress() {
        if (address == null) {
            return null;
        } else {
            return address.getHardwareAddress();
        }
    }

  /**
   * set the HardwareAddress of the DataTagAddress of this object.
   * @param hwa the HardwareAddress to set.
   */
  public void setHardwareAddress(HardwareAddress hwa) {
        address.setHardwareAddress(hwa);
    }

    @Override
    public Map<String, String> getAddressParameters() {
        if (address == null) {
            return null;
        } else {
            return address.getAddressParameters();
        }
    }


    /**
     * Gets the current value of this tag (might be null).
     *
     * @return The current value of this tag.
     */
    @Override
    public synchronized SourceDataTagValue getCurrentValue() {
      if (currentValue == null) {
        return null;
      }

      return this.currentValue.clone();
    }

    private void initCurrentValue() {
      SourceDataTagValue tagValue = new SourceDataTagValue(this.id, this.name, this.control);
      tagValue.setPriority(this.address.getPriority());
      tagValue.setGuaranteedDelivery(this.address.isGuaranteedDelivery());
      tagValue.setTimeToLive(this.address.getTimeToLive());
      tagValue.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));

      this.currentValue = tagValue;
    }

    public SourceDataTagValue update(final ValueUpdate update) {
      return update(update, new SourceDataTagQuality());
    }

    /**
     * Updates this source data tags value.
     *
     * @param update The new value. (Should match the right data type)
     * @param quality The new value quality
     * @return A SourceDataTag value object to send to the server.
     */
    public synchronized SourceDataTagValue update(final ValueUpdate update, SourceDataTagQuality quality) {
      if (this.currentValue == null) {
        initCurrentValue();
      }

      this.currentValue.setValue(update.getValue());
      this.currentValue.setValueDescription(update.getValueDescription());
      this.currentValue.setTimestamp(new Timestamp(update.getSourceTimestamp()));
      this.currentValue.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
      this.currentValue.setQuality(quality);

      return this.currentValue.clone();
    }

    public synchronized SourceDataTagValue update(final SourceDataTagQuality quality, long timestamp) {
      if (this.currentValue == null) {
        initCurrentValue();
      }

      this.currentValue.setQuality(quality);
      this.currentValue.setTimestamp(new Timestamp(timestamp));

      return this.currentValue.clone();
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
    public static SourceDataTag fromConfigXML(final org.w3c.dom.Element domElement) {

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
                    result.address = DataTagAddress.fromConfigXML((org.w3c.dom.Element) fieldNode);
                } else if (fieldName.equals("min-value") || fieldName.equals("max-value")) {
                    String dataType = fieldNode.getAttributes().item(0).getNodeValue();
                    Number value = (Number) TypeConverter.cast(fieldValueString, dataType);
                    if (fieldName.equals("min-value")) {
                        result.minValue = value;
                    } else {
                        result.maxValue = value;
                    }
                }
            }
        }
        
        result.adjustJmsPriority();
        return result;
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

            if (this.address != null) {
                clonedSourceDataTag.setAddress(this.address.clone());
            }
            if (this.currentValue != null) {
              clonedSourceDataTag.currentValue = this.currentValue.clone();
            }
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
        short valueDeadbandType = ValueDeadbandType.NONE.getId().shortValue();
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
   * Method for extracting the data from the addressParameters of this object.
   *
   * @param key The key which specify which data should be extracted
   * @param type The type the return value should have
   * @param <T> generic type definition for the method
   * @return value with the given type.
   */
  public <T> T getAddressParameter(String key, Class<T> type) {
    return getAddressParameter(this.getAddressParameters(), key, type);
  }

  /**
   * Method for extracting the data from the map with the correct type.
   *
   * @param parameters map which holds the data.
   * @param key The key which specify which data should be extracted
   * @param type The type the return value should have
   * @param <T> generic type definition for the method
   * @return value with the given type.
   */
  public static <T> T getAddressParameter(Map<String,String> parameters, String key, Class<T> type) {
    return objectMapper.convertValue(parameters.get(key), type);
  }

  public boolean isControlTag() {
    return this.control;
  }
  
  /**
   * The DAQ only supports four JMS priorities which are further described
   * by the given constants. This has influence also on the local message buffering
   * which is used to send value update in bunches.
   * <p>
   * This adjustment is necessary as the initial design has slightly changed over the
   * years to improve the overall sending performance.
   * 
   * @param tag The tag configuration received by the server
   * @see DataTagConstants
   */
  public void adjustJmsPriority() {
    if (address != null) {
      int priority = address.getPriority();
      JmsMessagePriority convertedPriority;
      
      if (isControl()) {
        convertedPriority = JmsMessagePriority.PRIORITY_HIGHEST;
      } else if (priority > JmsMessagePriority.PRIORITY_MEDIUM.getPriority()) {
        convertedPriority = JmsMessagePriority.PRIORITY_HIGH;
      } else if (priority == JmsMessagePriority.PRIORITY_MEDIUM.getPriority()) {
        convertedPriority =  JmsMessagePriority.PRIORITY_MEDIUM;
      } else {
        // priority < DataTagConstants.PRIORITY_MEDIUM
        convertedPriority =  JmsMessagePriority.PRIORITY_LOW;
      }
      
      if (priority != convertedPriority.getPriority()) {
        log.trace("Adjust JMS priority of tag #{}: {} --> {}", id, priority, convertedPriority);
        address.setPriority(convertedPriority);
      }
    }
  }
}
