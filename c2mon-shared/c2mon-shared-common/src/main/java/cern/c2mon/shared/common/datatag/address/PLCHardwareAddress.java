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
package cern.c2mon.shared.common.datatag.address;


import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressType;

/**
 * The PLCHardwareAddress interface is used by the JECMessageHandler.
 * 
 * @see cern.c2mon.daq.jec.JECMessageHandler
 * @author J. Stowisek
 * @version $Revision: 1.14 $ ($Date: 2006/10/10 14:03:39 $ - $State: Exp $)
 */

public interface PLCHardwareAddress  extends HardwareAddress {
  /**
   * Get the type of data block within the PLC.
   * The block type can only be one of the constant values defined in 
   * STRUCT_BOOLEAN, STRUCT_ANALOG, STRUCT_BOOLEAN_COMMAND and 
   * STRUCT_ANALOG_COMMAND.
   * @return the type of data block within the PLC
   * @see PLCHardwareAddressType#STRUCT_BOOLEAN
   * @see PLCHardwareAddressType#STRUCT_ANALOG
   * @see PLCHardwareAddressType#STRUCT_BOOLEAN_COMMAND
   * @see PLCHardwareAddressType#STRUCT_ANALOG_COMMAND
   */
  int getBlockType();

  /**
   * Get the identifier of the word within the data block.
   * The word id is an integer number >=0
   * @return the identifier of the word within the data block
   */
  int getWordId();

  /**
   * Get the identifier of the bit within the word.
   * The bit id is -1 for analog values, [0..15] for boolean values.
   * @return the identifier of the bit within the word
   */
  int getBitId();

  /**
   * Get the human-readable physical minimum value. 
   * This parameter, together with the physical maximum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #getPhysicalMaxVal()
   */
  float getPhysicalMinVal();

  /**
   * Get the human-readable physical maximum value. 
   * This parameter, together with the physical minimum value, is needed to 
   * convert hardware values sent by the PLC to engineering values.
   * @see #getPhysicalMinVal()
   */
  float getPhysicalMaxVal();

  /**
   * Get the physical address of the tag, depending on PLC model used.
   * @return the physical address of the tag, depending on PLC model used
   */
  String getNativeAddress();

  /**
   * Get the resolution of the A/D converter.
   * @return the resolution of the A/D converter
   */
  int getResolutionFactor();

  /**
   * Command pulse length in milliseconds for boolean commands.
   * The commund pulse length is 0 for all input tags (STRUCT_BOOLEAN and
   * STRUCT_ANALOG) as well as for analog commands (STRUCT_ANALOG_COMMAND). It
   * is a value >= 0 for boolean commands (STRUCT_BOOLEAN_COMMAND).
   * @return the command pulse length in milliseconds for boolean commands.
   */
  int getCommandPulseLength();

  int getStringLength();
}
