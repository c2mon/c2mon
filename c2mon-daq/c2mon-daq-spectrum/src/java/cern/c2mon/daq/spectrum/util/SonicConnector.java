/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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
    * @throws Exception
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