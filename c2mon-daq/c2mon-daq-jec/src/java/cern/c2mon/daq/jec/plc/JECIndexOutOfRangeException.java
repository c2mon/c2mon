/*//////////////////////////////////////////////////////////////////////////////
//                         TIM PLC INTERFACING SYSTEM                         //
//                                                                            //
//  Index out of range exception                                              //
//  This class is used to generate an exception when programmer tries to      //
//  write data bigger than the JEC Frame Data Buffer.                         //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes            26/07/2004         First Implementation            //
//////////////////////////////////////////////////////////////////////////////*/

package cern.c2mon.daq.jec.plc;

/**
This Exception should be used to inform the driver about errors that  
occures while specialized subclass of EquipmentMessageHandler connects
to or disconnecting from equipment.
 */
public class JECIndexOutOfRangeException extends Exception
{
  /**
     * Serial Version UID for the JECIndexOutOfRangeException class
     */
    private static final long serialVersionUID = -5047353888919481015L;

  /**
   * The constructor
   */
  public JECIndexOutOfRangeException(String descr)
  {
    super(descr);
  }
}