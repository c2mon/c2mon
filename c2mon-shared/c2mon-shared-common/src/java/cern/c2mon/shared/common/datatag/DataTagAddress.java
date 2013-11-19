package cern.c2mon.shared.common.datatag;

import java.io.Serializable;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.util.parser.ParserException;
import cern.c2mon.util.parser.SimpleXMLParser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

/**
 * Address associated with a DataTag DataTags are linked to data sources (e.g. PLCs, external SCADA systems etc.) via a
 * DataTagAddress object. This object contains the configuration parameters needed by a DAQ module to acquire values
 * from the data source. In addition to data acquisition parameters, the DataTagAddress object also configures deadband
 * filtering and transformation factors for a given tag.
 * 
 * @author Jan Stowisek
 * @version $Revision: 1.19 $ ($Date: 2007/07/04 12:38:55 $ - $State: Exp $)
 */

public class DataTagAddress implements Serializable, Cloneable, DataTagConstants {
    // ----------------------------------------------------------------------------
    // CONSTANT DEFINITIONS
    // ----------------------------------------------------------------------------

    /**
     * Version number of the class used during serialization/deserialization. This is to ensure that minor changes to
     * the class do not prevent us from reading back DataTagAddress objects we have serialized earlier. If fields are
     * added/removed from the class, the version number needs to change.
     */
    private static final long serialVersionUID = -145678L;

    /**
     * Log4j Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(DataTagAddress.class);

    /**
     * XML parser for this class (initialised in static initialiser)
     */
    private static transient SimpleXMLParser parser = null;

    // ----------------------------------------------------------------------------
    // PRIVATE MEMBERS
    // ----------------------------------------------------------------------------
    /**
     * Equipment-specific hardware address. The HardwareAddress objects is only used by the EquipmentMessageHandler. It
     * contains all equipment-specific parameters the message handler needs to acquire values for a data tag. The
     * HardwareAddress class is abstract, so the object will always be of a subclass of HardwareAddress (e.g.
     * PLCEquipmentAddress for the JECEquipmentHandler).
     */
    private HardwareAddress hardwareAddress;

    /**
     * Time-to-live in milliseconds. Values that have not been received by the application server before the expiration
     * of the TTL are discarded. The ttl is only relevant in times of great (huge) server load or, especially, in case
     * of server unavailability.
     */
    private int timeToLive;

    /**
     * Several types of value-based deadband filtering are possible and must be supported by all TIM drivers :
     * 
     * <PRE>
     * DEADBAND_ABSOLUTE, DEADBAND_RELATIVE, 
     * DEADBAND_NONE
     * </PRE>
     */
    private short valueDeadbandType;

    /**
     * Determines which type of value-related deadband filtering is applied.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagDeadband
     */
    private float valueDeadband;

    /**
     * If "timeDeadband" is greater than 0, a new value for the tag must be at least "timeDeadband" milliseconds old
     * before a new value is accepted by the driver. This is to avoid that a (faulty) equipment unit that bombs a driver
     * with values slows down the entire system.
     */
    private volatile int timeDeadband;

    /**
     * This is a field to indicate that a static timedeadband is activated. Not used on the server, only on the DAQ
     * level.
     */
    private transient boolean staticTimedeadband = false;

    /**
     * Tells the driver how "urgent" the propagation of a tag value update is. Valid priorities are:
     * <UL>
     * <LI>DataTagAddress.PRIORITY_HIGH
     * <LI>DataTagAddress.PRIORITY_LOW
     * </UL>
     * The priority value is also used as a JMS property when the driver sends a tag value update to the server.
     */
    private int priority;

    /**
     * Tells the driver how "important" it is that the server receives ALL tag updates for a certain tag. If
     * guaranteedDelivery is enabled, DataTagValueUpdate messages must always reach the server, even in case of a
     * temporary server failure.
     */
    private boolean guaranteedDelivery;

    private volatile ValueChangeMonitor valueChangeMonitor;

    // ----------------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------------
    /**
     * Default constructor creating an uninitialised DataTagAddress object. The HardwareAddress field of the created
     * object will be null. The timeToLive is set to TTL_FOREVER, the deadband is set to DEADBAND_NONE, the priority is
     * PRIORITY_LOW.
     */
    public DataTagAddress() {
        this(null, // no hardware address
                TTL_FOREVER, // maximum time-to-live
                DataTagDeadband.DEADBAND_NONE, // no deadband filtering
                0f, // no value deadband
                0, // no time deadband
                DataTagAddress.PRIORITY_LOW, // low priority on delivery
                false // no guaranteed message delivery
        );
    }

    /**
     * Constructor Default values: The timeToLive is set to TTL_FOREVER, the deadband is set to DEADBAND_NONE, the
     * transformation factor is set to TRANSFORMATION_NONE, the priority is PRIORITY_LOW.
     * 
     * @param hardwareAddress the hardware address for the DataTagAddress object
     */
    public DataTagAddress(HardwareAddress hardwareAddress) {
        this(hardwareAddress, TTL_FOREVER, // maximum time-to-live
                DataTagDeadband.DEADBAND_NONE, // no value deadband filtering
                0f, // no value deadband
                0, // no time deadband filtering
                DataTagAddress.PRIORITY_LOW, // low JMS priority on delivery
                false // no guaranteed message delivery
        );
    }

    /**
     * Constructor Default values: The deadband is set to DEADBAND_NONE, the transformation factor is set to
     * TRANSFORMATION_NONE, the priority is PRIORITY_LOW.
     * 
     * @param hardwareAddress the hardware address for the DataTagAddress object
     * @param timeToLive
     * @see #timeToLive
     */
    public DataTagAddress(HardwareAddress hardwareAddress, int timeToLive) {
        this(hardwareAddress, timeToLive, DataTagDeadband.DEADBAND_NONE, 0f, 0, DataTagAddress.PRIORITY_LOW, false);
    }

    /**
     * Constructor
     * 
     * @param hardwareAddress the hardware address for the DataTagAddress object
     * @param timeToLive TTL in seconds
     * @param valueDeadbandType type of value-based deadband filtering
     * @param valueDeadband parameter for value-based deadband filtering
     * @param timeDeadband parameter for time-based deadband filtering
     * @param priority priority of the tag.
     * @param pGuaranteedDelivery JMS guaranteed delivery flag The deadband is set to DEADBAND_NONE.
     */
    public DataTagAddress(HardwareAddress hardwareAddress, int timeToLive, short valueDeadbandType,
            float valueDeadband, int timeDeadband, int priority, boolean pGuaranteedDelivery) {
        this.hardwareAddress = hardwareAddress;
        this.timeToLive = timeToLive;
        this.valueDeadbandType = valueDeadbandType;
        this.valueDeadband = valueDeadband;
        this.timeDeadband = timeDeadband;
        this.priority = priority;
        this.guaranteedDelivery = pGuaranteedDelivery;
    }

    /**
     * Returns a new DataTagAddress object that is an exact copy of "this".
     * 
     * @return The clone of this data tag address.
     */
    public DataTagAddress clone() {
        DataTagAddress clonedAddress = null;
        try {
            clonedAddress = (DataTagAddress) super.clone();
            HardwareAddress hardwareAddress = getHardwareAddress();
            if (hardwareAddress != null)
                clonedAddress.setHardwareAddress(getHardwareAddress().clone());
        } catch (CloneNotSupportedException e) {
            // Should not happen if the hardware addresses remain as they are.
            e.printStackTrace();
        }
        return clonedAddress;
    }

    /**
     * Returns the hardware address for this DataTagAddress object. This value MUST NOT be null, otherwise the tag's
     * value cannot be acquired by the driver.
     */
    public HardwareAddress getHardwareAddress() {
        return this.hardwareAddress;
    }

    /**
     * Set the hardware address for the DataTag This is the central element of the DataTagAddress class. The hardware
     * address is a free-text string that can only be interpreted by the EquipmentMessageHandler class of the driver.
     */
    public void setHardwareAddress(HardwareAddress address) {
        this.hardwareAddress = address;
    }

    /**
     * Returns true if value-based deadband filtering is enabled for this DataTagAddress object.
     */
    public boolean isValueDeadbandEnabled() {
        return this.valueDeadbandType != DataTagDeadband.DEADBAND_NONE;
    }

    /**
     * Returns true if value-based deadband filtering is enabled for the process
     */
    public boolean isProcessValueDeadbandEnabled() {
        if (valueDeadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE
                || valueDeadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE
                || valueDeadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE
                || valueDeadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true if time-based deadband filtering is enabled for this DataTagAddress object.
     */
    public boolean isTimeDeadbandEnabled() {
        return this.timeDeadband > 0;
    }

    /**
     * If time-based deadband f iltering is enabled, this method returns the minimum number of milliseconds between two
     * values for the tag. If a new value is sent by the equipment more frequently, some of the values are to be
     * rejected by the driver. If the time-based deadband is 0, all values sent by the equipment are to be processed by
     * the system.
     */
    public int getTimeDeadband() {
        return this.timeDeadband;
    }

    public void setTimeDeadband(int value) {
        if (value > 0) {
            this.timeDeadband = value;
        } else {
            this.timeDeadband = 0;
        }
    }

    /**
     * Returns the type of value-based deadband filter that is enabled for this DataTagAddress object: DEADBAND_NONE,
     * DEADBAND_ABSOLUTE, DEADBAND_RELATIVE.
     */
    public short getValueDeadbandType() {
        return this.valueDeadbandType;
    }

    /**
     * Returns the value used for deadband filtering.
     */
    public float getValueDeadband() {
        return this.valueDeadband;
    }

    /**
     * Set the type of deadband filter for this DataTagAddress object. If an invalid value is specified, deadband
     * filtering is diabled.
     */
    public void setValueDeadbandType(final short type) {
        if (DataTagDeadband.isValidType(type)) {
            this.valueDeadbandType = type;
        } else {
            this.valueDeadbandType = DataTagDeadband.DEADBAND_NONE;
            this.valueDeadband = 0f;
        }
    }

    /**
     * Set the value for deadband filtering.
     * 
     * @param value value for deadband filtering. If a the specified value is negative, deadband filtering is disabled.
     */
    public void setValueDeadband(float value) {
        if (value >= 0f) {
            this.valueDeadband = value;
        } else {
            this.valueDeadband = 0f;
        }
    }

    /**
     * @return TTL in milliseconds.
     */
    public int getTimeToLive() {
        return this.timeToLive;
    }

    /**
     * Set the time-to-live (TTL) for values sent by a data source.
     * 
     * @param ttl the time-to-live in milliseconds. If the specified ttl is less than 0, ttl defaults to TTL_FOREVER.
     */
    public void setTimeToLive(int ttl) {
        if (ttl < 0) {
            this.timeToLive = TTL_FOREVER;
        } else {
            this.timeToLive = ttl;
        }
    }

    /**
     * Returns the priority of the tag. DataTagAddress.PRIORITY_LOW or DataTagAddress.PRIORITY_HIGH are accepted values.
     */
    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        if (priority == DataTagAddress.PRIORITY_HIGH || priority == DataTagAddress.PRIORITY_LOW) {
            this.priority = priority;
        }
    }

    /**
     * Returns true if guaranteed message delivery is enabled in this tag's configuration.
     */
    public boolean isGuaranteedDelivery() {
        return this.guaranteedDelivery;
    }

    /**
     * Enable/disable guaranteed message delivery for this tag.
     */
    public void setGuaranteedDelivery(boolean gd) {
        this.guaranteedDelivery = gd;
    }

    public String toConfigXML() {

        StringBuilder str = new StringBuilder("      <DataTagAddress>\n");

        if (hardwareAddress != null) {
            str.append(hardwareAddress.toConfigXML());
        }

        if (valueChangeMonitor != null) {
            str.append(valueChangeMonitor.toString());
        }
        
        if (timeToLive != TTL_FOREVER) {
            str.append("        <time-to-live>");
            str.append(timeToLive);
            str.append("</time-to-live>\n");
        }

        if (valueDeadbandType != DataTagDeadband.DEADBAND_NONE) {
            str.append("        <value-deadband-type>");
            str.append(this.valueDeadbandType);
            str.append("</value-deadband-type>\n");

            str.append("        <value-deadband>");
            str.append(valueDeadband);
            str.append("</value-deadband>\n");
        }

        if (timeDeadband != DataTagDeadband.DEADBAND_NONE) {
            str.append("        <time-deadband>");
            str.append(timeDeadband);
            str.append("</time-deadband>\n");
        }

        str.append("        <priority>");
        str.append(priority);
        str.append("</priority>\n");

        str.append("        <guaranteed-delivery>");
        str.append(guaranteedDelivery);
        str.append("</guaranteed-delivery>\n");

        /*
         * if (minValue != null) { str.append("        <min-value data-type=\"");
         * str.append(minValue.getClass().getName().substring(10)); str.append("\">"); str.append(minValue);
         * str.append("</min-value>\n"); }
         * 
         * if (maxValue != null) { str.append("        <max-value data-type=\"");
         * str.append(maxValue.getClass().getName().substring(10)); str.append("\">"); str.append(maxValue);
         * str.append("</max-value>\n"); }
         */

        str.append("      </DataTagAddress>\n");

        return str.toString();

    }

    public static DataTagAddress fromConfigXML(final String pXML) {
        if (parser == null) {
            try {
                parser = new SimpleXMLParser();
            } catch (ParserConfigurationException e) {
                LOG.error("fromConfigXML() : Unable to create instance of SimpleXMLParser", e);
                return null;
            }
        }
        if (pXML != null) {
            try {
                return fromConfigXML(parser.parse(pXML).getDocumentElement());
            } catch (ParserException e) {
                LOG.error("fromConfigXML() : Unable to parse XML for creating DataTagAddress", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static DataTagAddress fromConfigXML(Element element) {

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
                    result.setHardwareAddress(HardwareAddressFactory.getInstance().fromConfigXML((Element) fieldNode));
                } else if (fieldName.equals("value-change-monitor")) {
                    result.valueChangeMonitor = ValueChangeMonitor.fromConfigXML((Element) fieldNode);
                } else {
                    fieldValueString = fieldNode.getFirstChild().getNodeValue();
                    if (fieldName.equals("time-to-live")) {
                        result.timeToLive = Integer.parseInt(fieldValueString);
                    } else if (fieldName.equals("value-deadband-type")) {
                        try {
                            result.valueDeadbandType = Short.parseShort(fieldValueString);
                        } catch (NumberFormatException nfe) {
                            result.valueDeadbandType = DataTagDeadband.DEADBAND_NONE;
                        }
                    } else if (fieldName.equals("value-deadband")) {
                        result.valueDeadband = Float.parseFloat(fieldValueString);
                    } else if (fieldName.equals("time-deadband")) {
                        result.timeDeadband = Integer.parseInt(fieldValueString);
                    } else if (fieldName.equals("priority")) {
                        result.priority = Integer.parseInt(fieldValueString);
                    } else if (fieldName.equals("guaranteed-delivery")) {
                        result.guaranteedDelivery = fieldValueString.equals("true");
                    }
                }
            }
        }
        return result;
    }

    public boolean equals(Object pObj) {
        boolean result = pObj instanceof DataTagAddress;
        if (result) {
            DataTagAddress addr = (DataTagAddress) pObj;
            result = this.guaranteedDelivery == addr.guaranteedDelivery && this.priority == addr.priority
                    && this.timeDeadband == addr.timeDeadband && this.timeToLive == addr.timeToLive
                    && this.valueDeadband == addr.valueDeadband && this.valueDeadbandType == addr.valueDeadbandType;
            result = result && this.hardwareAddress == null ? addr.hardwareAddress == null : this.hardwareAddress
                    .equals(addr.hardwareAddress);

        }
        return result;
    }

    public void validate() throws ConfigurationException {
        if (this.priority != PRIORITY_HIGH && this.priority != PRIORITY_MEDIUM && this.priority != PRIORITY_LOW) {
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "Parameter \"priority\" must be either 2 (LOW) or 7 (HIGH)");
        }
        switch (this.valueDeadbandType) {
        case DataTagDeadband.DEADBAND_NONE:
            break;
        case DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE:
            break;
        case DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE:
            break;
        case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE:
            break;
        case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE:
            break;
        case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
            break;
        case DataTagDeadband.DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE:
            break;
        default:
            throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                    "Invalid value for parameter  \"deadbandType\".");
        }

        if (this.hardwareAddress != null) {
            this.hardwareAddress.validate();
        }
    }

    /**
     * Sets the static timedeadband to true timedeadband will not be deactivated.
     * 
     * @param staticTimedeadband True if static timedeadband should e active elese false.
     */
    public void setStaticTimedeadband(final boolean staticTimedeadband) {
        this.staticTimedeadband = staticTimedeadband;
    }

    /**
     * The static timedeadband switch.
     * 
     * @return True if static timedeadband should e active elese false.
     */
    public boolean isStaticTimedeadband() {
        return staticTimedeadband;
    }

    public ValueChangeMonitor getValueCheckMonitor() {
        return this.valueChangeMonitor;
    }

    public boolean hasValueCheckMonitor() {
        return this.valueChangeMonitor == null ? false : true;
    }

}
