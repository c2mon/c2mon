package cern.c2mon.pmanager.persistence.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.IAlarmListener;
import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.alarm.FallbackAlarmsInterface;
import cern.c2mon.pmanager.fallback.FallbackProperties;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.fallback.exception.SystemDiskSpaceException;
import cern.c2mon.pmanager.fallback.manager.FallbackFileManager;
import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.pmanager.persistence.util.DataRecoveryThread;


/**
 * This class implements the IPersistenceManager providing the logic to store
 * data that comes to the server to the DB. A fallback mechanism will be put
 * into action as soon as it is detected any problem with the DB. The fallback
 * mechanism will log the incoming data to a file instead of the DB, this way
 * the data will not be lost. This fallback mechanism will be automatically
 * activated and deactivated. The deactivation will take place as soon as it is
 * detected that the DB issues have been sorted out. At that same time the data
 * stored in the file will be committed back to the DB.
 * 
 * @author mruizgar
 * 
 */
public class TimPersistenceManager implements IPersistenceManager, FallbackAlarmsInterface {

    /**
     * Implementation of the IDBPersistenceHandler interface that will be used
     * to commit the data into the DB
     */
    private final IDBPersistenceHandler dbHandler;

    /**
     * Indicates the time that the DataRecoveryThread will sleep between each
     * operation of committing back the data recovered from the fallback file
     */
    private int sleepTime = -1;

    /**
     * Implementation of the IAlarmSender interface that will be used to send
     * alarms
     */
    private IAlarmListener alarmSender = null;

    /**
     * Instance of the FallbackFileManager that will deal with the fallback
     * mechanism
     */
    private final FallbackFileManager fallbackManager;

    /**
     * Instance of the class that will deal with the process of reading and
     * committing back the data from the fallback mechanism to the DB Each
     * instance of TimPersistenceManager will stored a unique instance of that
     * class, that will be executed as an independent thread. This way we will
     * avoid that the same fallback file tries to be treated by different
     * threads at the same time
     */
    private DataRecoveryThread dataRecovery = new DataRecoveryThread(this);

    /** Log4j Logger for this class */
    private static final Logger LOG = Logger.getLogger(TimPersistenceManager.class);

    /**
     * The minimal amount of space in MB that always shall be left free on the
     * server disk. In case that limit is exceeded not data will be written to
     * the fallback file
     */
    private int minFreeDiscSpace = FallbackProperties.getInstance().getMinimunDiscFreeSpace();

    /** Log4j logger for the fallback related debug and error messages */
    private static final Logger FALLBACK_LOG = Logger.getLogger("ShortTermLogFallbackLogger");

    /**
     * @return the dbHandler
     */
    public final IDBPersistenceHandler getDbHandler() {
        return dbHandler;
    }

    /**
     * @return the alarmSender
     */
    public final IAlarmListener getAlarmSender() {
        return alarmSender;
    }

    /**
     * @return the fallbackManager
     */
    public final FallbackFileManager getFallbackManager() {
        return fallbackManager;
    }

    /**
     * @return the sleepTime
     */
    public final int getSleepTime() {
        return sleepTime;
    }

    /**
     * @param sleepTime
     *            the sleepTime to set
     */
    public final void setSleepTime(final int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * @param minFreeDiscSpace
     *            the minFreeDiscSpace to set
     */
    public final void setMinFreeDiscSpace(final int minFreeDiscSpace) {
        this.minFreeDiscSpace = minFreeDiscSpace;
    }

    /**
     * Creates a new TimPersistenceManager object.
     * 
     * @param dbHandler
     *            IDBPersistenceHandler instance indicating which implementation
     *            will be used to write the data to the DB
     * @param falbackFile
     *            Absolute path of the fallback file used to store the data when
     *            the DB connection is lost
     * @param aSender
     *            IAlarmSender instance indicating which implementation will be
     *            used for sending alarms or notifying of any error during the
     *            storage mechanism
     * @param fallbackObj
     *            IFallback instance indicating the type of objects that will be
     *            logged into the DB and, if it is the case, in the fallback
     *            mechanism
     */
    public TimPersistenceManager(final IDBPersistenceHandler dbHandler,
            final String falbackFile, final IAlarmListener aSender, final IFallback fallbackObj) {
        this.dbHandler = dbHandler;
        this.alarmSender = aSender;
        fallbackManager = new FallbackFileManager(falbackFile, fallbackObj);
    }

    /**
     * Write a collection of IFallback objects to the short-term log DB table.
     * In case that it detects that the DB is unavailable the incoming data will
     * be temporarily stored into a fallback file, from which the data will be
     * read and committed back to the DB as soon as it detects that the DB
     * connection is up again
     * 
     * @param data
     *            List of IFallback objects to be written to the DB. It is up to
     *            the caller to ensure that all objects in the list are of type
     *            IFallback. No online type checking.
     */
    public final void storeData(final List data) {
        if (log(data) && !fallbackManager.isFallbackFileEmpty()) {
            if (!this.dataRecovery.isRunning()) {
                dataRecovery.setPersistenceManager(this);
                new Thread(dataRecovery).start();
            }
        }
    }

    /**
     * Write an IFallback object to the short-term log DB table. It relies on
     * the fallback mechanism for those cases in which the DB becomes
     * unavailable. If there is data temporarily stored in the fallback file it
     * will start a new thread which will independently deal with the task of
     * reading back the data from the file to the DB.
     * 
     * @param object
     *            IFallback object to be stored
     */
    public final void storeData(final IFallback object) {
        if (log(object) && !fallbackManager.isFallbackFileEmpty()) {
            if (!this.dataRecovery.isRunning()) {
              dataRecovery.setPersistenceManager(this);  
                new Thread(dataRecovery).start();
            }
        }
    }

    /**
     * Writes a list of IFallback objects to the shortermlog database table. If
     * the DB becomes unavailable during the logging process the IFallback
     * object will be temporarily stored into a fallback log file, which will
     * avoid losing that data
     * 
     * @param data
     *            The list of IFallback objects to be logged in the shorttermlog
     *            database
     * @return A boolean showing whether the database is up (true) or down
     *         (false)
     */
    private boolean log(final List data) {

        // size of the collection
        int size;
        // indicates whether the DB connection is up or not
        boolean connectionDown = false;
        int commitedTags;
        IFallback fallbackObj;

        if (data == null) {
            LOG.warn("log([Collection]) : called with a null collection.");
            return connectionDown;
        }
        size = data.size();

        // If the collection is empty, print a warning and return
        if (size == 0) {
            LOG.warn("log([Collection]) : called with an empty collection.");
            return connectionDown;
        }

        // If there are tags to be logged, print the size of the collection for
        // debugging
        if (LOG.isDebugEnabled()) {
            StringBuffer str = new StringBuffer("log([Collection]) : ");
            str.append(size);
            str.append(" data to be logged.");
            LOG.debug(str);
        }

        try {
            dbHandler.storeData(data);            
            LOG.info("log([Collection]) : " + size + " tags have been successfuly logged into the DB");
            alarmSender.dbUnavailable(DOWN, null, dbHandler.getDBInfo());
        } catch (IDBPersistenceException e) {
            connectionDown = true;           
            commitedTags = e.getCommited();
            if (size > commitedTags) {
                List temp = data.subList(commitedTags, size);
                synchronized (fallbackManager.getFallbackFileController()) {
                    if (!writeToFallback(temp)) {
                        for (int i = 0; i < temp.size(); i++) {
                            fallbackObj = (IFallback) temp.get(i);
                            FALLBACK_LOG.info(fallbackObj.toString());
                        }
                    }
                }
            }
            if (LOG.isDebugEnabled())
              LOG.debug("log([Collection]) : Sending an alarm for warning about the DB problems " + e.getMessage());
            // Send an ALARM to warn that the DB is down
            alarmSender.dbUnavailable(ACTIVATED, e.getMessage(), dbHandler.getDBInfo());
        }        
        return !connectionDown;
    }

    /**
     * Writes an IFallback object to the shortermlog database table. If the DB
     * becomes unavailable during the logging process the IFallback object will
     * be temporarily stored into a fallback log file, which will avoid losing
     * that data
     * 
     * @param object
     *            The IFallback object to be stored in the DB
     * @return A boolean value indicating whether the DB is available (true) or
     *         unavailable (false)
     */
    private boolean log(final IFallback object) {
        boolean dbConnectionUp = true;

        // Check whether the CommandTagHandle is not null.
        if (object == null) {
            LOG.warn("log([IFallback]) : not logging null object.");
            return dbConnectionUp;
        }

        try {
            dbHandler.storeData(object);
            alarmSender.dbUnavailable(DOWN, null, dbHandler.getDBInfo());
        } catch (SQLException e) {
            dbConnectionUp = false;            
            synchronized (fallbackManager.getFallbackFileController()) {
                ArrayList temp = new ArrayList();
                temp.add(object);
                if (!writeToFallback(temp)) {
                    FALLBACK_LOG.debug(temp.get(0).toString());
                }
            }
            LOG
            .error("log([IFallback]) - The IFallback object with id " + object.getId() + " could not be committed into the database due to "
                    + e.getMessage());
            alarmSender.dbUnavailable(ACTIVATED, e.getMessage(), dbHandler.getDBInfo());
        }
        return dbConnectionUp;
    }

    /**
     * Stores a collection of IFallback objects into the fallback file. In case
     * the disk space is close to get full not data will be written to the file
     * and a notification will be sent to warn about the problem. On the same
     * way, a notification will be sent when the file to which the data is
     * trying to be logged is not accessible
     * 
     * @param temp
     *            List of IFallback objects
     * @return A boolean indicating whether data should be logged to log4j
     *         instead (false) or not (true)
     */
    private boolean writeToFallback(final List temp) {
        boolean notLog4j = true;

        try {
            // We check the discSpace after having writing something in the
            // fallback files
            boolean checked = fallbackManager.isDiskSpaceCheckDone(this.minFreeDiscSpace);

            // There is still free disc space, so we TERMINATE the alarm if it
            // has been sent and reactivate the email sender
            if (checked) {
                alarmSender.diskFull(DOWN, fallbackManager.getFallbackFileController()
                        .getDataFile().getParentFile().getAbsolutePath());
            }
            // Start writing to the fallback file since there is still free disc
            // space
            try {
                FALLBACK_LOG.info("writeToFallback([Collection]) - Writing " + temp.size()
                        + " dataTags to the fallback file" + fallbackManager.getFallbackFileController().getDataFile().getAbsolutePath());
                fallbackManager.fallback(temp);
                // Check if the writing problem with the fallback file has been
                // fixed
                alarmSender.fileNotReachable(DOWN, fallbackManager.getFallbackFileController()
                        .getDataFile());

            } catch (DataFallbackException ex) {
                // Problems writing in the file
                FALLBACK_LOG
                        .error("writeToFallback([Collection]) - An error ocurred while trying to write the datatags in the logfile "
                                + ex.getMessage());
                FALLBACK_LOG
                        .error("writeToFallback([Collection]) - DataTags could not be written to the log file and therefore they won't be stored in the ShorttermLog");
                // Send an alarm warning the tim admins that not commited
                // datatags to the DB cannot either being logged into the
                // fallback log file
                alarmSender.fileNotReachable(ACTIVATED, fallbackManager
                        .getFallbackFileController().getDataFile());
                // Since there was a problem with the fallback mechanism (and
                // there is enough disk space),
                // we want to log the dataTags into one of the log4j log files
                notLog4j = false;
            }

        } catch (SystemDiskSpaceException e) {
            // Minimum free disc space reached
            alarmSender.diskFull(ACTIVATED, fallbackManager.getFallbackFileController()
                    .getDataFile().getParentFile().getAbsolutePath());
        } catch (Exception e) {
            FALLBACK_LOG.error("RuntimeException : " + e.getMessage());
        }
        return notLog4j;
    }

    /**
     * This method will be called by the client when this last one wants to
     * liberate its resources before it is destroyed. It takes care of stopping
     * the thread in charge of reading back from the fallback file and removing
     * the reference from the thread to this current object, so the garbage
     * collector identifies it as isolated.
     */
    public final void finalize() {

        if (LOG.isDebugEnabled()) {
            LOG
                    .debug("finalize() - Removing the references to the fallbackManager and the dataRecoveryThread");
        }
        // Stop the thread in case it was still running and delete the reference to it,
        // so its memory can be released by the garbage collector
        dataRecovery.stop();
        dataRecovery = null;
        fallbackManager.finalize();

    }

}
