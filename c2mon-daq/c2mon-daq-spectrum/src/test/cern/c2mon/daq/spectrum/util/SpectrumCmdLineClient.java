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
