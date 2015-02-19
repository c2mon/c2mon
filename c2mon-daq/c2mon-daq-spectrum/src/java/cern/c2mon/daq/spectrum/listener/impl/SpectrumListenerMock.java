
package cern.c2mon.daq.spectrum.listener.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumEquipConfig;
import cern.c2mon.daq.spectrum.SpectrumEvent;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerIntf;

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
    private SpectrumEventProcessor proc;
    private Queue<SpectrumEvent> eventQueue;        // reference to the @see EventProcessor queue
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
                String ligne = inr.readLine();
                if (ligne != null && ligne.indexOf(">>>") > 0) {
                    String msg = ligne.substring(ligne.indexOf(">>>") + 3);  
                    msg = msg.substring(0, msg.indexOf("<<<"));
                    LOG.debug("Generating an event ...");
                    LOG.trace("Raw:  {}", ligne);
                    LOG.trace("Trim: {}", msg);

                    SpectrumEvent event = new SpectrumEvent(config.getPrimaryServer(), msg);
                    if (proc.isInteresting(event))
                    {
                        Thread.sleep(5 * 1000); // first attempt: inject one event per n seconds
                    }
                    eventQueue.add(event);
                }
                if (ligne == null) {
                    Thread.sleep(15 * 1000); // first attempt: inject one event per n seconds
                    cont= false;
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
    public void setQueue(Queue<SpectrumEvent> eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    public void setProcessor(SpectrumEventProcessor proc) {
        this.proc = proc;
    }

}
