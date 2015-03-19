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
package cern.c2mon.web.configviewer.statistics;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.shared.client.lifecycle.LifecycleEventType;
import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.BarChart;
import cern.c2mon.web.configviewer.statistics.daqlog.charts.WebChart;

/**
 * This class serves as an entry point to retrieve various statistics from the
 * server and return them as JSON objects.
 *
 * The statistics come from three different places:
 *
 * <ul>
 * <li>Generated inside this package, by interfacing with the STL database on
 * request (such as server and DAQ availability).</li>
 *
 * <li>Retrieved directly from the server itself (such as the number of
 * configured/invalid tags in the server/per DAQ).</li>
 *
 * <li>Pulled from the c2mon-statistics-generator module. These statistics are
 * generated automatically every day from the filter log (timdaqlog) via Mark
 * Brightwell's stored procedures (see his master thesis for details).</li>
 * </ul>
 *
 * @author Justin Lewis Salmon
 */
@Controller
@RequestMapping("/statistics")
public class StatisticsController {

  private static final Logger logger = Logger.getLogger(StatisticsController.class);

  private static final Integer FIRST_YEAR_OF_SERVER_LIFECYCLE_DATA = 2012;

  /**
   * Reference to the {@link StatisticsService} bean.
   */
  @Autowired
  private StatisticsService service;

  /**
   * Retrieve the total server uptime (availability) for the current year.
   *
   * @return the uptime
   */
  @RequestMapping(value = "/server/uptime/total", method = RequestMethod.GET)
  @ResponseBody
  public Double getTotalServerUptime() {
    List<Double> monthlyUptimes;

    try {
      // Retrieve the uptimes for the current year
      monthlyUptimes = getServerUptimeByMonth(Calendar.getInstance().get(Calendar.YEAR)).getYAxis().getData();
    } catch (ParseException e) {
      logger.error("Error retrieving total server uptime", e);
      return null;
    }

    // Calculate the average for the entire year
    Double average = 0.0;

    for (Double uptime : monthlyUptimes) {
      if (uptime == 0.0) {
        average += 100.0;
      } else {
        average += uptime;
      }
    }

    return average / monthlyUptimes.size();
  }

  /**
   * Retrieve the monthly server availability statistics for a given year, as a
   * JSON object suitable for rendering as a bar chart.
   *
   * @param year the year of statistics to retrieve
   *
   * @return a JSON object containing the monthly availability statistics
   * @throws ParseException if an invalid year is given
   */
  @RequestMapping(value = "/server/uptime/monthly", method = RequestMethod.GET)
  @ResponseBody
  public BarChart getServerUptimeByMonth(@RequestParam("year") final Integer year) throws ParseException {
    // Prevent invalid years
    if (year > Calendar.getInstance().get(Calendar.YEAR) || year < FIRST_YEAR_OF_SERVER_LIFECYCLE_DATA) {
      return null;
    }

    List<ServerLifecycleEvent> all = service.getServerLifecycleEventsForYear(year);

    // Normalise the server names
    all = service.normaliseServerNames(all);

    List<List<ServerLifecycleEvent>> monthlyEvents = new ArrayList<>(12);
    for (int i = 0; i < 12; i++) {
      monthlyEvents.add(new ArrayList<ServerLifecycleEvent>());
    }

    for (ServerLifecycleEvent event : all) {
      monthlyEvents.get(event.getEventTime().getMonth()).add(event);
    }

    List<Double> monthlyUptimes = new ArrayList<>(12);
    for (int i = 0; i < 12; i++) {
      monthlyUptimes.add(calculateServerUptime(monthlyEvents.get(i), i, year));
    }

    List<String> xData = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    return new BarChart("Server Availability (By Month)", "Total availability of the C2MON server for the year " + year, "", "Month", xData,
        "Server Availability (%)", monthlyUptimes);
  }

  /**
   * Calculate the total uptime for a given set of lifecycle events for a given
   * month.
   *
   * @param events the events that occurred during the month
   * @param month the month in which the events occurred
   * @param year the year in which the events occurred
   *
   * @return the total uptime
   */
  private Double calculateServerUptime(List<ServerLifecycleEvent> events, int month, int year) {

    // If we have no events, then there were either no events for that month
    // (which implies 100% availability) or the month/year is in the future.
    if (events.size() == 0) {
      if (year == Calendar.getInstance().get(Calendar.YEAR) && month > Calendar.getInstance().get(Calendar.MONTH)) {
        return 0.0;
      } else {
        return 100.0;
      }
    }

    Timestamp oldest = events.get(0).getEventTime();
    Calendar calendar = new GregorianCalendar(oldest.getYear(), month, 1);
    // Find the number of seconds in the given month
    double totalTime = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * 60 * 60 * 24 * 100;

    double downtime = 0;
    int numServersDown = 0;

    for (int i = 0; i < events.size(); i++) {
      ServerLifecycleEvent event = events.get(i);

      if (event.getEventType().equals(LifecycleEventType.STOP) && numServersDown == 0) {
        numServersDown = 1;
      }

      else if (event.getEventType().equals(LifecycleEventType.STOP) && numServersDown == 1) {
        numServersDown = 2;
        ServerLifecycleEvent start = events.get(i + 1);
        downtime += start.getEventTime().getTime() - event.getEventTime().getTime();
      }

      else if (event.getEventType().equals(LifecycleEventType.START) && numServersDown == 1) {
        numServersDown = 0;
      }

      else if (event.getEventType().equals(LifecycleEventType.START) && numServersDown == 2) {
        numServersDown = 1;
      }
    }

    double totalUptime = (totalTime - downtime) / totalTime;
    return totalUptime * 100;
  }

  /**
   * Retrieve the number of times the server has been restarted this year
   * (either rolling restarts or full service outages).
   *
   * @param rolling whether or not to only consider rolling updates
   *
   * @return the number of server restarts (rolling or not)
   */
  @RequestMapping("/server/restarts")
  @ResponseBody
  public Integer getNumServerRestarts(@RequestParam(value = "rolling", required = false) final boolean rolling) {
    List<ServerLifecycleEvent> events;

    try {
      events = service.getServerLifecycleEventsForYear(Calendar.getInstance().get(Calendar.YEAR));
    } catch (ParseException e) {
      logger.error("Error retrieving number of server restarts", e);
      return null;
    }

    // Normalise the server names
    events = service.normaliseServerNames(events);

    int numServiceOutages = 0;
    int numRollingRestarts = 0;

    for (int i = 0; i < events.size(); i++) {
      ServerLifecycleEvent event = events.get(i);

      if (event.getEventType().equals(LifecycleEventType.STOP)) {
        if (i != events.size() - 1) {
          ServerLifecycleEvent next = events.get(i + 1);

          // If the next event is a START event and it is the same server, then
          // this is a rolling restart
          if (next.getEventType().equals(LifecycleEventType.START) && next.getServerName().equals(event.getServerName())) {
            numRollingRestarts++;
          }

          // If the next event is a STOP event and it is a different server,
          // then this is a service outage
          else if (next.getEventType().equals(LifecycleEventType.STOP) && !next.getServerName().equals(event.getServerName())) {
            numServiceOutages++;
          }
        }
      }
    }

    return rolling ? numRollingRestarts : numServiceOutages;
  }

  /**
   * Get a representation of the number of updates to the application server for
   * the last 3 weeks as a JSON object suitable for rendering as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module. Currently the time period is fixed
   * to the last 3 weeks.
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping("/server/updates")
  @ResponseBody
  public WebChart getServerUpdates() {
    return service.getChart("5");
  }

  /**
   * Get a representation of the number of updates (both filtered and
   * unfiltered) to the server sent by the top 30 most active DAQs for the
   * previous day as a JSON object suitable for rendering as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping("/server/updates/process/filtered")
  @ResponseBody
  public WebChart getFilteredServerUpdatesByProcess() {
    return service.getChart("15");
  }

  /**
   * Get a representation of the number of updates (unfiltered only) to the
   * server sent by the top 30 most active DAQs for the previous day as a JSON
   * object suitable for rendering as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping("/server/updates/process")
  @ResponseBody
  public WebChart getServerUpdatesByProcess() {
    return service.getChart("14");
  }

  /**
   * Retrieve a representation of the monthly availability of a given process
   * for a given year as a JSON object suitable for rendering as a chart.
   *
   * @param name the name of the process
   * @param year the year of statistics to retrieve
   *
   * @return a JSON object containing the monthly availability statistics
   * @throws Exception if an error occurs getting the statistics
   */
  @RequestMapping(value = "/process/{name}/uptime", method = RequestMethod.GET)
  @ResponseBody
  public BarChart getProcessUptime(@PathVariable("name") final String name, @RequestParam("year") final Integer year) throws Exception {
    List<SupervisionEvent> all = service.getSupervisionEventsForYear(name, year);

    List<List<SupervisionEvent>> monthlyEvents = new ArrayList<>(12);
    for (int i = 0; i < 12; i++) {
      monthlyEvents.add(new ArrayList<SupervisionEvent>());
    }

    for (SupervisionEvent event : all) {
      monthlyEvents.get(event.getEventTime().getMonth()).add(event);
    }

    List<Double> monthlyUptimes = new ArrayList<>(12);
    for (int i = 0; i < 12; i++) {
      monthlyUptimes.add(calculateProcessUptime(monthlyEvents.get(i), i, year));
    }

    List<String> xData = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    return new BarChart("Availability for " + name + " (By Month)", "Total availability of " + name + " for the year " + year, "", "Month", xData,
        "Availability (%)", monthlyUptimes);
  }

  /**
   * Calculate the total uptime of a process for a given set of supervision
   * events events for a given month.
   *
   * @param events the events that occurred during the month
   * @param month the month in which the events occurred
   * @param year the year in which the events occurred
   *
   * @return the total uptime
   */
  private Double calculateProcessUptime(List<SupervisionEvent> events, int month, int year) {

    // If we have no events, then there were either no events for that month
    // (which implies 100% availability) or the month/year is in the future.
    if (events.size() == 0) {
      if (year == Calendar.getInstance().get(Calendar.YEAR) && month > Calendar.getInstance().get(Calendar.MONTH)) {
        return 0.0;
      } else {
        return 100.0;
      }
    }

    Timestamp oldest = events.get(0).getEventTime();

    Calendar calendar = new GregorianCalendar(oldest.getYear(), month, 1);
    double totalTime = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * 60 * 60 * 24 * 100;

    double downtime = 0;
    for (int i = 0; i < events.size(); i++) {
      SupervisionEvent event = events.get(i);

      if (event.getStatus().equals(SupervisionStatus.DOWN) || event.getStatus().equals(SupervisionStatus.STOPPED)) {
        // find the next STARTUP or RUNNING event

        for (int j = i + 1; j < events.size(); j++) {
          SupervisionEvent event2 = events.get(j);
          if (event2.getStatus().equals(SupervisionStatus.RUNNING) || event2.getStatus().equals(SupervisionStatus.RUNNING_LOCAL)) {
            downtime += event2.getEventTime().getTime() - event.getEventTime().getTime();
            i = j;
            break;
          }
        }
      }
    }

    double totalUptime = (totalTime - downtime) / totalTime;
    return totalUptime * 100;
  }

  /**
   * Get a representation of the reasons why tags were filtered on a specific
   * DAQ over the last 7 days as a JSON object suitable for rendering as a
   * chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @param name the process name
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping(value = "/process/{name}/updates/filtered/reasons", method = RequestMethod.GET)
  @ResponseBody
  public WebChart getFilteredProcessUpdateReasons(@PathVariable("name") final String name) {
    String chartId = "fil_" + name;
    return service.getChart(chartId);
  }

  /**
   * Get a representation of the qualities of filtered invalid tags on a
   * specific DAQ over the last 7 days as a JSON object suitable for rendering
   * as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @param name the process name
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping(value = "/process/{name}/updates/filtered/qualities", method = RequestMethod.GET)
  @ResponseBody
  public WebChart getFilteredProcessUpdateQualities(@PathVariable("name") final String name) {
    String chartId = "fil_qual_" + name;
    return service.getChart(chartId);
  }

  /**
   * Get a representation of the number of source updates (filtered and
   * unfiltered) sent by a specific DAQ over the last 7 days as a JSON object
   * suitable for rendering as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @param name the process name
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping(value = "/process/{name}/updates", method = RequestMethod.GET)
  @ResponseBody
  public WebChart getProcessUpdates(@PathVariable("name") final String name) {
    String chartId = "last7_" + name;
    return service.getChart(chartId);
  }

  /**
   * Get a representation of the proportion of invalid tags on a specific DAQ
   * over the last 7 days as a JSON object suitable for rendering as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @param name the process name
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping(value = "/process/{name}/updates/invalid", method = RequestMethod.GET)
  @ResponseBody
  public WebChart getProcessInvalidUpdates(@PathVariable("name") final String name) {
    String chartId = "inv_" + name;
    return service.getChart(chartId);
  }

  /**
   * Get a representation of the qualities of unfiltered invalid tags on a
   * specific DAQ over the last 7 days as a JSON object suitable for rendering
   * as a chart.
   *
   * The chart retrieved by this method comes from the statistics generated by
   * the c2mon-statistics-generator module.
   *
   * @param name the process name
   *
   * @return a {@link WebChart} which will be serialised to JSON by Spring
   */
  @RequestMapping(value = "/process/{name}/updates/invalid/qualities", method = RequestMethod.GET)
  @ResponseBody
  public WebChart getProcessInvalidUpdateQualities(@PathVariable("name") final String name) {
    String chartId = "qual_" + name;
    return service.getChart(chartId);
  }

  /**
   * Retrieve the list of names of all processes currently configured on the
   * server.
   *
   * @return the list of process names
   */
  @RequestMapping(value = "/process/names", method = { RequestMethod.GET, RequestMethod.POST })
  @ResponseBody
  public Collection<String> getProcessNames() {
    List<String> processNames = (List<String>) service.getProcessNames();
    Collections.sort(processNames);
    return processNames;
  }

  /**
   * Retrieve tag statistics from the server (total number of configured/invalid
   * tags for the server and per process).
   *
   * @return the {@link TagStatisticsResponse} object which will be serialised
   *         to JSON by Spring
   */
  @RequestMapping(value = "/tags", method = { RequestMethod.GET, RequestMethod.POST })
  @ResponseBody
  public TagStatisticsResponse getTagStatistics() {
    return service.getTagStatistics();
  }
}
