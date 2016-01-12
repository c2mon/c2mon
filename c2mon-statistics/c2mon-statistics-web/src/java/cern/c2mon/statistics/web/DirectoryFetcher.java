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
package cern.c2mon.statistics.web;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;

/**
 * This class provides the method for walking through all the chart sub-directories and
 * returning those that contains files for inclusion into webpages.
 * 
 * This is used when providing the web user with the option of choosing the
 * charts he/she wishes to view.
 * 
 * @author mbrightw
 *
 */
public class DirectoryFetcher extends DirectoryWalker {
    
    /**
     * The directory where the chart web fragments are located.
     */
    private String fragmentsDir;
    
    /**
     * A filter distinguishing files from directories.
     */
    private FileFilter fileFilter;
    
    /**
     * The constructor.
     * 
     * @param pFragmentsDir the directory where the chart web fragments are located
     */
    public DirectoryFetcher(final String pFragmentsDir) {
        super();
        fragmentsDir = pFragmentsDir;
        
        //set the filter to one that filters out directories
        fileFilter = new OnlyFiles();
    }
    
    /**
     * Public method returning the list of sub-directories of the
     * fragments directory that contain chart html fragment files.
     * 
     * @return a list of the directories that contain chart web fragments
     *          (path is relative one with respect to the top fragments directory)
     * @throws IOException if there is a problem walking the directory structure
     */
    public final List fetch() throws IOException {
        List directories = new ArrayList();
        walk(new File(fragmentsDir), directories);
        return directories;
    }
    
    /**
     * Implementation of the super class method that handles directories during
     * the directory walk. All directories are examined and the relative path is recorded.
     * 
     * @param directory the directory that is being handled
     * @param depth the maximum depth to go
     * @param directories the list of directories with files in (modified by the method)
     * @return always true as we wish all directories to be examined
     */
    protected final boolean handleDirectory(final File directory, final int depth, Collection directories) {
        if (directory.listFiles(fileFilter).length != 0) {
            String relativePath = new File(fragmentsDir).toURI().relativize(directory.toURI()).getPath();
            
            //add to directories list
            directories.add(relativePath);
        }      
        return true;
    }

}
