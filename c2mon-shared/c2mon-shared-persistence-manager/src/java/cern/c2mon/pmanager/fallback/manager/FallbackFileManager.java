/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.pmanager.fallback.manager;

import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.FallbackFileController;
import cern.c2mon.pmanager.fallback.FallbackProperties;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.fallback.exception.SystemDiskSpaceException;
import cern.c2mon.pmanager.fallback.util.SystemResourcesParameters;

/**
 * This class is a helper that allows to encapsulate the mechanism that is being
 * used as a fallback, in this case a logfile. It works as a facade, making
 * totally independent the application logic from the final log system that is
 * used.
 * 
 * @author mruizgar
 * 
 */
public class FallbackFileManager {

    /** Log4j Logger for tags that cannot be logged to the database (emergency) */
    public static final Logger LOG = Logger.getLogger("ShortTermLogFallbackLogger");

    /** FallbackFileController instance */
    private final FallbackFileController fFileController;

    /**
     * Indicates which type of IFallback objects will be handled by this class
     * and by its FallbackFileController instance
     */
    private final IFallback fallbackObj;

    /**
     * It indicates the number of lines in the fallback file for which, when
     * reached, the free disk space in the system will be checked
     */
    private int nextCheckFallbackData = 0;

    /**
     * @return the nextCheckFallbackData
     */
    public final int getNextCheckFallbackData() {
        return nextCheckFallbackData;
    }

    /**
     * @param nextCheckFallbackData
     *            the nextCheckFallbackData to set
     */
    public final void setNextCheckFallbackData(final int nextCheckFallbackData) {
        this.nextCheckFallbackData = nextCheckFallbackData;
    }

    /**
     * Public constructor
     * 
     * @param fallbackFile
     *            The absolute path for the fallback file
     * @param fallbackObj
     *            IFallback object used for knowing with which concrete
     *            implementation of the interface deals each instance of this
     *            class
     * 
     */
    public FallbackFileManager(final String fallbackFile, final IFallback fallbackObj) {
        this.fFileController = new FallbackFileController(fallbackFile);
        this.fallbackObj = fallbackObj;
    }

    /**
     * Writes a set of DataCacheObjects into the fallback log mechanism
     * 
     * @param data
     *            List of DataCacheObjects to be logged
     * @throws DataFallbackException
     *             An exception is thrown in case something wrongs happens while
     *             writing in the file
     */
    public final void fallback(final List data) throws DataFallbackException {

        // If the file is empty we close the reading descriptor since it means
        // that a new file is going to be created
        if (isFallbackFileEmpty())
            fFileController.closeFallbackInputStream();
        // open the fallback log file output stream
        fFileController.openFallbackOutputStream();
        final int size = data.size();
        // Iterate through all the datatags and logged all the info that
        // contains into the fallback log file
        for (int i = 0; i != size; i++) {
            fFileController.writeLine((IFallback) data.get(i));
        }
        fFileController.setNumberOfLines(fFileController.getNumberOfLines() + size);
    }

    /**
     * Stores a IFallback object in the fallback file
     * 
     * @param fallbackObj
     *            The object whose information will be stored
     * @throws DataFallbackException
     *             Indicates that something went wrong during the process of
     *             writing the object
     */
    public final void fallback(final IFallback fallbackObj) throws DataFallbackException {

        try {
            // If the file is empty we close the reading descriptor since it
            // means that
            // a new file is going to be created
            if (isFallbackFileEmpty())
                fFileController.closeFallbackInputStream();
            // open the fallback log file output stream
            fFileController.openFallbackOutputStream();
            fFileController.writeLine(fallbackObj);
            fFileController.setNumberOfLines(fFileController.getNumberOfLines() + 1);
        } catch (DataFallbackException e) {
            throw new DataFallbackException(e.getMessage());
        }
    }

    /**
     * Checks the free disc space every time a determined number of lines are
     * written to the file
     * 
     * @param minimumFreeSpace
     *            The minimum disk space that has to be free for continue
     *            writing data into the fallback file
     * @return A boolean indicating whether the disk free space check has been
     *         done (true) or not (false)
     * @throws SystemDiskSpaceException
     *             An exception is thrown if the minimum free disc space has
     *             been reached
     */
    public final boolean isDiskSpaceCheckDone(final int minimumFreeSpace)
            throws SystemDiskSpaceException {
        final FallbackProperties fProperties = FallbackProperties.getInstance();
        boolean checkDone = false;

        if (fFileController.getNumberOfLines() > nextCheckFallbackData) {
            checkDone = true;
            if (LOG.isDebugEnabled())
                LOG
                        .debug("isDiskSpaceCheckDone() : Doing the check since the number of lines are "
                                + fFileController.getNumberOfLines()
                                + " and the current value for nextcheck is "
                                + nextCheckFallbackData);
            int freeSpace = SystemResourcesParameters.getFreeSpace();
            if (freeSpace < minimumFreeSpace
                    && freeSpace != FallbackProperties.CMD_FREE_SPACE_ERROR) {
                throw new SystemDiskSpaceException("Reached the limit of free space of "
                        + minimumFreeSpace + ", current free space is " + freeSpace);
            } else {
                nextCheckFallbackData = fFileController.getNumberOfLines()
                        + fProperties.getFreeSpaceCheckFrequency();
                if (LOG.isDebugEnabled())
                    LOG.debug("isDiskSpaceCheckDone() : New value of nextCheckDataTags is "
                            + nextCheckFallbackData);
            }
        }
        return checkDone;
    }

    /**
     * It retrieves an instance of the object in charge of dealing with the
     * fallback mechanism
     * 
     * @return A DataTagFallback object
     */
    public final FallbackFileController getFallbackFileController() {
        return this.fFileController;
    }

    /**
     * Reads a number of lines from the fallback mechanism
     * 
     * @param numberOfLines
     *            Number of lines to be read
     * @return Number of lines that have been read
     * @throws DataFallbackException
     *             An exception is thrown if something wrongs happens while
     *             reading the file
     */
    public final List readDataBack(final int numberOfLines) throws DataFallbackException {
        // close the outputstream if it was still opened
        fFileController.closeFallbackOutputStream();
        fFileController.openFallbackInputStream();
        // read from the fallback log mechanism the indicated number of lines
        final List data = fFileController.readLines(numberOfLines, fallbackObj);
        // dtFallback.closeFallbackInputStream();
        return data;
    }

    /**
     * It calls the remove functionality from the fallback log mechanism. It
     * updates the counter indicating the number of lines that have been already
     * processed from the file. When all the datatags contained in the file has
     * been committed to the DB all the contents of the fallback file are
     * completely removed
     * 
     * @param numberOfLines
     *            Number of lines that has been already processed from the log
     *            file
     * @return A boolean that indicates whether the lines were successfully
     *         removed or not
     * 
     */
    public final boolean removeReadData(final int numberOfLines) {

        boolean removed = true;

        // Update the number of already committed datatags
        fFileController.setReadBackLines(numberOfLines + fFileController.getReadBackLines());
        // Virtually remove those datatags from the file
        try {
            fFileController.updateNumberOfProcessedLines();
        } catch (DataFallbackException e) {
            removed = false;           
        }
        try {
            // Physically remove all the contents from the file once all the
            // datatags have been committed to the DB
            if (LOG.isDebugEnabled()) {
                LOG
                        .debug("removeReadData : Checking if the file can be removed the number of read lines is: "
                                + fFileController.getReadBackLines()
                                + " and the file's lines is "
                                + fFileController.getNumberOfLines());
            }
            if (fFileController.getReadBackLines() == fFileController.getNumberOfLines()) {
                // Close the reading descriptor
                fFileController.closeFallbackInputStream();
                fFileController.clearFileContents();
                fFileController.setReadBackLines(0);
                fFileController.setNumberOfLines(0);
                // We also set the number for doing the disc file check to zero
                resetNextDiscSpaceCheck();
            }
        } catch (DataFallbackException e) {
            removed = false;
            LOG.error("Exception caught while attempting to remove lines from the Fallback file", e);
        }
        return removed;
    }

    /**
     * Set to zero lines the next check of the disc free space
     */
    private void resetNextDiscSpaceCheck() {
        nextCheckFallbackData = 0;
    }

    /**
     * Places the file descriptor in the line indicated as parameter
     * 
     * @param lastCommited
     *            Number of lines already processed
     * @throws DataFallbackException
     *             An exception is thrown if something went wrong while reading
     *             the lines from the file
     */
    public final void goToLastProcessedLine(final int lastCommited)
            throws DataFallbackException {
        fFileController.closeFallbackInputStream();
        int readLines = fFileController.getReadBackLines();
        fFileController.goToLine(readLines + lastCommited);
    }

    /**
     * It checks whether the file is empty (true) or not (false)
     * 
     * @return The value indicating the status of the log
     */
    public final boolean isFallbackFileEmpty() {
        return !((fFileController.getNumberOfLines()) > 0);
    }

    /**
     * It releases the object memory
     */
    public final void finalize() {
        this.fFileController.finalize();
    }
}
