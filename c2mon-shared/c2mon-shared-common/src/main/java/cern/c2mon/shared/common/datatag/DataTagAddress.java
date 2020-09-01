/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.util.JmsMessagePriority;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.util.parser.ParserException;
import cern.c2mon.shared.util.parser.SimpleXMLParser;

import static cern.c2mon.shared.common.datatag.DataTagConstants.*;

/**
 * Address associated with a DataTag DataTags are linked to data sources (e.g.
 * PLCs, external SCADA systems etc.) via a DataTagAddress object. This
 * object contains the configuration parameters needed by a DAQ module to
 * acquire values from the data source. In addition to data acquisition
 * parameters, the DataTagAddress object also configures deadband
 * filtering and transformation factors for a given tag.
 *
 * @author Jan Stowisek, Matthias Braeger
 */
@Slf4j
@Data
public class DataTagAddress implements Serializable, Cloneable {

  /**
   * Version number of the class used during serialization/deserialization.
   * This is to ensure that minor changes to the class do not prevent us from
   * reading back DataTagAddress objects we have serialized earlier. If fields
   * are added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -145678L;

  /**
   * XML parser for this class (initialised in static initialiser)
   */
  private static transient SimpleXMLParser parser = null;

  /**
   * Equipment-specific hardware address. The HardwareAddress objects is only
   * used by the EquipmentMessageHandler. It contains all equipment-specific
   * parameters the message handler needs to acquire values for a data tag. The
   * HardwareAddress class is abstract, so the object will always be of a
   * subclass of HardwareAddress (e.g. PLCEquipmentAddress for the JECEquipmentHandler).
   */
  @Element(name = "HardwareAddress", required = false)
  private HardwareAddress hardwareAddress;

  /**
   * All address information of the given DataTag
   * This is an central element for this class which provides the information
   * for the daq to create an DataTag.
   */
  @ElementMap(name = "address-parameters", key = "key", attribute = true, required = false)
  private HashMap<String, String> addressParameters = new HashMap<>();

  /**
   * Time-to-live in milliseconds. Values that have not been received by the
   * application server before the expiration of the TTL are discarded. The
   * ttl is only relevant in times of great (huge) server load or, especially,
   * in case of server unavailability.
   */
  @Element(name = "time-to-live", required = false)
  private int timeToLive;

  /**
   * Determines which type of value-related deadband filtering is applied.
   * <p/>
   * Several types of value-based deadband filtering are possible which are
   * further explained in {@link ValueDeadbandType} Enum
   *
   * @see ValueDeadbandType
   */
  @Element(name = "value-deadband-type", required = false)
  private short valueDeadbandType;

  /**
   * Determines the value deadband for the chosen {@link #valueDeadbandType} 
   *
   * @see ValueDeadbandType
   */
  @Element(name = "value-deadband", required = false)
  private float valueDeadband;

  /**
   * If "timeDeadband" is greater than 0, a new value for the tag must be at
   * least "timeDeadband" milliseconds old before a new value is accepted by
   * the driver. This is to avoid that a (faulty) equipment unit that bombs a
   * driver with values slows down the entire system.
   */
  @Element(name = "time-deadband", required = false)
  private volatile int timeDeadband;

  /**
   * This is a field to indicate that a static timedeadband is activated. Not
   * used on the server, only on the DAQ level.
   */
  private transient boolean staticTimedeadband = false;

  /**
   * Tells the driver how "urgent" the propagation of a tag value update is.
   * Valid priorities are:
   * <UL>
   * <LI>DataTagAddress.PRIORITY_HIGH
   * <LI>DataTagAddress.PRIORITY_LOW
   * </UL>
   * The priority value is also used as a JMS property when the driver sends
   * a tag value update to the server.
   */
  @Element
  private int priority;

  /**
   * Tells the driver how "important" it is that the server receives ALL tag
   * updates for a certain tag. If guaranteedDelivery is enabled,
   * DataTagValueUpdate messages must always reach the server, even in case of
   * a temporary server failure.
   */
  @Element(name = "guaranteed-delivery")
  private boolean guaranteedDelivery;

  /**
   * The time in which a new value is expected. If this time expire the the
   * status of the tag changes to STALE.
   */
  @Element(required = false, name = "freshness-interval")
  private Integer freshnessInterval;

  /**
   * Default constructor creating an uninitialised DataTagAddress object. The
   * HardwareAddress field of the created object will be null. The timeToLive
   * is set to TTL_FOREVER, the deadband is set to DEADBAND_NONE, the priority
   * is PRIORITY_LOW.
   */
  public DataTagAddress() {
    this(TTL_FOREVER, // maximum time-to-live
        ValueDeadbandType.NONE, // no deadband filtering
        0f, // no value deadband
        0, // no time deadband
        JmsMessagePriority.PRIORITY_LOW, // low priority on delivery
        false // no guaranteed message delivery
    );
  }

  /**
   * Constructor Default values: The timeToLive is set to TTL_FOREVER, the
   * deadband is set to DEADBAND_NONE, the transformation factor is set to
   * TRANSFORMATION_NONE, the priority is PRIORITY_LOW.
   *
   * @param hardwareAddress the hardware address for the DataTagAddress object
   */
  public DataTagAddress(HardwareAddress hardwareAddress) {
    this();
    this.hardwareAddress = hardwareAddress;
  }

  /**
   * Constructor Default values: The deadband is set to DEADBAND_NONE, the
   * transformation factor is set to TRANSFORMATION_NONE, the priority is
   * PRIORITY_LOW.
   *
   * @param hardwareAddress the hardware address for the DataTagAddress object
   * @param timeToLive
   * @see #timeToLive
   */
  public DataTagAddress(HardwareAddress hardwareAddress, int timeToLive) {
    this();
    this.hardwareAddress = hardwareAddress;
    this.timeToLive = timeToLive;
  }

  /**
   * Constructor
   *
   * @param hardwareAddress     the hardware address for the DataTagAddress object
   * @param timeToLive          TTL in seconds
   * @param valueDeadbandType   type of value-based deadband filtering
   * @param valueDeadband       parameter for value-based deadband filtering
   * @param timeDeadband        parameter for time-based deadband filtering
   * @param priority            priority of the tag value update message transmission.
   * @param pGuaranteedDelivery JMS guaranteed delivery flag The deadband is
   *                            set to DEADBAND_NONE.
   */
  public DataTagAddress(HardwareAddress hardwareAddress, int timeToLive, ValueDeadbandType valueDeadbandType,
                        float valueDeadband, int timeDeadband, JmsMessagePriority priority, boolean pGuaranteedDelivery) {
    this(timeToLive, valueDeadbandType, valueDeadband, timeDeadband, priority, pGuaranteedDelivery);
    this.hardwareAddress = hardwareAddress;
  }

  /**
   * Constructor Default values: The timeToLive is set to TTL_FOREVER, the
   * deadband is set to DEADBAND_NONE, the transformation factor is set to
   * TRANSFORMATION_NONE, the priority is PRIORITY_LOW.
   *
   * @param addressParameters the address parameters the DataTagAddress object
   */
  public DataTagAddress(HashMap<String, String> addressParameters) {
    this();
    setAddressParameters(addressParameters);
  }

  /**
   * Constructor Default values: The deadband is set to DEADBAND_NONE, the transformation factor is set to
   * TRANSFORMATION_NONE, the priority is PRIORITY_LOW.
   *
   * @param addressParameters the address parameters for the DataTagAddress object
   * @param timeToLive
   * @see #timeToLive
   */
  public DataTagAddress(HashMap<String, String> addressParameters, int timeToLive) {
    this();
    setAddressParameters(addressParameters);
    this.timeToLive = timeToLive;
  }

  /**
   * Constructor with addressParameters as parameters. Node if the parameter is null the value is not set and the
   * default
   * vaule of an empty hashMap is used. Therefore cant be null.
   *
   * @param timeToLive          TTL in seconds
   * @param valueDeadbandType   type of value-based deadband filtering
   * @param valueDeadband       parameter for value-based deadband filtering
   * @param timeDeadband        parameter for time-based deadband filtering
   * @param priority            priority of the tag value update message transmission.
   * @param pGuaranteedDelivery JMS guaranteed delivery flag The deadband is set to DEADBAND_NONE.
   */
  public DataTagAddress(int timeToLive, ValueDeadbandType valueDeadbandType,
                        float valueDeadband, int timeDeadband, JmsMessagePriority priority, boolean pGuaranteedDelivery) {
    this.timeToLive = timeToLive;
    this.valueDeadbandType = valueDeadbandType.getId().shortValue();
    this.valueDeadband = valueDeadband;
    this.timeDeadband = timeDeadband;
    this.priority = priority.getPriority();
    this.guaranteedDelivery = pGuaranteedDelivery;
  }

  /**
   * Returns a new DataTagAddress object that is an exact copy of "this".
   *
   * @return The clone of this data tag address.
   */
  @Override
  public DataTagAddress clone() {
    DataTagAddress clonedAddress = null;
    try {
      clonedAddress = (DataTagAddress) super.clone();
      if (this.hardwareAddress != null) {
        clonedAddress.hardwareAddress = this.hardwareAddress.clone();
      }

      // Map with only primitive types does not require deep cloning
      if (this.addressParameters != null) {
        clonedAddress.addressParameters = new HashMap<>(addressParameters);
      }
    } catch (CloneNotSupportedException e) {
      // Should not happen if the hardware addresses remain as they are.
      log.error("Cloning of DataTagAddress failed: {}", this.toConfigXML(), e.getMessage());
      e.printStackTrace();
    }
    return clonedAddress;
  }

  /**
   * Returns the address parameters for the given DataTag.
   *
   * @return address parameters.
   */
  public Map<String, String> getAddressParameters() {
    return this.addressParameters;
  }

  /**
   * Set the parameters for the address of the given DataTag. This is an central element for this class
   * which provides all information for the daq to create a DataTag.
   *
   * @param addressParameters
   */
  public void setAddressParameters(HashMap<String, String> addressParameters) {
    if (addressParameters != null) {
      this.addressParameters = addressParameters;
    } else {
      this.addressParameters.clear();
    }
  }

  /**
   * Returns true if value-based deadband filtering is enabled for this DataTagAddress object.
   */
  @JsonIgnore
  public boolean isValueDeadbandEnabled() {
    return this.valueDeadbandType != ValueDeadbandType.NONE.getId().shortValue();
  }

  /**
   * Returns true if value-based deadband filtering is enabled for the process
   */
  @JsonIgnore
  public boolean isProcessValueDeadbandEnabled() {
    ValueDeadbandType enumType = ValueDeadbandType.getValueDeadbandType((int) this.valueDeadbandType);
    
    return (enumType == ValueDeadbandType.PROCESS_ABSOLUTE
         || enumType == ValueDeadbandType.PROCESS_RELATIVE
         || enumType == ValueDeadbandType.PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE
         || enumType == ValueDeadbandType.PROCESS_RELATIVE_VALUE_DESCR_CHANGE);
  }

  /**
   * Returns true if time-based deadband filtering is enabled for this DataTagAddress object.
   */
  @JsonIgnore
  public boolean isTimeDeadbandEnabled() {
    return this.timeDeadband > 0;
  }

  public void setTimeDeadband(int value) {
    if (value > 0) {
      this.timeDeadband = value;
    } else {
      this.timeDeadband = 0;
    }
  }
  
  /**
   * Method required for internal deserialization.
   */
  @SuppressWarnings("unused")
  private void setValueDeadbandType(short type) {
    this.valueDeadbandType = type;
  }

  /**
   * Set the type of deadband filter for this DataTagAddress object. If an
   * invalid value is specified, deadband filtering is disabled.
   */
  public void setValueDeadbandType(ValueDeadbandType type) {
    this.valueDeadbandType = type.getId().shortValue();
  }

  /**
   * Set the value for deadband filtering.
   *
   * @param value value for deadband filtering. If a the specified value is
   *              negative, deadband filtering is disabled.
   */
  public void setValueDeadband(float value) {
    if (value >= 0f) {
      this.valueDeadband = value;
    } else {
      this.valueDeadband = 0f;
    }
  }
  
  /**
   * Internal usage only, required for deserialization
   * 
   * @param priority The JMS message priority for a given tag
   */
  @SuppressWarnings("unused")
  private void setPriority(int priority) {
    this.priority = priority;
  }
  
  /**
   * Set the {@link JmsMessagePriority} level for the given tag, which shall be applied 
   * to all value updates that are sent from the DAQ layer to the C2MON server.
   * 
   * @param priority The JMS message priority for a given tag
   */
  public void setPriority(JmsMessagePriority priority) {
    this.priority = priority.getPriority();
  }

  /**
   * Set the time-to-live (TTL) for values sent by a data source.
   *
   * @param ttl the time-to-live in milliseconds. If the specified ttl is less
   *            than 0, ttl defaults to TTL_FOREVER.
   */
  public void setTimeToLive(int ttl) {
    if (ttl < 0) {
      this.timeToLive = TTL_FOREVER;
    } else {
      this.timeToLive = ttl;
    }
  }

  public String toConfigXML() {

    StringBuilder str = new StringBuilder("      <DataTagAddress>\n");

    if (hardwareAddress != null) {
      str.append(hardwareAddress.toConfigXML());
    }

    if (addressParameters != null && !addressParameters.isEmpty()) {
      str.append(SimpleXMLParser.mapToXMLString(addressParameters));
    }

    if (timeToLive != TTL_FOREVER) {
      str.append("        <time-to-live>");
      str.append(timeToLive);
      str.append("</time-to-live>\n");
    }

    if (isValueDeadbandEnabled()) {
      str.append("        <value-deadband-type>");
      str.append(this.valueDeadbandType);
      str.append("</value-deadband-type>\n");

      str.append("        <value-deadband>");
      str.append(valueDeadband);
      str.append("</value-deadband>\n");
    }

    if (isTimeDeadbandEnabled()) {
      str.append("        <time-deadband>");
      str.append(timeDeadband);
      str.append("</time-deadband>\n");
    }

    if (freshnessInterval != null) {
      str.append("        <freshness-interval>");
      str.append(freshnessInterval);
      str.append("</freshness-interval>\n");
    }

    str.append("        <priority>");
    str.append(priority);
    str.append("</priority>\n");


    str.append("        <guaranteed-delivery>");
    str.append(guaranteedDelivery);
    str.append("</guaranteed-delivery>\n");

    str.append("      </DataTagAddress>\n");

    return str.toString();

  }

  public static DataTagAddress fromConfigXML(final String pXML) {
    if (parser == null) {
      try {
        parser = new SimpleXMLParser();
      } catch (ParserConfigurationException e) {
        log.error("fromConfigXML() : Unable to create instance of SimpleXMLParser", e);
        return null;
      }
    }
    if (pXML != null) {
      try {
        return fromConfigXML(parser.parse(pXML).getDocumentElement());
      } catch (ParserException e) {
        log.error("fromConfigXML() : Unable to parse XML for creating DataTagAddress", e);
        return null;
      }
    } else {
      return null;
    }
  }

  public static DataTagAddress fromConfigXML(org.w3c.dom.Element element) {

    DataTagAddress result = new DataTagAddress();

    NodeList fields = element.getChildNodes();
    int fieldsCount = fields.getLength();

    for (int i = 0; i < fieldsCount; i++) {
      Node fieldNode = fields.item(i);
      String fieldName;
      String fieldValueString;

      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        if (fieldName.equals("HardwareAddress")) {
          result.setHardwareAddress(HardwareAddressFactory.getInstance()
              .fromConfigXML((org.w3c.dom.Element) fieldNode));
        } else if (fieldName.equals("address-parameters")) {
          result.setAddressParameters((HashMap<String, String>) SimpleXMLParser.domNodeToMap(fieldNode));
        } else {
          fieldValueString = fieldNode.getFirstChild().getNodeValue();
          if (fieldName.equals("time-to-live")) {
            result.timeToLive = Integer.parseInt(fieldValueString);
          } else if (fieldName.equals("value-deadband-type")) {
            try {
              result.valueDeadbandType = Short.parseShort(fieldValueString);
            } catch (NumberFormatException nfe) {
              result.valueDeadbandType = ValueDeadbandType.NONE.getId().shortValue();
            }
          } else if (fieldName.equals("value-deadband")) {
            result.valueDeadband = Float.parseFloat(fieldValueString);
          } else if (fieldName.equals("time-deadband")) {
            result.timeDeadband = Integer.parseInt(fieldValueString);
          } else if (fieldName.equals("priority")) {
            result.priority = Integer.parseInt(fieldValueString);
          } else if (fieldName.equals("freshness-interval") && !fieldValueString.equals("null")) {
            result.freshnessInterval = Integer.parseInt(fieldValueString);
          } else if (fieldName.equals("guaranteed-delivery")) {
            result.guaranteedDelivery = fieldValueString.equals("true");
          }
        }
      }
    }
    return result;
  }

  public void validate() throws ConfigurationException {
    JmsMessagePriority msgPrio = JmsMessagePriority.getJmsMessagePriority(priority);
    
    if (this.priority != msgPrio.getPriority()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"priority\" must be either 2 (LOW), 4 (MEDIUM) or 7 (HIGH)");
    }
    
    ValueDeadbandType type = ValueDeadbandType.getValueDeadbandType((int) this.valueDeadbandType);
    if (type == ValueDeadbandType.NONE && this.valueDeadbandType != 0) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
            "Invalid value for parameter  \"deadbandType\".");
    }

    if (this.hardwareAddress != null) {
      this.hardwareAddress.validate();
    }
  }
}
