package cern.c2mon.server.common.control;

import cern.c2mon.server.common.datatag.DataTagCacheObject;

public class ControlTagCacheObject extends DataTagCacheObject implements ControlTag, Cloneable {

    private static final long serialVersionUID = -4100866263977139930L;

    public ControlTagCacheObject() {
        super();
    }

    /**
     * Used to construct a fake cache object, which is returned when a key cannot be located in the cache.
     * 
     * @param id
     * @param name
     * @param datatype
     * @param mode
     */
    public ControlTagCacheObject(Long id, String name, String datatype, short mode) {
        super(id, name, datatype, mode);
    }

    public ControlTagCacheObject(Long id) {
        super(id);
    }

    /**
     * Clone implementation.
     * 
     * @throws CloneNotSupportedException
     */
    public ControlTagCacheObject clone() throws CloneNotSupportedException {
        return (ControlTagCacheObject) super.clone();
    }

}
