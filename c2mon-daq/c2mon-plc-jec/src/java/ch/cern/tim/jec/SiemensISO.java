/*//////////////////////////////////////////////////////////////////////////////
//                         TIM PLC INTERFACING SYSTEM                         //
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
public class SiemensISO implements PLCDriver
{
/**
 * Instanciate a logger in this class if there is no other instance created.
 * This logger allows to register the StdConstants.ERRORs, infos and debugs into a log file
 */
   // Instanciate, if not created, Log4J logger
   static Logger logger = Logger.getLogger(SiemensISO.class);                   

/**
 * Instance of Socket class to establish a connection to the PLC
 */   
   // Socket instance to connect to the PLC
   private Socket cli_socket;                                                   

/**
 * Data output buffer to be assigned to the opened socket (for data to send)
 */
   // Data buffer to send data
   private DataOutputStream s_data = null;                                      

/**
 * Data input buffer to be assigned to the opened socket (for data to receive)
 */
   // Data buffer to receive data
   private DataInputStream r_data = null;                                       

/**
 * Constant definition according the RFC1006 Protocol - (Iso-On-Tcp)
 */
   // 16 bits reserved to the protocol
   private static final int MAXPACKET_SIZE = 65535;                              
   // MAXPACKET_SIZE-(vrsn(1)+reserved(1)+packet_len(2))
   private static final int MAXTPDU_SIZE = MAXPACKET_SIZE - 4;                   
   // MAXTPDU_SIZE - (minimum_packet_length(7))
   private static final int MAXTSDU_SIZE = MAXTPDU_SIZE - 7;                     
   // S7 rule - max destination TSAP length in bytes
   private static final byte MAXTSAPDST_LENGTH = 0x08;                           
   // S7 rule - max source TSAP length in bytes
   private static final byte MAXTSAPSRC_LENGTH = 0x08;                           
   // 34 - max size for RFrame
   private static final byte MAXCFRAME_SIZE = 18 + MAXTSAPDST_LENGTH + MAXTSAPSRC_LENGTH;  
   // Maximum size for data transport
   private static final int MAXDFRAME_SIZE = MAXTPDU_SIZE;                       

   // Default Data Frame identifier
   private static final byte DT_FRAME = (byte ) 0xf0;                            
   // Default Expedited Data Exchange identifier
   private static final byte ED_FRAME = (byte ) 0x10;                            
   // Code to identify an Intermediate frame
   private static final byte INTERMEDIATE_FRAME = (byte ) 0x00;                  
   // Code to identify Last Frame
   private static final byte LAST_FRAME = (byte) 0x80;                           
   // Offset to indicate end of Frame Header
   private static final byte OFFSFRAME = 6;                                      
   // Offset to indicate begin of Data
   private static final byte OFFSDATA = 7;                                       

   // Connection Request - 1st 4 MSB (CR - 1110xxxx = E)
   private static final byte CR_FRAME = (byte ) 0xE0;                            
   // Connection Confirm - 1st 4 MSB (CC - 1101xxxx = D)  
   private static final byte CC_FRAME = (byte ) 0xD0;                            
   // Disconnect Request - all byte (DR - 1000xxxx = 80)
   private static final byte DR_FRAME = (byte ) 0x80;                            
   // Offset pointer to give Packet Length position
   private static final byte OFFSPACKET_LENGTH = 2;                              
   // Offset pointer to give Header Length position
   private static final byte OFFSHEADER_LENGTH = 4;                              
   // Offset pointer to give Credit Code position
   private static final byte OFFSCREDIT_CODE = 5;                                
   // Offset pointer to give Dest TSAP Length position
   private static final byte OFFSTSAPDST_LENGTH = 15;                            

/**
 * Squeleton for communication messages used by the RFC1006 protocol
 */
   // Command Frame squeleton - size defined by RFC1006
   private byte CommandFrame[] = new byte [MAXCFRAME_SIZE];                     
   // Data Frame squeleton - size is defined by the RFC1006   
   private byte sendDataFrame[] = new byte [MAXDFRAME_SIZE];
   private byte receiveDataFrame[] = new byte [MAXDFRAME_SIZE];
   
   /**
    * Lock for sychronising message sending and receiving
    */
   private ReentrantReadWriteLock sendLock = new ReentrantReadWriteLock();
   private ReentrantReadWriteLock receiveLock = new ReentrantReadWriteLock();

/*//////////////////////////////////////////////////////////////////////////////
//              METHOD SIEMENSISO - CONSTRUCTOR (no parameters)               //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Default Class Constructor with no parameters
 */
  public SiemensISO()
  {
  }

  
/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD FILLCommandFrame                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to fill the Command Frame with the standard protocol
 * defined values.
 */
  private void FillCommandFrame()
  {
    // vrsn: always 3 for this RFC version
    CommandFrame[0] = 0x03;                                                     
    // reserved: always 0
    CommandFrame[1] = 0x00;                                                     
    // Length of entire packet including header (2bytes)
    CommandFrame[2] = 0x00;                                                     
    CommandFrame[3] = MAXCFRAME_SIZE;                                                      

    // Length of entire packet excluding header length
    CommandFrame[4] = 0x1D;                                                     
    // code: (4 highest bits): CC/CR/DR
    // credit: (4 lowest bits): 0
    CommandFrame[5] = 0x00;                                                     
    // dstrf: always 0 on output, ignored on input (2bytes)
    CommandFrame[6] = 0x00;                                                     
    CommandFrame[7] = 0x00;                                                      

    // srcref: Siemens specific (2bytes)
    CommandFrame[8] = 0x44;                                                     
    CommandFrame[9] = 0x31;

    // classOptions: always 0
    CommandFrame[10] = 0x00;                                                    
    // TPDU size code: always 0xC0
    CommandFrame[11] = (byte)0xC0;                                              
    // TPDU size: always 0x01
    CommandFrame[12] = 0x01;                                                    
    // TPDU value: always 0x0A
    CommandFrame[13] = 0x0A;                                                    
    // TSAP destination size code: always 0xC1
    CommandFrame[14] = (byte)0xC1;                                              
    // TSAP destination size (max. 8 bytes)
    CommandFrame[15] = MAXTSAPDST_LENGTH;                                       
    // TSAP destination value[0]
    CommandFrame[16] = 0x00;                                                    
    // TSAP destination value[1]
    CommandFrame[17] = 0x00;                                                    
    // TSAP destination value[2]
    CommandFrame[18] = 0x00;                                                    
    // TSAP destination value[3]
    CommandFrame[19] = 0x00;                                                    
    // TSAP destination value[4]
    CommandFrame[20] = 0x00;                                                    
    // TSAP destination value[5]
    CommandFrame[21] = 0x00;                                                    
    // TSAP destination value[6]
    CommandFrame[22] = 0x00;                                                    
    // TSAP destination value[7]
    CommandFrame[23] = 0x00;                                                    
    // TSAP source size code: always 0xC2
    CommandFrame[24] = (byte)0xC2;                                              
    // TSAP source size (max. 8 bytes)
    CommandFrame[25] = MAXTSAPSRC_LENGTH;                                       
    // TSAP source value[0]
    CommandFrame[26] = 0x00;                                                    
    // TSAP source value[1]
    CommandFrame[27] = 0x00;                                                    
    // TSAP source value[2]
    CommandFrame[28] = 0x00;                                                    
    // TSAP source value[3]
    CommandFrame[29] = 0x00;                                                    
    // TSAP source value[4]
    CommandFrame[30] = 0x00;                                                    
    // TSAP source value[5]
    CommandFrame[31] = 0x00;                                                    
    // TSAP source value[6]
    CommandFrame[32] = 0x00;                                                    
    // TSAP source value[7]    
    CommandFrame[33] = 0x00;                                                    
  }

/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD FILLDATAFRAME                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to fill the Data Frame with the standard protocol 
 * defined values.
 */
  private void FillDataFrame(byte[] frame)
  {
    // vrsn: always 3 for this RFC version
    frame[0] = 0x03;                                                        
    // Reserved: always 0
    frame[1] = 0x00;                                                        
    // To fill up with the length of the entire packet (2 bytes)
    frame[2] = 0x00;                                                        
    frame[3] = 0x00;    

    // Length of entire packet excluding header
    frame[4] = 0x02;                                                        
    // Code: (4 highest bits) : DT
    // Credit: (4 lowest bits) : 0
    frame[5] = DT_FRAME;                                                    
    // 0x80 when its the Last Frame
    // 0x00 when its an Intermediate Frame
    frame[6] = 0x00;                                                        

    // User Data (size = MAXTSDU_SIZE)    
    // xx xx xx xx .. */                                                        
  }

/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD OPENSOCKET                                //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to establish a socket connection between the host and
 * the PLC.
 * @param ConnectionData param - Object that includes all the communication 
 * parameters (IP, Port, Tsap's, etc)
 * @return int - Returns a value representing the execution status of this
 * operation. (0 - StdConstants.SUCCESS; -1 - StdConstants.ERROR)
 */
  private int OpenSocket(ConnectionData param)
  {
    // Status of the OpenSocket() attempt
    int status = StdConstants.ERROR;                                            

    // INFO message that will appear in the logger
    logger.info("Trying to open socket to: " + param.ip + " ...");                  

    try
    {
      // Create a new socket connection with 'ip' and 'port'
      cli_socket = new Socket(param.ip,param.port);   
      // Enables the SO_KEEPALIVE option
      cli_socket.setKeepAlive(true);
      // Set the Socket timeout to 10sec
      cli_socket.setSoTimeout(10000);
      // If socket could not be opened, exits function returning error
      if(cli_socket == null)
      {
        // Assigns status with StdConstants.ERROR (-1)
        status = StdConstants.ERROR;
        logger.error("ERROR while trying to create new Socket CLASS...");
        return status;
      }
      // Create an output data buffer for the socket
      s_data = new DataOutputStream(cli_socket.getOutputStream());              
      // Create an input data buffer for the socket
      r_data = new DataInputStream(cli_socket.getInputStream());      
      // Assigns status with StdConstants.SUCCESS (0)      
      status = StdConstants.SUCCESS;                                            
    }
    // StdConstants.ERROR if the host is not reached
    catch (UnknownHostException e)                                              
    {
      // FATAL message that will appear in the logger
      logger.fatal("Host: "+param.ip+" is unknown! - ", e);          
      // Assigns status with StdConstants.ERROR (-1)
      status = StdConstants.ERROR;                                              
    }
    // StdConstants.ERROR if there's no I/O for the connection
    catch (IOException e)                                                       
    {
      // FATAL message that will appear in the logger
      logger.fatal("I/O unreachable for the connection to: "+param.ip, e); 
      // Assigns status with StdConstants.ERROR (-1)
      status = StdConstants.ERROR;                                              
    }

    // If socket is NULL, exits returning error
    if(cli_socket == null)                        // branch added - 1/8/2006
    {
      status = StdConstants.ERROR;
      logger.error("Unable to open Socket for "+param.ip);
      return status;
    }
    // Tests the result of the OpenSocket() attempt
    else if(status == StdConstants.SUCCESS)                                                             
    {
      // DEBUG message that will appear in the logger
      if (logger.isDebugEnabled()) {
        logger.debug("Socket opened to: "+cli_socket);                            
      }
    }
    else
    {
      // DEBUG message that will appear in the logger
      logger.debug("ERROR trying to open socket to: "+cli_socket);              
    }
    return status;                                                            
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
      if (logger.isDebugEnabled()) {
        logger.debug("Socket closed!");                                           
      }
    }
    // Exception if HOST UNREACHABLE
    catch (UnknownHostException e)                                              
    {
      // ERROR message that will appear in the logger
      logger.error("Error on trying to close socket.", e);                      
    }
    // Exception if IO ERROR
    catch (IOException e)                                                       
    {
      // ERROR message that will appear in the logger
      logger.error("Error on trying to close socket. ", e);                      
    }
  }
 
/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD CONNECT                                 //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * This class allows the connection between the host and a Siemens PLC using 
 * ISO-ON-TCP protocol.
 * @param ConnectionData param - IP address (or PLC hostname) in string format 
 * ,Port number in int format, Source and Destination TSAP in string format and 
 * Shutdown option in boolean format.
 * @return int - Result of the Connect() operation attempt (0 - StdConstants.SUCCESS;
 * -1 - StdConstants.ERROR)
 */
  public int Connect(ConnectionData param)
  {
    // Status of the connect() - initially is ERROR
    int status = -1;                                                            
    // If socket opened StdConstants.SUCCESSfully
    if(OpenSocket(param) == StdConstants.SUCCESS)                               
    {
      // Variable declaration and initialization
      // Pointer to navigate inside the array
      int aptr = 0;                                                             
      // String to store debug messages
      StringBuffer debug_msg = new StringBuffer();                                          
      // Fill up the Command Frame to connect using ISO-on-TCP
      this.FillCommandFrame();                                                  

      // Copy of Command Frame to send Connection Request
      byte[] ConnRequest = CommandFrame;                                        
      // Copy of Command Frame to receive Connection Confirm
      byte[] ConnConfirm = CommandFrame;                                        
      
      // Test the length of the Source and Destination TSAP
      // INFO message that will appear in the logger  
      logger.info("Checking TSAP's length...");         

      // If Source and Destination TSAP lengths are correct...
      if ((param.dest_tsap.length() > (int)MAXTSAPDST_LENGTH) || (param.src_tsap.length() > (int)MAXTSAPSRC_LENGTH))
      {
        // StdConstants.ERROR message that will appear in the logger        
        logger.error("ERROR - TSAP ID too long (max. 8 char)");                 
        // Assigns status with StdConstants.ERROR (-1)
        status = StdConstants.ERROR;  
      }
      // If TSAP sizes are OK, continue...
      else
      {
        // Fills the RFC1006 Connection Request Header
        // INFO message that will appear in the logger  
        logger.info("Creating Connection Request Frame...");                      
        // 'aptr' keeps the position 5 in array
        aptr = OFFSCREDIT_CODE;                                                   
        // In CRframe[5], write 0xE0
        ConnRequest[aptr] = CR_FRAME;                                             
        // Pointer for the 15th position in array
        aptr = OFFSTSAPDST_LENGTH;                                                
        // In CRframe[15], writes DEST_TSAP length
        ConnRequest[aptr] = (byte)param.dest_tsap.length();                       
        // Moves pointer to next position (pos=16)
        aptr ++;                                                                  
        // Array to put the DEST_TSAP in bytes
        byte[] dtsap = new byte[param.dest_tsap.length()];                        
        // Fill the dtsap array with 'dst' bytes
        dtsap = param.dest_tsap.getBytes();                                       
        // Pass each byte of 'dtsap' to 'dest'
        for(int i = 0; i < param.dest_tsap.length(); i++)                         
        {
          // Writes the DEST_TSAP byte by byte
          ConnRequest[aptr + i] = dtsap[i];                                       
        }
        // Move the pointer to the end of 'dtsap'
        aptr += param.dest_tsap.length();                                         
        // In this position, writes C2h (RFC1006)
        ConnRequest[aptr] = (byte)0xC2;                                           
        // Moves pointer to next position in array
        aptr ++;                                                                  

        // Here, writes SRC_TSAP length
        ConnRequest[aptr] = (byte)param.src_tsap.length();                        
        // Increase pointer position
        aptr ++;                                                                  
        // Array to put the DEST_TSAP in bytes
        byte[] stsap = new byte[param.src_tsap.length()];                         
        // Fill the tsap array with 'src' byte
        stsap = param.src_tsap.getBytes();                                        
        // Pass each byte of 'stsap' to 'src'
        for(int i = 0; i < param.src_tsap.length(); i++)                          
        {
          // Writes the SRC_TSAP byte by byte
          ConnRequest[aptr + i] = stsap[i];                                       
        }
        // Updates the pointer to position 3
        aptr = OFFSPACKET_LENGTH + 1;                                             
        // Writes the total package length
        ConnRequest[aptr] = (byte) (18+param.dest_tsap.length()+param.src_tsap.length());   
        // Updates the pointer to position 4
        aptr = OFFSPACKET_LENGTH + 2;                                             
        // Writes the package length minus header
        ConnRequest[aptr] = (byte) (ConnRequest[aptr-1] - 5);                     

        // Generate a DEBUG message with a CR Frame in HEX format
        if (logger.isDebugEnabled()) {
          debug_msg = new StringBuffer("Connection Request frame: ");
          // DEBUG: Put all CR array values in Hex
          for (int j = 0;j < ConnRequest[OFFSPACKET_LENGTH + 1];j++)                
          {
            debug_msg.append(" 0x");
            debug_msg.append(Integer.toHexString((int)ConnRequest[j] & 0xff));
          }
          // DEBUG message that will appear in the logger
          logger.debug(debug_msg);                      
        }

  // Get the size of data to send and to receive. Initializes the number of received bytes = 0
        // Reads the CRframe[3] to extract the size of message
        int s_data_size = ConnRequest[OFFSPACKET_LENGTH+1];                       
        // Same value as above: 18+dest_tsap_leng+src_tsap_leng
        int r_data_size = ConnRequest[OFFSPACKET_LENGTH+1];                       
        // Initialize variable to keep the number of received bytes
        int bytes_rcv = 0;                                                        
      
  // If data is consistent, then try to send the array and receive acknowledge through the socket
        // If there's some data to send/receive
        if (cli_socket != null && s_data_size > 0 && r_data_size > 0)             
        {
          try
          {
            // INFO message that will appear in the logger                
            logger.info("Sending Connection Request to PLC...");                  
            // Sends the Connection Request to the PLC
            s_data.write(ConnRequest,0,s_data_size);                              
            // INFO message that will appear in the logger                
            logger.info("Receiving Connection Confirm from PLC...");              
            // Gets the Connection Confirm from the PLC (get nr of bytes)
            bytes_rcv = r_data.read(ConnConfirm,0,r_data_size);                   

            // Analize the received CC frame: size and CC code
            if((bytes_rcv != r_data_size) || (ConnConfirm[OFFSCREDIT_CODE] != CC_FRAME))
            {
              // FATAL message that will appear in the logger        
              logger.fatal("No response or invalid Connection Confirm frame");      
              // Set status = StdConstants.ERROR
              status = StdConstants.ERROR;                                          
            }//if
            else
            {
              // Generate a DEBUG message with a CC Frame in HEX format
              if (logger.isDebugEnabled()) {
                debug_msg = new StringBuffer("Connection Confirm frame: ");
                // DEBUG: Put all CC array values in Hex
                for (int k = 0;k < ConnRequest[OFFSPACKET_LENGTH + 1];k++)              
                {
                  debug_msg.append(" 0x");
                  debug_msg.append(Integer.toHexString((int)ConnConfirm[k] & 0xff));
                }
                // DEBUG message that will appear in the logger
                logger.debug(debug_msg); 
              }
              // Set status = StdConstants.SUCCESS
              status = StdConstants.SUCCESS;                                        
              // INFO message that will appear in the logger                          
              logger.info("Successfully connected to PLC");
            }//else
          }//try

          // StdConstants.ERROR if the host is not reached
          catch (UnknownHostException e)                                          
          {
            // FATAL message that will appear in the logger
            logger.fatal("StdConstants.ERROR sending/receiving request/confirm to/from PLC.", e);   
            // Set status = StdConstants.ERROR
            status = StdConstants.ERROR;                                          
          }
          // StdConstants.ERROR if there's no I/O for the connection
          catch (IOException e)                                                   
          {
            // FATAL message that will appear in the logger
            logger.error("Read timeout from PLC.", e);                             
            // Set status = StdConstants.ERROR
            status = StdConstants.ERROR;                                          
          }

          // If status is not SUCCESS
          if (status != StdConstants.SUCCESS) 
            // Close the socket connection - DISCONNECT
            CloseSocket();
        }// IF testing if there's data to send/receive
        
      }// ELSE from TSAP length test
      
    }//IF from OpenSocket test

      // Return method execution status    
      return status;                                                              
  }

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD DISCONNECT                              //
//////////////////////////////////////////////////////////////////////////////*/  

/**
 * Method to disconnect from a Siemens PLC.
 * If shutdown option is set, it generates and send a disconnection request to the PLC.
 * Otherwise, closes the socket and the PLC will disconnect automatically
 * @param ConnectionData param - IP address (or PLC hostname) in string format 
 * ,Port number in int format, Source and Destination TSAP in string format and 
 * Shutdown option in boolean format.
 * @return int - Result of the Disconnect() operation attempt (0 - StdConstants.SUCCESS;
 * -1 - StdConstants.ERROR)
 */
  public int Disconnect(ConnectionData param) 
  {
    // Status of the Disconnect() attempt
    int status = StdConstants.ERROR;                                            
    // Copy of Command Frame to send Disconnection Request    
    byte[] DisconnRequest = CommandFrame;                                       
    // Poiter to 'navigate' inside the frame
    int aptr = 0;                                                               
    // String to store debug message to be sent to the logger
    StringBuffer debug_msg = new StringBuffer();                         
    // Size of data to be sent
    int s_data_size = 0;

     // Test the length of the Source and Destination TSAP
    // INFO message that will appear in the logger  
    logger.info("Checking TSAP's length...");       

    // If Source and Destination TSAP's have the correct size...
    if ((param.dest_tsap.length() > (int)MAXTSAPDST_LENGTH) || (param.src_tsap.length() > (int)MAXTSAPSRC_LENGTH))
    {
      // FATAL message that will appear in the logger      
      logger.fatal("Error - TSAP ID too long (max. 8 char)");                   
      // Set status = StdConstants.ERROR
      status = StdConstants.ERROR;                                              
    }
    else
    {
      // If the 'shutdown' parameter is true, send Disconnection Request
      if(param.shutdown)
      {
// Fills the RFC1006 Disconnection Request Header
        // INFO message that will appear in the logger  
        logger.info("Creating Disconnection Request Frame...");                     
        // 'aptr' keeps the position 5 in array
        aptr = OFFSCREDIT_CODE;                                                     
        // In DisconnRequest[5], write 0x80
        DisconnRequest[aptr] = DR_FRAME;                                            
        // Pointer for the 15th position in array
        aptr = OFFSTSAPDST_LENGTH;                                                  
        // In DisconnRequest[15], writes DEST_TSAP length
        DisconnRequest[aptr] = (byte)param.dest_tsap.length();                      
        // Moves pointer to next position (pos=16)
        aptr ++;                                                                    
        // Array to put the DEST_TSAP in bytes
        byte[] dtsap = new byte[param.dest_tsap.length()];                          
        // Fill the dtsap array with 'dst' bytes
        dtsap = param.dest_tsap.getBytes();                                         

        // Pass each byte of 'dtsap' to 'dest'
        for(int i = 0; i < param.dest_tsap.length(); i++)                           
        {
          // Writes the DEST_TSAP byte by byte
          DisconnRequest[aptr + i] = dtsap[i];                                      
        }
        // Move the pointer to the end of 'dtsap'
        aptr += param.dest_tsap.length();                                           
        // In this position, writes C2h (RFC1006)
        DisconnRequest[aptr] = (byte)0xC2;                                          
        // Moves pointer to next position in array
        aptr ++;                                                                    

        // Here, writes SRC_TSAP length
        DisconnRequest[aptr] = (byte)param.src_tsap.length();                       
        // Increase pointer position
        aptr ++;                                                                    
        // Array to put the DEST_TSAP in bytes
        byte[] stsap = new byte[param.src_tsap.length()];                           
        // Fill the tsap array with 'src' byte
        stsap = param.src_tsap.getBytes();                                          
        // Pass each byte of 'stsap' to 'src'
        for(int i = 0; i < param.src_tsap.length(); i++)                            
        {
          // Writes the SRC_TSAP byte by byte
          DisconnRequest[aptr + i] = stsap[i];                                      
        }
        // Updates the pointer to position 3
        aptr = OFFSPACKET_LENGTH + 1;                                               
        // Writes the total package length
        DisconnRequest[aptr] = (byte) (18+param.dest_tsap.length()+param.src_tsap.length());              
        // Updates the pointer to position 4
        aptr = OFFSPACKET_LENGTH + 2;                                               
        // Writes the package length minus header
        DisconnRequest[aptr] = (byte) (DisconnRequest[aptr-1] - 5);                 
        // Reads the DisconnRequest[3] to extract the size of message
        s_data_size = DisconnRequest[OFFSPACKET_LENGTH+1];                      

        // Prepares DEBUG message to send to the logger
        if (logger.isDebugEnabled()) {
          debug_msg = new StringBuffer((5 *s_data_size) + 20);
          debug_msg.append("Disconnection request frame: ");
          // DEBUG: Put all DR array values in Hex
          for (int j = 0;j != s_data_size;j++)                                         
          {
            debug_msg.append(" 0x");
            debug_msg.append(Integer.toHexString((int)DisconnRequest[j] & 0xff));
          }
          // DEBUG message that will appear in the logger
          logger.debug(debug_msg);
        } // if DEBUG
      }//IF from SHUTDOWN TEST

// Checks if there is an opened socket. If yes, send DR and close the socket
      try
      {
        // If there is an opened socket...
        if (cli_socket != null)                                                   
        {
          // If 'shutdown' is set TRUE, send DR before closing socket
          if (param.shutdown)                                                     
          { // INFO message that will appear in the logger          
            logger.info("Closing connection using SHUTDOWN...");                  
            // Sends the Disconnection Request to the PLC
            s_data.write(DisconnRequest,0,s_data_size);                           
            // Close the output stream
            s_data.close();                                                       
            // Close the input stream
            r_data.close();                                                       
          }
          else
          {
            // INFO message that will appear in the logger                    
            logger.info("Closing connection normally...");                        
          }

          // Close the socket
          this.CloseSocket();                                                     
          // INFO message that will appear in the logger             
          logger.info("Successfully disconnected from PLC");                      
          // Assigns status with SUCCESS (0)                        
          status = StdConstants.SUCCESS;                                          
        }
        else
        {
          // INFO message that will appear in the logger
          logger.info("No Socket available to disconnect from");
          // Assigns status with SUCCESS (0)                                  
          status = StdConstants.ERROR;                                          
        }
      }//try
      // Raised when task is completed - connection closed
      catch (IOException e)                                                       
      {
        // ERROR message that will appear in the logger          
        logger.error("Error while trying to close socket");                       
        // Assigns status with ERROR (-1)                              
        status = StdConstants.ERROR;                                              
      } 
    }// ELSE from the TSAP length test

    // Return method execution status    
    return status;                                                              
  }

/*//////////////////////////////////////////////////////////////////////////////
//                              METHOD SEND                                   //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * Method to send a message through the socket over ISO-on-TCP protocol. This 
 * method checks the size of the message and, if necessary, splits it into smaller
 * packets and send them one after the other.
 * @param byte[] msg - Array of bytes to be sent to the PLC
 * @return int - Result of the Send() operation attempt (0 - StdConstants.SUCCESS;
 * -1 - StdConstants.ERROR)
 */
  public int Send(JECPFrames Frame)
  {
    sendLock.writeLock().lock();
    try {
   // Variable declaration and initialization  
      // Status of the send() attempt
      int status = 0;                                                             
      // Variable to store the max size of a single data packet
      int n = 0;                                                                  
      // Variable to use in loop instructions
      int i = 0;                                                                  
      // Variable to store the full frame size to be sent
      int l = Frame.frame.length;                                                       
      // Variable to store the max frame size
      short size = 0;                                                             
      // Pointer to 'navigate' inside the message
      byte ptrmsg = 0;                                                            
      // Fill up the Data Frame to send data using ISO-on-TCP
      this.FillDataFrame(sendDataFrame);                                                       
      // Copy of Data Frame to send data to PLC
      byte[] MessageData = sendDataFrame;                                             
      // Array to store a 'short' type value splitted in 2 bytes
      byte[] bytes = new byte[2];                                                 
      // String to store debug message to be sent to the logger

      // Checks the size of entire message and, if necessary, splits it    
      // While the end of msg is not reached...
      while(l > 0)                                                                
      {
        // DEBUG message that will appear in the logger              
        logger.debug("Checking size of message to be sent...");                    
        // If message is bigger than the max packet size
        if(l > MAXTSDU_SIZE)                                                      
        {
          // Assigns the max packet size for 1st packet of data
          n = MAXTSDU_SIZE;                                                       
          // Converts and stores the max data frame size (RFC1006)
          size = (short)MAXDFRAME_SIZE;                                           
          // Split the 'short' type into 2 bytes 
          // Ex.: 3550 = 0DDEh -> DT[2]=0x0D DT[3]=0xDE
          for(i = 0; i < bytes.length; i++)                                       
          {                                                                       
            // Offset calculation according the number of bytes used (2)
            int offset = (bytes.length - i - 1) * 8;                              
            // Masks size with 0xFF shifted by the offset value
            bytes[i] = (byte)((size & (0xff << offset)) >>> offset);              
            // In MessageData[2 and 3] writes the 'short' splitted
            MessageData[OFFSPACKET_LENGTH + i] = bytes[i];                        
          }
          // In MessageData[6] writes 0x00 meaning Intermediate Frame
          MessageData[OFFSFRAME] = INTERMEDIATE_FRAME;                            
          // Decrease 'L' in 'N' units (L = L - N)
          l -= n;                                                                 
        }
        // If message is not bigger than the max packet size
        else                                                                      
        {
          // Assigns the size of data as being the max packet size
          n = l;                                                                  
          // Size of frame is: 65531 -(65524-4) = 11
          size = (short)(MAXDFRAME_SIZE - (MAXTSDU_SIZE - n));                    
          // Split the 'short' type into 2 bytes 
          // Ex.: 3550 = 0DDEh -> DT[2]=0x0D DT[3]=0xDE
          for(i = 0; i < bytes.length; i++)                                       
          {                                                                       
            // Offset calculation according the number of bytes used (2)
            int offset = (bytes.length-i-1)*8;                                    
            // Masks size with 0xFF shifted by the offset value
            bytes[i] = (byte)((size & (0xff << offset)) >>> offset);              
            // In MessageData[2 and 3] writes the 'short' splitted
            MessageData[OFFSPACKET_LENGTH + i] = bytes[i];                        
          }
          // In MessageData[6] writes 0x80 meaning Last Frame
          MessageData[OFFSFRAME] = LAST_FRAME;                                    
          // Resets 'l' variable and exit cycle
          l = 0;                                                                  
        }

        // Fill frame with the data to be sent down to the PLC      
        // DEBUG message that will appear in the logger    
        if (logger.isDebugEnabled()) {
          logger.debug("Creating Data Frame to send...");                           
        }
        // Writes the bytes of data in DTframe
        for(int k = ptrmsg; k < n ; k++)                                          
        {
          // Arranges the data, starting at the position DTframe[7]
          MessageData[OFFSFRAME + 1 + k] = Frame.frame[k];                              
        }
        // 'PTRMSG' updated if data's splitted in several packets
        ptrmsg += n;                                                              

        // Checks if there is an opened socket. If yes, send Data through the socket
        try
        {
          // Get the system time and format it into Date type
          Date dt = new Date(System.currentTimeMillis());                         
          // If there is a opened socket
          if (cli_socket != null)                                                 
          {
            // INFO message that will appear in the logger                
            logger.debug("Sending Data Frame...");                                 
            // Sends the Data Frame to the PLC
            s_data.write(MessageData,0,(int)size);                                

            // Preparing DEBUG message to send to the logger
            if (logger.isDebugEnabled()) {
              StringBuffer debug_msg = new StringBuffer("DT frame: ");                                                           
              // DEBUG: Put all DR array values in Hex
              for (int j = 0 ; j != (int)size ; j++)                                     
              {  
                debug_msg.append(" 0x");
                debug_msg.append(Integer.toHexString((int)MessageData[j] & 0xff));
              }
              // DEBUG message that will appear in the logger
              logger.debug(debug_msg);                                      
            }

            // INFO message that will appear in the logger                              
            logger.debug("Data Frame sent on : "+dt.toString());                     
            // Assigns status with SUCCESS (0)                              
            status = StdConstants.SUCCESS;                                          
          }
          else
          {
            // INFO message that will appear in the logger                
            logger.info("No Socket to send data");                          
            // Assigns status with ERROR (-1)                              
            status = StdConstants.ERROR;                                            
          }
        }//try
        
        // Raised when there's an error on SEND
        catch (IOException e)                                                     
        {
          // ERROR message that will appear in the logger                
          logger.error("Error sending Data Frame ", e);                            
          // Assigns status with ERROR (-1)                                      
          status = StdConstants.ERROR;                                            
        } 

      }//WHILE loop

      // Resets the pointer that tracks the written positions inside the JEC Frame data area
      Frame.ResetDataBufferOffset();

      // Return method execution status    
      return status; 
    } finally {
      sendLock.writeLock().unlock();
    }                                                            
  }  

/*//////////////////////////////////////////////////////////////////////////////
//                              METHOD RECEIVE                                //
//////////////////////////////////////////////////////////////////////////////*/
  
/**
 * Method to receive a byte array message through the socket over ISO-ON-TCP protocol
 * @param int timeout - Timeout to receive a JEC message from socket
 * @return byte[] msg - Message received from PLC (NULL in case of error receiving)
 */
  public int Receive(JECPFrames buffer, int timeout) 
  {
    receiveLock.writeLock().lock();
    try {
   // Variable declaration and initialization    
      // Status of the send() attempt
      int status = 0;                                                             
      // Variable to store the number of data bytes to read
      int n = 0;                                                                  
      // Variable to store the size of entire packet
      short size = 0;                                                             
      // Fill up the Data Frame to send data using ISO-on-TCP
      this.FillDataFrame(receiveDataFrame);                                                       
      // Copy of Data Frame to send data to PLC
      byte[] MessageData = receiveDataFrame;                                             
      // Pointer to 'navigate' inside the array of data
      int dptr = 0;                                                               
      // Array to store the received data (filtered - no header)
      byte[] msg = new byte[StdConstants.max_frame_size];                         
      // Variable to store the position left in the buffer
      int msgptr = 0;                                                             
      // Number of received bytes
      int bytes_rcv = 0;                                                          
      // Variable to store the size of Data header
      int rcv_len = 0;                                                            
      // Length of all data from one or more packets
      int total_data_len = 0;                                                                                              
      // Variable to store the number of receive attempts
      int retry = 0;     

      StringBuffer debug_msg = new StringBuffer();

      // Tries to receive all the packages assigned to a message
      do                                                                          
      {
        // Size of the Data header (7)
        rcv_len = MAXDFRAME_SIZE - MAXTSDU_SIZE;                                  
        try
        {
          // INFO message that will appear in the logger     
          logger.debug("Waiting to receive data...");                 
          // Set the timeout value to read from socket of 2sec
          // Writes the received frame to DTframe structure
          bytes_rcv = r_data.read(MessageData);
          // Assigns status with SUCCESS (0)
          status = StdConstants.SUCCESS;                                          
          // Increase the number of retries        
          retry ++;                                                               
        } 
        // Raised when there's an error on RECEIVE
        catch (SocketTimeoutException e)                                                     
        { 
          logger.error("Timeout while waiting for data on the incoming stream", e);                            
          // Assigns status with ERROR (-1)                                      
          status = StdConstants.ERROR;                                            
        }
        catch (IOException e)                                                     
        { 
          logger.error("Unexpected IOException caught while waiting for data on the incoming stream", e);                            
          // Assigns status with ERROR (-1)                                      
          status = StdConstants.ERROR;                                            
        }

        // If the reception was succeeded
        if(status == StdConstants.SUCCESS)
        {
  // If something was received, print the message header in the logger
          if(bytes_rcv > 0 && logger.isDebugEnabled())                                                         
          {
            // Reset the debug_msg string
            debug_msg = new StringBuffer("DT header: ");                                                         
            // DEBUG: Put all DR array values in Hex
            for (int j = 0;j != rcv_len ; j++)                                       
            {
              debug_msg.append(" 0x");
              debug_msg.append(Integer.toHexString((int)MessageData[j] & 0xff));
            }
            // DEBUG message that will appear in the logger
            logger.debug(debug_msg);                                   
          }
          
          // Analize DT Frame to see if it's not a Disconnection Request from the PLC
          // Check if its a Disconnection Request Frame
          if(MessageData[OFFSCREDIT_CODE] == DR_FRAME)                            
          {
            // ERROR message that will appear in the logger            
            logger.error("PLC sent a Disconnection Request instead of data "+debug_msg);  
            // Assigns status with ERROR (-1)                                        
            status = StdConstants.ERROR;                                          
          }

        // Extract the data from DTframe
          // Gets the position of MessageData[6+1] - Data Start
          dptr = OFFSFRAME+1;                                                       

          // The next 3 lines pick 2 bytes in the frame and convert them into a 'short' type variable
          // Mask the first 8 bits DTframe[2] with a 16 bit structure
          size = (short)(0xFFFF & (short)MessageData[OFFSPACKET_LENGTH]);           
          // Shift those bits 8 positions left - assign as MSByte
          size = (short)(size << 8);                                                
          // Pick the second byte DTframe[3] and assign it as LSByte
          size = (short)(size | MessageData[OFFSPACKET_LENGTH+1]);                  

          // Size of entire frame (Header and Data)
          // JAVA 'byte' type: 0x00 -> 0x7f = 00 -> 127
          //                   0x80 -> 0xff = -128 -> -1
          size = (short)((size < 0) ? (256 + size) : size);                         
                                                                                  
          // Size of 'usefull' data (Frame - Header)
          n = (int)(size) - (int)(MAXDFRAME_SIZE - MAXTSDU_SIZE);                   

    // Retrieve the 'usefull' data from the DTframe
          // If some data was received
          if(n > 0 )
          {
            if (logger.isDebugEnabled()) {
              // Reset the debug_msg string
              debug_msg = new StringBuffer("Data received: ");
              // DEBUG: Put all DR array values in Hex
              for(int k = dptr; k != size ; k++)                                       
              {
                // Stores the data in a 'usefull' data array so that more data can be added (in case of several packets)
                debug_msg.append(" 0x");
                debug_msg.append(Integer.toHexString((int)MessageData[k] & 0xff));
              }

              // DEBUG message that will appear in the logger
              logger.debug(debug_msg);                               
            }

            // Adds data from different packets to msg[] buffer
            System.arraycopy(MessageData, dptr, msg, msgptr, n);

            /*
            for(int l = 0; l != n; l++)
            {
              // Adds data from different packets to msg[] buffer
              msg[msgptr + l] = MessageData[dptr + l];                              
            }
            */
            // Saves the current position where to write new data
            msgptr += n;                                                            
          }  
        }
        // If status is different from SUCCESS
        else
        {
          // DEBUG message that will appear in the logger
     /*     if (logger.isDebugEnabled()) {
            logger.debug("Could not receive data");
          }
     */        
          // Exits from the while loop
          break;
        }
      // While the current packet is not LAST_FRAME and the number of retries is not exceeded
      }while((MessageData[OFFSFRAME] != LAST_FRAME) && (retry <= StdConstants.recv_retry));

  // Tests the status of the Receive() attempt
      if (status == StdConstants.SUCCESS)
      {
        // INFO message that will appear in the logger
        logger.debug("Receive data...complete!");                                  
        // Returns the received data
        buffer.frame = msg;

        return StdConstants.SUCCESS;                                                               
      }
      else
      {
        // Function returns NULL in case of problems during reception
        return StdConstants.ERROR;                                                
      }
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
 * @return byte[] - Message received from PLC (NULL in case of error receiving)
 */    
  public int Receive(JECPFrames buffer)  
  {
    // Calls the Receive function above with the default timeout=infinit 
    return Receive(buffer,0);
  }

}

/*//////////////////////////////////////////////////////////////////////////////
//                                 END OF CLASS                               //
//////////////////////////////////////////////////////////////////////////////*/
