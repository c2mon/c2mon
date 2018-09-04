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
package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.PLCHardwareAddress;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Element;

import static cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressType.*;


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

  /**
   * Type of data block within the PLC.
   * The block type can only be one of the constant values defined in
   * STRUCT_BOOLEAN, STRUCT_ANALOG, STRUCT_BOOLEAN_COMMAND and
   * STRUCT_ANALOG_COMMAND.
   * @see PLCHardwareAddressType#STRUCT_BOOLEAN
   * @see PLCHardwareAddressType#STRUCT_ANALOG
   * @see PLCHardwareAddressType#STRUCT_BOOLEAN_COMMAND
   * @see PLCHardwareAddressType#STRUCT_ANALOG_COMMAND
   */
  @Getter
  @Element(name = "block-type")
  protected int blockType = 0;

  /**
   * Identifier of the word within the data block.
   * The word id is an integer number >=0
   */
  @Getter
  @Element(name = "word-id")
  protected int wordId = 0;

  /**
   * Identifier of the bit within the word.
   * The bit id is -1 for analog values, [0..15] for boolean values.
   */
  @Getter
  @Element(name = "bit-id")
  protected int bitId = 0;

  /**
   * Identifier of the string length
   * the string length is an integer number >= 0 and <= 112
   */
  @Getter
  @Setter
  @Element(name = "string-length")
  protected int stringLength = 0;

  /**
   * Human-readable physical minimum value.
   * This parameter, together with the physical maximum value, is needed to
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMaxVal
   */
  @Getter
  @Setter
  @Element(name = "physical-min-val")
  protected float physicalMinVal = 0f;

  /**
   * Human-readable physical maximum value.
   * This parameter, together with the physical minimum value, is needed to
   * convert hardware values sent by the PLC to engineering values.
   * @see #physicalMinVal
   */
  @Getter
  @Setter
  @Element(name = "physical-max-val")
  protected float physicalMaxVal = 0f;

  /**
   * Resolution of the A/D converter.
   */
  @Getter
  @Setter
  @Element(name = "resolution-factor")
  protected int resolutionFactor = 0;

  /**
   * Physical address of the tag, depending on PLC model used.
   */
  @Getter
  @Setter
  @Element(name = "native-address", required = false)
  protected String nativeAddress = null;

  /**
   * Command pulse length in milliseconds for boolean commands.
   */
  @Getter
  @Element(name = "command-pulse-length")
  protected int commandPulseLength;

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

  /**
   * @param pBlockType the type of data block within the PLC
   * @param pWordId the identifier of the word within the data block
   * @param pBitId the identifier of the bit within the word
   * @param pResolutionFactor the resolution of the A/D converter
   * @param pMinVal Human-readable physical minimum value.
   * @param pMaxVal Human-readable physical maximum value.
   * @param pNativeAddress the physical address of the tag, depending on PLC model used
   * @param pCmdPulseLength the pulse length in milliseconds for boolean commands.
   * @param stringLen length of the string
   * @throws ConfigurationException In case the given configuration parameters are wrong
   */
  public PLCHardwareAddressImpl (
          final int pBlockType, final int pWordId, final int pBitId, final int stringLen,
          final int pResolutionFactor, final float pMinVal, final float pMaxVal, final String pNativeAddress,
          final int pCmdPulseLength
  ) throws ConfigurationException {
    this(pBlockType, pWordId, pBitId, pResolutionFactor, pMinVal, pMaxVal, pNativeAddress, pCmdPulseLength);
    setStringLength(stringLen);
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
            || pBlockType == STRUCT_ANALOG_COMMAND
            || pBlockType == STRUCT_STRING)) {
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
