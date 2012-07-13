package cern.c2mon.statistics.web;

import java.io.File;
import java.io.FileFilter;

/**
 * This filter picks out the files in a given directory,
 * and filters out the directories.
 * 
 * @author mbrightw
 *
 */
public class OnlyFiles implements FileFilter {
   
    /**
     * Implementation of the method detecting the files.
     * 
     * @param file the File object to check
     * @return true if the File is a file, false if a directory
     */
    public final boolean accept(final File file) {
        return file.isFile();
    }

}
