
package cern.c2mon.daq.spectrum.listener.impl;

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

import cern.c2mon.daq.spectrum.SpectrumEquipConfig;
import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerIntf;
import cern.c2mon.daq.spectrum.util.JmsProviderIntf;
import cern.c2mon.daq.spectrum.util.SonicConnector;

/**
 * 
 * @author mbuttner
 */
public class SpectrumListenerJms implements SpectrumListenerIntf, MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(SpectrumListenerJms.class);
    
    private Queue<SpectrumEvent> eventQueue;    // reference to the @see EventProcessor queue
    private boolean cont = true;                // used by shutdown for smooth interrupt

    //
    // --- Implements Runnable ---------------------------------------------------------------
    //    
    @Override
    public void run() {
        cont = true;
        try {
//            Class<?> cls = Class.forName(System.getProperty("diamon.jms.provider"));
            JmsProviderIntf jms = new SonicConnector();
            Connection conn = jms.getConnection();
            conn.start();
            
            Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination topic = sess.createTopic("CERN.DIAMON.SPECTRUM");
            MessageConsumer cons = sess.createConsumer(topic);
            cons.setMessageListener(this);
            
            while (cont) {
                try {
                    Thread.sleep(2 * 1000); // first attempt: inject one event per n seconds
                } catch (Exception e) {
                    LOG.error("Sleep interrupted in main loop of JMS listener!");
                }
            }
            cons.close();
            sess.close();
            conn.close();
            
        } catch (Exception e) {
            LOG.error("Failure in Spectrum mock thread", e);
        }                    
        LOG.info("Spectrum mock stopped...");
    }

    //
    // --- Implements SpectrumListenerIntf ---------------------------------------------------------
    //
    @Override
    public void setConfig(SpectrumEquipConfig config) {
//        this.config = config;
    }

    @Override
    public void shutdown() {
        LOG.info("Stopping the Spectrum mock ...");
        cont = false;
    }

    @Override
    public void setQueue(Queue<SpectrumEvent> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public void setProcessor(SpectrumEventProcessor proc) {
        //
    }

    @Override
    public void onMessage(Message msg) {
        try {
            TextMessage tm = (TextMessage) msg;
            String content = tm.getText();
            String server = msg.getStringProperty("spectrum_Server");
            SpectrumEvent event = new SpectrumEvent(server, content);
            eventQueue.add(event);        
            LOG.info("Queued message from {}", server);            
            LOG.info("-> content: {}", content);            
        } catch (Exception e) {
            LOG.error("Failed to process JMS message", e);            
        }
    }

}
