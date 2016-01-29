package cern.c2mon.web.manager.serialization;

import cern.c2mon.web.manager.statistics.daqlog.charts.JFreePieChart;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;

import java.io.IOException;

public class PieChartSerializer extends JsonSerializer<JFreePieChart> {

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
   */
  @Override
  public void serialize(JFreePieChart chart, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
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