/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The message reception thread. The thread is waiting on a given TCP port, for each incoming request
 * a single line is taken from the socket and converted into an Event object placed on the event queue.
 * 
 * To smoothly stop the thread, call shutdown().
 * 
 * @author mbuttner
 * @version 29 May 2009
 * 
 */
public class SpectrumListener implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(SpectrumListener.class);

    private SpectrumEquipConfig config;
    private boolean cont = true;
        
    //
    // --- CONSTRUCTION --------------------------------------------------------------------
    //
    public SpectrumListener(SpectrumEquipConfig config) {
        this.config = config;
    }
        
    //
    // --- PUBLIC METHODS -------------------------------------------------------------------
    //
    /**
     * Sets the "cont" flag to false, so that the thread stops after the next request.
     */
    public void shutdown() {
        cont = false;
    }

        
    /**
     * The thread's main. Listens on the Server.port and accepts incoming requests. Each message
     * is considered to be single line. The message is turned into an instance of Event and added
     * to the event queue for further processing.
     */
    @Override
    public void run() {
        try {
            int loopCounter = 0;
            ServerSocket srvr = new ServerSocket(config.getPort());
            while (cont) {
                try {
                    loopCounter++;
                    Socket skt = srvr.accept();
                        
                    // Add the originating server 
                    InetAddress ia = skt.getInetAddress();
                    String serverName = ia.getHostName();
                        
                    if (serverName.equals(config.getPrimaryServer()) || 
                        serverName.equals(config.getSecondaryServer())) {                 
                        
                        log.info("Request " + loopCounter + " received from " + serverName);
                        BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                        while (!in.ready()) { /**/ }
                        String msg = in.readLine();
                        Event event = new Event(serverName, msg);
                        EventQueue.getQueue().addEvent(event);
                        in.close();
                        log.info("Event " + msg + " processed.");
                    } else {
                        log.error("Rejected request from " + serverName + " (this server IS NOT on the whitelist !!!)");
                    }
                    skt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            srvr.close();
        } catch(Exception eFatal) {
            eFatal.printStackTrace();
        }
    }
    
}

    
