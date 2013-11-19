package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;


/**
 * Implementation of the DIPHardwareAddress interface.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.3 $ ($Date: 2007/07/04 12:39:13 $ - $State: Exp $)
 * @see cern.c2mon.shared.common.datatag.address.DIPHardwareAddress
 */

public final class DIPHardwareAddressImpl extends HardwareAddressImpl implements DIPHardwareAddress {
  private static final long serialVersionUID = 4223251625259225608L;
  // ---------------------------------------------------------------------------
  // Private member definitions
  // ---------------------------------------------------------------------------
  /** 
   * The name of the DIP publication (DIP item) to which we subscribe.
   */  
  protected String itemName;

  /** 
   * If the DIP publication is a structure, this field specifies the field name 
   * of the desired value within the structure.
   */  
  protected String fieldName;

  /** 
   * If the DIP data type is an array type, this field specifies the index
   * of the desired value within the array.
   */
  protected int fieldIndex = -1;
  
  // ---------------------------------------------------------------------------
  // Constructors
  // ---------------------------------------------------------------------------

  /**
   * Create a simple DIPHardwareAddress object.
   * @param pItemName name of the DIP publication
   */
  public DIPHardwareAddressImpl(final String pItemName) throws ConfigurationException {
    this(pItemName, null, -1);
  }

  /**
   * Create a DIPHardwareAddress object for a field within a structure.
   * Example: "item.field"
   * @param pItemName name of a structured DIP publication
   * @param pFieldName field name the desired value within the structure
   */
  public DIPHardwareAddressImpl(final String pItemName, final String pFieldName) throws ConfigurationException {
    this(pItemName, pFieldName, -1);
  }

  /**
   * Create a DIPHardwareAddress object for an array element.
   * Example: "item[index]"
   * @param pItemName name of a DIP publication
   * @param pFieldIndex array index of the desired value within the item
   */
  public DIPHardwareAddressImpl(final String pItemName, final int pFieldIndex) throws ConfigurationException {
    this(pItemName, null, pFieldIndex);
  }

  /**
   * Create a DIPHardwareAddress object for an array element within a structure.
   * Example: "item.field[index]"
   * @param pItemName name of a structured DIP publication
   * @param pFieldName name of an array field within the publication
   * @param pFieldIndex array index of the desired value within the field
   */
  public DIPHardwareAddressImpl(final String pItemName, final String pFieldName, final int pFieldIndex) throws ConfigurationException {
    setItemName(pItemName);
    setFieldName(pFieldName);
    setFieldIndex(pFieldIndex);
  }

  /**
   * Internal constructor required by the fromConfigXML method of the super class.
   */
  protected DIPHardwareAddressImpl() {
    /* Nothing to do */
  }

  // ---------------------------------------------------------------------------
  // Public accessor methods
  // ---------------------------------------------------------------------------

  /** 
   * Get the name of the DIP publication (DIP item).
   * The item name can never be null.
   * @return the name of the DIP publication
   */
  public final String getItemName() {
    return this.itemName;
  }

  /**
   * Get the name of the field within the published structure.
   * Note: If isComplexItem() returns false, the field will be null. In this 
   * case the DIP publication is not a structure.
   */
  public final String getFieldName() {
    return this.fieldName;
  }

  /**
   * Get the index of the element within an array published on DIP.
   */
  public final int getFieldIndex() {
    return this.fieldIndex;  
  }

  // ---------------------------------------------------------------------------
  // Public utitlity methods
  // ---------------------------------------------------------------------------

  /**
   * Returns true if the DIP publication is a structure. 
   * In this case, the DIPHardwareAddress object refers to a given field 
   * within the structure.
   * @see #getFieldName()
   */
  public boolean isComplexItem() {
    return (this.fieldName != null);
  }
 
  /**
   * Returns true if the DIP publication contains an array. 
   * In this case the DIPHardwareAddress object refers to a field within the 
   * array.
   * @see #getFieldIndex()
   */
  public boolean isArrayItem() {
    return (this.fieldIndex > -1);
  }

  // ---------------------------------------------------------------------------
  // Private accessor methods
  // ---------------------------------------------------------------------------

  protected final void setItemName(final String pItemName) throws ConfigurationException {
    if (pItemName == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"item name\" cannot be null."
      );
    }
    this.itemName = pItemName;
    return;
  }

  protected final void setFieldName(final String pFieldName) throws ConfigurationException {
    this.fieldName = pFieldName;
    return;
  }

  protected final void setFieldIndex(final int pFieldIndex) throws ConfigurationException {
    this.fieldIndex = pFieldIndex;
    return;
  }

  public void validate() throws ConfigurationException {
    if (this.itemName == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"itemName\" must not be null");
    }
    
    if (this.fieldIndex < -1) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"fieldIndex\" must >= -1");
    }
  }


}
