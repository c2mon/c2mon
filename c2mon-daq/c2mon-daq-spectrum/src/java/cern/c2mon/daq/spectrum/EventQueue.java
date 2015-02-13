/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.util.ArrayList;

/**
 * In addition, static data is provided for the event queue to 
 * be processed by the main class of this app.
 * 
 * The class acts as a buffer between Spectrum message reception and output to LASER.
 * 
 * @author mbuttner
 * 
 * TODO could probably be replaced by some (now) standard JAva queue-type class
 */
public class EventQueue {

    private static EventQueue queue;
    
    private ArrayList<Event> eventQueue = new ArrayList<Event>();
    

    //
    // --- CONSTRUCTION -------------------------------------------------------------------
    //
    private EventQueue()
    {
        
    }
    
    public static EventQueue getQueue()
    {
        if (queue == null)
        {
            queue = new EventQueue();
        }
        return queue;
    }
    
    //
    // --- PUBLIC METHODS -------------------------------------------------------------------
    //
    
    /**
     * @return <code>boolean</code> true if an event is available on queue
     */
    public boolean available() {
        synchronized(eventQueue) {
            if (eventQueue.size() > 0) return true;
        }
        return false;
    }
    
    /**
     * @param event <code>Event</code> to be added on the event queue
     */
    public void addEvent(Event event) {
        synchronized(eventQueue) {
            eventQueue.add(event);
        }
    }
    
    /**
     * @return <code>Event</code> in position 0 of event queue, is returned and removed from queue
     */
    public Event consumeEvent() {
        synchronized(eventQueue) {
            Event event = eventQueue.get(0);
            eventQueue.remove(0);
            return event;
        }
    }

    
}
