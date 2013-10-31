package cern.c2mon.daq.common;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 * This class is used for formating the logging output of equipment message handlers. It overrides the standard log4j's
 * Logger class functionality
 */
public class EquipmentLogger {
    /**
     * The log4j logger.
     */
    private Logger logger;
    /**
     * Combination of equipment id and name.
     */
    private String eqNameAndId;
    /**
     * The String pushed into NDC.
     */
    private String ndcPush;

    /**
     * Creates a new equipment logger.
     * 
     * @param eqNameAndId Combination of equipment id and name.
     * @param className The name of the logger.
     * @param handlerClassName The name of the handler class.
     */
    public EquipmentLogger(final String eqNameAndId, final String className, final String handlerClassName) {
        if (className != null && !className.equals("")) {
            logger = Logger.getLogger(eqNameAndId + "." + className);
        } else {
            logger = Logger.getLogger(eqNameAndId);
        }
        ndcPush = handlerClassName;
        this.eqNameAndId = eqNameAndId;
    }

    /**
     * Logs the object o at the provided level and a throwable of the caller.
     * 
     * @param level The level to log.
     * @param o The object to log.
     * @param t The throwable to log.
     */
    private void log(final Level level, final Object o, final Throwable t) {
        if (logger.isEnabledFor(level)) {
            NDC.push(ndcPush);
            logger.log(level, o, t);
            NDC.pop();
        }
    }

    /**
     * Logs the object o with the provided level.
     * 
     * @param level The level to log the object.
     * @param o The object to log.
     */
    private void log(final Level level, final Object o) {
        if (logger.isEnabledFor(level)) {
            NDC.push(ndcPush);
            logger.log(level, o);
            NDC.pop();
        }
    }

    /**
     * This method logs the ERROR message
     * 
     * @param o Logs the object with the error level.
     */
    public final void error(final Object o) {
        log(Level.ERROR, o);
    }

    /**
     * This method logs the ERROR message
     * 
     * @param o Logs the object with the error level.
     * @param t throwable to log.
     */
    public final void error(final Object o, final Throwable t) {
        log(Level.ERROR, o, t);
    }

    /**
     * This method logs the TRACE message
     * 
     * @param o Logs the object with the debug level.
     */
    public final void trace(final Object o) {
        log(Level.TRACE, o);
    }

    /**
     * This method logs the TRACE message
     * 
     * @param o Logs the object with the debug level.
     * @param t throwable to log.
     */
    public final void trace(final Object o, final Throwable t) {
        log(Level.TRACE, o, t);
    }

    /**
     * This method logs the DEBUG message
     * 
     * @param o Logs the object with the debug level.
     */
    public final void debug(final Object o) {
        log(Level.DEBUG, o);
    }

    /**
     * This method logs the DEBUG message
     * 
     * @param o Logs the object with the debug level.
     * @param t throwable to log.
     */
    public final void debug(final Object o, final Throwable t) {
        log(Level.DEBUG, o, t);
    }

    /**
     * This method logs the WARNING message
     * 
     * @param o Logs the object with the warning level.
     */
    public final void warn(final Object o) {
        log(Level.WARN, o);
    }

    /**
     * This method logs the WARNING message
     * 
     * @param o Logs the object with the warning level.
     * @param t throwable to log.
     */
    public final void warn(final Object o, final Throwable t) {
        log(Level.WARN, o, t);
    }

    /**
     * This method logs the INFO message
     * 
     * @param o Logs the object with the info level.
     */
    public final void info(final Object o) {
        log(Level.INFO, o);
    }

    /**
     * This method logs the INFO message
     * 
     * @param o Logs the object with the info level.
     * @param t throwable to log.
     */
    public final void info(final Object o, final Throwable t) {
        log(Level.INFO, o, t);
    }

    /**
     * This method logs the FATAL message
     * 
     * @param o Logs the object with the fatal level.
     */
    public final void fatal(final Object o) {
        log(Level.FATAL, o);
    }

    /**
     * This method logs the FATAL message
     * 
     * @param o Logs the object with the fatal level.
     * @param t throwable to log.
     */
    public final void fatal(final Object o, final Throwable t) {
        log(Level.FATAL, o, t);
    }

    /**
     * The finalizer
     */
    public void finalize() {
        NDC.remove();
    }

    /**
     * Returns true if trace is enabled.
     * 
     * @return True if trace is enabled else false.
     */
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    /**
     * Returns true if debug is enabled.
     * 
     * @return True if debug is enabled else false.
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * Returns true if info is enabled.
     * 
     * @return True if info is enabled else false.
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * Adds an appender to this logger.
     * 
     * @param appender The appender to add.
     */
    public void addAppender(final Appender appender) {
        logger.addAppender(appender);
    }

    /**
     * Sets the additivity of this logger.
     * 
     * @param additivity The additivity to set.
     */
    public void setAdditivity(final boolean additivity) {
        logger.setAdditivity(additivity);
    }

    /**
     * Sets the log level for this logger.
     * 
     * @param level The level to set.
     */
    public void setLevel(final Level level) {
        logger.setLevel(level);
    }

    /**
     * Returns the equipment name and id.
     * 
     * @return Name and id of the equipment.
     */
    public String getEquipmentNameAndId() {
        return eqNameAndId;
    }
}
