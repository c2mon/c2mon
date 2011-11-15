package cern.c2mon.statistics.generator.values;

/**
 * Represents a value (or segment) in a pie chart.
 * 
 * @author mbrightw
 *
 */
public class PieChartValue implements IChartValue {
    
    /**
     * The value of the pie segment to represent.
     */
    private double value;
    
    /**
     * The key of the pie segment.
     */
    private String key;

    /**
     * Default constructor.
     */
    public PieChartValue() {
        
    }
    /**
     * Constructor.
     * 
     * @param value the value of the statistic
     * @param key the pie chart key
     */
    public PieChartValue(double value, String key) {
        setValue(value);       
        this.key = key;
    }

    /**
     * Getter method for value attribute.
     * 
     * @return the value
     */
    public final double getValue() {
        return value;
    }

    /**
     * Setter method for value attribute.
     * 
     * @param value the value to set
     */
    public final void setValue(final double value) {
        this.value = value;
    }

    /**
     * Getter method for key attribute.
     * 
     * @return the key
     */
    public final String getKey() {
        return key;
    }

    /**
     * Setter method for key attribute.
     * 
     * @param key the key to set
     */
    public final void setKey(final String key) {
        this.key = key;
    }
}
