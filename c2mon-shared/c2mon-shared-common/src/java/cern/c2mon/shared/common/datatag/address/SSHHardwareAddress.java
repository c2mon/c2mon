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
