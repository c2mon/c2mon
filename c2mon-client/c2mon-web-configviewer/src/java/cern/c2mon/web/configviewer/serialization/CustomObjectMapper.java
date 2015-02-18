package cern.c2mon.web.configviewer.serialization;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.stereotype.Component;

import cern.c2mon.statistics.generator.charts.JFreeBarChart;
import cern.c2mon.statistics.generator.charts.JFreePieChart;
import cern.c2mon.statistics.generator.charts.JFreeStackedBarChart;

/**
 * This class extends the default Jackson {@link ObjectMapper} in order to allow
 * custom serialisation of resources.
 *
 * @author Justin Lewis Salmon
 */
@Component
public class CustomObjectMapper extends ObjectMapper {

  /**
   * Constructor.
   */
  public CustomObjectMapper() {
    super();
    CustomSerializerFactory factory = new CustomSerializerFactory();

    // Add custom serializers here
    factory.addSpecificMapping(JFreeBarChart.class, new BarChartSerializer());
    factory.addSpecificMapping(JFreeStackedBarChart.class, new StackedBarChartSerializer());
    factory.addSpecificMapping(JFreePieChart.class, new PieChartSerializer());

    this.setSerializerFactory(factory);

    // Enable pretty printing
    enable(Feature.INDENT_OUTPUT);
  }
}
