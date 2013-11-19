/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.patterncache;

import static java.lang.String.format;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * The pattern cache class implements a cache which holds in memory objects associated with string (java regexp)
 * patterns The cache can be initialize both from a string buffer or from an external file
 * 
 * @author wbuczak
 */
public class PatternCache<T extends Cachable> {

    private List<String> patterns;
    private List<T> cachables;
    private Map<String, T> cache;

    private static final Logger LOG = Logger.getLogger(PatternCache.class);

    private Class<T> clazz;

    public PatternCache(Class<T> clazz) {
        this.clazz = clazz;
    }

    public synchronized void load(final StringBuilder cacheTxt) {
        LOG.trace("entering load() ..");

        patterns = new ArrayList<String>();
        cachables = new ArrayList<T>();
        cache = new HashMap<String, T>();

        parse(cacheTxt);

        LOG.trace("leaving load(%s)");
    }

    /**
     * This method is synchronized - only one thread should be able to load cache at a time
     * 
     * @param dataTagList
     * @return
     */
    public synchronized void load(final File patternFile) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(format("entering load(%s) ..", patternFile.getAbsolutePath()));
        }
        patterns = new ArrayList<String>();
        cachables = new ArrayList<T>();
        cache = new HashMap<String, T>();

        parse(patternFile);

        if (LOG.isTraceEnabled()) {
            LOG.trace(format("leaving load(%s)", patternFile.getAbsolutePath()));
        }
    }

    /**
     * checks if there is a match in the cache between the passed string key and registered patterns
     * 
     * @param text
     * @return instance of the <code>Cachable</code> if match is found, null otherwise
     */
    public synchronized T findMatch(String text) {
        T result = null;

        // first, check if cache already has resolved association
        if (cache.containsKey(text)) {
            result = cache.get(text);
        } else {
            for (int i = 0; i < patterns.size(); i++) {
                if (Pattern.matches(patterns.get(i), text)) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(format("found match: pattern: %-15s  text: %-15s", patterns.get(i), text));
                    }
                    result = cachables.get(i);
                    cache.put(text, result);
                    break;
                }// if
            }// for
        }
        return result;
    }

    /**
     * @return current size of the cache
     */
    public synchronized int getSize() {
        LOG.trace("entering getSize(%s) ..");
        if (LOG.isTraceEnabled()) {
            LOG.trace("returning: " + cachables.size());
        }
        return cachables.size();
    }

    /**
     * Parses the given file and caches the list of cachables
     */
    public synchronized void parse(final StringBuilder cacheTxt) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("entering parse() ..");
        }

        BufferedReader in = new BufferedReader(new StringReader(cacheTxt.toString()));

        commonParse(in);

        if (LOG.isTraceEnabled()) {
            LOG.trace("leaving parse()");
        }
    }

    /**
     * Parses the given file and caches the list of cachables
     */
    private void parse(final File patternFile) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(format("entering parse(%s) ..", patternFile.getAbsolutePath()));
        }

        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(patternFile));
        } catch (FileNotFoundException e) {
            LOG.error("parse() - The file " + patternFile.getAbsolutePath() + " does not exist!");
            return;
        }

        commonParse(in);

        if (LOG.isTraceEnabled()) {
            LOG.trace(format("leaving parse(%s)", patternFile.getAbsolutePath()));
        }
    }

    private void commonParse(final Reader r) {

        BufferedReader in = null;

        in = new BufferedReader(r);

        try {
            // The current line of the file
            String line = in.readLine();

            // The current line split into pieces
            String[] lineBuffer = null;
            while (line != null) {

                // skip comments
                if (!line.trim().startsWith("#")) {

                    // split the line
                    lineBuffer = line.split(" +");
                    if (lineBuffer != null && lineBuffer.length > 1) {

                        // first token is the pattern
                        String pattern = lineBuffer[0];

                        // do unless pattern's already registered (skip duplicates)
                        if (!patterns.contains(pattern)) {

                            // remaining tokens are to be used for the initialization of the Cachable instance
                            String[] tokens = Arrays.copyOfRange(lineBuffer, 1, lineBuffer.length);
                            try {
                                // create and initialise cachable instance
                                T instance = clazz.newInstance();
                                instance.init(tokens);

                                patterns.add(pattern);
                                cachables.add(instance);
                            } catch (Exception ex) {
                                LOG.warn(ex);
                            }
                        }// if

                    }// if
                }
                // read next line from file
                line = in.readLine();
            }// while
        } catch (IOException ex) {
            LOG.error("parse() - An error occured while parsing the file.", ex);
        }

        try {
            in.close();
        } catch (IOException e) {
            LOG.error("parse() - Error while closing reader ", e);
        }

    }

}
