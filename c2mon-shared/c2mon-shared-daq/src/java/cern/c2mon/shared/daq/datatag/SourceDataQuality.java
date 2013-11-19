package cern.c2mon.shared.daq.datatag;


import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * SourceDataQuality
 * Represents data quality information sent by a data source
 *
 * @author Jan Stowisek
 * @version $Revision: 1.21 $ ($Date: 2007/07/04 12:38:55 $ - $State: Exp $)
 * @see cern.c2mon.shared.common.datatag.DataTagCacheObject
 * @see cern.c2mon.server.datatag.impl.DataTagBean
 */
 
public final class SourceDataQuality implements Serializable {
  // ----------------------------------------------------------------------------
  // CONSTANT DEFINITIONS
  // ----------------------------------------------------------------------------
  /** Version number of the class used during serialization/deserialization.
   * This is to ensure that minore changes to the class do not prevent us
   * from reading back SourceDataQuality objects we have serialized earlier. If
   * fields are added/removed from the class, the version number needs to 
   * change.
   */
  public  static final String XML_ROOT_ELEMENT="quality";
  private static final String XML_ELEMENT_QUALITY_CODE="code";
  private static final String XML_ELEMENT_QUALITY_DESC="desc";

  static final long serialVersionUID = -234567L;

  /** An array containing a textual description for the quality codes. Make sure 
   * that this array is always updated when you add/remove a quality code!
   */
  private static final String[] codeDescriptors = {
    "OK", 
    "Value out of range",
    "Corrupted source value",
    "Data type conversion error",
    "Value unavailable",
    "Unknown",
    "Unsupported data type",
    "Incorrect hardware address",
    "Source timestamp in the future"
  };

  /**
   * Quality code representing a VALID SourceDataTag value.
   */
  public static final short OK = 0;

  /**
   * Quality code representing a SourceDataTag value that is outside
   * the min/max range defined for the DataTag.
   */
  public static final short OUT_OF_BOUNDS = 1;

  /**
   * Quality code representing a SourceDataTag value that has been 
   * corrupted before it was received by the DAQ.
   */
  public static final short VALUE_CORRUPTED = 2;

  /** 
   * This quality code must be set if source data cannot be converted to TIM
   * data because source and TIM data type are not compatible 
   * (for example: float -> integer)
   */
  public static final short CONVERSION_ERROR = 3;

  /**
   * Quality code representing a SourceDataTag value that is currently not
   * available from the source. <br>
   * Please use that error code with care, because it implicitly allows
   * that it gets overwritten in the server cache by an update with an older
   * time stamp.
   * @see cern.c2mon.shared.common.datatag.DataTagQuality#INACCESSIBLE
   * @see cern.c2mon.shared.common.datatag.DataTagCacheObject
   */
  public static final short DATA_UNAVAILABLE = 4;

  /**
   * Quality code representing a SourceDataTag value that is invalid for
   * an unknown reason or for a reason not covered by the other quality
   * codes.
   */
  public static final short UNKNOWN = 5;
  
  /**
   * Quality code representing a SourceDataTag value that cannot be decoded
   * because the data type sent by the source is not supported by the handler.
   */
  public static final short UNSUPPORTED_TYPE = 6;

  /**
   * Quality code representing a SourceDataTag value that cannot be acquired
   * because of an error in the tag's hardware address.
   */
  public static final short INCORRECT_NATIVE_ADDRESS = 7;

  
  /**
   * Quality code representing a SourceDataTag value that is received from the equipment with 
   * source timestamp set in the future in relation to the DAQ time, at the moment of acquisition
   */  
  public static final short FUTURE_SOURCE_TIMESTAMP = 8;

  // ----------------------------------------------------------------------------
  // PRIVATE MEMBERS
  // ----------------------------------------------------------------------------

  /** quality code sent by the data source */
  private short qualityCode;

  /** optional detailed quality description from the data source */
  private String description = null;

  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------

  /**
   * Default constructor 
   * Sets the quality code to OK and the description to null
   */
  public SourceDataQuality() {
    this(OK, null);
  }

  /**
   * Constructor 
   * The description is set to a null string by default.
   * @param code  quality code sent by the data source
   */
  public SourceDataQuality(final short pCode) {
    this(pCode, null);
  }

  /**
   * Constructor 
   * @param code        quality code sent by the data source
   * @param description additional info about the quality sent by the source
   */
  public SourceDataQuality(final short pCode, final String pDescription) {
    setQualityCode(pCode);
    setDescription(pDescription);
  }

  /**
   * Copy constructor
   * @param old the SourceDataQuality object serving as a template for the one
   * to be created.
   */
  public SourceDataQuality(final SourceDataQuality pOld) {
    this(pOld.qualityCode, pOld.description);
  }

  // ----------------------------------------------------------------------------
  // ACCESSOR METHODS
  // ----------------------------------------------------------------------------
  /**
   * Get the current quality code
   * @return the quality code
   */
  public final short getQualityCode() {
    return this.qualityCode;
  }

  /**
   * Set a new quality code
   * @param code  the quality code sent by the data source
   */
  public final void setQualityCode(final short pCode) {
    if (pCode >= 0 && pCode < codeDescriptors.length) {
      this.qualityCode = pCode;
    } else {
      this.qualityCode = UNKNOWN;
    }
  }

  /**
   * Get the detailed quality description as set by the data source
   * @return the textual quality description (may be null)
   */
  public final String getDescription() {
    return this.description;
  }

  /**
   * Set the detailed quality description
   * @param description   the detailed quality description (may be null)
   */
  public final void setDescription(final String pDescription) {
    this.description = pDescription;
  }

  // ----------------------------------------------------------------------------
  // BUSINESS METHODS
  // ----------------------------------------------------------------------------
  /**
   * This method returns a more complete description of the source value's data 
   * quality, taking into account the meaning of the quality code as well as the
   * optional description set by the user.
   * @return the textual representation of the quality code as well as the detailed
   * description sent by the data source (if any)
   */
  public final String getFullDescription() {
    StringBuffer desc = null;
    desc = new StringBuffer(getDefaultDescription(this.qualityCode));
    if (this.description != null) {
        desc.append(" - ");
        desc.append(this.description);
    }
    return desc.toString();
  }
  
  /**
   * Utility method for obtaining a textual representation of a quality code.
   * For example, for SourceDataQuality.OK the method will return "OK". 
   * If an undefined quality code is passed to the method as a parameter, it
   * will return the description for SourceDataQuality.UNKNOWN.
   * @param pCode the (defined) quality code for which you want to obtain a description. 
   * @return
   */
  public static final String getDefaultDescription(final short pCode) {
	  String desc = null;
	  try {
        desc = codeDescriptors[pCode];
	  } catch (ArrayIndexOutOfBoundsException e) {
	    desc = codeDescriptors[UNKNOWN];
      } 
	  return desc;
  }

  /**
   * Check whether this SourceDataQuality object represents a VALID value.
   * The method will return true if the quality code is SourceDataQuality.OK
   * @return true if the quality object represents a "valid" object
   */
  public final boolean isValid() {
    return qualityCode == SourceDataQuality.OK;
  }

  /**
   * Check whether this SourceDataQuality object represents a VALID value.
   * The method will return true if the quality code is anything else but 
   * SourceDataQuality.OK
   * @return true if the quality object represents an invalid tag value.
   */
  public final boolean isInvalid() {
    return qualityCode != SourceDataQuality.OK;
  }

  // ----------------------------------------------------------------------------
  // UTILITY METHODS
  // ----------------------------------------------------------------------------
  public final String toString() {
    return getFullDescription();
  }
  
  /**
   * Create an XML representation of a SourceDataQuality object.
   * The resulting XML can be used to create a SourceDataQuality object
   * using the class's fromXML method.
   * @return
   */
  public String toXML() {

    /* Open <quality> tag */
    StringBuffer str = new StringBuffer();

    str.append("<");
    str.append(XML_ROOT_ELEMENT);
    str.append(">");

    /* Add <code></code> element */
    str.append("<");
    str.append(XML_ELEMENT_QUALITY_CODE);
    str.append(">");
    str.append(qualityCode);
    str.append("</");
    str.append(XML_ELEMENT_QUALITY_CODE);
    str.append(">");

    /* If there is a description, add <description></description> element */
    if (description != null) {
      str.append("<");
      str.append(XML_ELEMENT_QUALITY_DESC);
      str.append("><![CDATA[");
      str.append(description);
      str.append("]]></");
      str.append(XML_ELEMENT_QUALITY_DESC);
      str.append(">");
    }

    /* Close <quality> tag */
    str.append("</");
    str.append(XML_ROOT_ELEMENT);
    str.append(">\n");

    return str.toString();
  }

  /**
   * Create a SourceDataQuality object from its XML representation.
   * The format of the XML required is determined by the output of the
   * toXML() method.
   * @param domElement
   * @return
   */
  public static SourceDataQuality fromXML(Element domElement) {
    NodeList fields = domElement.getChildNodes();
    int fieldsCount = fields.getLength();
    String fieldName;
    String fieldValueString;
    Node fieldNode;

    // Create result object
    SourceDataQuality result = new SourceDataQuality();

    // Extract information from DOM elements
    for (int i = 0; i != fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        Node fieldValueNode = fieldNode.getFirstChild();
        if (fieldValueNode != null) {
          fieldValueString = fieldValueNode.getNodeValue();
        }
        else {
          fieldValueString = "";
        }

        if (fieldName.equals(XML_ELEMENT_QUALITY_CODE)) {
          result.qualityCode = Short.parseShort(fieldValueString);
        }
        else if (fieldName.equals(XML_ELEMENT_QUALITY_DESC)) {
          result.description = fieldValueString;
        }
      }
    }
    
    return result;
  }
  
  /**
   * Determine whether two SourceDataQuality objects are equal.
   * Two SourceDataQuality objects are considered equal if they have
   * the same quality code AND the same quality description.
   */
  public boolean equals(Object o) {
	  boolean result = false;
	  if (o != null && o instanceof SourceDataQuality) {
		 result = this.qualityCode == ((SourceDataQuality)o).qualityCode;
		 if (this.description == null) {
			 result = result && (((SourceDataQuality)o).description == null);
		 }
		 else {
			 result = result && this.description.equals(((SourceDataQuality)o).description);	 
		 }
			 
	  }
	  return result;
  }
  
  /**
   * Return a clone of this SourceDataQuality object
   */
  public Object clone() {
    return new SourceDataQuality(this);
  }
}
