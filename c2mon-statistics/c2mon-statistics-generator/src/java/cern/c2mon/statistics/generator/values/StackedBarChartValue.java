package cern.c2mon.statistics.generator.values;


/**
 * Represents a single value in a stacked bar chart.
 * 
 * @author mbrightw
 *
 */
public class StackedBarChartValue extends BarChartValue implements IChartValue {
    
    /**
     * The stacked bar group that the value belongs to.
     */
    private String group;

    /**
     * Default public constructor for subclasses.
     */
    public StackedBarChartValue() {
    }
    
    /**
     * Constructor.
     * 
     * @param pValue
     * @param pSeriesKey
     * @param pCategoryKey
     * @param group
     */
    public StackedBarChartValue(double pValue, String pSeriesKey, String pCategoryKey, String group) {
        setCategoryKey(pCategoryKey);
        setSeriesKey(pSeriesKey);
        setValue(pValue);
        setGroup(group);
    }
    
    /**
     * @return the group
     */
    public final String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public final void setGroup(final String group) {
        this.group = group;
    }
    
    

}
