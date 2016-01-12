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
