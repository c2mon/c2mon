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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    SimpleModule module = new SimpleModule();

    module.addSerializer(JFreeBarChart.class, new BarChartSerializer());
    module.addSerializer(JFreeStackedBarChart.class, new StackedBarChartSerializer());
    module.addSerializer(JFreePieChart.class, new PieChartSerializer());

    // Enable pretty printing
    enable(SerializationFeature.INDENT_OUTPUT);

    registerModule(module);
  }
}
