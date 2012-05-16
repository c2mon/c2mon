////////////////////////////////////////////////////////////////////////////////
//                        TIM PLC INTERFACING SYSTEM                          //
//                                                                            //
//  Standard squeleton for a JEC Frame message.                               //
//  This class includes methods to fill up a JECP message.                    //
//  Its used to send/receive data blocks to/from the PLC                      //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes            01/07/2004          First Implementation           //
//  Joao Simoes            25/07/2004         Comment ameiloration            //
//////////////////////////////////////////////////////////////////////////////*/

package ch.cern.tim.jec;

import java.util.*;
import java.lang.Math;
import org.apache.log4j.Logger;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.Cloneable;
import java.text.*;

/**
 * This class defines a structure for a standard JEC Frame.
 * It contains JEC protocol predefined values
 */
public class JECPFrames
{
/**
 * Instanciate a logger in this class if there is no other instance created.
 * This logger allows to register the errors, infos and debugs into a log file
 */
  // Instanciate, if not created, Log4J logger
  static Logger logger = Logger.getLogger(JECPFrames.class);                    
  // Instantiate stdConstants to access JEC protocol predefined constants


  //static StdConstants stdConstants = new StdConstants();

/**
 * This hashtable is used to store message ID numbers and the timestamp related to 
 * when the message was sent. If the Actual Time minus the message timestamp is 
 * bigger than Message_Timeout, the message is signed as NO LONGER VALID and is 
 * deleted.
 */
 public static Hashtable SeqNumberArray = new Hashtable();        
  
           
/**
 * Byte array to store the JEC messages with a null size
 */  
  // Frame body initialized with null (no size)
  public byte[] frame = null;                                                   

/**
 * This variable is used to define an offset in the JECPFrame because Schneider
 * needs 2 more bytes in the beginning with the total frame size (240 bytes - F0)
 */
  // Default offset value - 0
  public int offset = 0;                                                        

/**
 * Variables to store the date and time attributes
 */
  // Last two digits of the year (i.e. 2004 -> 04)
  public byte Year = 0;                                                         
  // Month number coded in packed BCD (i.e. December -> 12 -> 0001_0010)  
  public byte Month = 0;                                                        
  // Day number coded in packed BCD (1 < Day < 31)
  public byte Day = 0;                                                          
  // Hour coded in packed BCD (between 0 and 23)
  public byte Hour = 0;                                                         
  // Minute coded in packed BCD (between 0 and 59)
  public byte Minute = 0;                                                       
  // Second coded in packed BCD (between 0 and 59)
  public byte Second = 0;                                                       
  // Two most significant milliseconds digits coded in packed BCD (0..99)
  public byte Msec_Hi = 0;                                                      
  // Single less significant milliseconds digit coded in packed BCD (0..9)
  public byte Msec_Lo = 0;                                                      
  // Week_Day is the day number coded in packed BCD (1=Sunday, 2=Monday,...,7=Saturday)  
  public byte Week_Day = 0;                                                     

/**
 * This variable keeps the position of the last value added to a JEC frame so
 * that we can add more values in the same frame in separated instructions.
 * This variable is reseted by the SEND() functions in each protocol definition.
 */
  private int DataBufferOffset = 0;   
  
/*//////////////////////////////////////////////////////////////////////////////
//                  METHOD JECFRAME - CONSTRUCTOR                             //
//////////////////////////////////////////////////////////////////////////////*/
   
/**
 * Creates a new JEC Frame structure with predefined values - JECP
 * @param byte TypeOfMsg - This parameter is used to define the type of message
 * this frame will represent (INIT, ACK, ANALOG_DATA, etc.)
 * @param int PLCOffset - This parameter is used only because Schneider frame 
 * must have 2 additional bytes in the beginning of the frame with the data size
 */
  public JECPFrames(byte TypeOfMsg, int PLCOffset)
  {
    // Saves the 'offset' parameter
    offset = PLCOffset;                                                         
    if (offset == 2)
    {
      // JEC Frame - max size is 242 bytes (for Schneider)
      frame = new byte[StdConstants.max_frame_size + offset];     
      
      // Schneider JECP Frame Size - MSByte is 0
      frame[0] = 0x00;                                                          
      // Schneider JECP Frame Size - LSByte is F0 (240)
      frame[1] = (byte)0xF0;                                                    
    }
    else
    {
      // JEC Frame - max size is 240 bytes (for Siemens)
      frame = new byte[StdConstants.max_frame_size];
    }
    
    // Header for JECP INIT message
    // Year (i.e. 2000 = 0x00 - Two last digits from the year)
    frame[0 + offset] = 0x00;                                                  
    // Month (i.e. 1 = 0x01 - 1 means January)
    frame[1 + offset] = 0x01;                                                  
    // Day (i.e. 1 = 0x01 - 1st day of the month)
    frame[2 + offset] = 0x01;                                                  
    // Hour (i.e. 0 = 0x00 - 0 hours)
    frame[3 + offset] = 0x00;                                                  
    // Minute (i.e. 0 = 0x00 - 0 minutes)
    frame[4 + offset] = 0x00;                                                  
    // Second (i.e. 0 = 0x00 - 0 seconds)
    frame[5 + offset] = 0x00;                                                  
    // Two MSB of milliseconds (i.e. 0 = 0x00 - 00x msecs)
    frame[6 + offset] = 0x00;                                                  
    // 4MSB: two LSB of milliseconds (i.e. 0 = 0x00 - xx0 msecs)
    // 4LSB: Day of week (1=Sunday,...,7=Saturday)
    // i.e. value 0x01 means: 0 for milliseconds and 1 for Sunday
    frame[7 + offset] = 0x00;                                                  

    // Message identifier (i.e. 0x01 is the INIT message)                                                                                                                                                                
    frame[8 + offset] = TypeOfMsg;                                             
    // Data Type
    frame[9 + offset] = 0x00;                                                  
    // Data Start Number: Byte1
    frame[10 + offset] = 0x00;                                                 
    // Data Start Number: Byte2    
    frame[11 + offset] = 0x00;                                                 
    // Data Offset: Byte1    
    frame[12 + offset] = 0x00;                                                 
    // Data Offset: Byte2        
    frame[13 + offset] = 0x00;                                                 
    // Sequence Number (message number) - Byte1
    frame[14 + offset] = (byte)0x00;                                           
    // Sequence Number (message number) - Byte2
    frame[15 + offset] = (byte)0x00;                                           

    // Data for INIT message
    // JECPIc Alive period (0, no alive, to 4294967296 ms)
    frame[16 + offset] = 0x00;                                                 
    // JECPIc Alive period (i.e. 5sec = 0x00001388 ms)
    frame[17 + offset] = 0x00;                                                 
    // JECPIc Alive period 
    frame[18 + offset] = 0x00;                                                 
    // JECPIc Alive period 
    frame[19 + offset] = 0x00;                                                 

    // User Data
    // xx .. xx .. xx                                                           
  }
  
/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD UPDATEMSGTYPE                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method allows the changing of the type of message. We can instanciate a
 * JECP frame to send data that can be used for INIT_MSG, SET_TIME_MSG, etc. 
 */
 public void UpdateMsgID(byte newType)
 {
   // Overwrites the message type passed as argument
   frame[8+offset] = newType;                                                   
 }

/*//////////////////////////////////////////////////////////////////////////////
//                              METHOD GETMSGID                               //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method us used to extract the Message Identifier from a JEC Frame
 * This value (byte) is converted to int and then returned back
 */

  public int getMsgID() 
  {
    // Casts the byte value into int
    return (int)(frame[8+offset] & 0xFF);
  }

/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD SET MESSAGE IDENTIFIER                    //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to assign a specific Message Identifier in a JEC Frame
 */

  public void SetMessageIdentifier(byte msgType)
  {
    // Writes the msgType parameter in the right position in JEC Frame
    frame[8+offset] = msgType;
  }

/*//////////////////////////////////////////////////////////////////////////////
//                           METHOD SET DATA TYPE                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to assign a specific Data Type in a JEC Frame
 */

  public void SetDataType(byte dataType)
  {
    // Writes the dataType parameter in the right position in JEC Frame
    frame[9+offset] = dataType;
  }

/*//////////////////////////////////////////////////////////////////////////////
//                              METHOD GET DATA TYPE                          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to extract the Data Type from a JEC Frame
 */

  public int GetDataType()
  {
    // Casts the byte value into int
    return (int)(frame[9+offset] & 0xFF);
  }

/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD SET DATA START NUMBER                       //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to set the Data Start Number in a JEC Frame
 */

  public void SetDataStartNumber(short DataStartNr)
  {
    // e.g. DataStartNr = aaaaaaaabbbbbbbb (16 bits)
    // 8 less significant bits from Data Start Number - bbbbbbbb
    frame[11+offset] = (byte) (DataStartNr & 0xFF);
    // 8 most significant bits from Data Start Number - aaaaaaaa
    frame[10+offset] = (byte)((DataStartNr >> 8) & 0xFF);
  }

/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD GET DATA START NUMBER                       //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to get the Data Start Number from a JEC Frame
 */

  public int GetDataStartNumber()
  {
    // byte[10] = aaaaaaaa
    // byte[11] = bbbbbbbb
    // Variable 'val' has value: aaaaaaaa
    int val = (int)frame[10+offset] & 0xFF;
    // Variable 'val' has value: aaaaaaaabbbbbbbb
    return (val << 8) | frame [11+offset];
  }  

/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD SET DATA OFFSET                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to set the Data Offset in a JEC Frame
 */

  public void SetDataOffset(short DataOffset)
  {
    // DataOffset = aaaaaaaabbbbbbbb
    // 8 less significant bits from Data Offset - bbbbbbbb
    frame[13+offset] = (byte) (DataOffset & 0xFF);
    // 8 most significant bits from Data Offset - aaaaaaaa
    frame[12+offset] = (byte)((DataOffset >> 8) & 0xFF);
  }

/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD GET DATA OFFSET                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to get the Data Offset from a JEC Frame
 */

  public int GetDataOffset()
  {
    // byte[12] = aaaaaaaa
    // byte[13] = bbbbbbbb
    // Variable 'val' has value: aaaaaaaa
    int val = (int)frame[12+offset] & 0xFF;
    // Variable 'val' has value: aaaaaaaabbbbbbbb
    return (val << 8) | frame [13+offset];
  }

/*//////////////////////////////////////////////////////////////////////////////
//                          METHOD SET PLC CONFIG                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method receives the necessary parameters for the Set Configuration in one
 * time. Those parameters are read and then assigned inside the JECFrame.
 */
 public void SetPLCConfig(int LengthBoolDataToRead,
                           int LengthAnalogDataToRead,
                           int NumberMMDBoolInputs,
                           int NumberMMDAnalogInputs,
                           int NumberMMDBoolCommands,
                           int NumberMMDAnalogCommands,
                           int NumberDPSlaves,
                           Vector SlaveAddresses)
 {
    frame[17 + offset] = (byte)(LengthBoolDataToRead & 0xFF);
    frame[16 + offset] = (byte)((LengthBoolDataToRead >> 8) & 0xFF);

    frame[19 + offset] = (byte)(LengthAnalogDataToRead & 0xFF);
    frame[18 + offset] = (byte)((LengthAnalogDataToRead >> 8) & 0xFF);

    frame[21 + offset] = (byte)(NumberMMDBoolInputs & 0xFF);
    frame[20 + offset] = (byte)((NumberMMDBoolInputs >> 8) & 0xFF);

    frame[23 + offset] = (byte)(NumberMMDAnalogInputs & 0xFF);
    frame[22 + offset] = (byte)((NumberMMDAnalogInputs >> 8) & 0xFF);

    frame[25 + offset] = (byte)(NumberMMDBoolCommands & 0xFF);
    frame[24 + offset] = (byte)((NumberMMDBoolCommands >> 8) & 0xFF);

    frame[27 + offset] = (byte)(NumberMMDAnalogCommands & 0xFF);
    frame[26 + offset] = (byte)((NumberMMDAnalogCommands >> 8) & 0xFF);

    frame[29 + offset] = (byte)(NumberDPSlaves & 0xFF);
    frame[28 + offset] = (byte)((NumberDPSlaves >> 8) & 0xFF);

// ----------------- PUT DP SLAVE ADDRESSES INSIDE JEC FRAME -------------------

    Enumeration e = SlaveAddresses.elements();
    int i = 0;

    // If, number of slaves is zero (no slaves)
    if(NumberDPSlaves != 0)
    {
      while(e.hasMoreElements())
      {
        Byte DPSlave = (Byte)e.nextElement();
        // DP Slave address value 
        frame[30 + offset + i] = (byte)(DPSlave.byteValue() & 0xFF);
        // Simple debug message to print the DP Slave addresses used
        if (logger.isDebugEnabled()) {
          logger.debug("DP SLAVES: "+DPSlave);
        }
        // Increment in the iterator
        i++;
      }// while
    }// else
 }

/*//////////////////////////////////////////////////////////////////////////////
//                          METHOD FOUNDINTABLE                               //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to check if a sequence number is already assigned to a 
 * message. 
 */
 private boolean FoundInTable(short value)
 {
   // Returns TRUE if value was found and FALSE if not
   return SeqNumberArray.containsKey(new Short(value));
 }

/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD SET SEQUENCE NUMBER                         //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to generate a random ID to assign to the message. After
 * generating and assigning this ID, it will be registered in a Vector with a 
 * timestamp. This is used to trace the acknowledges received from the PLC.
 */

/*
 public short SetSequenceNumber()
 {
   // Random function using as seed the system time in msecs
   Random generator = new Random();                                             
   // Variable to store the random value generated   
   short rnd_val = 0;                                                           

// Tests if the generated number already exists
   do
   {
     // The nextFloat() returns a random value between 0 and 1.
     // Generates a random SHORT between 0 and 65535
     rnd_val = (short)Math.round(0xFFFF * generator.nextFloat());               
   }
   // Until its not found in the table
   while(FoundInTable(rnd_val)); // do                                          

   // NOTO TO EXPLAIN NEXT STATEMENTS: In Java, SHORT type is represented as:
   // 0x8000 = -32768
   // 0xFFFF = -1
   // 0x0000 = 0
   // 0x7FFF = 32768
 
   // Tests if the generated number is negative. If it is, put it in positive.
   // If the result value is negative...
   if(rnd_val < 0)                                                              
     // Add 32768 to be inside the positive range
     rnd_val = (short)(rnd_val + 0x8000);                                       

   // Writes this identifier in the JECP Frame
   // Sequence Number - 8 LSB
   frame[15+offset] = (byte)(rnd_val & 0xFF);                                   
   // Sequence Number - 8 MSB
   frame[14+offset] = (byte)((rnd_val >> 8) & 0xFF);                            

   // Insert the new unique number in the Vector
   SeqNumberArray.put(new Short(rnd_val),
                                   new Long(System.currentTimeMillis()));

   // Returns a 16bit value with Message Identifier
   return rnd_val;                                                              
 }

*/ 

/*//////////////////////////////////////////////////////////////////////////////
//                  METHOD SET SEQUENCE NUMBER - FREE SELECTION               //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to assign a free sequence number to the message.
 * This is used to trace the acknowledges received from the PLC.
 * @param seqnum - Sequence number to be assigned to the message
 */
 public void SetSequenceNumber(byte seqnum)
 {
   // Writes this identifier in the JECP Frame
   // Sequence Number - 8 MSB
   frame[14 + offset] = seqnum;                                   
 } 

/*//////////////////////////////////////////////////////////////////////////////
//                       METHOD GET NUMBER OF RETRIES                         //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to read the number of retries used from the PLC to send
 * acknowledges.
 * @return byte - Returns the number of retries
 */
 public byte GetRetryNumber()
 {
   // Reads this identifier from the JECP Frame
   return frame[15+offset];                                   
 } 


/*//////////////////////////////////////////////////////////////////////////////
//                         METHOD GET SEQUENCE NUMBER                         //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method reads a JECP Frame and returns the sequence number as SHORT
 */ 
 public byte GetSequenceNumber()
 {
   // Returns the Sequence Number
   return frame[14 + offset];
 }

/*//////////////////////////////////////////////////////////////////////////////
//                       METHOD FREE SEQUENCE NUMBER                          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to remove an assigned value in the sequence number table.
 * This methos is called when an acknowledge is received. The sequence number
 * can be now re-used for other messages.
 */ 
 public void FreeSequenceNumber(short id)
 {
   // Casts the short type into Short Object to use as key
   Short key = new Short(id);
   // If the key was found inside the vector...
   if (SeqNumberArray.containsKey(key)) 
   {
     // Remove this key
     SeqNumberArray.remove(key);
   }
 }

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD CHECKTIDIMEOUT                           //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to check if the sequence numbers are still valid.
 * If a message is sent to the PLC and there's no acknowledge back, the identifier
 * expires and is then deleted.
 */ 
 public void CheckIDTimeout()
 {
   // Variable for Loop Cycle
   int i = 0;                                                                   
   // Current system time in milliseconds
   long curr_time = System.currentTimeMillis();                                 

   // Makes a clone of the Sequence Number Hashtable to be able to delete
   // NOTE: Clones just have copy of the reference in the original Hashtable
   Hashtable SeqNumberArrayClone = (Hashtable)SeqNumberArray.clone();
   // Enumerator used to navigate inside the Hashtable elements
   Enumeration e = SeqNumberArrayClone.elements();
   // Enumerator used to navigate inside the Hashtable keys
   Enumeration e1 = SeqNumberArrayClone.keys();
   // While there are more elements in the Hashtable...
   while (e.hasMoreElements()) 
   {
     // Extract the first element (Timestamp in Long format)
     Long el  = (Long)e.nextElement();
     // Extract the first key (key of the extracted timestamp)
     Short k1 = (Short)e1.nextElement();
     // If the actual time minus the one in vector is bigger that timeout...
     // curr_time - timestamp > timeout
     if((curr_time - el.longValue()) > StdConstants.Message_Timeout)  
     {
        // Send information to the logger saying that the (value,key) pair was removed
        logger.info("JECP ID timed out! "+SeqNumberArray.get(k1)+" removed from table"); 
        // Remove the value and key from the table
        SeqNumberArray.remove(k1);
     }
   }//while

   // Garbage collect the Cloned vector
   SeqNumberArrayClone = null;
 }

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD GETDATETOSTRING                          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method extracts the JECP date and time and convert it in a 'human readable'
 * format to get the debugging easier.
 */
 public String GetDateToString()
 {
   // Takes a JEC frame as parameter and extract the date from it
   return Tools.JECPtoString(frame,offset);                                     
 }

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD SYNCHRONIZEPLC                           //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This Class gets the date and time from the system, prepares the JEC Frame to 
 * be sent and add 1 second to the time (frame with 1sec in advance in relation  
 * to system) so that the SEND trigger happens nearest the second transition - sync.
 * This class also tests if the 1 sec increase of time doesn't change the day.
 * NOTE: The synchronization must be the last instruction before SEND()
 */
  public void JECSynchronize()                                                
  {
    // Time to put system time in advance (in msecs)
    long DeltaT = 1000;                                                         
    // Variable to store the Day number
    byte temp = 0x00;                                                           
    
    do
    {
      // Extracts the current system date (in milliseconds)
      long sys_date = System.currentTimeMillis();                               

      logger.info("Sending synchronization to PLC...");

      // With the previous value, extracts the date into JEC format
      this.ExtractDate(sys_date);                                               
      // Assigns the current day to the variable 'temp'
      temp = Day;                                                               
      // Extracts the time plus the advance delay (1 sec)
      this.ExtractTime(sys_date + DeltaT);                                      
      // Extracts the date plus the advance delay (1 sec)
      this.ExtractDate(sys_date + DeltaT);                                      
      // If the date in advance is smaller than current time
      while ((sys_date + DeltaT) > System.currentTimeMillis())                  
      {                                                       
        try { Thread.sleep(50); } catch (InterruptedException ie) {}
        // Does nothing (just waits before send synchronization)
      }
    // Repeats previous steps if the day is not the same
    } while (temp != Day);                                                       
  } 

/*//////////////////////////////////////////////////////////////////////////////
//                METHOD EXTRACTDATE (returns the formated date)              //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Receives a date in long format (number of milliseconds since 1/1/1970, convert
 * int into string format (Fri May 28 11:42:36 CEST 2004) and then extracts the
 * YY, MM, DD and Week Day (i.e. 04 05 28 03)
 * @param long date - Receive a long value with the date and time in milliseconds 
 * since 1/1/1970
 */

  private void ExtractDate(long date)                                       
  {
    // Pointer to start a string search
    int pos_start = 0;                                                          
    // Pointer to end a string search
    int pos_end = 0;                                                            
    // Character to search inside the string ( )
    byte src_char = 0x00;                                                       
    // Converts the millisecond based date into Date format
    Date curr_date = new Date(date);                                            
    // String to store the date in String format
    String date_str = new String();                                             
    // String array to store WeekDay,Month, day and Year
    String[] split_date = new String[4];                                        

    // Converts the Date Format into String
    date_str = curr_date.toString();                                            

    // Character to search in the string - SPACE = 0x20
    src_char = 0x20;                                                            
    // Pointer that gives the position where to start
    pos_start = 0;                                                              
    // Pointer that gives the position where to finish
    pos_end = date_str.indexOf(src_char,0);                                     
    // Position 0 - Day of the week
    split_date[0] = date_str.substring(pos_start,pos_end);                      

    // Next start is 1 position after last end
    pos_start = pos_end + 1;                                                    
    // Next end is the next SPACE character
    pos_end = date_str.indexOf(src_char,pos_start);                             
    // Position 1 - Month
    split_date[1] = date_str.substring(pos_start,pos_end);                      

    // Next start is 1 position after last end
    pos_start = pos_end + 1;                                                    
    // Next end is the next SPACE character
    pos_end = date_str.indexOf(src_char,pos_start);                             
    // Position 2 - Day number
    split_date[2] = date_str.substring(pos_start,pos_end);                      

    // Next start is the position of last SPACE + 1 (+2 is used to erase the 2 first digits)
    pos_start = date_str.lastIndexOf(src_char) + 1 + 2;                         
    // Position 3 (last string after last SPACE) - Year
    split_date[3] = date_str.substring(pos_start);                              

    // Test to define the Week Day                                              
    // NOTE: The CASE statement doesn't work here 
    // If the extracted day is Sunday, zero is returned
    if (split_date[0].compareTo("Sun") == 0)                                    
      // For Sunday, the value 1 is assigned
      Week_Day = Tools.StringToPackedBCD("1");                                  
      // If the extracted day is Monday, zero is returned
    else if (split_date[0].compareTo("Mon") == 0)                               
      // For Monday, the value 2 is assigned
      Week_Day = Tools.StringToPackedBCD("2");                                  
      // If the extracted day is Tueday, zero is returned
    else if (split_date[0].compareTo("Tue") == 0)                               
      // For Tueday, the value 3 is assigned
      Week_Day = Tools.StringToPackedBCD("3");                                  
      // If the extracted day is Wednesday, zero is returned
    else if (split_date[0].compareTo("Wed") == 0)                               
      // For Wednesday, the value 4 is assigned
      Week_Day = Tools.StringToPackedBCD("4");                                  
      // If the extracted day is Thursday, zero is returned
    else if (split_date[0].compareTo("Thu") == 0)                               
      // For Thursday, the value 5 is assigned
      Week_Day = Tools.StringToPackedBCD("5");                                  
      // If the extracted day is Friday, zero is returned
    else if (split_date[0].compareTo("Fri") == 0)                               
      // For Friday, the value 6 is assigned
      Week_Day = Tools.StringToPackedBCD("6");                                  
      // If the extracted day is Saturday, zero is returned
    else if (split_date[0].compareTo("Sat") == 0)                               
      // For Saturday, the value 7 is assigned
      Week_Day = Tools.StringToPackedBCD("7");                                  

    // Test to define the Month                                                 
    // NOTE: The CASE statement doesn't work here 
    // If the extracted month is January, zero is returned
    if (split_date[1].compareTo("Jan") == 0)                                    
      // For January, 1 is assigned
      Month = Tools.StringToPackedBCD("1");                                     
      // If the extracted month is February, zero is returned
    else if (split_date[1].compareTo("Feb") == 0)                               
      // For February, 2 is assigned
      Month = Tools.StringToPackedBCD("2");                                     
      // If the extracted month is March, zero is returned
    else if (split_date[1].compareTo("Mar") == 0)                               
      // For March, 3 is assigned
      Month = Tools.StringToPackedBCD("3");                                     
      // If the extracted month is April, zero is returned
    else if (split_date[1].compareTo("Apr") == 0)                               
      // For April, 4 is assigned
      Month = Tools.StringToPackedBCD("4");                                     
      // If the extracted month is May, zero is returned
    else if (split_date[1].compareTo("May") == 0)                               
      // For May, 5 is assigned
      Month = Tools.StringToPackedBCD("5");                                     
      // If the extracted month is June, zero is returned
    else if (split_date[1].compareTo("Jun") == 0)                               
      // For June, 6 is assigned
      Month = Tools.StringToPackedBCD("6");                                     
      // If the extracted month is July, zero is returned
    else if (split_date[1].compareTo("Jul") == 0)                               
      // For July, 7 is assigned
      Month = Tools.StringToPackedBCD("7");                                     
      // If the extracted month is August, zero is returned
    else if (split_date[1].compareTo("Aug") == 0)                               
      // For August, 8 is assigned
      Month = Tools.StringToPackedBCD("8");                                     
      // If the extracted month is September, zero is returned
    else if (split_date[1].compareTo("Sep") == 0)                               
      // For September, 9 is assigned
      Month = Tools.StringToPackedBCD("9");                                     
      // If the extracted month is October, zero is returned
    else if (split_date[1].compareTo("Oct") == 0)                               
      // For October, 10 is assigned
      Month = Tools.StringToPackedBCD("10");                                    
      // If the extracted month is November, zero is returned
    else if (split_date[1].compareTo("Nov") == 0)                               
      // For November, 11 is assigned
      Month = Tools.StringToPackedBCD("11");                                    
      // If the extracted month is December, zero is returned
    else if (split_date[1].compareTo("Dec") == 0)                               
      // For December, 12 is assigned
      Month = Tools.StringToPackedBCD("12");                                    

    // Test to define the Day
    // The day is converted from String to Packed BCD
    Day = Tools.StringToPackedBCD(split_date[2]);                               
  
    // Test to define the Year
    // The year is converted from String to Packed BCD
    Year = Tools.StringToPackedBCD(split_date[3]);                              

    // Assign the captured values to the JEC Frame
    // First JEC byte is the Year
    frame[0+offset] = Year;                                                     
    // Second JEC byte is the Month
    frame[1+offset] = Month;                                                    
    // Third JEC byte is the Day
    frame[2+offset] = Day;                                                      
    // The 4 LSB of the Eigth JEC byte are the Week Day
    frame[7+offset] = (byte)(frame[7+offset] | Week_Day);                       
  }

/*//////////////////////////////////////////////////////////////////////////////
//                METHOD EXTRACTTIME (returns the formated time)              //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * Receives a date in long format (number of milliseconds since 1/1/1970, convert
 * int into string format (Fri May 28 11:42:36 CEST 2004) and then extracts HH, 
 * MM, SS and Milliseconds (i.e. 14 25 46 193)
 * @param long date - Receive a long value with the date and time in milliseconds 
 * since 1/1/1970
 */
  
  private void ExtractTime(long date)
  {
    // Pointer to start a string search
    int pos_start = 0;                                                          
    // Pointer to end a string search
    int pos_end = 0;                                                            
    // Variable for the loop cycle
    int i = 0;                                                                  
    // Character to search inside the string SPACE
    byte src_char = 0x20;                                                       
    // Converts the millisecond based date into Date format
    Date curr_date = new Date(date);                                            
    // String to store the date in String format
    String time_str = new String();                                             
    // String array to store Hour, Minute, Second and Msec
    String[] split_time = new String[5];                                        

    // Converts the Date Format into String
    time_str = curr_date.toString();                                            

    // Search the 3 first spaces before the time in string (check time format)    
    for(i = 0 ; i < 3 ; i++)                                                    
    {
      // Pointer that gives the position where to finish      
      pos_end = time_str.indexOf(src_char,pos_start);                           
      // Increase the start position to search forward
      pos_start = pos_end + 1;                                                  
    }

    // Pointer that gives the position where to finish      
    pos_end = time_str.indexOf(src_char,pos_start);                             
    // Time_str keeps only the time in string (i.e.10:22:31)
    time_str = time_str.substring(pos_start,pos_end);                           

    // Character to search in the string - ':' = 0x3A
    src_char = 0x3A;                                                            
    // Pointer that gives the position where to start
    pos_start = 0;                                                              
    // Search the position of the first ':'
    pos_end = time_str.indexOf(src_char,pos_start);                             
    // Stores first value (hour) in split_time[0]
    split_time[0] = time_str.substring(pos_start,pos_end);                      
    // Assigns the new start as being the last end plus 1
    pos_start = pos_end + 1;                                                    
    // Search the position of the next ':'
    pos_end = time_str.indexOf(src_char,pos_start);                             
    // Stores second value (minute) in split_time[1]
    split_time[1] = time_str.substring(pos_start,pos_end);                      
    // Stores third value (second) in split_time[2]
    split_time[2] = time_str.substring(pos_end + 1);                            
    // Stores the Higher part of millisecs in split_time[3]
    split_time[3] = Long.toString((date % 1000) / 10);                          
    // Stores the Lower part of millisecs in split_time[4]
    split_time[4] = Long.toString((date % 1000) % 10);                          

    // The hour is converted from String to Byte 
    Hour = Tools.StringToPackedBCD(split_time[0]);                              
    // The minutes are converted from String to Byte 
    Minute = Tools.StringToPackedBCD(split_time[1]);                            
    // The seconds are converted from String to Byte 
    Second = Tools.StringToPackedBCD(split_time[2]);                            
    // The highest part of Msecs is converted to Byte 
    Msec_Hi = Tools.StringToPackedBCD(split_time[3]);                           
    // The lowest part of Msecs is converted to Byte
    Msec_Lo = Tools.StringToPackedBCD(split_time[4]);                           

    // Write the hour into JEC Frame
    frame[3+offset] = Hour;                                                     
    // Write the minute into JEC Frame 
    frame[4+offset] = Minute;                                                   
    // Write the second into JEC Frame
    frame[5+offset] = Second;                                                   
    // Write the highest part of Msec into JEC Frame
    frame[6+offset] = Msec_Hi;                                                  

    // This instruction puts the 4MSB as being the lower part of Msec
    // and puts the 4LSB as being the week day
    // Write the lower part of Msec and week day into JEC Frame
    frame[7+offset] = (byte)((Msec_Lo << 4) | Week_Day);                        
  }   

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD ADD JEC DATA                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to write data inside a JEC Frame (in the Data area)
 * This method manages the available free space in the data area and, if its
 * too big, throws an exception (JECIndexOutOfRangeException)
 * @param byte[] Data - Array of data to write inside the JEC Frame (bytes)
 * @param int startPos - Used to give the start position inside the array
 * @param int offsetPos - Used to give the offset (data length)
 */
  public void AddJECData(byte[] Data, int startPos, int offsetPos) throws JECIndexOutOfRangeException
  {
    // Tests the size needed in bytes to store the data inside the JEC Frame
    // Maximum number of bytes allowed is 224
    if((offsetPos + DataBufferOffset ) > 224) 
    {
      throw new JECIndexOutOfRangeException("Data doesn't fit in JEC frame");
    }
    else
    {
      // Variable for the FOR loop cycle
      for(int i = 0; i < offsetPos; i++)
      {
        frame[16 + offset + DataBufferOffset + i] = Data[startPos + i];
      }

      // Updates pointer. Prepare the next free position to write data
      DataBufferOffset += offsetPos;
    }//else
  }

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD ADD JEC DATA                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to write data (words) inside a JEC Frame (in the Data area)
 * This method doesn't manages the available free space in the data area.
 * NOTE: This method overwrites the selected word in data area
 * @param short Data - Two bytes of data to write inside the JEC Frame
 * @param int pos - Used to give the position inside the array (in word)
 */
  public void AddJECData(short Data, int pos) throws JECIndexOutOfRangeException
  {
    frame[16 + offset + (2 * pos) + 1] = (byte)(Data & 0xFF);
    frame[16 + offset + (2 * pos)] = (byte)((Data >> 8) & 0xFF);
  }

/*//////////////////////////////////////////////////////////////////////////////
//                            METHOD ADD JEC DATA                             //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to write data (ints) inside a JEC Frame (in the Data area)
 * This method doesn't manages the available free space in the data area.
 * NOTE: This method overwrites the selected word in data area
 * @param int Data - Two bytes of data to write inside the JEC Frame
 * @param int pos - Used to give the position inside the array (in word)
 */
  public void AddJECData(int Data, int pos) throws JECIndexOutOfRangeException
  {
    frame[16 + offset + (4 * pos) + 3] = (byte)(Data & 0xFF);
    frame[16 + offset + (4 * pos) + 2] = (byte)((Data >> 8) & 0xFF);  
    frame[16 + offset + (4 * pos) + 1] = (byte)((Data >> 16) & 0xFF);
    frame[16 + offset + (4 * pos)] = (byte)((Data >> 24) & 0xFF);
  }  

/*//////////////////////////////////////////////////////////////////////////////
//                       METHOD RESET DATA BUFFER OFFSET                      //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method resets the pointer to indicate the next position where the data 
 * can be written inside a JEC Frame Data area.
 */
 public void ResetDataBufferOffset()
 {
   // Reset the pointer for the data buffer
   DataBufferOffset = 0;
 }

/*//////////////////////////////////////////////////////////////////////////////
//                       METHOD TO HANDLER ALIVE PERIOD                       //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method sets the Alive period to be sent from the PLC to the Handler (driver).
 * This value is sent in 4 bytes (32bits) and is represented in milliseconds.
 * Maximum value is 9990000 ms = 199 minutes = 3.316 hours (0 means no alive)
 */
 public void ToHandlerAlivePeriod(int period)
 {
    frame[19 + offset] = (byte)(period & 0xFF);
    frame[18 + offset] = (byte)((period >> 8)& 0xFF);
    frame[17 + offset] = (byte)((period >> 16)& 0xFF);
    frame[16 + offset] = (byte)((period >> 24)& 0xFF);    
 }

/*//////////////////////////////////////////////////////////////////////////////
//                       METHOD GET HANDLER ALIVE PERIOD                      //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method gets the Handler Alive period from the driver (JEC FRAME).
 * This value is sent as an INT and is represented in milliseconds.
 * Maximum value is 9990000 ms = 199 minutes = 3.316 hours (0 means no alive)
 */
 public long getHandlerAlivePeriod()
 {
    short msWord = 0;
    short lsWord = 0;
    long timeVal = 0;

    msWord = GetJECWord(1);
    lsWord = GetJECWord(2);

    timeVal = (msWord << 16) & 0xFFFF0000;
    timeVal = timeVal | (lsWord & 0xFFFF);

    return timeVal;
 } 

/*//////////////////////////////////////////////////////////////////////////////
//                     METHOD GET SUPERVISION ALIVE PERIOD                    //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method gets the Supervision Alive period from the driver (JEC FRAME).
 * This value is sent as an INT and is represented in milliseconds.
 * Maximum value is 9990000 ms = 199 minutes = 3.316 hours (0 means no alive)
 */
 public long getSupervisionAlivePeriod()
 {
    short msWord = 0;
    short lsWord = 0;
    long timeVal = 0;

    msWord = GetJECWord(3);
    lsWord = GetJECWord(4);

    timeVal = (msWord << 16) & 0xFFFF0000;
    timeVal = timeVal | (lsWord & 0xFFFF);

    return timeVal;
 }  

/*//////////////////////////////////////////////////////////////////////////////
//                     METHOD TO SUPERVISION ALIVE PERIOD                     //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method sets the Alive period to be sent from the PLC to the Supervision (TIM).
 * This value is sent in 4 bytes (32bits) and is represented in milliseconds.
 * Maximum value is 9990000 ms = 199 minutes = 3.316 hours (0 means no alive)
 */
 public void ToSupervisionAlivePeriod(long period)
 {
    frame[23 + offset] = (byte)(period & 0xFF);
    frame[22 + offset] = (byte)((period >> 8) & 0xFF);
    frame[21 + offset] = (byte)((period >> 16) & 0xFF);
    frame[20 + offset] = (byte)((period >> 24)& 0xFF);    
 } 

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD GET JEC DATA                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to extract the data part from the JEC frame.
 * @param int size - Higgest offset value (in WORDS) within this block (0 <= size < 112)
 * @return byte[] - Returns a byte array with the data retrieved
 */
 public byte[] GetJECData(int size)
 {
    byte[] retdata = null;
    // size must be bigger than 0
    if((size >= 0) || (size < 112))
    {
      // Converts the Data Offset into a Length
      int length = (2 * size) + 2;
      // Loop variable
      int j = 0;
      // Creates a byte array with, at least, size 1 (minimum value for JECoffset)
      // The size is multiplied by 2 to have bytes
//      if (length == 0)
//      {
//        retdata = new byte[length + 2];
//      }
//      else
//      {
        retdata = new byte[length];
//      }
     
      // Reads the N bytes from the JEC Frame
      for(j = 0; j < length; j++)
      {
        // The data starts on the 16th position inside the JEC frame
        retdata[j] = frame[16 + offset + j];
      }
      return retdata;
    }
    else 
    {
      if (logger.isDebugEnabled()) {
        logger.debug("GET JEC DATA returned null");
      }
      return null;
    }
 } 

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD GET JEC BYTE                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to extract the data part from the JEC frame.
 * @param int JECoffset - Offset value (in bytes) to set the end position of data to be read (minimum is 1)
 * @return byte - Returns a byte with the data extracted
 */
 public byte GetJECByte(int JECoffset)
 {
    // JECoffset must be bigger or equal than 0
    if((JECoffset >= 0) && (JECoffset <= 223))
    {
      // Short variable to store the extraction result
      byte retdata = 0x00;

      // Extract the JECoffset position - byte
      retdata = frame[16 + offset + JECoffset];
      return retdata;
    }
    else return StdConstants.ERROR;
 }  

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD GET JEC STRING                          //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to extract a string from the data part of JEC frame.
 * @param int startAddress - Position where the string starts
 * @return String - Returns a the extracted string ("ERROR" in case of problems)
 */
 public String GetJECString(int startAddress)
 {
    // Gets the jec frame as being an array of chars.
    // Starts in byte 16 (where data part starts) and defines maximum size as being 224
    String temp = new String(frame,16,224);
    // Gets the position of where is the EOT character (0x03)
    int endOfString = temp.indexOf(StdConstants.END_OF_TEXT);
    // If character was not found, returns "ERROR"
    if(endOfString == StdConstants.ERROR) 
      return "ERROR";
    else
      return temp.substring(0,endOfString);
 }   

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD GET JEC WORD                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to extract the data part from the JEC frame.
 * @param int JECoffset - Word number to be extracted  (minimum is 1)
 * @return short - Returns a word (short) with the data extracted
 */
 public short GetJECWord(int JECoffset)
 {
    // JECoffset must be bigger than 0
    if((JECoffset >= 1) && (JECoffset <= 111))
    {
      // Short variable to store the extraction result
      short retdata = 0x00;

      // Extract the JECoffset position - MSB Byte (pos 16)
      retdata = (short)((frame[16 + offset + (2 * (JECoffset - 1))] << 8) & 0xFF00);
      // Extract the JECoffset position - MSB Byte (pos 17)
      retdata = (short)(retdata | (frame[16 + offset + ((2 * JECoffset) - 1)]) & 0xFF);
      return retdata;
    }
    else return StdConstants.ERROR;
 }   

/*//////////////////////////////////////////////////////////////////////////////
//                             METHOD SET JEC WORD                            //
//////////////////////////////////////////////////////////////////////////////*/

/**
 * This method is used to set the data part in the JEC frame.
 * @param int JECoffset - Word number to be setted  (minimum is 1)
 * @param short value - Word to be written in JEC frame
 */
 public void SetJECWord(int JECoffset, short value)
 {
    // JECoffset must be bigger than 0
    if((JECoffset >= 1) && (JECoffset <= 111))
    {
      // Less significant byte
      frame[16 + offset + ((2 * JECoffset) - 1)] = (byte)(value & 0xFF);
      // Most significant byte
      frame[16 + offset + (2 * (JECoffset - 1))] = (byte)(((value & 0xFF00) >> 8) & 0xFF);
    }
    else {
      if (logger.isDebugEnabled()) {
        logger.debug("ERROR: while trying to set data to JEC - step IGNORED");
      }
    }
 }    

/*//////////////////////////////////////////////////////////////////////////////
//                      METHOD GET JEC TIME MILLISECONDS                      //
//////////////////////////////////////////////////////////////////////////////*/ 

/**
 * This method is used to retrieve the JEC Frame timestamp in millisecond format.
 * JECPlc send the JEC frame with data in packed BCD and this data comes in the 
 * right format. As Java assumes these values as being in hexadecimal, we have to
 * convert them again in decimal format.
 * It was developed to stick the real timestamp for the Source Data Tags.
 * @return long - JEC timestamp in milliseconds
 */
 public long GetJECCurrTimeMilliseconds()
 {
   // Creates a Calendar instance with JEC Frame timestamp values
   Calendar cal = Calendar.getInstance();
   // Clears all Calendar parameters (reset)
   cal.clear();

   // Set the new Calendar with the JEC frame parameters
   cal.set(Tools.PackedBCDToInt(frame[0 + offset]) + 2000,   // Year (val + 2000)
           Tools.PackedBCDToInt(frame[1 + offset]) - 1,      // Month (0-11)
           Tools.PackedBCDToInt(frame[2 + offset]),          // Day
           Tools.PackedBCDToInt(frame[3 + offset]),          // Hour
           Tools.PackedBCDToInt(frame[4 + offset]),          // Minutes
           Tools.PackedBCDToInt(frame[5 + offset]));         // Seconds     

   // Extracts the millisecond value from JEC Frame and casts it to SHORT
   // E.g. two bytes received with the msec value: b0=0x17; b1=0x36
   // This means that the msecs value is in fact 173 because '6' is the day number (friday)
   // So, we get b0, multiply by 10 and add the most significant part of the second byte (17*10 + 3 = 173)
   short millisecs = 0;

   int first = Tools.PackedBCDToInt(frame[6 + offset]) * 10;
   int second = Tools.PackedBCDToInt(frame[7 + offset]) / 10;

   millisecs = (short)(first + second);

   long timeStamp = cal.getTimeInMillis() + millisecs;
   long tst = cal.getTimeInMillis();
   if (logger.isDebugEnabled()) {
     logger.debug("TIMESTAMP EXTRACTED FROM JEC FRAME: "+timeStamp);
   }

   // Return the date (Calendar) plus the computed milliseconds
   return timeStamp;
 }

/*//////////////////////////////////////////////////////////////////////////////
//                    METHOD SET JEC SYNCHRONIZATION TYPE                       //
//////////////////////////////////////////////////////////////////////////////*/ 

/**
 * This method is used to send the type of synchronization to be used (NTP or JEC)
 * This information is sent during initialization (in the data are) just after the
 * 'Supervision Alive Period'.
 * @param byte syncType - JEC synchronization type to be used
 */
 public void SetSyncType(byte syncType)
 {
   // Writes the received synchronization type in the position 24 (after Supervision Alive)
   frame[24 + offset] = syncType;
 }

/*//////////////////////////////////////////////////////////////////////////////
//                    METHOD SET JEC SYNCHRONIZATION TYPE                       //
//////////////////////////////////////////////////////////////////////////////*/ 

/**
 * This method is used to get the JEC frame data into hexadecimal format (for debugging).
 * @return String - JEC data in HEX format
 */
  public String GetFrameData()
  {
    StringBuffer debug_msg = new StringBuffer(125);
    for (int j = 0;j != 25 ; j++)
    {
      debug_msg.append(" 0x");
      debug_msg.append(Integer.toHexString((int)this.frame[j] & 0xff));
    }
    return debug_msg.toString();
  }

}
/*//////////////////////////////////////////////////////////////////////////////
//                               END OF CLASS                                 //
//////////////////////////////////////////////////////////////////////////////*/
