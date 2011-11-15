package cern.c2mon.statistics.generator.charts;

import java.sql.SQLException;
import java.util.List;

import cern.c2mon.statistics.generator.SqlMapper;
import cern.c2mon.statistics.generator.values.IChartCollectionValue;

public class JFreeBarChartCollection extends JFreeWebChartCollection {
    
    /**
     * Set the chart class at initialisation.
     */
    public JFreeBarChartCollection() {
        super();
        chartClass = JFreeBarChart.class;
    }
    
    
    public List<IChartCollectionValue> getValuesFromDatabase(String tableName) throws SQLException {
        //retrieve the chart values from the database
        return SqlMapper.getBarChartCollectionData(tableName);
    }
}
