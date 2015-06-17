
package cern.c2mon.daq.spectrum.listener;

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

import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;
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
        Connection conn = null;
        MessageConsumer cons = null;
        Session sess = null;
        try {
            LOG.info("Starting Spectrum JMS listener ...");
            JmsProviderIntf jms = new SonicConnector();
            conn = jms.getConnection();
            conn.start();
            
            sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination topic = sess.createTopic("CERN.DIAMON.SPECTRUM");
            cons = sess.createConsumer(topic);
            cons.setMessageListener(this);
        } catch (Exception e) {
            LOG.error("Failure during Spectrum JMS boot.", e);
        }                    
            
        while (cont) {
            try {
                Thread.sleep(2 * 1000); // first attempt: inject one event per n seconds
                LOG.trace("... waiting ...");
            } catch (Exception e) {
                LOG.error("Sleep interrupted in main loop of JMS listener!");
            }
        }
        try {
            if (cons != null) cons.close();
            if (sess !=null) sess.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            LOG.error("Failure during Spectrum JMS stop.", e);
        }                    
        LOG.info("Spectrum JMS listener stopped.");
    }

    //
    // --- Implements SpectrumListenerIntf ---------------------------------------------------------
    //

    @Override
    public void shutdown() {
        LOG.info("Stopping the Spectrum JMS listener ...");
        cont = false;
    }

    @Override
    public void setProcessor(SpectrumEventProcessor proc) {
        this.eventQueue = proc.getQueue();
    }

    @Override
    public void onMessage(Message msg) {
        try {
            LOG.debug("... data incoming ...");
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
