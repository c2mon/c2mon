package cern.c2mon.shared.daq.datatag;


import java.io.Serializable;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.datatag.DataTagAddress;



/**
 * Objects of the SourceDataTagValue class represent the current value of a 
 * SourceDataTag. They are created within the TIM DAQ process whenever a SourceDataTag
 * is updated, usually by a call to the SourceDataTag's update() and invalidate() methods.
 * 
 * Subsequently, an XML representation of the SourceDataTagValue object is sent  
 * to the application server as an XML message. The application
 * server decodes the contents using the fromXML method and propagates the
 * value update to the server representation of the DataTag.
 *
 * A new SourceDataTagValue object is created for each value update. This is 
 * why the class contains no setXXX() method for any of its fields.
 * 
 * Please note that the toXML()/fromXML methods of this class disregard any fields
 * that need not be transmitted to the application server. The fields guaranteedDelivery,
 * priority and timeToLive are only needed as message parameters and are therefore not
 * included in the XML. Therefore, a SourceDataTagValue object created via the fromXML
 * method mayno be equal to the original object (equals() may return false).
 *
 * @author Jan Stowisek
 * @version $Revision: 1.14 $ ($Date: 2008/11/27 10:22:36 $ - $State: Exp $) 
 */
public final class SourceDataTagValue implements Cloneable, Serializable {
  // ----------------------------------------------------------------------------
  // PRIVATE STATIC MEMBERS
  // ----------------------------------------------------------------------------
  public static final String XML_ROOT_ELEMENT = "DataTag";
  
  /**
   * Log4j Logger for the DataTagValueUpdate class.
   */
  protected static final Logger LOG = Logger.getLogger(DataTagValueUpdate.class);

  /**
   * Log4j Logger for logging DataTag values.
   */
  protected static final Logger TAG_LOG = Logger.getLogger("SourceDataTagLogger");

  // ----------------------------------------------------------------------------
  // MEMBERS
  // ----------------------------------------------------------------------------

  /** Unique numeric identifier of the tag */
  protected Long id;

  /** Unique name of the tag */
  protected String name;

  /** Flag indicating whether the tag is used for system supervision */
  protected boolean controlTag;

  /** Current value of the tag (may be null if invalid) */
  protected Object value;

  /** Optional value description **/
  protected String valueDescription="";

  /** Current data quality of the tag (may be null if value is valid) */
  protected SourceDataQuality quality;

  /** Source timestamp of the current value */
  protected Timestamp timestamp;

  /** 
   * Timestamp set locally when the value was last updated
   * (the DAQ server system time at the time the update was performed). 
   */
  protected Timestamp daqTimestamp;
  
  /** Priority with which the tag value has to be sent to the server:
   * either DataTagAddress.PRIORITY_HIGH or DataTagAddress.PRIORITY_LOW 
   */
  protected int priority;

  /**
   * Flag indicating whether a guaranteed delivery mechanism has to be used
   * for transmitting the value to the server.
   */
  protected boolean guaranteedDelivery;

  /**
   * Time to live in milliseconds. If the transmission to the server takes longer,
   * the value is discarded.
   */
  protected int timeToLive;
  
  /**
   * Flag indicating whether the value is the result of a simulation.
   */
  protected boolean simulated;
  
  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------
  
  /**
   * Constructor. The DAQ timestamp is set at object creation.
   */
  public SourceDataTagValue(
		  final Long pId, 
		  final String pName, 
		  final boolean pControlTag
  ) {
    this(
    		pId, 
    		pName, 
    		pControlTag, 
    		null, // value 
    		null, // quality
    		null, // timestamp    	
    		DataTagAddress.PRIORITY_LOW,
            false, // no guaranteed delivery
            null,  // no value description
            DataTagAddress.TTL_FOREVER
     );
  }
  
  /**
   * Constructor. The DAQ timestamp is set at object creation.
   * @param pId
   * @param pName
   * @param pControlTag
   * @param pValue
   * @param pQuality
   * @param pTimestamp
   * @param pPriority
   * @param pGuaranteedDelivery
   * @param pDescription
   * @param pTimeToLive
   */
  public SourceDataTagValue(
    final Long pId, final String pName, final boolean pControlTag, 
    final Object pValue, final SourceDataQuality pQuality, final long pTimestamp,
    final int pPriority, final boolean pGuaranteedDelivery, final String pDescription, final int pTimeToLive
  ) {
    this(
    		pId, 
    		pName, 
    		pControlTag, 
    		pValue, 
    		pQuality, 
    		new Timestamp(pTimestamp),    		
            pPriority, 
            pGuaranteedDelivery, 
            pDescription, 
            pTimeToLive
    );
  }

  /**
   * Constructor. The DAQ timestamp is set at object creation.
   * 
   * @param pId
   * @param pName
   * @param pControlTag
   * @param pValue
   * @param pQuality
   * @param pTimestamp
   * @param pPriority
   * @param pGuaranteedDelivery
   * @param pDescription the value description
   * @param pTimeToLive
   */
  public SourceDataTagValue(
    final Long pId, final String pName, final boolean pControlTag, 
    final Object pValue, final SourceDataQuality pQuality, final Timestamp pTimestamp,
    final int pPriority, final boolean pGuaranteedDelivery, final String pDescription, final int pTimeToLive
  ) {
    this.id = pId;
    this.name = pName;
    this.controlTag = pControlTag;
    this.value = pValue;
    this.setValueDescription(pDescription);
    this.quality = pQuality;
    this.timestamp = pTimestamp;   
    this.priority = pPriority;
    this.guaranteedDelivery = pGuaranteedDelivery;
    this.timeToLive = pTimeToLive;
    this.simulated = false;
    daqTimestamp = new Timestamp(System.currentTimeMillis());
  }

  /**
   * Copy constructor.
   * Create a deep copy of the SourceDataTagValue object passed
   * as a parameter. Notice the DAQ timestamp is also copied: depending 
   * on the context it may make more sense to reset it using the setter method.
   * @param pOld original to be copied.
   */
  public SourceDataTagValue(final SourceDataTagValue pOld) {
        this(
                pOld.id, 
                pOld.name, 
                pOld.controlTag, 
                pOld.value, 
                pOld.quality == null ? null : new SourceDataQuality(pOld.quality), 
                pOld.timestamp,
                pOld.priority, 
                pOld.guaranteedDelivery,
                pOld.valueDescription, 
                pOld.timeToLive
             );
        // also set simulated flag, which is not part of any constructor
        this.simulated = pOld.simulated;
        //override new DAQ timestamp set by constructor
        this.daqTimestamp = pOld.daqTimestamp;
  }

  // ----------------------------------------------------------------------------
  // READ-ONLY MEMBER ACCESSORS and UTILITY METHODS
  // ----------------------------------------------------------------------------
  
  /**
   * Get the DataTag's unique numeric identifier.
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Get the DataTag's unique name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Check whether the DataTag is configured as a control tag.
   * @return true if the DataTag is configured as a control tag.
   */
  public boolean isControlTag() {
    return this.controlTag;
  }

  /**
   * Check whether the DataTag's values shall be transmitted to
   * the server using a guaranteed delivery mechanism.
   */
  public boolean isGuaranteedDelivery() {
    return this.guaranteedDelivery;
  }
  
  /**
   * Check whether the value represented by this object is the
   * result of a simulated.
   * @return true if the value represented by this object is the 
   * result of a simulation.
   */
  public boolean isSimulated() {
	return this.simulated;
  }
  
  /**
   * Indicate whether the value represented by this object is the
   * result of a simulation. 
   * @param pSimulated true if the value represented by this object is
   * the result of a simulation.
   */
  public void setSimulated(final boolean pSimulated) {
	  this.simulated = pSimulated;
  }

  /**
   * Get the message priority to be used for transmitting this object
   * to the server.
   */
  public int getPriority() {
    return this.priority;
  }

  /**
   * Get the time-to-live in milliseconds to be used for transmitting
   * this object to the server. If the message hasn't been received
   * by the server within the specified time-to-live, the message may
   * be discarded (lost!).
   * @return
   */
  public int getTimeToLive() {
    return this.timeToLive;
  }

  /**
   * Get the DataTag's current value.
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * Set the DataTag's current value.
   * @param newValue
   */
  public void setValue(Object newValue) {
    this.value = newValue;
  }

  /**
   * Get the data type of the DataTag's current value as 
   * a string. This method will return null if the tag's
   * current value is null.
   */
  public String getDataType() {
    if (this.value != null) {
      
      return this.value.getClass().getName().substring(10);
    } else {
      return null;
    }
  }

  /**
   * Set a free-text description for the tag's current value.
   */
  public void setValueDescription(final String pDescription) {
    // if description is not defined, store empty string instead of null
    if (pDescription == null)
      this.valueDescription = "";
    else 
      this.valueDescription = pDescription;
  }
  
  /**
   * Get the optional free-text description for the tag's current
   * value.
   */
  public String getValueDescription() {
    return this.valueDescription;
  }

  /**
   * Get the quality of the DataTag's current value.
   * If no quality has been set for this object, we assume that the
   * value is of GOOD quality. Therefore, a new SourceDataQuality object
   * will be returned.
   */
  public SourceDataQuality getQuality() {
    return 
      this.quality != null ? this.quality : new SourceDataQuality(SourceDataQuality.OK);
  }

  /**
   * Set the quality for the DataTag's current value.
   */
  public void setQuality(SourceDataQuality newQuality) {
    this.quality = newQuality;
  }

  /**
   * Check whether the DataTag's current value is valid.
   * A value is considered valid if the associated SourceDataQuality
   * object is valid OR if no SourceDataQuality object is associated 
   * with the value.
   */
  public boolean isValid() {
    return (quality == null || quality.isValid());
  }

  /**
   * Get the source timestamp of the DataTag's current value.
   */
  public Timestamp getTimestamp() {
    return this.timestamp;
  }

  /**
   * Set the timestamp for the DataTag's current value.
   * @param newTimestamp
   */
  public void setTimestamp(Timestamp newTimestamp) {
    this.timestamp = newTimestamp;
  }
  
  /**
   * @param daqTimestamp the daqTimestamp to set
   */
  public void setDaqTimestamp(Timestamp daqTimestamp) {
    this.daqTimestamp = daqTimestamp;
  }
  
  /**
   * @return the daqTimestamp
   */
  public Timestamp getDaqTimestamp() {
    return daqTimestamp;
  }
  

  // ----------------------------------------------------------------------------
  // METHODS FOR XML-IFICATION and DE-XML-IFICATION 
  // ----------------------------------------------------------------------------

  /**
   * Create an XML string from the data contained in this object.
   * As this method is only used by the driver to send DataTagValueUpdate 
   * messages to the application server, the generated XML will never contain
   * any information about priority, guaranteedDelivery and timeToLive
   * @return an XML representation of the SourceDataTagValue object.
   */
  public String toXML() {

    /* Open <DataTag> tag with all its attributes */
    StringBuffer str = new StringBuffer();

    str.append('<');
    str.append(XML_ROOT_ELEMENT);
    str.append(' ');
    str.append(XML_ATTRIBUTE_ID);
    str.append("=\"");
    str.append(id);
    str.append("\" ");
    str.append(XML_ATTRIBUTE_NAME);
    str.append("=\"");
    str.append(name);
    str.append("\" ");
    str.append(XML_ATTRIBUTE_CONTROLTAG);
    str.append("=\"");
    str.append(Boolean.toString(this.controlTag));
    str.append("\">\n");

    /* If the value isn't null, add <value></value> tag */
    if (value != null) {
      str.append("<value data-type=\"");

      /* extract data-type information from the value itself (cutting off
       * the java.lang part of the class name)
       */
      str.append(getDataType());
      str.append("\">");
      str.append(value.toString());
      str.append("</value>\n");
    }
  
    if (valueDescription != null && !valueDescription.equals("")) {
      str.append("<value-description><![CDATA[");
      str.append(valueDescription);
      str.append("]]></value-description>\n");
    }

    /* If the value is invalid, add a <quality></quality> tag */
    if (quality != null && !quality.isValid()) {
      str.append(quality.toXML());
    }

    /* Add <timestamp></timestamp> tag */
    str.append("<timestamp>");
    str.append(timestamp.getTime());
    str.append("</timestamp>\n");
    
    if (daqTimestamp != null) {
      str.append("<daq-timestamp>");
      str.append(daqTimestamp.getTime());
      str.append("</daq-timestamp>\n");
    }
    
    if (simulated) {
    	str.append("<simulated>true</simulated>\n");
    }
    
    /* Close <DataTag> tag */
    str.append("</");
    str.append(XML_ROOT_ELEMENT);
    str.append(">\n");
    String result = str.toString();
    LOG.trace(result);
    return result;
  }

  /**
   * Create a SourceDataTagValue object from a DOM element.
   * As this method is only used on the application server, to received tag
   * value updates sent by a driver, this method will initialise the fields
   * priority, guaranteedDelivery and timeToLive. 
   *
   * @param domElement DOM element
   * SourceDataTagValue object
   * @return a SourceDataTagValue object.
   */

  private static final String XML_ATTRIBUTE_ID = "id";
  private static final String XML_ATTRIBUTE_NAME = "name";
  private static final String XML_ATTRIBUTE_CONTROLTAG = "control";
  
  
  /**
   * Create a SourceDataTagValue from its XML representation.
   * It is ESSENTIAL to know that the following fields are NOT initialised from the XML:
   * <UL>
   *   <LI>guaranteedDelivery</LI>
   *   <LI>priority</LI>
   *   <LI>timeToLive</LI>
   * </UL>
   * @return
   */
  
  public static SourceDataTagValue fromXML(Element domElement) { 

    /* We assume that the root element really is <DataTag> as this method is
     * only called from DataTagValueUpdate.fromXML if a <DataTag> element is found.
     */
    Long id = null;
    String name;
    boolean control;
    SourceDataTagValue result = null;
 
    /* Try to extract the datatag id from the XML content */
    try {
      id = Long.valueOf(domElement.getAttribute(XML_ATTRIBUTE_ID));
    } catch (NumberFormatException nfe) {
      LOG.error("Cannot extract valid id attribute from <DataTag> element.");
      throw nfe;
    }    

    /* Only proceed if the id has been extracted successfully */
    if (id != null) {
      name = domElement.getAttribute(XML_ATTRIBUTE_NAME);
      if (domElement.getAttribute(XML_ATTRIBUTE_CONTROLTAG) != null){
        control = domElement.getAttribute(XML_ATTRIBUTE_CONTROLTAG).equals("true");
      } else {
        throw new RuntimeException("Control tag attribute not set in SourceDataTagValue XML - unable to decode it.");
      }
     
      result = new SourceDataTagValue(id, name, control);

      NodeList fields = domElement.getChildNodes();
      String fieldName;
      String fieldValueString;
      Node fieldNode;
      
      int fieldsCount = fields.getLength();

      for (int i = 0; i != fieldsCount; i++) {
        fieldNode = fields.item(i);
        if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
          fieldName = fieldNode.getNodeName();
          if (fieldNode.getFirstChild() != null) {
            fieldValueString = fieldNode.getFirstChild().getNodeValue();         
            if (fieldName.equals("value")) {
              String dataType = fieldNode.getAttributes().item(0).getNodeValue();
            
              if (dataType.equals("Integer")) {
                result.value = Integer.valueOf(fieldValueString);
              } else if (dataType.equals("Float")) {
                result.value = Float.valueOf(fieldValueString);
              } else if (dataType.equals("Double")) {
                result.value = Double.valueOf(fieldValueString);
              } else if (dataType.equals("Long")) {
                result.value = Long.valueOf(fieldValueString);
              } else if (dataType.equals("Boolean")) {
                result.value = Boolean.valueOf(fieldValueString);
              } else if (dataType.equals("String")) {
                result.value = fieldValueString;
              }
            }
            else if (fieldName.equals("value-description")) 
            {
              result.valueDescription = fieldNode.getFirstChild().getNodeValue();
            }
            else if (fieldName.equals("quality")) {
              result.quality = SourceDataQuality.fromXML((Element) fieldNode);
            }
            else if (fieldName.equals("timestamp")) {
              try {
                result.timestamp = new Timestamp(Long.parseLong(fieldValueString));
              } catch (NumberFormatException nfe) {
                LOG.error("Error during timestamp extraction.");
                result.timestamp = new Timestamp(System.currentTimeMillis());
              }
            }
            else if (fieldName.equals("daq-timestamp")) {
              try {
                result.daqTimestamp = new Timestamp(Long.parseLong(fieldValueString));
              } catch (NumberFormatException nfe) {
                LOG.error("Error during DAQ timestamp extraction - leaving null.");
                result.daqTimestamp = null;
              }
            }
            else if (fieldName.equals("simulated")) {
              result.simulated = true;
            }
          }
        }
      }

      // If no quality was specified in the XML, we assume that the value is valid
      if (result.quality == null) {
        result.quality = new SourceDataQuality();
      }
    }
    return result;
  }

  /**
   * Create a clone of this SourceDataTagValue object.
   */
  public SourceDataTagValue clone() {
    return new SourceDataTagValue(this);
  }

  public void log() {
    if (TAG_LOG.isInfoEnabled()) {
      TAG_LOG.info(this);
    }    
  }
  
  public boolean equals(final Object pObj) {
	boolean result = pObj != null && pObj instanceof SourceDataTagValue;
	if (result) {
	  SourceDataTagValue copy = (SourceDataTagValue)pObj;
	  result = 
		  this.controlTag == copy.controlTag &&
		  this.guaranteedDelivery == copy.guaranteedDelivery &&
		  this.simulated == copy.simulated &&
		  this.priority == copy.priority;
	  if (result) {
		if (this.value != null) {
			result = result && this.value.equals(copy.value);
		}
		if (this.valueDescription != null) {
			result = result && this.valueDescription.equals(copy.valueDescription);
		}
		if (this.id != null) {
			result = result && this.id.equals(copy.id);
		}
		if (this.name != null) {
			result = result && this.name.equals(copy.name);
		}
		if (this.quality != null) {
			result = result && this.quality.equals(copy.quality);
		}
		if (this.timestamp != null) {
			result = result && this.timestamp.equals(copy.timestamp);
		}
		if (this.daqTimestamp != null) {
      result = result && this.daqTimestamp.equals(copy.daqTimestamp);
    }
	  }
		  
		  
		  
		
	}
	return result;
  }
  
}
