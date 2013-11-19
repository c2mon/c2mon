package cern.c2mon.shared.daq.config;

import java.util.ArrayList;
import java.util.List;
/**
 * A ConfigurationReport hold a list of change reports which represent
 * the result of every provided single change.
 * 
 * @author Andreas Lang
 *
 */
public class ConfigurationChangeEventReport {
    /**
     * If the whole configuration fails this should be set.
     */
    private String error;
    /**
     * The default initial capacity.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    /**
     * The list of change reports.
     */
    private final List<ChangeReport> changeReports;
    
    /**
     * Creates a configuration report with a default initial capacity
     * of change reports.
     */
    public ConfigurationChangeEventReport() {
        this(DEFAULT_INITIAL_CAPACITY);
    }
    
    /**
     * This constructor is a kind copy constructor it may be used from
     * subclasses to create an object of this class for serialization to
     * the DAQ core.
     * 
     * @param configurationChangeEventReport The change object to copy.
     */
    public ConfigurationChangeEventReport(final ConfigurationChangeEventReport configurationChangeEventReport) {
        this(configurationChangeEventReport.getChangeReports().size());
        setError(configurationChangeEventReport.getError());
        for (ChangeReport changeReport : configurationChangeEventReport.getChangeReports()) {
            getChangeReports().add(new ChangeReport(changeReport));
        }
    }
    
    /**
     * Creates a new configuration report with a provided initial capacity
     * of change reports.
     * 
     * @param initialCapacity The initial capacity to be used.
     */
    public ConfigurationChangeEventReport(final int initialCapacity) {
        changeReports = new ArrayList<ChangeReport>(initialCapacity);
    }

    /**
     * Live list of the change reports. Is never null. Changes are reflected in
     * the reports list.
     * @return the changeReports
     */
    public List<ChangeReport> getChangeReports() {
        return changeReports;
    }
    
    /**
     * Shortcut method to add a change report.
     * @param changeReport The change report to add.
     */
    public void appendChangeReport(final ChangeReport changeReport) {
        changeReports.add(changeReport);
    }
    
    /**
     * Shortcut method to remove a change report.
     * @param changeReport The change report to remove. 
     * If not present nothing will happen.
     */
    public void removeChangeReport(final ChangeReport changeReport) {
        changeReports.remove(changeReport);
    }
    
    /**
     * Shortcut method to remove all change reports from this object.
     */
    public void clear() {
        changeReports.clear();
    }

    /**
     * @return the error
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message. If this is set a general error occurred
     * and no change reports only the error is reported. The change reports get
     * cleared from the object.
     * 
     * @param error the error to set
     */
    public void setError(final String error) {
        clear();
        this.error = error;
    }
    
    /**
     * Creates a readable String for the change event report.
     * @return A readable String for this change event report.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("\nConfigurationChangeEventReport:\n");
        if (error != null) {
            result.append("Error:\n");
            result.append(error);
        }
        else {
            for (ChangeReport changeReport : changeReports) {
                result.append(changeReport.toString());
            }
        }
        return result.toString();
    }
}
