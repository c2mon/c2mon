
package cern.c2mon.shared.common.datatag.address;



/**
 * The ProcessControlHardwareAddress interface is used by the 
 * ProcessControlMessageHandlers
 * @see cern.c2mon.daq.opc.ProcessControlMessageHandler
 * @author Wojtek Buczak
 */
public interface SSHHardwareAddress extends HardwareAddress 
{
   
   /**
    * @return java.lang.String
    * @roseuid 432EB8D60044
    */
   public String getUserName();
   
   /**
    * @return java.lang.String
    * @roseuid 432EB8D60045
    */
   public String getUserPassword();
   
   
   
   /**
    * @return java.lang.String
    * @roseuid 432EB8D6004E
    */
   public String getServerAlias();
   
   /**
    * @return java.lang.String
    * @roseuid 432EB8D60057
    */
   public String getSystemCall();

   
   public String getSshKey();
   
   
   public String getKeyPassphrase();


   public long getCallInterval();   
   
   
   public long getCallDelay();
   
   
   /**
   * Answers if the address is configuerd to work with XML-based protocol
   * or not
   * @return boolean
   */
   public boolean isXMLProtocolConfigured();
   
}
