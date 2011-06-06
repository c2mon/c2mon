package cern.c2mon.pmanager.persistence.util;

import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.alarm.FallbackAlarmsInterface;
import cern.c2mon.pmanager.fallback.FallbackProperties;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.pmanager.persistence.impl.TimPersistenceManager;

/**
 * This class implements the Runnable interface. It runs as a separate thread
 * that takes care of committing back to the DB that data stored in a fallback
 * file
 * 
 * @author mruizgar
 * 
 */
public class DataRecoveryThread implements Runnable, FallbackAlarmsInterface {

    /**
     * It indicates whether a thread of this class has been already started and
     * it is running
     */
    private boolean stopped = true;

    /** Log4j Logger for this class (debug and error messages) */
    private static final Logger LOG = Logger.getLogger(DataRecoveryThread.class);

    /** Log4j logger for the fallback related debug and error messages */
    private static final Logger FALLBACK_LOG = Logger.getLogger("ShortTermLogFallbackLogger");

    /**
     * Number of miliseconds that the thread will sleep after each iteration by
     * default
     */
    private static final int DEFAULT_SLEEP_TIME = 500;

    /**
     * Instance to the TimPersistenceManager object that created this object.
     * This way the thread will be able to access its fields knowing from which
     * file it has to read back, etc.
     */
    private TimPersistenceManager persistenceManager = null;

    /**
     * @param persistenceManager
     *            the persistenceManager to set
     */
    public final void setPersistenceManager(final TimPersistenceManager persistenceManager) {
         this.persistenceManager = persistenceManager;
         
    }

    /**
     * Creates a new DataRecoveryThread object specifying the time it will sleep
     * between each commit to the DB
     * 
     * @param persistence
     *            Indicates the number of miliseconds the thread has to sleep
     */
    public DataRecoveryThread(final TimPersistenceManager persistence) {
        if (persistence.getSleepTime() == -1) { // The sleeptime has not been
                                                // provided by the client
            persistence.setSleepTime(DEFAULT_SLEEP_TIME);
        }
        this.persistenceManager = persistence;
    }

    /**
     * @return the stopRunning
     */
    public final boolean isRunning() {
        return !stopped;
    }

    /**
     * It stops the thread and removes the reference to the
     * TimPersistenceManager object so its memory can be released by the garbage
     * collector
     */
    public final void stop() {
        if (!isRunning()) {
            resetPersistenceManager();
        }
        stopped = true;
    }

    /**
     * Removes the reference to the TimPersistenceManager object
     */
    private void resetPersistenceManager() {
        persistenceManager = null;
    }

    /**
     * This method calls the functionality defined in the FallbackHelper class
     * to achieve the process of committing back into the DB those tags that
     * have been stored in the fallback files while the DB connection was broken
     */
    public final void run() {
        boolean committed = true;

        stopped = false;
        if (FALLBACK_LOG.isDebugEnabled())
            FALLBACK_LOG.debug("Thread of instance " + this.hashCode() + " beginning");
        synchronized (persistenceManager.getFallbackManager().getFallbackFileController()) {
            while (!persistenceManager.getFallbackManager().isFallbackFileEmpty() && committed
                    && isRunning()) {
                int numberOfTags;
                // The data from the log file will be read from the file and
                // committed in the database in bunches of a defined size
                numberOfTags = commitFallbackData();
                if (numberOfTags > 0) {
                    // Remove the dataTags that have been committed back from
                    // the fallback file
                    removeReadData(numberOfTags);
                }
                if (numberOfTags == 0) {
                    committed = false;
                }
                try {
                    Thread.sleep(persistenceManager.getSleepTime());
                } catch (InterruptedException e) {
                    FALLBACK_LOG.error("An error occurred while trying to make the thread to sleep");
                }
            }
        }
        /*
         * if (!isRunning()) { // The thread was externally stopped by
         * persistenceManager who needs to be destroyed and therefore // we need
         * to remove the reference to it, so the garbage collector can done its
         * work persistenceManager = null;
         */
        stopped = true;
        persistenceManager = null;
        if (FALLBACK_LOG.isDebugEnabled()) {
            FALLBACK_LOG.debug("Removing the reference to the persistenceManager");
        }

        if (LOG.isDebugEnabled())
            FALLBACK_LOG.debug("Thread of instance " + this.hashCode() + " terminated");
    }

    /**
     * Reads back data from a fallback file and commits it to the DB.
     * 
     * @return An integer value indicating the number of fallback lines that
     *         were read from the fallback log file and successfully committed
     *         to the DB
     * 
     */
    private int commitFallbackData() {

        List data = null;
        int committed = 0;

        if (FALLBACK_LOG.isDebugEnabled())
            FALLBACK_LOG
                    .debug("commitFallbackData() : Commiting the tags stored in the logfile back into the database");
        try {
            // Get all the datatags stored in the log file
            data = persistenceManager.getFallbackManager().readDataBack(
                    FallbackProperties.getInstance().getNumberLinesToReadFromFile());
            // Insert the datatags into the database           
            if (LOG.isDebugEnabled()) {
                LOG.debug("commitFallBackData() - Inserting " + data.size()
                        + " tags from the fallback file into the database");
            }
            
            persistenceManager.getDbHandler().storeData(data);
            committed = data.size();
        } catch (IDBPersistenceException e) {
            FALLBACK_LOG.error(
                    "CommitFallBackData : Error executing/committing prepared statement.", e);
            try {
                // We reset the file descriptor to the last committed line,
                // ignoring those last ones
                // that have been already read, but not committed
                committed = e.getCommited();
                persistenceManager.getFallbackManager().goToLastProcessedLine(e.getCommited());
            } catch (DataFallbackException fe) {
                FALLBACK_LOG
                        .error("CommitFallBackData : The file desciptor could not be placed in the right place,"
                                + " some tags will be ignored" + fe);
            }
        } catch (DataFallbackException ex) {
            FALLBACK_LOG.error(
                    "commitFallBackData() : Unable to read the data from the log file ", ex);
            try {
                // Place the cursor in the last processed line, as if this
                // called had never been made
                persistenceManager.getFallbackManager().goToLastProcessedLine(committed);
            } catch (DataFallbackException fe) {
                FALLBACK_LOG
                        .error("CommitFallBackLogs : The file desciptor could not be placed in the right place,"
                                + " some tags will be ignored" + fe);
            }
        }
        return committed;
    }

    /**
     * Marks the indicated number of lines as removed in the fallback file
     * 
     * @param size
     *            The number of lines we want to mark as removed from the
     *            fallback file
     */
    private void removeReadData(final int size) {

        if (!persistenceManager.getFallbackManager().removeReadData(size)) {
            persistenceManager.getAlarmSender().fileNotReachable(
                    ACTIVATED,
                    persistenceManager.getFallbackManager().getFallbackFileController()
                            .getDataFile());
            FALLBACK_LOG
                    .error("commitFallbackCommandLogs() - CommandTags cannot be removed from the fallback log file");
        } else {
            persistenceManager.getAlarmSender().fileNotReachable(
                    DOWN,
                    persistenceManager.getFallbackManager().getFallbackFileController()
                            .getDataFile());
        }

    }

    /**
     * It sets the persistenceManager to null, so it could be released by the
     * garbage collector if needed
     */
    public final void finalize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("finalize() - Removing the reference to the TimPersistenManager");
        }
        persistenceManager = null;
    }
}
