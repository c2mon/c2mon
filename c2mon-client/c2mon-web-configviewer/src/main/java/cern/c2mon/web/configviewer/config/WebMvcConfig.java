/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.web.configviewer.config;

import cern.c2mon.web.configviewer.controller.HandlerInterceptor;
import cern.c2mon.web.configviewer.serialization.BarChartSerializer;
import cern.c2mon.web.configviewer.serialization.PieChartSerializer;
import cern.c2mon.web.configviewer.serialization.StackedBarChartSerializer;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreeBarChart;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreePieChart;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.JFreeStackedBarChart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new HandlerInterceptor());
  }

  @Bean
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

    // Add custom serializers here
    builder.serializerByType(JFreeBarChart.class, new BarChartSerializer());
    builder.serializerByType(JFreeStackedBarChart.class, new StackedBarChartSerializer());
    builder.serializerByType(JFreePieChart.class, new PieChartSerializer());

    // Enable pretty printing
    builder.indentOutput(true);

    return builder;
  }
}
