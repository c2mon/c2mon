/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.configviewer.serialization;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.stereotype.Component;

import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreeBarChart;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreePieChart;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreeStackedBarChart;

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
