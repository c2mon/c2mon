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
package cern.c2mon.pmanager.fallback;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import cern.c2mon.pmanager.fallback.manager.FallbackObjectContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that holds all the common functionality to the different
 * fallback files that may exists
 *
 * @author mruizgar
 *
 */
public class FallbackFileController {

    /** Log4j Logger for tags that cannot be logged to the database */
    public static final Logger LOG = LoggerFactory.getLogger("HistoryFallbackLogger");

    /** The file storing the datatags */
    private File dataFile;

    /** The file storing the line counter */
    private File counterFile;

    /** Stream file output for the fallback log */
    private BufferedWriter output;

    /** Stream file output for the file storing the line counter */
    private BufferedWriter dOutput;

    /** Stream file input for the file storing the line counter */
    private BufferedReader dInput;

    /** Stream file input for the fallback log file */
    private BufferedReader inputBuf;

    /**
     * Counter for the file, that shows the number of lines that are still in
     * the file but has been already read back and commited to the database
     */
    private int readBackLines;

    /**
     * The numberOfLines that has the file are stored in the object attribute in
     * order to avoid its recalculation all the time
     */
    private int numberOfLines;

    /**
     * @param tFile
     *            the tagsFile to set
     */
    protected final void setTagsFile(final File tFile) {
        this.dataFile = tFile;
    }

    /**
     * @return the tagsFile
     */
    public final File getDataFile() {
        return dataFile;
    }

    /**
     * @return the counterFile
     */
    public final File getCounterFile() {
        return counterFile;
    }

    /**
     * @return the numberOfLines
     */
    public final int getNumberOfLines() {
        return numberOfLines;
    }

    /**
     * @param nLines
     *            the numberOfLines to set
     */
    public final void setNumberOfLines(final int nLines) {
        this.numberOfLines = nLines;
    }

    /**
     * @return the output
     */
    public final BufferedWriter getOutput() {
        return output;
    }

    /**
     * @param out
     *            the output to set
     */
    public final void setOutput(final BufferedWriter out) {
        this.output = out;
    }

    /**
     * @return the input
     */
    public final BufferedReader getInput() {
        return inputBuf;
    }

    /**
     * @param in
     *            the input to set
     */
    public final void setInput(final BufferedReader in) {
        this.inputBuf = in;
    }

    /**
     * @return the dInput
     */
    public final BufferedReader getDInput() {
        return dInput;
    }

    /**
     * @param in
     *            the dInput to set
     */
    public final void setDInput(final BufferedReader in) {
        dInput = in;
    }

    /**
     * @return the dOutput
     */
    public final BufferedWriter getDOutput() {
        return dOutput;
    }

    /**
     * @param out
     *            the dOutput to set
     */
    public final void setDOutput(final BufferedWriter out) {
        dOutput = out;
    }

    /**
     * @return the readBackLines
     */
    public final int getReadBackLines() {
        return readBackLines;
    }

    /**
     * @param readLines
     *            the readBackLines to set
     */
    public final void setReadBackLines(final int readLines) {
        this.readBackLines = readLines;
    }

    /**
     * Gets the absolute path of the counter file, given the absolute path of
     * its corresponding fallback file
     *
     * @param filePath
     *            The absolute path of its corresponding fallback file
     * @return The absolute path of the counter file
     */
    private String getCounterPath(final String filePath) {
        int index = filePath.lastIndexOf('/');
        String counterPath = filePath.substring(0, index + 1) + ".Counter"
                + filePath.substring(index + 1, filePath.length());
        return counterPath;
    }

    /**
     * Constructor for the class
     *
     * @param fallbackPath
     *            path of the fallback file
     */
    public FallbackFileController(final String fallbackPath) {
        // Set the paths for the fallback files
        this.dataFile = new File(fallbackPath);
        this.counterFile = new File(getCounterPath(fallbackPath));

        // Initialize the fallback log file
        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile()) {
                    LOG.info("constructor() : The file " + dataFile.getName()
                            + " has been created");
                }
            } catch (IOException e) {
                LOG.error("constructor() - Error while creating the logfile", e);
            }
        }

        // Initialize the counter holder file
        if (!counterFile.exists()) {
            try {
                if (counterFile.createNewFile()) {
                    LOG.info("constructor() : The file " + counterFile.getName()
                            + " has been created");
                }
                initializeCounterFile();
            } catch (IOException e) {
                LOG.error("constructor() - Error while creating the counterfile", e);
            }
        }

        // There is a counter in the file and must be read
        try {
            // initialize the lines counter
            setNumberOfLines(calcNumberOfLines());

            //The counter file can't have a bigger number than the number of lines present in the fallback file
            if(readCounter(false) > numberOfLines){
                initializeCounterFile();
            }

            // Initialize the counter for the number of lines already proccessed
            // and prepared the
            // file descriptor to point to the next line to read
            setReadBackLines(readCounter(true));
            LOG.info("FallbackFileController() - The number of lines of the "
                    + dataFile.getName() + " file is " + getNumberOfLines());
        } catch (Exception e) {
            LOG
                    .error("Error while trying to initialize the lines counter for the fallback file", e);
        }
    }

    /**
     * Reads the number that is stored in the counter file and which represents
     * the already committed data
     *
     * @param moveReadingDescriptor If true move the reading descriptor to the first line that has yet to be processed
     * @return The counter stored in the counter file
     * @throws DataFallbackException
     *             An exception is thrown when the counter cannot be read from
     *             the file
     */
    protected int readCounter(boolean moveReadingDescriptor) throws DataFallbackException {
        final int counter;
        openCounterDataInputStream();
        try {
            String line;
            line = dInput.readLine();
            if (line != null) {
                counter = Integer.parseInt(line);
                // Put the reading descriptor in the first line that has not
                // been yet processed
                if(moveReadingDescriptor){
                    goToLine(counter);
                }
                if (LOG.isDebugEnabled())
                    LOG.debug("The counter file " + counterFile.getName() + " has been read");
            } else {
                throw new DataFallbackException(
                        "readCounter() - The counter could not be read, the counter file is empty");
            }
        } catch (IOException e) {
            LOG.error("Exception is: " + e.getMessage());
            throw new DataFallbackException("readCounter() - The counter could not be read "
                    + e.getMessage());
        } finally {
            closeCounterDataInputStream();
        }
        return counter;
    }

    /**
     * It prepares the file descriptor to read from a certain number of lines
     *
     * @param lineNumber
     *            The number of lines to be read to prepare the file descriptor
     * @throws DataFallbackException
     *             An exception is thrown if there was any problems while
     *             reading the file
     */
    public final void goToLine(final int lineNumber) throws DataFallbackException {
        boolean moreLines = true;
        int readlines = 0;

        openFallbackInputStream();
        if (lineNumber != 0) {
            try {
                while (moreLines && readlines < lineNumber) {
                    if (getInput().readLine() == null)
                        moreLines = false;
                    else
                        readlines++;
                }
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            }
        }
    }

    /**
     * Create a stream to read from the counter file
     *
     * @throws DataFallbackException
     *             An exception is thrown when the input stream cannot be opened
     */
    private void openCounterDataInputStream() throws DataFallbackException {
        if (dInput == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("openDataInputStream() ; Opening the reader for the "
                        + counterFile.getName() + " file");
            try {
                dInput = new BufferedReader(new FileReader(counterFile));
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            }
        }
    }

    /**
     * Close the input stream opened for the counter file
     *
     * @throws DataFallbackException
     *             An exception is thrown if the stream could not be closed
     */
    private void closeCounterDataInputStream() throws DataFallbackException {
        if (LOG.isDebugEnabled())
            LOG.debug("Closing the counter file reader for the file " + counterFile.getName());
        if (dInput != null) {
            try {
                dInput.close();
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            } finally {
                dInput = null;
            }
        }
    }

    /**
     * It opens the counter binary file for writing
     *
     * @throws DataFallbackException
     *             An exception is thrown if the file can not be opened for
     *             wrtiting
     */
    private void openCounterDataOutputStream() throws DataFallbackException {
        if (dOutput == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("openDataOutputStream() ; Opening the writer for the "
                        + counterFile.getName() + " file");
            try {
                dOutput = new BufferedWriter(new FileWriter(counterFile));
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            }
        }
    }

    /**
     * The output stream for the counter file is closed
     *
     * @throws DataFallbackException
     *             An exception is thrown if the descriptor cannot be closed
     */
    private void closeCounterDataOutputStream() throws DataFallbackException {
        if (LOG.isDebugEnabled())
            LOG.debug("Closing the counter file writer for the file " + counterFile.getName());
        if (dOutput != null) {
            try {
                dOutput.close();
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            } finally {
                dOutput = null;
            }
        }
    }

    /**
     * It opens a file output stream
     *
     * @throws DataFallbackException
     *             An exception is thrown if the output stream cannot be opened.
     */
    public final void openFallbackOutputStream() throws DataFallbackException {
        if (output == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("openOutputStream() : Opening the writer for the "
                        + dataFile.getName() + " file");
            try {
                output = new BufferedWriter(new FileWriter(dataFile, true));
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            }
        }
    }

    /**
     * It opens a file input stream
     *
     * @throws DataFallbackException
     *             An exception is thrown if the input stream cannot be opened
     */
    public final void openFallbackInputStream() throws DataFallbackException {
        if (inputBuf == null) {
            if (LOG.isDebugEnabled())
                LOG.debug("openFallbackInputStream() : Opening the reader for the "
                        + dataFile.getName() + "file");
            try {
                setInput(new BufferedReader(new FileReader(dataFile)));
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            }
        }
    }

    /**
     * It closes the input stream
     *
     * @throws DataFallbackException
     *             An exception is thrown if the input stream cannot be closed
     */
    public final void closeFallbackInputStream() throws DataFallbackException {
        if (getInput() != null) {
            try {
                if (LOG.isDebugEnabled())
                    LOG.debug("closeInputStream() : Closing the file reader for the file "
                            + dataFile.getName());
                getInput().close();
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            } finally {
                setInput(null);
            }
        }
    }

    /**
     * It closes the output stream
     *
     * @throws DataFallbackException
     *             An exception is thrown if the output stream cannot be closed
     */
    public final void closeFallbackOutputStream() throws DataFallbackException {

        if (output != null) {
            if (LOG.isDebugEnabled())
                LOG.debug("Closing the file writer for the file " + dataFile.getName());
            try {
                output.close();
            } catch (IOException e) {
                throw new DataFallbackException(e.getMessage());
            } finally {
                output = null;
            }
        }
    }

    /**
     * It sets the file counter to zero
     */
    private void initializeCounterFile() {
        try {
            openCounterDataOutputStream();
            dOutput.write("0");
            dOutput.newLine();
            dOutput.flush();
            if (LOG.isDebugEnabled())
                LOG
                        .debug("initializeCounterFile() - The counter file has been updated with zero");
        } catch (IOException e) {
            LOG
                    .error("initializeCounterFile() - Error while trying to write in the counter file");
        } catch (DataFallbackException e) {
            LOG.error("initializeCounterFile() - The counter file could not be opened");
        } finally {
            try {
                closeCounterDataOutputStream();
            } catch (DataFallbackException ex) {
                LOG.error("initializeCounterFile() - The counter file could not be closed");
            }
        }
    }

    /**
     * It writes an object into one of the fallback files
     *
     * @param object
     *            The IFallback object to be written to the fallback file
     * @throws DataFallbackException
     *             An exception is thrown if the tag cannot be written to the
     *             fallback file
     */

    public final void writeLine(final IFallback object) throws DataFallbackException {

        String str = object.toString().replaceAll("\n", "");
        try {
            output.write(str);
            output.newLine();
            output.flush();
        } catch (IOException e) {
            throw new DataFallbackException(
                    "writeLine() : An error has ocurred while trying to write in the "
                            + getDataFile().getName() + " file " + e.getMessage());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("writeLine() - The object with id " + object.getId() + " has been logged to the " + dataFile.getPath()
                    + " file");
        }
    }

    /**
     * It reads a list of objects from a fallback file
     *
     * @param numOfLines
     *            The number of lines to be read from the fallback file
     * @param fallbackObj
     *            An IFallback object to which type (IFallback implementation)
     *            the read lines will be transformed
     * @return A List of objects representing, each of it, a line from the
     *         fallback file
     * @throws DataFallbackException
     *             An exception is thrown if tags cannot be read from the
     *             fallback file
     */
    public final FallbackObjectContainer readLines(final int numOfLines, final IFallback fallbackObj)
            throws DataFallbackException {
        // line counter
        int readlines = 0;
        List<IFallback> objects = new ArrayList<>();
        String line = null;
        IFallback obj;

        // Process each line until we reach the specified number of lines or the
        // end of file
        boolean moreLines = true;
        try {
            while (moreLines && readlines < numOfLines) {
                line = getInput().readLine();
                if (line == null) {
                    moreLines = false;
                } else {
                    try {
                        // Parse the line to get the datatags values
                        obj = fallbackObj.getObject(line);
                        objects.add(obj);
                    } catch (DataFallbackException e) {
                         LOG.error("readLines() - " + e.getMessage() + "" + line);
                    }
                    readlines++;
                }
            }
        } catch (IOException e) {
            throw new DataFallbackException(e.getMessage());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("readLines() - " + objects.size()
                    + " lines has been read from the log file");
        }
        return new FallbackObjectContainer(objects, readlines);
    }

    /**
     * Checks whether the fallback log file has any contents or not
     *
     * @return A boolean indicating whether the file is empty (true) or not
     *         (false)
     */
    public final boolean isFallBackFileEmpty() {
        boolean isEmpty = false;
        try {
            openFallbackInputStream();
            if (!getInput().ready()) {
                isEmpty = true;
            }
        } catch (IOException e) {
            LOG
                    .error(
                            "isFallBackFileEmpty() : A problem has ocurred while trying to determine if the file was empty",
                            e);
        } catch (Exception ex) {
            LOG.error("isFallBackFileEmpty() : " + ex.getMessage());
        } finally {
            try {
                closeFallbackInputStream();
            } catch (Exception ex) {
                LOG.error("isFallBackFileEmpty() : " + ex.getMessage());
            }
        }
        if (LOG.isDebugEnabled())
            LOG
                    .debug("isFallBackFileEmpty() : The result of checking whether the file is empty or not is "
                            + isEmpty);
        return isEmpty;
    }

    /**
     * It checks the number of lines that the fallback log file has
     *
     * @return Number of lines of the file
     * @throws DataFallbackException
     *             An exception is thrown if something wrong happens while
     *             trying to read the file
     */
    private int calcNumberOfLines() throws DataFallbackException {
        int lines = 0;
        openFallbackInputStream();
        try {
            while (getInput().readLine() != null) {
                lines++;
            }
        } catch (IOException e) {
            throw new DataFallbackException();
        } finally {
            closeFallbackInputStream();
        }
        return lines;
    }

    /**
     * It stores the current file counter (indicating the number of lines
     * already processed from the file), so they won't be treated next time
     *
     * @throws DataFallbackException
     *             An exception is thrown if the counter could not be written to
     *             the file
     */
    public final void updateNumberOfProcessedLines() throws DataFallbackException {

        // Write the counter into the fallback counter file
        openCounterDataOutputStream();
        try {
            this.getDOutput().write(new Integer(getReadBackLines()).toString());
            getDOutput().newLine();
            if (LOG.isDebugEnabled())
                LOG.debug("removeLines() - The counter file has been updated with the value "
                        + getReadBackLines());
        } catch (IOException e) {
            throw new DataFallbackException(e.getMessage());
        }
        closeCounterDataOutputStream();
    }

    /**
     * Removes the contents of the fallback log file and updates the counter stored in the
     * counter file
     *
     * @throws DataFallbackException
     *             An exception is thrown if the fallback file cannot be deleted
     */
    public final void clearFileContents() throws DataFallbackException {

        // Delete the content of the original file
        try {
            new PrintWriter(dataFile).close();
        } catch (FileNotFoundException e) {
            LOG.error("clearFileContents() - The contents of the fallback log file {} could not be deleted", dataFile.getAbsolutePath());
            throw new DataFallbackException("The contents of the fallback log file " + dataFile.getAbsolutePath()
                    + " could not be deleted");
        }
        initializeCounterFile();
    }

    /**
     * This method closes all the file descriptors that could be still opened
     * before destroying the object
     *
     */
    public final void finalize() {
        try {
            closeFallbackInputStream();
            closeFallbackOutputStream();
            closeCounterDataInputStream();
            closeCounterDataOutputStream();
        } catch (Exception e) {
            LOG.error("finalize() - The opened streams to the files could not be closed");
        }
    }
}
