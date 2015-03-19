package cern.c2mon.web.configviewer.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreePieChart;

public class PieChartSerializer extends SerializerBase<JFreePieChart> {

  /**
   * Constructor.
   */
  public PieChartSerializer() {
    super(JFreePieChart.class);
  }

  /**
   * Example JSON output:
   *
   * <p>
   * {
   *   "title" : "Graph Title",
   *   "description" : "Graph Description"
   *   "series": {
   *     type: 'pie',
   *     name: 'Browser share',
   *     data: [
   *       ['Firefox', 45.0],
   *       ['IE', 26.8],
   *       ['Safari', 8.5],
   *       ['Opera', 6.2],
   *       ['Others', 0.7]
   *     ]
   *   }
   * }
   * </p>
   *
   * @see org.codehaus.jackson.map.ser.std.SerializerBase#serialize(java.lang.Object
   *      , org.codehaus.jackson.JsonGenerator,
   *      org.codehaus.jackson.map.SerializerProvider)
   */
  @Override
  public void serialize(JFreePieChart chart, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonGenerationException {
    generator.writeStartObject();

    generator.writeStringField("title", chart.getTitle());
    generator.writeStringField("subtitle", chart.getParagraphTitle());
    generator.writeStringField("description", chart.getGraphDescription());

    generator.writeFieldName("series");
    generator.writeStartObject();
    generator.writeStringField("type", "pie");
    generator.writeStringField("name", chart.getTitle());
    generator.writeFieldName("data");
    generator.writeStartArray();
    PieDataset dataset = ((PiePlot) chart.getJFreeChart().getPlot()).getDataset();
    for (int i = 0; i < dataset.getItemCount(); i++) {
      generator.writeStartArray();
      generator.writeString(dataset.getKey(i).toString());
      generator.writeNumber(dataset.getValue(i).doubleValue());
      generator.writeEndArray();
    }
    generator.writeEndArray();
    generator.writeEndObject();

    generator.writeEndObject();
  }
}