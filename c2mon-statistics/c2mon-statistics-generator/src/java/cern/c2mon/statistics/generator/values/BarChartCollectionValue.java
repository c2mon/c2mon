package cern.c2mon.statistics.generator.values;


/**
 * Class used to gather rows from the collection-of-barcharts
 * tables.
 * 
 * @author mbrightw
 *
 */
public class BarChartCollectionValue extends BarChartValue implements IChartCollectionValue {

    /**
     * The name of the chart this value is linked with.
     */
    private String memberName;
    
    /**
     * Implementation of the required interface method. Returns the underlying BarChartValue.
     */
    public BarChartValue returnChartValue() {
        return new BarChartValue(getValue(), getSeriesKey(), getCategoryKey());
    }
    
    /**
     * @return the memberName
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * @param memberName the memberName to set
     */
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    

   
    
}
