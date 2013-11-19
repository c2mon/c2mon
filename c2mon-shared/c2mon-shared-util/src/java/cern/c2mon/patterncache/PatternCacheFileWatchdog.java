/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.patterncache;

import static java.lang.String.format;
import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.FileWatchdog;

/**
 * The <code>PatternCacheWatchdog</code> class implements a pattern-cache file watchdog
 * 
 * @author wbuczak
 */
public class PatternCacheFileWatchdog<T extends Cachable> extends FileWatchdog {

    private static Logger LOG = Logger.getLogger(PatternCacheFileWatchdog.class);

    private PatternCache<T> pcache;

    public PatternCacheFileWatchdog(final PatternCache<T> pcache, final String fileName) {
        this(pcache, fileName, DEFAULT_DELAY);
    }

    public PatternCacheFileWatchdog(final PatternCache<T> pcache, final String fileName, final long delay) {
        super(fileName);
        super.setDelay(delay);
        this.pcache = pcache;
        // initialize cache
        reloadCache();    
    }

    @Override
    protected void doOnChange() {
        reloadCache();
    }

    private void reloadCache() {
        if (pcache != null) {
            LOG.info(format("reloading pattern cache. file: %s", this.filename));
            pcache.load(new File(this.filename));
        }

    }

}
