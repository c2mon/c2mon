
package cern.c2mon.daq.monit.listener;

import java.util.Queue;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.monit.MonitEventProcessor;
import cern.c2mon.daq.monit.MonitUpdateEvent;

/**
 * 
 * @author mbuttner
 */
public class MonitListenerHttp implements MonitListenerIntf {

    private static final Logger LOG = LoggerFactory.getLogger(MonitListenerHttp.class);
    
    private Queue<MonitUpdateEvent> eventQueue;    // reference to the @see EventProcessor queue
    
    private int port;
    
    //
    // --- PUBLIC METHODS -----------------------------------------------------------------------
    //
    public void setPort(int port) {
        this.port = port;
    }
    
    public int getPort() {
        return this.port;
    }
    
    //
    // --- Implements MonitListenerIntf ---------------------------------------------------------
    //
    @Override
    public void connect() {
        // TODO connect this as a servlet to internal jetty!
        // TODO configure webserver port from spring into this class
    }

    @Override
    public void disconnect() {
        // TODO stop webserver
        LOG.info("Stopping the Spectrum JMS listener ...");
    }

    @Override
    public void setProcessor(MonitEventProcessor proc) {
        this.eventQueue = proc.getQueue();
    }

}
