/*//////////////////////////////////////////////////////////////////////////////
//                         TIM PLC INTERFACING SYSTEM                         //
//                                                                            //
//  TIM Data Aquisition System. CERN. All rights reserved.                    //
//                                                                            //
//  Connection data structure                                                 //
//  This class is used to collect the communication parameters such as:       //
//  IP, Port, Source and Destination TSAP and Timeout                         //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes            01/07/2004         First Implementation            //
//  Joao Simoes            29/11/2004         Comment ameiloration            //
//////////////////////////////////////////////////////////////////////////////*/

package cern.c2mon.daq.jec.plc;

/**
 * This class is used to collect the communication parameters such as:
 * IP, Port, Source and Destination TSAP and Timeout.
 */
public class ConnectionData 
{
/**
 * Variable to store the IP address in String format. Can be the IP number or
 * the hostname (eg. "172.18.161.103" or "plcstaa02.cern.ch")
 */
  // IP address of the remote PLC
  public String ip = null;                                                      

/**
 * Variable to store the server Port number in Integer format. (eg. 1020)
 * NOTE: For Siemens over ISO-ON-TCP the port must be 102
 * For Schneider over TCP the port must be >5010
 */  
  // Port opened in the PLC for communication
  public int port = 0;                                                          

/**
 * Variable to store the source TSAP in String format. (eg. "TCP-1")
 * This TSAP is only used by Siemens ISO-on-TCP protocol and must match with
 * the PLC configuration - MAX 8 characters.
 */  
  // Source TSAP address
  public String src_tsap = null;                                                

/**
 * Variable to store the destination TSAP in String format. (eg. "TCP-1")
 * This TSAP is only used by Siemens ISO-on-TCP protocol and must match with
 * the PLC configuration - MAX 8 characters.
 */  
  // Destination TSAP address
  public String dest_tsap = null;                                               

/**
 * This variable is used only for SiemensISO and allows the soft shutdown mode to
 * disconnect from the PLC. This mode sends a Disconnection Request before closing
 * the socket connection.
 */  
  // Boolean value to select soft disconnect option
  public boolean shutdown = true;                                               

/*//////////////////////////////////////////////////////////////////////////////
//             METHOD CONNECTIONDATA - CONSTRUCTOR (with parameters)          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default Class Constructor with parameters - This method is used for Siemens
 */
  public ConnectionData(String _ip, int _port, String _dst, String _src, boolean _shutdown) 
  {
    // IP address or PLC hostname in string format
    ip = _ip;                                                                   
    // PORT where the connection must be established
    port = _port;                                                               
    // Destination TSAP in string format (PLC TSAP)
    dest_tsap = _dst;                                                           
    // Source TSAP in string format (PC TSAP)
    src_tsap = _src;                                                            
    // When true, sends Disconnect Request before closing socket
    shutdown = _shutdown;                                                       
  }

/*//////////////////////////////////////////////////////////////////////////////
//             METHOD CONNECTIONDATA - CONSTRUCTOR (with parameters)          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default Class Constructor with parameters - This method is used for Schneider
 */
  public ConnectionData(String _ip, int _port) 
  {
    // IP address or PLC hostname in string format
    ip = _ip;                                                                   
    // PORT where the connection must be established
    port = _port;                                                               
  }

/*//////////////////////////////////////////////////////////////////////////////
//             METHOD CONNECTIONDATA - CONSTRUCTOR (with parameters)          //
//////////////////////////////////////////////////////////////////////////////*/

 /**
  * Overwrite the toSring() method to get some meaningful debug output.
  */
  public String toString() {
    StringBuffer str = new StringBuffer("ConnectionData[IP:");
    str.append(this.ip);
    str.append(",PORT:");
    str.append(this.port);
    str.append(",DEST_TSAP:");
    str.append(this.dest_tsap);
    str.append(",SRC_TSAP:");
    str.append(this.src_tsap);
    str.append(",SHUTDOWN?");
    str.append(this.shutdown);
    str.append("]");
    return str.toString();
  }
}
/*//////////////////////////////////////////////////////////////////////////////
//                                 END OF CLASS                               //
//////////////////////////////////////////////////////////////////////////////*/
