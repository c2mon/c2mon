package cern.c2mon.server.ehcache.event;

import cern.c2mon.server.ehcache.Ehcache;
import cern.c2mon.server.ehcache.Element;

public class RegisteredEventListeners {

    private final Ehcache cache;

    public RegisteredEventListeners(Ehcache cache) {
        this.cache = cache;
    }

    public final void notifyElementUpdated(Element element, boolean remoteEvent) {
    //TODO
    }
}
