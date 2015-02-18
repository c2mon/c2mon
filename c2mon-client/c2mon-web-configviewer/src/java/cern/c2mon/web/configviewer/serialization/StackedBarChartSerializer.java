package cern.c2mon.web.configviewer.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.jfree.data.category.CategoryDataset;

import cern.c2mon.statistics.generator.charts.JFreeStackedBarChart;

public class StackedBarChartSerializer extends SerializerBase<JFreeStackedBarChart> {

  /**
   * Constructor.
   */
  public StackedBarChartSerializer() {
    super(JFreeStackedBarChart.class);
  }

  /**
   * Example JSON output:
   *
   * <p>
   * {
   *   "title" : "Graph Title",
   *   "description" : "Graph Description"
   *   "xAxis": {
   *     "label" : "Category of thing",
   *     "data" : ["1", "2", "3", "4"]
   *   },
   *   "yAxis": {
   *     "label" : "Number of things",
   *     "data" : ["41", "56", "34", "44"]
   *   }
   * }
   * </p>
   *
   * @see org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object
   *      , org.codehaus.jackson.JsonGenerator,
   *      org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(JFreeStackedBarChart chart, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();

    generator.writeStringField("title", chart.getTitle());
    generator.writeStringField("subtitle", chart.getParagraphTitle());
    generator.writeStringField("description", chart.getGraphDescription());

    generator.writeFieldName("xaxis");
    generator.writeStartObject();
    generator.writeStringField("label", chart.getDomainAxis());
    generator.writeFieldName("categories");
    generator.writeStartArray();
    for (Object object : chart.getJFreeChart().getCategoryPlot().getDataset().getColumnKeys()) {
      generator.writeString(object.toString());
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeFieldName("yaxis");
    generator.writeStartObject();
    generator.writeStringField("label", chart.getRangeAxis());

    generator.writeFieldName("series");
    generator.writeStartArray();
    CategoryDataset dataset = chart.getJFreeChart().getCategoryPlot().getDataset();
    for (int r = 0; r < dataset.getRowCount(); r++) {
      generator.writeStartObject();
      generator.writeStringField("name", dataset.getRowKey(r).toString());

      generator.writeFieldName("data");
      generator.writeStartArray();
      for (int c = 0; c < dataset.getColumnCount(); c++) {
        Number number = dataset.getValue(r, c);
        if (number != null) {
          generator.writeNumber(number.doubleValue());
        } else {
          generator.writeNumber(0.0);
        }
      }
      generator.writeEndArray();
      generator.writeEndObject();
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeEndObject();
  }
}