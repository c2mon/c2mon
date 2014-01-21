/*//////////////////////////////////////////////////////////////////////////////
//                         TIM PLC INTERFACING SYSTEM                         //
//                                                                            //
//  Standard Constants Class                                                  //
//  This class is used centralize all the common variables used in the package//
//  The main ideia is, when changes are needed, we only do them in one place. //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes            01/07/2004         First Implementation            //
//  Joao Simoes            25/07/2004         Comment ameiloration            //
//////////////////////////////////////////////////////////////////////////////*/

package cern.c2mon.daq.jec.plc;

/**
 * This class is used centralize all the common variables used in the package
 * The main ideia is, when changes are needed, we only do them in one place.
 */
public class StdConstants 
{

/**
 * Constant declaration to define a maximum number of retries to receive data
 */
 public static final int recv_retry = 5;                                               

/**
 * Constant declaration to define the delay between Set Time messages (in hours)
 */
 public static final int SET_TIME_DELAY = 1;

/**
 * Number of bytes allowed to be allocated inside a JEC frame (data area only)
 */
 public static final int JEC_DATA_SIZE = 224;

/**
 * This variable is used to convert the floating point number into HEX format.
 * The value represents the mantissa number of digits (e.g. 100 means to digits in mantissa)
 * For example, to convert 2.5 to raw data (HEX), we multiply 2.5 by 100 (250) and in the 
 * PLC side the inverse operation is done, divide by 100 (2.5)
 */
 public static final int MANTISSA = 100;

/*//////////////////////////////////////////////////////////////////////////////
//                              JECP FRAMES OFFSET                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Constant declaration to represent SIEMENS and SCHNEIDER offsets to be used in
 * the JECP frames
 */
 public static final int SIEMENS_OFFSET = 0; 

 public static final int SCHNEIDER_OFFSET = 2;

/*//////////////////////////////////////////////////////////////////////////////
//                            JECP STATUS CONSTANTS                           //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Constant declaration to represent an operation status - SUCCESS
 */  
 // Status variable
 public static final int SUCCESS = 0;                                                 

/**
 * Constant declaration to represent an operation status - ERROR
 */
 // Status variable
 public static final int ERROR = -1;                                                  

/*//////////////////////////////////////////////////////////////////////////////
//                             JECP TIMEOUT VALUES                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default timeout value to receive data from socket - in milliseconds
 */
 // Predefined timeout for data reception - 5 sec
 public static final int TIMEOUT = 5000;      

/**
 * Watchdog timeout value to be used in Initialization Procedure
 */
 // Watchdog timeout - 30 seconds
 public static final long watchdogTimeout = 30000;

/**
 * Watchdog constants
 */
 public static final int NO_WATCHDOG = -1;

/**
 * This variable defines the timeout for receiving a message confirm from PLC.
 * The difference between this timeout and the one above is that this one is 
 * oriented to the JEC communication and the one above concerns the socket
 * connection timeout.
 */
 // Maximum time to wait answer from the PLC (1 min)
 public static final long Message_Timeout = 60000;        

 /**
  * Reconnect waiting period to give time to the communications processor to
  * reinitialize. Value must be in milliseconds.
  */
  public static final long reconnectTimeout = 5000;

 /**
  * Maximum number of connection attempts in case of connection failed
  */
  public static final int maxConnAttempt = 5;

 /**
  * This variable defines a minimum timeout value needed to execute the commands
  * and return the corresponding report to C2MON. (In milliseconds)
  */
  public static final short minCmdTimeout = 120;

/**
 * Default JECP frame size
 */
 // Maximum size of a JEC message
 public static final int max_frame_size = 240;                                        

/*//////////////////////////////////////////////////////////////////////////////
//                         JECP MESSAGE IDENTIFIERS                           //
//////////////////////////////////////////////////////////////////////////////*/
/**
 * INIT MESSAGE - CODE 01
 */
 public static final byte INIT_MSG = 0x01;                                            

/**
 * SET TIME MESSAGE - CODE 02;
 */
 public static final byte SET_TIME_MSG = 0x02;                                        

/**
 * SET CONFIGURATION MESSAGE - CODE 03
 */
 public static final byte SET_CFG_MSG = 0x03;                                         

/**
 * END CONFIGURATION MESSAGE - CODE 04
 */
  public static final byte END_CFG_MSG = 0x04;

/**
 * GET ALL DATA MESSAGE - CODE 05
 */
  public static final byte GET_ALL_DATA_MSG = 0x05;

/**
 * ACKNOWLEDGE MESSAGE - CODE 06
 */
  public static final byte ACK_MSG = 0x06;

/**
 * BOOLEAN DATA MESSAGE - CODE 07
 */
  public static final byte BOOL_DATA_MSG = 0x07;

/**
 * ANALOGIC DATA MESSAGE - CODE 08
 */
  public static final byte ANALOG_DATA_MSG = 0x08;

/**
 * TO HANDLER ALIVE MESSAGE - CODE 09
 */
  public static final byte HANDLER_ALIVE_MSG = 0x09;

/**
 * TO SUPERVISION ALIVE MESSAGE - CODE 10
 */
  public static final byte SUPERV_ALIVE_MSG = 0x0A;

/**
 * BOOLEAN COMMAND MESSAGE - CODE 11
 */
  public static final byte BOOL_CMD_MSG = 0x0B;

/**
 * ANALOGIC COMMAND MESSAGE - CODE 12
 */
  public static final byte ANALOG_CMD_MSG = 0x0C;

/**
 * SET DEADBAND MESSAGE - CODE 13
 */
  public static final byte SET_DEADBAND_MSG = 0x0D;

/**
 * CONFIRM BOOLEAN COMMAND MESSAGE - CODE 17
 */
  public static final byte CONFIRM_BOOL_CMD_MSG = 0x11;

/**
 * CONFIRM BOOLEAN CONTROL COMMAND MESSAGE - CODE 22
 */
  public static final byte CONFIRM_BOOL_CMD_CTRL_MSG = 0x16;  

/**
 * CONFIRM ANALOG COMMAND MESSAGE - CODE 18
 */
  public static final byte CONFIRM_ANA_CMD_MSG = 0x12;  

/**
 * BOOLEAN DATA CONTROL MESSAGE - CODE 19
 */
  public static final byte BOOL_DATA_CTRL_MSG = 0x13;  

/**
 * ANALOG DATA CONTROL MESSAGE - CODE 20
 */
  public static final byte ANA_DATA_CTRL_MSG = 0x14;    

/**
 * BOOLEAN COMMAND CONTROL MESSAGE - CODE 21
 */
  public static final byte BOOL_CMD_CTRL_MSG = 0x15;    

/**
 * NON ACKNOWLEDGE MESSAGE - CODE 15            
 */
  public static final byte NACK_MSG = 0x0F;

/**
 * INFORMATION MESSAGE - CODE 16
 */
 public static final byte INFO_MSG = 0x10;

/*//////////////////////////////////////////////////////////////////////////////
//                PARAMETERS USED FOR SET CONFIGURATION MESSAGE               //
//////////////////////////////////////////////////////////////////////////////*/
/**
 * PLC Configuration Data - CODE 01
 */
 public static final byte PLC_CONF_DATA = 0x01;

/**
 * Analogical Data Deadbands - CODE 02
 */
 public static final byte ANA_DATA_DEADBAND = 0x02;

/**
 * Boolean Command Data Value - CODE 03
 */
 public static final byte BOOL_CMD_VALUE = 0x03; 

/**
 * Analog Command Data Value - CODE 04
 */
 public static final byte ANA_CMD_VALUE = 0x04; 

/*//////////////////////////////////////////////////////////////////////////////
//                   SYNCHRONIZATION METHODS ALLOWED BY JEC                   //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Synchronization method - NTP (Network Time Protocol)
 */
 public static final byte SYNC_NTP = 0x00;

/**
 * Synchronization method - JEC (Host timestamp)
 */
 public static final byte SYNC_JEC = 0x01; 

/**
 * Bit ID used for Analog Messages - value is -1
 */
 public static final byte ANALOG_BITID = -1;

/*//////////////////////////////////////////////////////////////////////////////
//                        TYPES OF INFORMATION MESSAGES                       //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Constant to identify that the INFO message represents a lost of one DP slave
 */
 public static final short DP_SLAVE_LOST = 0x01;

/**
 * Constant to sign that this is an INVALIDATION message
 */
 public static final int SLAVE_INVALIDATE = 0x0039;

/**
 * Constant to sign that this is a VALIDATION message
 */
 public static final int SLAVE_VALIDATE = 0x0038;
 
/**
 * Constant to identify the END OF TEXT CHARACTER
 */ 
 public static final byte END_OF_TEXT = 0x03;

/**
 * Constant to sign that the DP slave is ALIVE
 */
 public static final int SLAVE_STATUS_ALIVE = 0;

/**
 * Constant to sign that the DP slave is LOST
 */
 public static final int SLAVE_STATUS_LOST = 1;

/*//////////////////////////////////////////////////////////////////////////////
//                           FILTERING TYPE PARAMETERS                        //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Constant to sign that analog data will be sent to TIM without taking in 
 * account if the change is enough to be sent or not.
 */
 public static final boolean FILTER_OFF = false;

/**
 * Constant to sign that analog data will be sent to TIM taking in account if 
 * the change is enough to be sent.
 */
 public static final boolean FILTER_ON = true; 

/*//////////////////////////////////////////////////////////////////////////////
//                           ANALOG COMMAND PARAMETERS                        //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Constant to define that the analog command should be represented in IEEE format
 */
 public static final int FLOAT_DATA = 1;

/**
 * Constant to define that the analog command should be represented in raw data
 */
 public static final int RAW_DATA = 0;
 
/*//////////////////////////////////////////////////////////////////////////////
//             METHOD STDCONSTANTS - CONSTRUCTOR (without parameters)         //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default Class Constructor without parameters
 */  
  public StdConstants()
  {
  }
}

/*//////////////////////////////////////////////////////////////////////////////
//                               END OF CLASS                                 //
//////////////////////////////////////////////////////////////////////////////*/
