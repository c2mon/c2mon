
package cern.c2mon.daq.spectrum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing without real connection to the Spectrum server. This class starts a thread
 * which reads events from the communication log of the Spectrum-LASER bridge and injects
 * the data into the development environment.
 * 
 * @author mbuttner
 */
public class SpectrumListenerMock implements SpectrumListenerIntf {

    private static final Logger LOG = LoggerFactory.getLogger(SpectrumListenerMock.class);
    
    private SpectrumEquipConfig config;     // primary and secondary server, port for listening
    private Queue<Event> eventQueue;        // reference to the @see EventProcessor queue
    private boolean cont = true;            // used by shutdown for smooth interrupt

    //
    // --- Implements Runnable ---------------------------------------------------------------
    //    
    @Override
    public void run() {
        cont = true;
        BufferedReader inr = null;
        try {
            inr = new BufferedReader(new FileReader("testdata/log-comm.log"));
            while (cont) {
                Thread.sleep(1 * 1000); // first attempt: inject one event per n seconds

                String ligne = inr.readLine();
                if (ligne != null && ligne.indexOf(">>>") > 0) {
                    String msg = ligne.substring(ligne.indexOf(">>>") + 3);  
                    msg = msg.substring(0, msg.indexOf("<<<"));
                    LOG.debug("Generating an event ...");
                    LOG.trace("Raw:  {}", ligne);
                    LOG.trace("Trim: {}", msg);
                    eventQueue.add(new Event(config.getPrimaryServer(), msg));
                    // TODO sleep longer if we have a real event, less or not at all if we do not have 
                    // a valid alarm event
                }
            }
        } catch (Exception e) {
            LOG.error("Failure in Spectrum mock thread", e);
        }            
        finally {
            try {
                if (inr != null) {
                    inr.close();
                }
            } catch (IOException e) {
                LOG.error("Failure in Spectrum mock thread", e);
            }
        }
        
        LOG.info("Spectrum mock stopped...");
    }

    //
    // --- Implements SpectrumListenerIntf ---------------------------------------------------------
    //
    @Override
    public void setConfig(SpectrumEquipConfig config) {
        this.config = config;
    }

    @Override
    public void shutdown() {
        LOG.info("Stopping the Spectrum mock ...");
        cont = false;
    }

    @Override
    public void setQueue(Queue<Event> eventQueue) {
        this.eventQueue = eventQueue;
    }

}
