package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.TDSHardwareAddress;

/**
 * Implementation of the TDSHardwareAddress interface.
 * 
 * This class represents a single hardware address for the TIM TDS driver.
 * TDS tags are addressed by their tag name, the name of the subject on which
 * the tag is distributed as well as a data type.

 * @author J. Stowisek
 * @version $Revision: 1.1 $ ($Date: 2005/02/01 17:07:16 $ - $State: Exp $)
 * @see cern.c2mon.shared.common.datatag.address.TDSHardwareAddress
 */
public class TDSHardwareAddressImpl extends HardwareAddressImpl implements TDSHardwareAddress {

  //---------------------------------------------------------------------------
  // Private members
  //---------------------------------------------------------------------------

  /** Unique name of the tag within the TDS */
  protected String tagName;

  /** Name of the "subject" on which the TDS tag is distributed via SmartSockets */
  protected String subjectName;

  /** TDS data type. Can be either TDS_NUMERIC or TDS_BOOLEAN */
  protected String dataType;
  
  //---------------------------------------------------------------------------
  // Constructors
  //---------------------------------------------------------------------------

  /**
   * Private default constructor.
   */
  protected TDSHardwareAddressImpl() {
    /* Nothing to do */
  }

  /**
   * Constructor.
   * @param pTdsTagName  a valid TDS tag name, preferably not null;
   * @param pSubject  smartsockets subject on which the tag is distributed
   * @param pDataType smartsockets data type of the tag (NUM or BOOL)
   */
  public TDSHardwareAddressImpl(
      final String pTagName, final String pSubjectName, final String pDataType
  ) throws ConfigurationException {
    setTagName(pTagName);
    setSubjectName(pSubjectName);
    setDataType(pDataType);
  }

  //---------------------------------------------------------------------------
  // Public members accessors
  //---------------------------------------------------------------------------

  /**
   * Gets the TDS tag name.
   */
  public final String getTagName() {
    return this.tagName;
  }

  /**
   * Get the SmartSockets subject name on which the tag is distributed.
   */
  public final String getSubjectName() {
    return this.subjectName;
  }

  /**
   * Get the TDS data type for this tag.
   * @see #TDS_STRING
   * @see #TDS_NUM
   * @see #TDS_BOOL
   */
  public final String getDataType() {
    return this.dataType;
  }

  //---------------------------------------------------------------------------
  // Private members accessors
  //---------------------------------------------------------------------------

  /**
   * Set the TDS tag name for this data tag.
   * @param pTdsTagName  a valid TDS tag name, not null;
   */
  protected final void setTagName(final String pTagName) throws ConfigurationException {
    if (pTagName == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"tag name\" must not be null."
      );
    }
    this.tagName = pTagName;
    return;
  }

  /**
   * @param pSubjectName name of the subject on which the tag is distributed
   */
  protected final void setSubjectName(final String pSubjectName) throws ConfigurationException {
    this.subjectName = pSubjectName;
    return;
  }

  /**
   * Sets the TDS data type for this tag
   * @param pDataType a valid TDS data type.
   * @see #TDS_STRING
   * @see #TDS_NUM
   * @see #TDS_BOOL
   */
  protected final void setDataType(final String pDataType) throws ConfigurationException {
    if (pDataType == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"data type\" must not be null."
      );
    }
    if (! (pDataType.equals(TDS_BOOL) || pDataType.equals(TDS_NUM) || pDataType.equals(TDS_STRING))) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"data type\": " + pDataType
      );
    }
    this.dataType = pDataType;
    return;
  }
}
