package cern.c2mon.statistics.generator.values;

/**
 * This class represents a value to be added to a bar chart.
 * 
 * @author mbrightw
 *
 */
public class BarChartValue implements IChartValue {
    
    /**
     * The value of the statistic.
     */
    private double value;
    
    /**
     * The series key.
     */
    private String seriesKey;

    /**
     * The category key.
     */
    private String categoryKey;

    /**
     * Default constructor (used by extensions).
     */
    public BarChartValue() {
        
    }
    
    /**
     * Constructor.
     * 
     * @param pValue
     * @param pSeriesKey
     * @param pCategoryKey
     */
    public BarChartValue(double pValue, String pSeriesKey, String pCategoryKey) {
        setCategoryKey(pCategoryKey);
        setSeriesKey(pSeriesKey);
        setValue(pValue);
    }
    
    /**
     * @return the value
     */
    public final double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public final void setValue(final double value) {
        this.value = value;
    }

    /**
     * @return the seriesKey
     */
    public final String getSeriesKey() {
        return seriesKey;
    }

    /**
     * @param seriesKey the seriesKey to set
     */
    public final void setSeriesKey(final String seriesKey) {
        this.seriesKey = seriesKey;
    }

    /**
     * @return the categoryKey
     */
    public final String getCategoryKey() {
        return categoryKey;
    }

    /**
     * @param categoryKey the categoryKey to set
     */
    public final void setCategoryKey(final String categoryKey) {
        this.categoryKey = categoryKey;
    }
    
    
}
