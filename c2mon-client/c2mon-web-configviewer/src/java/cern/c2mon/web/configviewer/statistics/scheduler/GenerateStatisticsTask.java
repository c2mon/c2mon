/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.configviewer.statistics.scheduler;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import cern.c2mon.web.configviewer.statistics.StatisticsService;
import cern.c2mon.web.configviewer.statistics.daqlog.GraphDeployer;

/**
 * This class runs as a Spring scheduled task to generate statistics from the
 * DAQ filter log and STL databases via the c2mon-statistics-generator module.
 *
 * It runs every day at midnight, and also when the class is first instantiated.
 *
 * @author Justin Lewis Salmon
 */
@Component
@EnableScheduling
class GenerateStatisticsTask {

  private static final Logger logger = LoggerFactory.getLogger(GenerateStatisticsTask.class);

  /**
   * Reference to the {@link StatisticsService} bean, into which we will inject
   * the generated statistics.
   */
  @Autowired
  private StatisticsService service;

  @Autowired
  private GraphDeployer deployer;

  /**
   * System property defining the location of the XML configuration file from
   * which we find the charts we need to generate. If this property is not
   * defined, no charts will be generated.
   */
  @Value("${c2mon.web.statistics.chart.config.location:}")
  public String chartConfigLocation;

  /**
   * Generate the statistics from the XML configuration file. The configuration
   * file specifies the charts to be generated. The file is given to the
   * c2mon-statistics-generator module, which generates the charts accordingly,
   * using stored database procedures.
   *
   * Run every day at 00:01am, and also when the class is instantiated.
   */
  @Scheduled(cron = "0 1 0 ? * *")
  @PostConstruct
  public void generateStatistics() {
    logger.info("Generating statistics from database...");

    if (chartConfigLocation == null || chartConfigLocation.isEmpty()) {
      logger.info("Not generating statistics as \"c2mon.web.statistics.chart.config.location\" property was not supplied.");
      return;
    }

    Document graphXMLDocument;

    try {
      // Parse graph XML file
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      graphXMLDocument = builder.parse(new FileInputStream(chartConfigLocation));

      // Generate the charts from the XML
      deployer.configure(graphXMLDocument);
    } catch (IOException e) {
      logger.error("Graph configuration file could not be read", e);
    } catch (org.xml.sax.SAXException e) {
      logger.error("Error in parsing web configuration XML document", e);
    } catch (Exception e) {
      logger.error("Unidentified exception caught", e);
    }

    service.setCharts(deployer.getWebCharts());
    logger.info("Finished generating statistics from database.");
  }
}
