package cern.c2mon.client.core.jms;

public interface EnqueuingEventListener {

    void onEnqueuingEvent(String details, int percentage);
}
