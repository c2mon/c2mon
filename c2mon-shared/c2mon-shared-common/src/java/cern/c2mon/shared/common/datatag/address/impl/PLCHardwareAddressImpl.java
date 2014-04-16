package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;


/**
 * Implementation of the PLCHardwareAddress interface.
 * Objects of this class represent hardware addresses for DataTags and 
 * CommandTags that are linked to PLCs. The address structure is generic, 
 * so it covers Siemens as well as Schneider PLCs. 
 *
 * @author Jan Stowisek
 * @version $Revision: 1.5 $ ($Date: 2006/09/11 10:35:37 $ - $State: Exp $) 
 * @see cern.c2mon.shared.common.datatag.address.PLCHardwareAddress
 */
 
public class PLCHardwareAddressImpl extends HardwareAddressImpl implements PLCHardwareAddress {
  
  /** Auto-generated serial versin UID */
  private static final long serialVersionUID = -4631934618859298750L;

  // ---------------------------------------------------------------------------
  // PROTECTED members
  // ---------------------------------------------------------------------------
  /**
   * Type of data block within the PLC.
   * The block type can only be one of the constant values defined in 
   * STRUCT_BOOLEAN, STRUCT_ANALOG, STRUCT_BOOLEAN_COMMAND and 
   * STRUCT_ANALOG_COMMAND.
   * @see PLCHardwareAddress#STRUCT_BOOLEAN
   * @see PLCHardwareAddress#STRUCT_ANALOG
   * @see PLCHardwareAddress#STRUCT_BOOLEAN_COMMAND
   * @see PLCHardwareAddress#STRUCT_ANALOG_COMMAND
   */
  @Element
  protected int blockType = 0;
  
  /**
   * Identifier of the word within the data block.
   * The word id is an integer number >=0
   */
  @Element
  protected int wordId = 0;
  
  /**
   * Identifier of the bit within the word.
   * The bit id is -1 for analog values, [0..15] for boolean values.
   */
  @Element
  protected int bitId = 0;
  
  /**
   * Human-readable physical minimum value. 
   * This parameter, together with the physical maximum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMaxVal
   */
  @Element
  protected float physicalMinVal = 0f;
  
  /**
   * Human-readable physical maximum value. 
   * This parameter, together with the physical minimum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMinVal
   */
  @Element
  protected float physicalMaxVal = 0f;

  /**
   * Resolution of the A/D converter.
   */
  @Element
  protected int resolutionFactor = 0;

  /**
   * Physical address of the tag, depending on PLC model used.
   */
  @Element
  protected String nativeAddress = null;

  /**
   * Command pulse length in milliseconds for boolean commands.
   */
  @Element
  protected int commandPulseLength;
  
  // ----------------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------------

  /**
   * Default Constructor needed for reflection call
   * @see HardwareAddressImpl#fromConfigXML(org.w3c.dom.Element)
   */
  protected PLCHardwareAddressImpl() {
    // Do nothing
  }
  
  /**
   * @param pBlockType the type of data block within the PLC
   * @param pWordId the identifier of the word within the data block
   * @param pBitId the identifier of the bit within the word
   * @param pResolutionFactor the resolution of the A/D converter
   * @param pMinVal Human-readable physical minimum value.
   * @param pMaxVal Human-readable physical maximum value.
   * @param pNativeAddress the physical address of the tag, depending on PLC model used
   * @throws ConfigurationException In case the given configuration parameters are wrong
   */
  public PLCHardwareAddressImpl (
      final int pBlockType, final int pWordId, final int pBitId, 
      final int pResolutionFactor, final float pMinVal, final float pMaxVal, final String pNativeAddress
  ) throws ConfigurationException {
    this(pBlockType, pWordId, pBitId, pResolutionFactor, pMinVal, pMaxVal, pNativeAddress, 0);
  }
  
  /**
   * @param pBlockType the type of data block within the PLC
   * @param pWordId the identifier of the word within the data block
   * @param pBitId the identifier of the bit within the word
   * @param pResolutionFactor the resolution of the A/D converter
   * @param pMinVal Human-readable physical minimum value.
   * @param pMaxVal Human-readable physical maximum value.
   * @param pNativeAddress the physical address of the tag, depending on PLC model used
   * @param pCmdPulseLength the pulse length in milliseconds for boolean commands.
   * @throws ConfigurationException In case the given configuration parameters are wrong
   */
  public PLCHardwareAddressImpl (
      final int pBlockType, final int pWordId, final int pBitId, 
      final int pResolutionFactor, final float pMinVal, final float pMaxVal, final String pNativeAddress,
      final int pCmdPulseLength
  ) throws ConfigurationException {
    setBlockType(pBlockType);
    setWordId(pWordId);
    setBitId(pBitId);
    setResolutionFactor(pResolutionFactor);
    setPhysicalMinVal(pMinVal);
    setPhysicalMaxVal(pMaxVal);
    setNativeAddress(pNativeAddress);
    setCommandPulseLength(pCmdPulseLength);
  }

  // ---------------------------------------------------------------------------
  // Public member accessors
  // ---------------------------------------------------------------------------

  /**
   * Get the type of data block within the PLC.
   * The block type can only be one of the constant values defined in 
   * STRUCT_BOOLEAN, STRUCT_ANALOG, STRUCT_BOOLEAN_COMMAND and 
   * STRUCT_ANALOG_COMMAND.
   * @return the type of data block within the PLC
   * @see PLCHardwareAddress#STRUCT_BOOLEAN
   * @see PLCHardwareAddress#STRUCT_ANALOG
   * @see PLCHardwareAddress#STRUCT_BOOLEAN_COMMAND
   * @see PLCHardwareAddress#STRUCT_ANALOG_COMMAND
   */
  public final int getBlockType() {
    return this.blockType;
  }

  /**
   * Get the identifier of the word within the data block.
   * The word id is an integer number >=0
   * @return the identifier of the word within the data block
   */
  public final int getWordId() {
    return this.wordId;
  }

  /**
   * Get the identifier of the bit within the word.
   * The bit id is -1 for analog values, [0..15] for boolean values.
   * @return the identifier of the bit within the word
   */
  public final int getBitId() {
    return this.bitId;
  }

  /**
   * Get the human-readable physical minimum value. 
   * This parameter, together with the physical maximum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #getPhysicalMaxVal()
   */
  public final float getPhysicalMinVal() {
    return this.physicalMinVal;
  }

  /**
   * Get the human-readable physical maximum value. 
   * This parameter, together with the physical minimum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #getPhysicalMinVal()
   */
  public final float getPhysicMaxVal() {
    return this.physicalMaxVal;
  }

  /**
   * Get the physical address of the tag, depending on PLC model used.
   * @return the physical address of the tag, depending on PLC model used
   */
  public final String getNativeAddress() {
    return this.nativeAddress;
  }

  /**
   * Get the resolution of the A/D converter.
   * @return the resolution of the A/D converter
   */
  public final int getResolutionFactor() {
    return this.resolutionFactor;
  }

  public int getCommandPulseLength() {
    return this.commandPulseLength;
  }

  /**
   * Set the type of data structure in the PLC 
   * The type can be STRUCT_BOOLEAN, STRUCT_ANALOG or STRUCT_COMMAND.
   */
  public final void setBlockType(final int pBlockType) throws ConfigurationException {
    if (!(pBlockType == STRUCT_BOOLEAN
        || pBlockType == STRUCT_ANALOG
        || pBlockType == STRUCT_BOOLEAN_COMMAND
        || pBlockType == STRUCT_DIAG_BOOLEAN
        || pBlockType == STRUCT_DIAG_ANALOG
        || pBlockType == STRUCT_DIAG_BOOLEAN_COMMAND
        || pBlockType == STRUCT_ANALOG_COMMAND)) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"block type\": " + pBlockType
      );
    }
    this.blockType = pBlockType;
    return;
      
  }

  /**
   * Sets the identifier of the word within the data block.
   * The word id is an integer number >=0
   * @param pWordId The identifier of the word within the data block.
   * @throws ConfigurationException In case <code>pWordId < 0</code>
   */
  public final void setWordId(final int pWordId) throws ConfigurationException {
    if (pWordId < 0) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Parameter \"word id\" must be >= 0"
      );
    }
    this.wordId = pWordId;
    return;
  }

  /**
   * Sets the identifier of the bit within the word.
   * The bit id is -1 for analog values, [0..15] for boolean values.
   * @param pBitId the identifier of the bit within the word.
   * @throws ConfigurationException In case <code>pBitId < -1 || pBitId > 15</code>
   */
  public final void setBitId(final int pBitId) throws ConfigurationException {
    if (pBitId < -1 || pBitId > 15) {
      throw new ConfigurationException(
          ConfigurationException.INVALID_PARAMETER_VALUE,
          "Invalid value for parameter \"bit it\": " + pBitId
      );
    }
    this.bitId = pBitId;
  }

  /**
   * Sets the human-readable physical minimum value. 
   * This parameter, together with the physical maximum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @param pMinVal the human-readable physical minimum value 
   */
  public final void setPhysicalMinVal(final float pMinVal) {
    this.physicalMinVal = pMinVal;
  }

  /**
   * Sets the human-readable physical maximum value. 
   * This parameter, together with the physical minimum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @param pMaxVal the human-readable physical maximum value 
   */
  public final void setPhysicalMaxVal(final float pMaxVal) {
    this.physicalMaxVal = pMaxVal;
  }
  
  /**
   * Sets the physical address of the tag, depending on PLC model used.
   * @param pNativeAddress the physical address of the tag, depending on PLC model used.
   */
  public final void setNativeAddress(final String pNativeAddress) {
    this.nativeAddress = pNativeAddress;
  }
  
  /**
   * Sets the resolution of the A/D converter.
   * @param pResolutionFactor the resolution of the A/D converter.
   */
  public final void setResolutionFactor(final int pResolutionFactor) {
    this.resolutionFactor = pResolutionFactor;
  }

  /**
   * Set the pulse length in milliseconds for boolean commands.
   * If the command is not pulsed, this value must be 0. For pulsed commands,
   * the pulse length must be between 100 and 5000 ms.
   * @param pCmdPulseLength pulse length im milliseconds
   */
  public final void setCommandPulseLength(final int pCmdPulseLength) throws ConfigurationException {
    if (pCmdPulseLength > 5000) {
      throw new ConfigurationException(
        ConfigurationException.INVALID_PARAMETER_VALUE,
        "Parameter \"command pulse length\" must be <= 5000 milliseconds"
      );
    }
    if (pCmdPulseLength != 0 && pCmdPulseLength < 100) {
      throw new ConfigurationException(
        ConfigurationException.INVALID_PARAMETER_VALUE,
        "Parameter \"command pulse length\" must be >= 100 milliseconds, unless it is 0"
      );
    }
    this.commandPulseLength = pCmdPulseLength;
    return;
  }
}
