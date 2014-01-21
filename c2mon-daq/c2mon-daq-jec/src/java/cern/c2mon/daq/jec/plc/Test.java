package cern.c2mon.daq.jec.plc;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import java.util.Random;
import java.util.TimeZone;
import java.util.Date;
import java.util.ArrayList;
import java.io.IOException;

public class Test 
{
  static Logger logger = Logger.getLogger(Test.class);                          // Instanciate, if not created, Log4J logger
  
  public Test()
  {
  }

  public static void ConnectTo(PLCDriver plc, ConnectionData dt)
  {
    plc.Connect(dt);
//    plc.Disconnect(dt);
  }

  public static void main(String[] args)
  {
  /**
   * Just a Test class to invoke created classes and methods.
   */

      PropertyConfigurator.configure("c:\\log4j.conf");                         // Receives as parameter the log filename to dump info  

int connectionAttempts = 0;
// Calculates the range (human readable format)
    float range = 10;
    // Calculates raw deadband value
    short rawDB = (short)Math.round((32767 - (-32768)) * 0.1 / range);
    // Return calculated raw deadband value
    System.out.println(rawDB);
    System.out.println("ABSOLUTE DEADBAND VALUE: 0x"+Integer.toHexString((int)(rawDB & 0x7FFF)));


  connectionAttempts = 3;

    System.out.println("Connection attempts: "+(connectionAttempts + 1) % Integer.MAX_VALUE);

      // Creates an instance of a connection data for a Siemens PLC
//      ConnectionData data = new ConnectionData("137.138.249.16",102,"TCP-1","TCP-1",true);
//      SiemensISO s1 = new SiemensISO();                                         // Creates an Siemens instance using ISO-on-TCP

//      SiemensTCP s1 = new SiemensTCP();                                         // Creates an Siemens instance using TCP

//      JECPFrames jps = new JECPFrames(StdConstants.INIT_MSG,0);                 // Creates a JEC frame to send commands
//      jps.SetSequenceNumber();                                                              // Assign a sequence number to this frame
//      ConnectTo(s1,data);                                                       // Connect to this PLC using the type of PLC and data

//      jps.JECSynchronize();                                                     // Fills frames with synchronization parameters
//      s1.Send(jps);                                                             // Send the formated frame to the PLC

/*      JECPFrames jpr = new JECPFrames(StdConstants.HANDLER_ALIVE_MSG,0);                  // Creates a JEC frame to receive commands
    Random r = new Random(System.currentTimeMillis());
    while(true)
    {
      s1.Receive(jpr);                                                  // Receive data from PLC and put it in a JEC frame
      try 
      {
        Thread.sleep((int)(r.nextFloat()*1000));
      }
      catch (InterruptedException ex) 
      {  
      }
      
    }
//      jpr.GetDateToString();                                                    // Read the date received and convert it to sting

//      jps.CheckIDTimeout();                                                     // Check if there are some timed out ID's in table
//      jps.FreeSequenceNumber(jpr.GetSequenceNumber());                                                  // Frees the received ID to be used again later
*/
  }
}