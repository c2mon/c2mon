
package cern.c2mon.daq.spectrum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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
    
    private SpectrumEquipConfig config;
    private boolean cont = true;

    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    
    @Override
    public void run() {
        BufferedReader inr = null;
        try {
            inr = new BufferedReader(new FileReader("testdata/log-comm.log"));
            while (cont) {
                Thread.sleep(2 * 1000); // firs attempt: inject one event per 3s

                String ligne = inr.readLine();
                if (ligne != null && ligne.indexOf(">>>") > 0) {
                    String msg = ligne.substring(ligne.indexOf(">>>") + 4);  
                    msg = msg.substring(0, msg.indexOf("<<<"));
                    LOG.info("Raw:  {}", ligne);
                    LOG.info("Trim: {}", msg);
                    eventQueue.add(new Event(config.getPrimaryServer(), msg));
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
    public Queue<Event> getQueue() {
        return eventQueue;
    }

}
