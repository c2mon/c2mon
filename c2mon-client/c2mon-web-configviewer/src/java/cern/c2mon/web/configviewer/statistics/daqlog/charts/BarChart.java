package cern.c2mon.web.configviewer.statistics.daqlog.charts;

import java.util.List;

/**
 * This class is a custom utility bean, used to take data representing a bar
 * chart and wrap it up in a format that can easily be serialised to JSON and
 * subsequently used on a web page.
 *
 * @author Justin Lewis Salmon
 *
 */
public class BarChart {

  /**
   * The chart title.
   */
  private String title;

  /**
   * The chart subtitle.
   */
  private String subtitle;

  /**
   * A description of the chart.
   */
  private String description;

  /**
   * The x-axis component of the chart.
   */
  private XAxis xAxis;

  /**
   * The y-axis component of the chart.
   */
  private YAxis yAxis;

  /**
   * Constructor.
   *
   * @param title the chart title
   * @param subtitle the chart subtitle
   * @param description a description of the chart
   * @param xLabel the x-axis label
   * @param xData the x-axis data
   * @param yLabel the y-axis label
   * @param yDatathe y-axis data
   */
  public BarChart(final String title, final String subtitle, final String description, final String xLabel, final List<String> xData, final String yLabel,
      final List<Double> yData) {
    this.title = title;
    this.subtitle = subtitle;
    this.description = description;
    this.xAxis = new XAxis(xLabel, xData);
    this.yAxis = new YAxis(yLabel, yData);
  }

  /**
   * Retrieve the title of the chart.
   *
   * @return the chart title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Retrieve the subtitle of the chart.
   *
   * @return the chart subtitle
   */
  public String getSubtitle() {
    return subtitle;
  }

  /**
   * Retrieve the description of the chart.
   *
   * @return the chart description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Retrieve the x-axis of the chart.
   *
   * @return the x-axis
   */
  public XAxis getXAxis() {
    return xAxis;
  }

  /**
   * Retrieve the y- axis of the chart.
   *
   * @return the y-axis
   */
  public YAxis getYAxis() {
    return yAxis;
  }

  /**
   * Simple class representing the x-axis component of a bar chart.
   *
   * @author Justin Lewis Salmon
   */
  public class XAxis {

    /**
     * The axis label
     */
    private String label;

    /**
     * The axis data points
     */
    private List<String> data;

    /**
     * Constructor.
     *
     * @param label the axis label
     * @param data the axis data
     */
    public XAxis(String label, List<String> data) {
      this.label = label;
      this.data = data;
    }

    /**
     * Retrieve the axis label.
     *
     * @return the axis label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Retrieve the axis data.
     *
     * @return the axis data
     */
    public List<String> getData() {
      return data;
    }
  }

  /**
   * Simple class representing the y-axis component of a bar chart.
   *
   * @author Justin Lewis Salmon
   */
  public class YAxis {

    /**
     * The axis label
     */
    private String label;

    /**
     * The axis data points
     */
    private List<Double> data;

    /**
     * Constructor.
     *
     * @param label the axis label
     * @param data the axis data
     */
    public YAxis(String label, List<Double> data) {
      this.label = label;
      this.data = data;
    }

    /**
     * Retrieve the axis label.
     *
     * @return the axis label
     */
    public String getLabel() {
      return label;
    }

    /**
     * Retrieve the axis data.
     *
     * @return the axis data
     */
    public List<Double> getData() {
      return data;
    }
  }
}
