package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.SGatewayHardwareAddress;

/**
 * Implementation of the SGatewayHardwareAddress interface.
 *
 * @author Jan Stowisek
 * @version $Revision: 1.2 $ ($Date: 2005/03/07 10:40:30 $ - $State: Exp $)
 */

public class SGatewayHardwareAddressImpl extends HardwareAddressImpl implements SGatewayHardwareAddress {

  //---------------------------------------------------------------------------
  // PROTECTED class members
  //---------------------------------------------------------------------------

  /**
   * Data format for the tag. 
   * Only the values defined in the constants FORMAT_BIN_DATA and 
   * FORMAT_TS_DATA are allowed.
   * @see SGatewayHardwareAddress#FORMAT_BIN_DATA
   * @see SGatewayHardwareAddress#FORMAT_TS_DATA
   */
  protected String dataFormat;

  /**
   * Default value for the tag. 
   * Only the String values "TRUE" and "FALSE" as well as String representations
   * of numbers are allowed.
   */
  protected String defaultValue;

  /**
   * Human-readable physical minimum value. 
   * This parameter, together with the physical maximum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMaxValue
   */
  protected float physicalMinValue;

  /**
   * Human-readable physical maximum value. 
   * This parameter, together with the physical minimum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMinValue
   */
  protected float physicalMaxValue;

  /**
   * Type of tag.
   * Only the values defined in the constants TAG_TYPE_NRM, TAG_TYPE_CMD and 
   * TAG_TYPE_AVA are allowed.
   * @see SGatewayHardwareAddress#TAG_TYPE_NRM
   * @see SGatewayHardwareAddress#TAG_TYPE_AVA
   * @see SGatewayHardwareAddress#TAG_TYPE_CMD
   */
  protected String tagType;

  /**
   * Network name of the PLC.
   */
  protected String plcName;

  /**
   * Id of the data block within the address space of the plc.
   */
  protected int blockId;

  /**
   * Index of the structure within the data block.
   */
  protected int wordId;

  /**
   * Offset within the wordId.
   */
  protected int bitId;

  /**
   * Command pulse length for boolean commands in milliseconds.
   */
  protected int commandPulseLength;

  //---------------------------------------------------------------------------
  // Constructors
  //---------------------------------------------------------------------------

  /**
   * Constructor for internal use.
   */
  protected SGatewayHardwareAddressImpl() {
    /* Nothing to do */
  }

  public SGatewayHardwareAddressImpl (
    final String pDataFormat,
    final String pDefaultValue,
    final float pPhysicalMinValue,
    final float pPhysicalMaxValue,
    final String pTagType,
    final String pPlcName,
    final int pBlockId,
    final int pWordId,
    final int pBitId,
    final int pCommandPulseLength
  ) throws ConfigurationException {
    setDataFormat(pDataFormat);
    setDefaultValue(pDefaultValue);
    setPhysicalMinValue(pPhysicalMinValue);
    setPhysicalMaxValue(pPhysicalMaxValue);
    setTagType(pTagType);
    setPlcName(pPlcName);
    setBlockId(pBlockId);
    setWordId(pWordId);
    setBitId(pBitId);
    setCommandPulseLength(pCommandPulseLength);
  }

  //---------------------------------------------------------------------------
  // Public members accessors
  //---------------------------------------------------------------------------
  /** 
   * Get the data format.
   * @return the tag's data format.
   * @see #dataFormat
   */
  public final String getDataFormat() {
    return this.dataFormat;
  }

  /**
   * @return the default value for the tag
   * @see #defaultValue
   */
  public final String getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * @return the physical minimum value
   * @see #physicalMinValue
   */
  public final float getPhysicalMinValue() {
    return this.physicalMinValue;
  }

  /**
   * @return the physical maximum value
   * @see #physicalMaxValue
   */
  public final float getPhysicalMaxValue() {
    return this.physicalMaxValue;
  }

  /**
   * @return the tag type
   * @see SGatewayHardwareAddress#tagType
   */
  public final String getTagType() {
    return this.tagType;
  }

  /**
   * @return the network name of the PLC.
   */
  public final String getPlcName() {
    return this.plcName;
  }

  /**
   * @return the block id within the address space of the PLC.
   */
  public final int getBlockId() {
    return this.blockId;
  }

  /**
   * @return the word id within the data block
   */
  public final int getWordId() {
    return this.wordId;
  }
  
  /**
   * @return the offset within the word
   */
  public final int getBitId() {
    return this.bitId;
  }

  /**
   * @return the command pulse length in milliseconds
   */
  public final int getCommandPulseLength() {
    return this.commandPulseLength;
  }

  //---------------------------------------------------------------------------
  // Private members accessors
  //---------------------------------------------------------------------------

  protected final void setDataFormat(final String pDataFormat) throws ConfigurationException {
    if (pDataFormat == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"data format must not be null."
      );
    }
    if (! (pDataFormat.equals(FORMAT_BIN_DATA) || pDataFormat.equals(FORMAT_TS_DATA)) ) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"data format\": " + pDataFormat
      );
    }
    this.dataFormat = pDataFormat;
    return;
  }

  protected final void setDefaultValue(final String pDefaultValue) throws ConfigurationException {
    if (pDefaultValue == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"default value\" must not be null."
      );
    }
    this.defaultValue = pDefaultValue;
    return;
  }

  protected final void setPhysicalMinValue(final float pPhysicalMinValue) throws ConfigurationException {
    this.physicalMinValue= pPhysicalMinValue;
    return;
  }

  protected final void setPhysicalMaxValue(final float pPhysicalMaxValue) throws ConfigurationException {
    this.physicalMaxValue = pPhysicalMaxValue;
    return;
  }

  protected final void setTagType(final String pTagType) throws ConfigurationException {
    if (pTagType == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"tag type\"must not be null."
      );
    }
    if (!(pTagType.equals(TAG_TYPE_NRM) || pTagType.equals(TAG_TYPE_AVA) ||
          pTagType.equals(TAG_TYPE_CMD))
    ){
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"tag type\": " + pTagType
      );
    }
    this.tagType = pTagType;
    return;
  }

  protected final void setPlcName(final String pPlcName) throws ConfigurationException {
    if (pPlcName == null) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"plc name\" must not be null."
      );
    }
    this.plcName = pPlcName;
    return;
  }

  protected final void setBlockId(final int pBlockId) throws ConfigurationException {
    if (pBlockId < 0) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"block id\" must be >= 0" 
      );
    }
    this.blockId = pBlockId;
    return;
  }

  protected final void setWordId(final int pWordId) throws ConfigurationException {
    if (pWordId < 0) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"word id\" must be >= 0" 
      );
    }
    this.wordId = pWordId;
    return;
  }

  protected final void setBitId(final int pBitId) throws ConfigurationException {
    if (pBitId < -1) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"bit id\" must be >= -1" 
      );
    }
    this.bitId = pBitId;
    return;
  }

  protected final void setCommandPulseLength(final int pCommandPulseLength) throws ConfigurationException {
    if (pCommandPulseLength < 0) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"command pulse length\" must be >= 0" 
      );
    }
    this.commandPulseLength = pCommandPulseLength;
    return;
  }
}