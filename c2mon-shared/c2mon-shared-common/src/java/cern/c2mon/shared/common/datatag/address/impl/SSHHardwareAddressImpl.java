//Source file: E:\\development\\CERN\\tim-shared\\src\\java\\ch\\cern\\tim\\shared\\datatag\\address\\impl\\SSHHardwareAddressImpl.java

package cern.c2mon.shared.common.datatag.address.impl;

import org.simpleframework.xml.Element;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.SSHHardwareAddress;


/**
 * Implementation of the ProcessControlHardwareAddress interface.
 * Objects of this class represent hardware addresses for CommandTags 
 * that are linked to process-control DAQ.
 * @author Wojtek Buczak
 */
public class SSHHardwareAddressImpl extends HardwareAddressImpl implements SSHHardwareAddress 
{
  private static final long serialVersionUID =  -6785327286660373802L;
  public static final String XML_PROTOCOL = "xml";
  public static final String SIMPLE_IO_PROTOCOL = "simple-io";
  
  
   @Element(required = false)
   protected String userName;
   
   @Element(required = false)
   protected String userPassword;
   
   @Element
   protected String systemCall;
   
   @Element
   protected long callInterval=-1;
   
   @Element
   protected long callDelay=-1;
   
   @Element(required = false)
   protected String serverAlias;
   
   @Element
   protected String protocol;
   
   @Element(required = false)
   protected String sshKey;
   
   @Element(required = false)
   protected String keyPassphrase;
  
   /**
   * Internal constructor required by the fromConfigXML method of the super class.
   */
   protected SSHHardwareAddressImpl() {
    /* Nothing to do */
   }
  
  
   /**
   * This constructor takes only the system-call as an argument. The system call and the protocol are
   * the only mandatory fields of all ssh hw. addresses
   * @throws cern.c2mon.shared.common.ConfigurationException
   * @param systemCall
   */
   public SSHHardwareAddressImpl(final String pSystemCall,final String pProtocol) throws ConfigurationException {
     this(null,null,null,pSystemCall,-1,-1,pProtocol,null,null);    
   }
  
  

   /**
    * @param userName
    * @param userPasswd
    * @param systemCall
    * @param machineAlias
    * @param callInterval
    * @roseuid 432EC11C0107
    */
   public SSHHardwareAddressImpl(final String pServerAlias,                          
                                 final String pUserName, 
                                 final String pUserPasswd, 
                                 final String pSystemCall,                                 
                                 final long pCallInterval,
                                 final long pCallDelay,
                                 final String pProtocol,
                                 final String pSshKey,
                                 final String pKeyPassphrase) throws ConfigurationException {
                                 
      this.setServerAlias(pServerAlias);                              
      this.setUserName(pUserName);
      this.setUserPassword(pUserPasswd);
      this.setSystemCall(pSystemCall);
      this.setCallInterval(pCallInterval);
      this.setCallDelay(pCallDelay);  
      this.setProtocol(pProtocol);
      this.setSshKey(pSshKey);
      this.setKeyPassphrase(pKeyPassphrase);
   }



   protected final void setUserName(String pName) throws ConfigurationException {
     this.userName = pName;
   }
   
   /**
    * @return java.lang.String
    * @roseuid 432EB96701AA
    */
   public final String getUserName() 
   {
     return this.userName;
   }


   protected final void setUserPassword(String pPassword) throws ConfigurationException {
     this.userPassword = pPassword;
   }
   
   /**
    * @return java.lang.String
    * @roseuid 432EB96701FA
    */
   public final String getUserPassword() 
   {
     return this.userPassword;
   }

  
   public final String getSshKey() 
   {
     return this.sshKey;
   }
   
   
   public String getKeyPassphrase() 
   {
     return this.keyPassphrase;
   }


   protected final void setServerAlias(String pSrvAlias) throws ConfigurationException {
     this.serverAlias = pSrvAlias;
   }

   
   protected final void setProtocol(String pProtocol) throws ConfigurationException 
   {
     if (!pProtocol.equals(XML_PROTOCOL) && !pProtocol.equals(SIMPLE_IO_PROTOCOL))
       throw new ConfigurationException(
           ConfigurationException.INVALID_PARAMETER_VALUE,
           "Protocol \""+pProtocol+"\" is not supported."
        );
     this.protocol = pProtocol;
   }
   
   
   protected final void setSshKey(String pSshKey) throws ConfigurationException {
     this.sshKey = pSshKey;
   }
   
   protected final void setKeyPassphrase(String pKeyPassphrase) throws ConfigurationException {
     this.keyPassphrase = pKeyPassphrase;
   }
  
   
   /**
    * @return java.lang.String
    * @roseuid 432EB9670268
    */
   public final String getServerAlias() 
   {
     return this.serverAlias;
   }

   
   public final String getProtocol() 
   {
     return this.protocol;
   }


   protected final void setSystemCall(String pSystemCall) throws ConfigurationException {
      if (pSystemCall == null) {
        throw new ConfigurationException(
           ConfigurationException.INVALID_PARAMETER_VALUE,
           "Parameter \"tag type\"must not be null."
        );
      }
      this.systemCall = pSystemCall;
   }

   
   /**
    * @return java.lang.String
    * @roseuid 432EB967027D
    */
   public final String getSystemCall() 
   {
     return this.systemCall;
   }


   protected final void setCallInterval(long interval) throws ConfigurationException {
       this.callInterval = interval;
   }

   
   /**
    * @return long
    * @roseuid 432ECF84020E
    */
   public final long getCallInterval() 
   {
     return this.callInterval;
   }
 
 
   protected final void setCallDelay(long delay) throws ConfigurationException {
       this.callDelay = delay;
   }
 
   
   /**
    * @return long
    * @roseuid 432ECF84020E
    */
   public final long getCallDelay() 
   {
     return this.callDelay;
   }

   public final boolean  isXMLProtocolConfigured() 
   {
     boolean result = false;
     if (protocol == null) result = false;
     else  
       if (protocol.equals(XML_PROTOCOL)) 
         result = true;
       else return false;
    
     return result;
   }
}
