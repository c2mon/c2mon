package cern.c2mon.statistics.generator.values;

public class PieChartCollectionValue extends PieChartValue implements IChartCollectionValue {
    
    /**
     * The name of the chart this value is linked with.
     */
    private String memberName;
    
    /**
     * Implementation of the required interface method. Returns the underlying BarChartValue.
     */
    public PieChartValue returnChartValue() {
        return new PieChartValue(getValue(), getKey());
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
