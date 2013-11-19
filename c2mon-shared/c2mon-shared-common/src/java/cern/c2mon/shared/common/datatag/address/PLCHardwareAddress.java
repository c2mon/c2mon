package cern.c2mon.shared.common.datatag.address;


/**
 * The PLCHardwareAddress interface is used by the JECMessageHandler.
 * 
 * @see cern.c2mon.daq.jec.JECMessageHandler
 * @author J. Stowisek
 * @version $Revision: 1.14 $ ($Date: 2006/10/10 14:03:39 $ - $State: Exp $)
 */

public interface PLCHardwareAddress  extends HardwareAddress {
  // ---------------------------------------------------------------------------
  // Public constant definitions
  // ---------------------------------------------------------------------------

  /** Structure type for boolean data tags (inputs) */
  int STRUCT_BOOLEAN = 1;

  /** Structure type for analog (numeric) data tags (inputs)*/
  int STRUCT_ANALOG = 2;

  /** Structure type for boolean command tags (outputs) */
  int STRUCT_BOOLEAN_COMMAND = 3;

  /** Structure type for analog command tags (outputs) */
  int STRUCT_ANALOG_COMMAND = 4;

  /** Structure type for internal boolean states for diagnostic purposes  */
  int STRUCT_DIAG_BOOLEAN = 5;

  /** Structure type for internal analogue states for diagnostic purposes  */
  int STRUCT_DIAG_ANALOG = 6;

  /** Structure type for internal boolean commands for diagnostic purposes
   * (e.g. PING) 
   */
  int STRUCT_DIAG_BOOLEAN_COMMAND = 7;

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
  float getPhysicMaxVal();

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

}
