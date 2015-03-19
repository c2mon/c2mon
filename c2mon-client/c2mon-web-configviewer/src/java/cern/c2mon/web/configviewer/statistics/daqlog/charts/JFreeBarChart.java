package cern.c2mon.web.configviewer.statistics.daqlog.charts;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Element;

import cern.c2mon.web.configviewer.statistics.daqlog.C2MONChartStyles;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.GraphConfigException;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.InvalidTableNameException;
import cern.c2mon.web.configviewer.statistics.daqlog.styles.BarChartStyle;
import cern.c2mon.web.configviewer.statistics.daqlog.values.BarChartValue;
import cern.c2mon.web.configviewer.statistics.daqlog.values.IChartValue;

public class JFreeBarChart extends JFreeWebChart {

  protected String title;

  protected String domainAxis;

  protected String rangeAxis;

  protected String orientation;

  protected PlotOrientation plotOrientation;

  protected String tableName;

  protected JFreeBarChart() {

  }

  /**
   * Configures the JFreeBarChart.
   *
   * Manages both simple bar charts and stacked bar charts.
   *
   * @param chartElement the JFreeBarChart XML element
   * @param C2MONChartStyles reference to all the styles used by the application
   * @throws GraphConfigException problem recognizing graph configuration
   *           parameters
   * @throws SQLException problem in getting data from database
   * @throws InvalidTableNameException the table name does not satisfy the
   *           required [a-zA-Z0-9_]+ format
   */
  @Override
  public void configure(Element chartElement, C2MONChartStyles C2MONChartStyles) throws GraphConfigException, SQLException, InvalidTableNameException {

    logger.info("generating bar chart with id " + chartId + " from XML");

    // set the remaining BarChart-specific object attributes from the XML file
    getParametersFromXML(chartElement);

    // get the dataset
    DefaultCategoryDataset dataset = getBarChartData(tableName);
    jFreeChart = createChart(dataset, C2MONChartStyles.getBarChartStyle());
  }

  /**
   * Configures the JFreeBarChart when the chart is the member of a collection.
   *
   * @throws InvalidTableNameException
   */
  @Override
  public void configureMember(String memberName,
                              WebChartCollection webChartCollection,
                              Element chartElement,
                              C2MONChartStyles C2MONChartStyles,
                              List<IChartValue> valueList) throws GraphConfigException, SQLException, InvalidTableNameException {
    setGlobalParameters(memberName, webChartCollection);
    getParametersFromXML(memberName, chartElement);
    jFreeChart = createChart(toDataset(valueList), C2MONChartStyles.getBarChartStyle());
  }

  /**
   * Method used to finalize the configuration (this part is overridden for
   * stacked bar charts). Creates a JFreeChart.
   *
   * @param dataset the JFree dataset
   * @param barChartStyle the bar chart style element
   * @return the generated JFreeChart
   * @throws SQLException
   */
  protected JFreeChart createChart(Object datasetObject, BarChartStyle barChartStyle) throws SQLException {
    if (logger.isDebugEnabled()) {
      logger.debug("entering createChart()...");
    }
    DefaultCategoryDataset dataset = (DefaultCategoryDataset) datasetObject;
    // create the bar chart
    JFreeChart jFreeChart = ChartFactory.createBarChart(title, // chart title
        domainAxis, // domain axis label
        rangeAxis, // range axis label
        dataset, // data
        plotOrientation, // orientation
        true, // include legend
        true, // tooltips?
        false // URLs?
        );

    // apply the TIM bar chart style to the chart
    barChartStyle.applyTo(jFreeChart, dataset);
    if (logger.isDebugEnabled()) {
      logger.debug("...leaving createChart()");
    }
    return jFreeChart;
  }

  /**
   * Connects to the database and retrieves the bar chart data in the table (for
   * a simple bar chart).
   *
   * @param tableName the DB table to retrieve the data from
   * @return the JFreeChart dataset, populated with the data
   * @throws SQLException error in retrieving data from database
   */
  private DefaultCategoryDataset getBarChartData(final String tableName) {

    // retrieve the chart values from the database
    try {
      List chartValues = mapper.getBarChartData(tableName);
      return (DefaultCategoryDataset) toDataset(chartValues);
    } catch (Exception e) {
      logger.error("Unexpected exception caught while generating chart (check your chart config)", e);
    }

    return null;
  }

  /**
   * Converts a list of BarChartValues to a JFree dataset object.
   *
   * @param valueList the list of values
   * @return the JFree dataset object
   */
  protected Object toDataset(final List<IChartValue> valueList) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    Iterator<IChartValue> it = valueList.iterator();
    BarChartValue currentValue;

    // add the values to the dataset one by one
    while (it.hasNext()) {
      currentValue = (BarChartValue) it.next();
      dataset.addValue(currentValue.getValue(), currentValue.getSeriesKey(), currentValue.getCategoryKey());
    }
    return dataset;
  }

  /**
   * Gets the BarChart specific parameters from the XML file and stores them in
   * the object attributes.
   *
   * @param chartElement the XML "chart" or "chart-group" element
   * @throws GraphConfigException problem in XML file
   * @throws InvalidTableNameException
   */
  void getParametersFromXML(final Element chartElement) throws GraphConfigException, InvalidTableNameException {
    try {
      title = chartElement.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
      domainAxis = chartElement.getElementsByTagName("domain-name").item(0).getFirstChild().getNodeValue();
      rangeAxis = chartElement.getElementsByTagName("range-name").item(0).getFirstChild().getNodeValue();
      orientation = chartElement.getElementsByTagName("orientation").item(0).getFirstChild().getNodeValue();
      if (orientation.equalsIgnoreCase("vertical")) {
        plotOrientation = PlotOrientation.VERTICAL;
      } else if (orientation.equalsIgnoreCase("horizontal")) {
        plotOrientation = PlotOrientation.HORIZONTAL;
      } else {
        throw new GraphConfigException();
      }
      tableName = chartElement.getElementsByTagName("database-table").item(0).getFirstChild().getNodeValue();
      // if the table name is not alphanumeric (together with _), then reject
      if (!tableName.matches(new String("[a-zA-Z0-9_]+"))) {
        throw new InvalidTableNameException();
      }
    } catch (NullPointerException ex) {
      logger.fatal("Nullpointer exception caught when retrieving XML data - check no fields are missing!");
      ex.printStackTrace();
      throw ex;
    }
  }

  void getParametersFromXML(String memberName, Element chartElement) throws GraphConfigException, InvalidTableNameException {
    getParametersFromXML(chartElement);
    subMemberName(memberName);
  }

  /**
   * Substitutes the member name for JFreeBarChart-specific attributes.
   *
   * @param memberName the name of the member chart
   */
  @Override
  public void chartSubMemberName(String memberName) {
    title = title.replace("$CHART_NAME$", memberName);
    domainAxis = domainAxis.replace("$CHART_NAME$", memberName);
    rangeAxis = rangeAxis.replace("$CHART_NAME$", memberName);
  }

  public String getTitle() {
    return title;
  }

  public String getDomainAxis() {
    return domainAxis;
  }

  public String getRangeAxis() {
    return rangeAxis;
  }

  protected String getOrientation() {
    return orientation;
  }

  protected PlotOrientation getPlotOrientation() {
    return plotOrientation;
  }

  protected String getTableName() {
    return tableName;
  }
}
