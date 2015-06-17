/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.util;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.SpectrumTestUtil;

public class SpectrumCmdLineClient 
{

    
    public static void main(String[] args) throws Exception
    {
        JmsProviderIntf jms = new SonicConnector();
        Connection conn = jms.getConnection();
        
        try
        {
            conn.start();
        
            Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination topic = sess.createTopic("CERN.DIAMON.SPECTRUM");
            MessageProducer prod = sess.createProducer(topic);

            SpectrumEventType cmd = SpectrumEventType.valueOf(args[0]);
            String hostname = args[1];
            int causeId = Integer.parseInt(args[2]);
            String source = args[3];
        
            String strmsg = SpectrumTestUtil.buildMessage(cmd, hostname, causeId);
            TextMessage message = sess.createTextMessage(strmsg);
            message.setStringProperty("spectrum_Server", source);
        
            prod.send(message);
        
            prod.close();
            sess.close();
        }
        catch (Exception e)
        {
            System.err.println("Usage: SpectrumCmdLineClient CLR|RST|SET|UPD|KAL hostname cause_id source_server");
            e.printStackTrace();
        }
        finally
        {
            conn.close();
        }
        
    }
}
