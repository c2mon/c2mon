package cern.c2mon.server.cache.common;

public class FailedCacheElementCloningException extends RuntimeException {
    
    private static final long serialVersionUID = -428213822632206960L;

    public FailedCacheElementCloningException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
