/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

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


   @Element(name = "user-name", required = false)
   protected String userName;

   @Element(name = "user-password", required = false)
   protected String userPassword;

   @Element(name = "system-call")
   protected String systemCall;

   @Element(name = "call-interval")
   protected long callInterval=-1;

   @Element(name = "call-delay")
   protected long callDelay=-1;

   @Element(name = "server-alias", required = false)
   protected String serverAlias;

   @Element
   protected String protocol;

   @Element(name = "ssh-key", required = false)
   protected String sshKey;

   @Element(name = "key-passphrase", required = false)
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
   @Override
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
   @Override
  public final String getUserPassword()
   {
     return this.userPassword;
   }


   @Override
  public final String getSshKey()
   {
     return this.sshKey;
   }


   @Override
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
   @Override
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
   @Override
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
   @Override
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
   @Override
  public final long getCallDelay()
   {
     return this.callDelay;
   }

   @Override
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
