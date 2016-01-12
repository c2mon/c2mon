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

package cern.c2mon.daq.spectrum.util;



import javax.jms.JMSException;

import org.apache.log4j.Logger;

import cern.c2mon.daq.spectrum.util.JmsProviderIntf;
import progress.message.jclient.Connection;
import progress.message.jclient.ConnectionFactory;
import progress.message.jclient.ConnectionStateChangeListener;


/**
* The vendor specific code for a SonicMQ JMS broker connection. This class provides a 
* common interface for the supported JMS providers with the specific calls, 
* namely the ConnectionFactory implementation and the listener to the connection status 
* changes.
* 
* @author ${user} 
* @version $Revision$, $Date$, $Author$
* 
*/

public class SonicConnector implements JmsProviderIntf
{
   private static final Logger LOG = Logger.getLogger("Sonic");

   public SonicConnector() {
       
   }
   
   /**
    * The method uses the vendor specific ConnectionFactory and connects the (also)
    * vendor specific listener for connection state changes (as inline)
    * 
    * @param brokers <code>String</code> the connection string, typically broker:port,other_broker:port, ...
    * @return <code>javax.jms.Connection</code> a connection as provided by the vendors connection factory
    * @throws JMSException when connection fails
    */
   public javax.jms.Connection getConnection(String brokers) throws JMSException
   {
       LOG.info("Creating connection for config string [" + brokers + "]");
       ConnectionFactory factory = new ConnectionFactory();
       factory.setReconnectInterval(10);
       factory.setConnectionURLs(brokers);
       
       LOG.info("Connection factory created, requesting a connection ...");
       Connection conn = (Connection) factory.createConnection("laser_usr", "laser_pwd");
       conn.setConnectionStateChangeListener(new ConnectionStateChangeListener() 
       {
           @SuppressWarnings("synthetic-access")
           @Override
           public void connectionStateChanged(int arg0) 
           {
               LOG.warn("Connection status changed (" + arg0 + ")");                
           }
           
       });
       LOG.info("Connection created.");
       return conn;
   }

   @Override
   public javax.jms.Connection getConnection() {
       try {
           return getConnection("tcp://sljas2:2506,tcp://sljas3:2506");
       } catch (JMSException e) {
           LOG.error("Failed to provide valid connection", e);
       }
       return null;
   }
   

}
