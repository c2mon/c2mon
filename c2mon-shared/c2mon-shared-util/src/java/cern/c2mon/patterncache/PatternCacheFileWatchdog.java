/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.patterncache;

import static java.lang.String.format;
import java.io.File;

import org.apache.log4j.helpers.FileWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>PatternCacheWatchdog</code> class implements a pattern-cache file watchdog
 *
 * @author wbuczak
 */
public class PatternCacheFileWatchdog<T extends Cachable> extends FileWatchdog {

    private static Logger LOG = LoggerFactory.getLogger(PatternCacheFileWatchdog.class);

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
