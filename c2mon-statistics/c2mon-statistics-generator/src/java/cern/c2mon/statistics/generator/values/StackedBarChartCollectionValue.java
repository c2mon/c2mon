package cern.c2mon.statistics.generator.values;

/**
 * Class used for collecting the database values for collection StackedBarChart's (ibatis).
 * @author mbrightw
 *
 */
public class StackedBarChartCollectionValue extends StackedBarChartValue implements IChartCollectionValue {
    
    /**
     * The name of the chart this value is linked with.
     */
    private String memberName;
    
    /**
     * Implementation of the required interface method. Returns the underlying BarChartValue.
     * @return the value for the underlying chart
     */
    public final StackedBarChartValue returnChartValue() {
        return new StackedBarChartValue(getValue(), getSeriesKey(), getCategoryKey(), getGroup());
    }
    
    /**
     * @return the memberName
     */
    public final String getMemberName() {
        return memberName;
    }

    /**
     * @param memberName the memberName to set
     */
    public final void setMemberName(final String memberName) {
        this.memberName = memberName;
    }
}

