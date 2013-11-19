package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.LASERHardwareAddress;



/**
 * Implementation of the LASERHardwareAddress interface.
 *
 * @author Wojtek Buczak
 * @see cern.c2mon.shared.common.datatag.address.LASERHardwareAddress
 */

public final class LASERHardwareAddressImpl extends HardwareAddressImpl implements LASERHardwareAddress {
 
  // ---------------------------------------------------------------------------
  // Private member definitions
  // ---------------------------------------------------------------------------

  /** 
   * the name of the LASER alarm category
   */  
  protected String alarmCategory;

  /** 
   * the name of the alarm family
   */  
  protected String faultFamily;

  /**
   * the name of the alarm fault member
   */
  protected String faultMember;
  
  
  /**
   * the alarm fault code
   */
  protected int faultCode = -1;
  
  
  
  // ---------------------------------------------------------------------------
  // Constructors
  // ---------------------------------------------------------------------------

  
  /**
   * Create a LASERHardwareAddress object with specified category, fault family, member and code.
   * @param pCategoryName name of a structured DIP publication
   * @param pFaultFamily
   * @param pFaultMember
   * @param pFalutCode
   */
  public LASERHardwareAddressImpl(final String pCategoryName, final String pFaultFamily, 
                                final String pFaultMember, final int pFaultCode) throws ConfigurationException {
    setAlarmCategory(pCategoryName);
    setFaultFamily(pFaultFamily);
    setFaultMember(pFaultMember);
    setFaultCode(pFaultCode);
  }



  /**
   * Internal constructor required by the fromConfigXML method of the super class.
   */
  protected LASERHardwareAddressImpl() {
    /* Nothing to do */
  }

  // ---------------------------------------------------------------------------
  // Public accessor methods
  // ---------------------------------------------------------------------------

   /** 
   * Get the name of the LASER alarm category
   * The category me can never be null.
   * @return the name of the alarm category
   */
  public final String getAlarmCategory() {
    return this.alarmCategory;
  }


  /** 
   * Get the name of the alarm family
   * The alarm family can never be null.  
   * @return the name of the alarm category
   */
  public String getFaultFamily(){
    return this.faultFamily;
  }


  /** 
   * Get the name of the alarm fault member
   * The fault member me can never be null.
   * @return the name of the alarm category
   */  
  public String getFaultMember() {
    return this.faultMember;  
  }


  /** 
   * Get the name of the alarm fault code
   * The code me can never be null.
   * @return the name of the alarm category
   */  
  public int getFalutCode() {
    return this.faultCode;  
  }


  // ---------------------------------------------------------------------------
  // Public utitlity methods
  // ---------------------------------------------------------------------------

 

  // ---------------------------------------------------------------------------
  // Private accessor methods
  // ---------------------------------------------------------------------------

  protected final void setAlarmCategory(final String pAlarmCategory) throws ConfigurationException {
    if (pAlarmCategory == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"alarm category\" cannot be null."
      );
    }
    this.alarmCategory = pAlarmCategory;
    return;
  }
  
  
  protected final void setFaultFamily(final String pFaultFamily) throws ConfigurationException {
    if (pFaultFamily == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"fault family\" cannot be null."
      );
    }
    this.faultFamily = pFaultFamily;
    return;
  }
  
  
  protected final void setFaultMember(final String pFaultMember) throws ConfigurationException {
    if (pFaultMember == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"fault member\" cannot be null."
      );
    }
    this.faultMember = pFaultMember;
    return;
  }  


  
  protected final void setFaultCode(final int pFaultCode) throws ConfigurationException {    
    this.faultCode = pFaultCode;
    return;
  }  

  public void validate() throws ConfigurationException {
    if (this.alarmCategory == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"alarmCategory\" must not be null");
    }

    if (this.faultFamily == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultFamily\" must not be null");
    }

    if (this.faultMember == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultMember\" must not be null");
    }
    
    if (this.faultCode < 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultCode\" must be >= 0");
    }
  }

}
