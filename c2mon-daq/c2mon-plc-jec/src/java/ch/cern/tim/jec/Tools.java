/*//////////////////////////////////////////////////////////////////////////////
//                         TIM PLC INTERFACING SYSTEM                         //
//                                                                            //
//  Class to convert byte arrays into Strings and vice-versa.                 //
//  Here, the data to send is converted from String to Byte Array and the     //
//  data to receive is converted from Byte Array to String.                   //
//                                                                            //
// -------------------------------------------------------------------------- //
// Changes made by            Date                Changes Made                //
// -------------------------------------------------------------------------- //
//  Joao Simoes           01/07/2004          First Implementation            //
//  Joao Simoes           25/07/2004          Comment ameiloration            //
//  Joao Simoes           25/07/2004          StringToPackedBCD - result var  //
//////////////////////////////////////////////////////////////////////////////*/

package ch.cern.tim.jec;

import org.apache.log4j.Logger;
import java.io.*;
import java.lang.*;
import java.util.Date;

/**
This class is used to convert data to be sent from String to byte array and
data to be received from byte array to String.
 */
public class Tools
{
/**
Log4j logger instance - One instance is created only if there is no other one created
 */
  // Instanciate, if not created, Log4J logger
  static Logger logger = Logger.getLogger(Tools.class);                         

/**
Maximum value that ca be achieved by a byte value. We need this change because Java
bytes support, by default, values from -127 to 128
 */  
  // Maximum numeric value for bytes: (1+127)-(-128)=256
  private static final int BYTE_RANGE = (1 + Byte.MAX_VALUE) - Byte.MIN_VALUE;  

/**
Byte temporary buffer used for data conversion
 */  
  // Array to store received bytes to be converted
  private static byte[] allBytes = new byte[BYTE_RANGE];                        

/**
Char temporary buffer used for data conversion
 */    
  // Array to store the chars resulting from the conversion
  private static char[] byteToChars = new char[BYTE_RANGE];                     

/**
Static function to generate the ASCII table
 */
static 
{
  for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) 
  {
    // Fill array with vales from 0 to 256 in byte format
    allBytes[i - Byte.MIN_VALUE] = (byte) i;                                    
  }
  // Creates a string with the values of 'allBytes' from 0 to 256
  String allBytesString = new String(allBytes, 0,Byte.MAX_VALUE - Byte.MIN_VALUE);  
  // Gets the string length
  int allBytesStringLen = allBytesString.length();                              
  // Converts all string byte values into chars
  for (int i = 0;(i < (Byte.MAX_VALUE - Byte.MIN_VALUE))&& (i < allBytesStringLen); i++) 
  {
    byteToChars[i] = allBytesString.charAt(i);                                  
  }
}

/*//////////////////////////////////////////////////////////////////////////////
//              METHOD TOOLS - CONSTRUCTOR (without parameters)               //
//////////////////////////////////////////////////////////////////////////////*/

/**
Default Class Constructor with no parameters
 */
   public Tools()
   {
   }

/*//////////////////////////////////////////////////////////////////////////////
//   METHOD String TOASCIISTRING(byte[] buffer, int startPos, int length)     //
//////////////////////////////////////////////////////////////////////////////*/

/**
This method receives a byte[] array and convert it into a String
@param byte[] buffer - Input buffer to be converted into a string
@param int startPos - Position in the array where the data is located
@param int length - Offset of the data inside the array
@return String - Returns the byte array converted into a string
 */
   public static String toAsciiString(byte[] buffer, int startPos, int length)
   {
      // Creates an array of chars with the size of received array
      char[] charArray = new char[length];                                      
      // The start position for reading data in the array
      int readpoint = startPos;                                                 

      for (int i = 0; i < length; i++)                            
      {
        // Convert all the received bytes into chars
        charArray[i] = byteToChars[(int) buffer[readpoint] - Byte.MIN_VALUE];   
        // Changes the position
        readpoint++;                                                            
      }
      // Convert the array of chars into a string - result
      return new String(charArray);                                             
   }

/*//////////////////////////////////////////////////////////////////////////////
//               METHOD byte StringToPackedBCD(String input)                  //
//////////////////////////////////////////////////////////////////////////////*/

/**
This method receives a String value and converts it into a Packed BCD byte
@param String input - Input string to be converted into Packed BCD format
@return byte - Byte with the received value converted into Packed BCD format
 */
   public static byte StringToPackedBCD(String input)
   {
      // Byte to store the conversion result
      byte result = 0x00;                                                       
      // Array of char to store each digit of the number
      char[] array = new char[2];                                               
      // Array of int to store the value of each digit
      int[] vals = new int[2];                                                  

      // If the input string is not empty and size lower than 3
      if ((input != "") && (input.length() < 3))                                
      {
          // Split all digits from the string and put them in array
          array = input.toCharArray();                                          
          // If there's only one digit...
          if (input.length() == 1)                                              
          {
            // Most Significant value is 0
            vals[0] = 0;                                                        
            // Less Significant value is the first position in array
            vals[1] = Integer.parseInt(String.valueOf(array[0]));               
          }
          // Otherwise...
          else                                                                  
          {
            // Most Significant value is the first char in array
            vals[0] = Integer.parseInt(String.valueOf(array[0]));               
            // Less Significant value is the last char in array
            vals[1] = Integer.parseInt(String.valueOf(array[1]));               
          }

          // Shift MSV four positions to the left
          vals[0] = vals[0] << 4;                                               
          // OR logic between MSV and LSV
          result = (byte)(vals[0] | vals[1]);                                   
      }
      else
      {
        // DEBUG message that will appear in the logger
        logger.fatal("Error in conversion from String to BCD - invalid string size");   
        // Assigns 0xFF to the result 
        // NOTE: 0x00 can be a key to detect a problem
        result = 0x00;
      }
      // Resturn a byte with the result in Packet BCD format
      return result;                                                            
   }

/*//////////////////////////////////////////////////////////////////////////////
//                 METHOD String JECPtoString(byte[] frame)                   //
//////////////////////////////////////////////////////////////////////////////*/

/**
This method receives a JECP frame from the PLC and returns a string with the 
date and time in a 'human readable' format.
@param byte[] frame - JECP frame received to extract the date from
@param int offset - Position where to start searching the date and time depending
on the PLC protocol.
@return String - Date extracted from JECP and converted to 'human readable' format
 */
   public static String JECPtoString(byte[] frame, int offset)
   {
      // String variable to store the result
      String str_date = "";                                                     
      // String variable to store the Week Day value
      String Week_day = "";                                                     
      // String variable to store the Month value
      String Month = "";                                                        
      // Extract the week day number (byte)
      String hexstr = Integer.toHexString((int)frame[7+offset] & 0xf);          
      // Convert the day number from byte to integer
      Integer wd = new Integer(hexstr);                                         

      // Test the value...
      switch(wd.intValue())                                                     
      {
        // 1 means Sunday
        case 1: Week_day = "Sun "; break;                                       
        // 2 means Monday
        case 2: Week_day = "Mon "; break;                                       
        // 3 means Tuesday
        case 3: Week_day = "Tue "; break;                                       
        // 4 means Wednesday
        case 4: Week_day = "Wed "; break;                                       
        // 5 means Thursday
        case 5: Week_day = "Thu "; break;                                       
        // 6 means Friday
        case 6: Week_day = "Fri "; break;                                       
        // 7 means Saturday
        case 7: Week_day = "Sat "; break;                                       
      }

      // Extract the month number (byte)
      hexstr = Integer.toHexString((int)frame[1+offset] & 0xff);                
      // Convert the month number from byte to integer
      Integer mth = new Integer(hexstr);                                        

      // Test the value...
      switch(mth.intValue())                                                    
      {
        // 1 means January
        case 1: Month = "Jan "; break;                                          
        // 2 means February
        case 2: Month = "Feb "; break;                                          
        // 3 means March
        case 3: Month = "Mar "; break;                                          
        // 4 means April
        case 4: Month = "Apr "; break;                                          
        // 5 means May
        case 5: Month = "May "; break;                                          
        // 6 means June
        case 6: Month = "Jun "; break;                                          
        // 7 means July
        case 7: Month = "Jul "; break;                                          
        // 8 means August
        case 8: Month = "Aug "; break;                                          
        // 9 means September
        case 9: Month = "Sep "; break;                                          
        // 10 means October
        case 10: Month = "Oct "; break;                                         
        // 11 means November
        case 11: Month = "Nov "; break;                                         
        // 12 means December
        case 12: Month = "Dec "; break;                                         
      }

      // Extract and convert the day number from the frame
      String Day = Integer.toHexString((int)frame[2+offset] & 0xff);            
      // Extract and convert the hour number from the frame
      String Hour = Integer.toHexString((int)frame[3+offset] & 0xff);           
      // Extract and convert the minutes number from the frame
      String Min = Integer.toHexString((int)frame[4+offset] & 0xff);            
      // Extract and convert the seconds number from the frame
      String Sec = Integer.toHexString((int)frame[5+offset] & 0xff);            
      // Extract and convert the year number from the frame
      String Year = Integer.toString((int)(2000+(frame[0+offset] & 0xff)));     
      // Extract and convert the MsecsHi number from the frame
      String MsecsHi = Integer.toHexString((int)frame[6+offset] & 0xff);        
      // Extract and convert the MsecsLo number from the frame
      String MsecsLo = Integer.toHexString((int)((frame[7+offset] & 0xf0)>>4)); 

      // Group the individual date values to form a 'human readable string'
      // e.g. Tue Jun 16 17:28:12,874 2004
      str_date = Week_day + Month + Day +" "+ Hour +":"+ Min +":"+Sec + ","+MsecsHi+MsecsLo+" "+ Year;
      // Write the received timestamp in 'human readable form'
      if (logger.isDebugEnabled()) {
        logger.debug("Timestamp received from PLC: "+str_date);                   
      }

      // Return the formated string
      return str_date;                                                          
   }

/*//////////////////////////////////////////////////////////////////////////////
//                     METHOD int JECHexToInt(byte val)                          //
//////////////////////////////////////////////////////////////////////////////*/

/**
This method receives a JEC byte in hexadecimal format and converts it into an integer.
@param byte val - Value in hexadecimal to be converted
@return int - Conversion result
 */   
 public int JECHexToInt(byte val)
 {
   Byte byteVal = new Byte(val);
   String strVal = byteVal.toString();
   String firstPart = strVal.substring(0,1);
   String secondPart = strVal.substring(1,2);
   
   return 0;
 }

/*//////////////////////////////////////////////////////////////////////////////
//               METHOD int PackedBCDToInt(byte packedByte)             //
//////////////////////////////////////////////////////////////////////////////*/

/**
This method receives a Packed BCD Byte value and converts it into an integer
@param byte packedByte - Input byte in packed BCD to be converted into integer
@return int - Packed BCD byte converted into integer format
 */
   public static int PackedBCDToInt(byte packedByte)
   {
      // Reads the byte as being int and converts it into hex string
      String tempValue = Integer.toHexString((int)(packedByte & 0xFF));

      // Converts the received string into int
      int result = new Integer(tempValue).intValue();

      // Returns the result
      return result;                                                            
   }
 
}

/*//////////////////////////////////////////////////////////////////////////////
//                                 END OF CLASS                               //
//////////////////////////////////////////////////////////////////////////////*/
