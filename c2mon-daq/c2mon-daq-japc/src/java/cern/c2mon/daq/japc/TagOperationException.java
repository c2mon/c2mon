package cern.c2mon.daq.japc;

public class TagOperationException extends Exception {

    private static final long serialVersionUID = 7906984509174711852L;
    
    private long tagId;

    public TagOperationException(final String errMessage) {
        super(errMessage);
    }
    
    public TagOperationException(final long tagId, final String errMessage) {
        this(errMessage);
        this.tagId = tagId;
    }
        
    public long getTagId() {
        return this.tagId;
    }
}
