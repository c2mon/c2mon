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

/**
 * This file contains the initialisation and event handling code for the C2MON
 * web interface dashboard page.
 * 
 * @author Justin Lewis Salmon
 */
(function() {
  "use strict";

  var tagStatistics;
  var selectedProcessName;

  // Get the process names and fill the dropdown box
  $.getJSON("statistics/process/names", function(data) {
    var options = '';
    for (var i = 0; i < data.length; i++) {
      options += '<option value="' + data[i] + '">' + data[i] + '</option>';
    }
    $('#process-names').html(options);
    selectedProcessName = data[0];

  }).fail(function(e) {
    console.log("error");
  });

  // Fill the dropdown box to select the year of availability
  // TODO: maybe get the first year from the server?
  var firstYearOfServerAvailabilityData = 2012;
  var years = '';
  for (var i = new Date().getFullYear(); i >= firstYearOfServerAvailabilityData; i--) {
    years += '<option value="' + i + '">' + i + '</option>';
  }
  $('#server-availability-year').html(years);

  // Detect change event on the availability year select box
  $('#server-availability-year').change(function() {
    initialiseServerAvailabilityChart();
  });

  // Get the total server uptime
  $.getJSON('statistics/server/uptime/total', function(data) {
    $('#server-uptime-total').text(data.toFixed(2));
  });

  // Get the total number of rolling restarts
  $.getJSON('statistics/server/restarts?rolling=true', function(data) {
    $('#num-rolling-restarts').text(data);
  });

  // Get the total number of service outages
  $.getJSON('statistics/server/restarts', function(data) {
    $('#num-service-outages').text(data);
  });

  // Get the total number of configured/invalid tags for the server and the
  // currently selected process
  $.getJSON('statistics/tags', function(data) {
    tagStatistics = data;
    $('#num-tags-configured').text(tagStatistics.total);
    $('#num-invalid-tags').text(tagStatistics.invalid);
    $('#num-process-tags-configured').text(tagStatistics.processes[selectedProcessName].total);
    $('#num-process-invalid-tags').text(tagStatistics.processes[selectedProcessName].invalid);
  }).fail(function(e) {
    console.log('error');
  });

  // Initialise the charts
  initialiseServerCharts();

  // Redraw the charts when a tab is clicked
  $('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
    var target = $(e.target).attr("href");

    if ((target == '#application-server')) {
      initialiseServerCharts();
      initialiseServerAvailabilityChart();

    } else if ((target == '#process')) {
      selectedProcessName = $("#process-names option:selected").text();
      $('#process-name').text(selectedProcessName);
      initialiseProcessCharts(selectedProcessName);
    }
  });

  // Detect change event on the select box
  $('#process-names').change(function() {
    var optionSelected = $("option:selected", this);
    selectedProcessName = this.value;
    $('#process-name').text(selectedProcessName);
    $('#num-process-tags-configured').text(tagStatistics.processes[selectedProcessName].total);
    $('#num-process-invalid-tags').text(tagStatistics.processes[selectedProcessName].invalid);
    initialiseProcessCharts(selectedProcessName);
  });
})();

/**
 * This function will request the necessary data from the server backend and
 * draw the server statistics charts that are shown on the default tab of the
 * dashboard.
 * 
 * If something goes wrong loading a chart, it will be replaced by a warning
 * div.
 */
function initialiseServerCharts() {

  // Initialise the chart which shows the number of updates to the server over
  // the last 30 days.
  $.getJSON('statistics/server/updates', function(chart) {
    var options = getColumnChartOptions(chart, 'server-updates');

    options.series[0].data = chart.yaxis.data;
    var serverUpdatesChart = new Highcharts.Chart(options);

    // Initialise the little box whcih shows the number of updates for
    // yesterday.
    $('#num-updates-yesterday').text(numberWithCommas(chart.yaxis.data[0]));

  }).fail(function(e) {
    $('#server-updates').html('<div class="alert alert-warning"><strong>Oh dear!</strong> Something went wrong retrieving this chart.</div>');
  });

  // Initialise the availability chart.
  initialiseServerAvailabilityChart();

  // Initialise the chart that shows the number of updates (filtered and
  // unfiltered) received yesterday from the top 30 most active DAQs.
  $.getJSON("statistics/server/updates/process/filtered", function(chart) {
    var options = getStackedColumnChartOptions(chart, 'server-updates-per-daq-filtered', horizontal = true);
    var updatesPerDaqChartFiltered = new Highcharts.Chart(options);

  }).fail(function(e) {
    console.log(e.statusText);
    $('#server-updates-per-daq-filtered').html('<div class="alert alert-warning"><strong>Oh dear!</strong> Something went wrong retrieving this chart.</div>');
  });

  // Initialise the chart that shows the number of updates (unfiltered only)
  // received yesterday from the top 30 most active DAQs.
  $.getJSON("statistics/server/updates/process", function(chart) {
    var options = getStackedColumnChartOptions(chart, 'server-updates-per-daq', horizontal = true);
    var updatesPerDaqChart = new Highcharts.Chart(options);

  }).fail(function(e) {
    $('#server-updates-per-daq').html('<div class="alert alert-warning"><strong>Oh dear!</strong> Something went wrong retrieving this chart.</div>');
  });
}

/**
 * Initialise the availability chart.
 */
function initialiseServerAvailabilityChart() {
  var year = $("#server-availability-year").val();
  $.getJSON("statistics/server/uptime/monthly", {
    year : year
  }, function(chart) {
    var options = getColumnChartOptions(chart, 'server-availability-monthly', 100);

    options.series[0].data = chart.yaxis.data;
    var serverAvailabilityChart = new Highcharts.Chart(options);

  }).fail(function(e) {
    $('#server-availability-monthly').html('<div class="alert alert-warning"><strong>Oh dear!</strong> Something went wrong retrieving this chart.</div>');
  });
}

/**
 * This function will request the necessary data from the server backend and
 * draw the statistics charts for a specific process.
 * 
 * If a chart is not available, then its containing div will be hidden.
 * 
 * @param process the process name
 */
function initialiseProcessCharts(process) {
  // Unhide all the chart containers
  $('.chart-container').show();

  $.getJSON('statistics/process/' + process + '/uptime', {
    year : new Date().getFullYear()
  }, function(chart) {
    var options = getColumnChartOptions(chart, 'daq-availability-monthly', 100);

    options.series[0].data = chart.yaxis.data;
    var serverAvailabilityChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-availability-monthly').html('<div class="alert alert-warning"><strong>Oh dear!</strong> Availability statistics are not available for this DAQ.</div>');
  });

  $.getJSON('statistics/process/' + process + '/updates/filtered/reasons', function(chart) {
    var options = getPieChartOptions(chart, 'daq-filtered-reasons');

    options.series[0].data = chart.series.data;
    var serverUpdatesChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-filtered-reasons').closest('.chart-container').hide();
  });

  $.getJSON('statistics/process/' + process + '/updates/filtered/qualities', function(chart) {
    var options = getPieChartOptions(chart, 'daq-filtered-qualities');

    options.series[0].data = chart.series.data;
    var serverUpdatesChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-filtered-qualities').closest('.chart-container').hide();
  });

  $.getJSON('statistics/process/' + process + '/updates', function(chart) {
    var options = getStackedColumnChartOptions(chart, 'daq-updates');

    // options.series[0].data = chart.yaxis.data;
    var serverUpdatesChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-updates').closest('.col-lg-6').hide();
  });

  $.getJSON('statistics/process/' + process + '/updates/invalid', function(chart) {
    var options = getPieChartOptions(chart, 'daq-updates-invalid');

    options.series[0].data = chart.series.data;
    var serverUpdatesChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-updates-invalid').closest('.chart-container').hide();
  });

  $.getJSON('statistics/process/' + process + '/updates/invalid/qualities', function(chart) {
    var options = getPieChartOptions(chart, 'daq-updates-invalid-qualities');

    options.series[0].data = chart.series.data;
    var serverUpdatesChart = new Highcharts.Chart(options);
  }).fail(function(e) {
    $('#daq-updates-invalid-qualities').closest('.chart-container').hide();
  });
}

/**
 * Retrieve a Highcharts options object to draw a column chart.
 * 
 * @param chart the chart data
 * @param container the div container to render to
 * @param max the maximum y-value
 */
function getColumnChartOptions(chart, container, max) {
  var options = {
    chart : {
      renderTo : container,
      type : 'column'
    },
    title : {
      text : chart.title
    },
    subtitle : {
      text : chart.subtitle
    },
    xAxis : {
      categories : chart.xaxis.data,
      title : {
        text : chart.xaxis.label
      }
    },
    yAxis : {
      min : 0,
      max : max,
      title : {
        text : chart.yaxis.label
      }
    },
    tooltip : {
      headerFormat : '<span style="font-size:10px">{point.key}</span><table>',
      pointFormat : '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' + '<td style="padding:0"><b>{point.y:.1f}</b></td></tr>',
      footerFormat : '</table>',
      shared : true,
      useHTML : true
    },
    plotOptions : {
      column : {
        pointPadding : 0.2,
        borderWidth : 0
      }
    },
    legend : {
      enabled : false
    },
    credits : {
      enabled : false
    },
    series : [ {} ]
  }

  return options;
}

/**
 * Retrieve a Highcharts options object to draw a stacked column chart.
 * 
 * @param chart the chart data
 * @param container the div container to render to
 * @param horizontal whether or not to render the chart horizontally or
 *          vertically
 */
function getStackedColumnChartOptions(chart, container, horizontal) {
  var options = {
    chart : {
      renderTo : container,
      type : horizontal ? 'bar' : 'column'
    },
    title : {
      text : chart.title
    },
    subtitle : {
      text : chart.subtitle
    },
    xAxis : {
      categories : chart.xaxis.categories,
      title : {
        text : chart.xaxis.label
      }
    },
    yAxis : {
      min : 0,
      title : {
        text : chart.yaxis.label
      }
    },
    legend : {
      reversed : true
    },
    plotOptions : {
      series : {
        stacking : 'normal'
      }
    },
    credits : {
      enabled : false
    },
    series : chart.yaxis.series
  }

  return options;
}

/**
 * Retrieve a Highcharts options object to draw a pie chart.
 * 
 * @param chart the chart data
 * @param container the div container to render to
 */
function getPieChartOptions(chart, container) {
  var options = {
    chart : {
      renderTo : container,
      type : 'pie',
      plotBackgroundColor : null,
      plotBorderWidth : null,
      plotShadow : false
    },
    title : {
      text : chart.title
    },
    subtitle : {
      text : chart.subtitle
    },
    tooltip : {
      pointFormat : '{series.name}: <b>{point.percentage:.1f}%</b>'
    },
    plotOptions : {
      pie : {
        allowPointSelect : true,
        cursor : 'pointer',
        dataLabels : {
          enabled : true,
          format : '<b>{point.name}</b>: {point.percentage:.1f} %',
          style : {
            color : (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
          }
        }
      }
    },
    credits : {
      enabled : false
    },
    series : [ {} ]
  };

  return options;
}

/**
 * Format a number by adding commas, e.g.:
 * 
 * 1234567 -> 1,234,567
 * 
 * @param x the number to format
 * @returns the formatted number
 */
function numberWithCommas(x) {
  return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}