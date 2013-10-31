package cern.c2mon.daq.common.messaging.impl;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.tim.shared.daq.command.SourceCommandTagReport;
import cern.tim.shared.daq.config.ConfigurationChangeEventReport;
import cern.tim.shared.daq.datatag.SourceDataTagValueResponse;

/**
 * Dummy implementation of the ProcessMessageReceiver that can be used in the
 * Spring XML to start up without the DAQ command/request functionality.
 * 
 * @author mbrightw
 * 
 */
public class DummyMessageReceiver extends ProcessMessageReceiver {

    @Override
    public void connect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendDataTagValueResponse(SourceDataTagValueResponse sourceDataTagValueResponse, Topic replyTopic, Session session) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendConfigurationReport(ConfigurationChangeEventReport configurationChangeEventReport, Destination destination, Session session) throws TransformerException, ParserConfigurationException,
            IllegalAccessException, InstantiationException, JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendCommandReport(SourceCommandTagReport commandReport, Destination destination, Session session) throws JMSException {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdown() {
      // TODO Auto-generated method stub
      
    }

}
