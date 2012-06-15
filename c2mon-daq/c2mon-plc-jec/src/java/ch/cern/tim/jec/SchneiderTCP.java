/*//////////////////////////////////////////////////////////////////////////////
//                        TIM PLC INTERFACING SYSTEM                          //
//                                                                            //
//  PLC communication library.                                                //
//  Used to open a connection, send and receive data and disconnect from PLC. //
//  The application allows to send/receive commands/data to/from the PLC.     //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes            01/07/2004         First Implementation            //
//  Joao Simoes            25/07/2004         Comment ameiloration            //
//  Joao Simoes            25/07/2004         Send and Receive optimization   //
//////////////////////////////////////////////////////////////////////////////*/

package ch.cern.tim.jec;

import java.io.*;
import java.net.*;
import org.apache.log4j.Logger;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to connect/disconnect and exchange data between PC and PLC.
 * When invoked, allows a socket connection to the PLC and allows some methods 
 * used for the communication.
 */
public class SchneiderTCP implements PLCDriver
{
/**
 * Instanciate a logger in this class if there is no other instance created.
 * This logger allows to register the errors, infos and debugs into a log file
 */
  // Instanciate, if not created, Log4J logger
  static Logger logger = Logger.getLogger(SchneiderTCP.class);                  
  
/**
 * Instance of Socket class to establish a connection to the PLC
 */  
  // Socket instance
  private Socket cli_socket;                                                    

/**
 * Data output buffer to be assigned to the opened socket (for data to send)
 */
  // Output stream buffer for send data
  private DataOutputStream s_data = null;                                       

/**
 * Data input buffer to be assigned to the opened socket (for received data)
 */
  // Input stream buffer for receive data - data
  private DataInputStream r_data_data = null;                                   

/**
 * Data input buffer to be assigned to the opened socket (for received data size)
 */
  // Input stream buffer for receive data - size
  private DataInputStream r_data_size = null;     
  
  /**
   * Locks sending and receiving on streams.
   */
  private ReentrantReadWriteLock sendLock = new ReentrantReadWriteLock();
  private ReentrantReadWriteLock receiveLock = new ReentrantReadWriteLock();
  

/*//////////////////////////////////////////////////////////////////////////////
//             METHOD SCHNEIDERTCP - CONSTRUCTOR (no parameters)              //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default Class Constructor with no parameters
 */
  public SchneiderTCP()
  {
  }

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD CLOSESOCKET                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Method to close the socket connection between PC and PLC. 
 * This function tries to close a socket connection, returning exception on error.
 */
  private void CloseSocket()                                                    
  {
    try
    {
      // Call to system to close opened socket
      cli_socket.close();                                                       
      // DEBUG message that will appear in the logger          
      logger.debug("Socket closed!");                                           
    }
    // Exception if HOST UNREACHABLE
    catch (UnknownHostException e)                                              
    {
      // ERROR message that will appear in the logger
      logger.error("Error on trying to close socket: "+e);                      
    }
    // Exception if IO ERROR
    catch (IOException e)                                                       
    {
      // ERROR message that will appear in the logger
      logger.error("Error on trying to close socket: "+e);                      
    }
  }

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD CONNECT                                 //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * This class allows the connection between the host and a Schneider PLC using 
 * TCP protocol.
 * @param ConnectionData param - IP address (or PLC hostname) in string format 
 * and Port number in int format
 * @return int - Result of the Connect() operation attempt (0 - success;
 * -1 - error)
 */
  public int Connect(ConnectionData param)                                      
  {
    // Status of the connect() - initially is ERROR (-1)
    int status = StdConstants.ERROR;                                            
    // INFO message that will appear in the logger
    logger.info("Trying to open socket to: "+param.ip+" ...");                  

    try
    {
      // Creates socket instance for the IP and PORT given
      cli_socket = new Socket(param.ip,param.port);                             
      // Assign output stream buffer to this connection
      s_data = new DataOutputStream(cli_socket.getOutputStream());              
      // Assign input stream buffer to this connection - data
      r_data_data = new DataInputStream(cli_socket.getInputStream());           
      // Assign input stream buffer to this connection - size
      r_data_size = new DataInputStream(cli_socket.getInputStream());           
      // Assigns status with SUCCESS (0)
      status = StdConstants.SUCCESS;                                            
    }
    // Exception if HOST UNREACHABLE
    catch (UnknownHostException e)                                              
    {
      // FATAL message that will appear in the logger
      logger.fatal("Impossible to reach the host: "+param.ip+" - "+e);          
      // Assigns status with ERROR (-1)
      status = StdConstants.ERROR;                                              
    }
    // Exception if IO ERROR
    catch (IOException e)                                                       
    {
      // FATAL message that will appear in the logger
      logger.fatal("I/O unreachable for the connection to: "+param.ip+" - "+e); 
      // Assigns status with ERROR (-1)
      status = StdConstants.ERROR;                                              
    }

// Tests the result of the OpenSocket() attempt
    if(status == 0)                             
    {
      // DEBUG message that will appear in the logger
      logger.debug("Socket opened to: "+cli_socket);                            
      // Return the actual status - SUCCESS
      return status;                                                            
    }
    else
    {
      // DEBUG message that will appear in the logger
      logger.debug("Error trying to open socket to: "+cli_socket);              
      // Return the actual status - ERROR
      return status;                                                            
    }
  }

/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD DISCONNECT                                //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Method to disconnect from a Schneider PLC.
 * Closes the socket and the PLC will disconnect automatically
 * @param ConnectionData param - IP address (or PLC hostname) in string format 
 * and Port number in int format
 * @return int - Result of the Connect() operation attempt (0 - success;
 * -1 - error)
 */
  public int Disconnect(ConnectionData param)
  {
    // Status of the disconnection attempt
    int status = StdConstants.ERROR;                                            
    try
    {
      // If there is an opened socket...
      if (cli_socket != null)                                                   
      {
        // Close the output stream
        s_data.close();                                                         
        // Close the input data stream
        r_data_data.close();                                                    
        // Close the input size stream
        r_data_size.close();                                                    
        // INFO message that will appear in the logger                    
        logger.info("Closing connection...");                                   
        // Close the socket
        this.CloseSocket();                                                     
        // INFO message that will appear in the logger             
        logger.info("Successfully disconnected from PLC");                      
        // Assigns status with SUCCESS (0)                
        status = StdConstants.SUCCESS;                                          
      }
    }
    // Raised when task is completed - connection closed
    catch (IOException e)                                                        
    { // ERROR message that will appear in the logger          
      logger.error("Error while trying to close connection");                   
      // Assigns status with ERROR (-1)                      
      status = StdConstants.ERROR;                                              
    } 
    // Return the actual status
    return status;                                                              
  }

/*//////////////////////////////////////////////////////////////////////////////
//                                METHOD SEND                                 //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * Method to send a message through the socket over TCP/IP protocol
 * @param byte[] frame - Array of byte with the data to be sent down to the PLC
 * @return int - Result of the Send() operation attempt (0 - success;
 * -1 - error)
 */
  public int Send(JECPFrames Frame)
  {   
    sendLock.writeLock().lock();
    try {
      // Status of the send() attempt
      int status = StdConstants.ERROR;                                            
      // String to store debug message to be sent to the logger    
      String debug_msg;                                            
      // Variable to read system date in Date format
      Date dt = new Date(System.currentTimeMillis());                             

  // Checks if there is an opened socket. If yes, send Data through the socket
      try
      { 
        // Repeat as long as socket is opened and connected
        if(cli_socket != null)                                                    
          // Send the byte array through the opened socket
          s_data.write(Frame.frame,0,Frame.frame.length);                                     
        // INFO message that will appear in the logger                                
        logger.info("Data Frame sent on : "+dt.toString());                       
        // Assigns status with SUCCESS (0)                      
        status = StdConstants.SUCCESS;                                            
      }
      // Exception if UNKNOWN HOST
      catch (IOException e)                                                       
      {
        // ERROR message that will appear in the logger
        logger.error("Error sending Data Frame "+e);                              
        // Assigns status with ERROR (-1)                      
        status = StdConstants.ERROR;                                              
      }

      // Preparing DEBUG message to send to the logger
      // Reset the debug_msg string
      debug_msg = "";                   
      if (status == StdConstants.SUCCESS)
      {
        // Generate the debug message to put in the logger
        for (int j = 0;j < Frame.frame.length ; j++)                                      
        {
          // Create the debug frame...
          debug_msg = debug_msg + (" 0x"+Integer.toHexString((int)Frame.frame[j] & 0xff)); 
        }
        // DEBUG message that will appear in the logger
        logger.debug("Data sent:"+debug_msg);                                       
      }
           // DEBUG message that will appear in the logger
      else logger.debug("No data was sent due to an error");

      // Resets the pointer that tracks the written positions inside the JEC Frame data area
      Frame.ResetDataBufferOffset();

      // Return the actual status
      return status;
    } finally {
      sendLock.writeLock().unlock();      
    }                                                                
  }

/*//////////////////////////////////////////////////////////////////////////////
//                               METHOD RECEIVE                               //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * Method to receive a byte array message through the socket over TCP/IP protocol
 * @params JECPFrames - the buffer where the received data should be stored
 * @params int - timeout (ms)
 * @return int  - execution code (0 : success, -1 : error)
 */   
  public int Receive(JECPFrames buffer, int timeout)
  {
    receiveLock.writeLock().lock();
    try {
   // Status of the Receive() attempt
      int status = StdConstants.ERROR;                                                                                     
      // Array definition to store received data size (2 bytes)
      byte[] recv_frame_size = new byte[2];                                       
      // Array definition to store received JEC data (240 bytes)   
      byte[] recv_frame_data = new byte[StdConstants.max_frame_size];             
      // variable to store number of bytes received for data
      int bytes_recv = 0;                                                         

  // Try to receive data from the socket (PLC)
      try
      {
        // INFO message that will appear in the logger                    
        logger.info("Waiting to receive data...");                                
        // Writes the received frame into recv_frame_size
        r_data_size.read(recv_frame_size,0,recv_frame_size.length);               
        // Writes the received frame into recv_frame_data
        bytes_recv = r_data_data.read(recv_frame_data,0,recv_frame_data.length);  
        // Assigns status with SUCCESS (0)                            
        status = StdConstants.SUCCESS;                                            
      }
      // Raised when there's an error on RECEIVE
      catch (IOException e)                                                       
      {
        // ERROR message that will appear in the logger                    
        logger.error("Error while receive data "+e);                              
        // Assigns status with ERROR (-1)                            
        status = StdConstants.ERROR;                                              
      }

  // Tests the status of the Receive() attempt
      if (status == StdConstants.SUCCESS)
      {
      // If there is data received, generates a DEBUG message to send to logger
        // If something was received (nr of bytes > 0)
        if(bytes_recv > 0)                                                         
        {
          // Reset the debug_msg string
          StringBuilder debug_msg = new StringBuilder();                                                           
          // Generate the debug message to put in the logger
          for (int j = 0;j < bytes_recv ; j++)                                      
          {
            // Create the debug frame...
            debug_msg.append(" 0x" + Integer.toHexString((int)recv_frame_data[j] & 0xff)); 
          }
          // DEBUG message that will appear in the logger
          logger.debug("Data received:" + debug_msg.toString());                                 
        }

        // INFO message that will appear in the logger
        logger.info("Receive data...complete!");                                  
        // Returns a 240 byte frame with the received data      
        buffer.frame = recv_frame_data;
        return StdConstants.SUCCESS;                                           
      }
      else
        // Function returns ERROR
        return StdConstants.ERROR;
    } finally {
      receiveLock.writeLock().unlock();
    }                                             
  }

/*//////////////////////////////////////////////////////////////////////////////
//                               METHOD RECEIVE                               //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * Method to receive a byte array message through the socket over TCP/IP protocol
 * If called without arguments, the default value for timeout is assigned to 'infinit'
 * @params JECPFrames - the buffer where the received data should be stored
 * @return int  - execution code (0 : success, -1 : error)
 */    
  public int Receive(JECPFrames buffer)  
  {
    // Calls the Receive function above with the default timeout=infinit
    return Receive(buffer,0);
  }

}  

/*//////////////////////////////////////////////////////////////////////////////
//                             END OF CLASS                                   //
//////////////////////////////////////////////////////////////////////////////*/
