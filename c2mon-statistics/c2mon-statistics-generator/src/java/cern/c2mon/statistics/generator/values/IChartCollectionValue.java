package cern.c2mon.statistics.generator.values;

import java.sql.SQLException;
import java.util.List;

public interface IChartCollectionValue {

    public String getMemberName();
    
    /**
     * Returns the value object for the underlying chart
     * (i.e. without the member name).
     * @return the chart value object
     */
    public IChartValue returnChartValue();
    
  
}
