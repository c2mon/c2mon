/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
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
 * This object is the main trend view chart.
 * 
 * @param data: CSV data of the data points to plot.
 * @param invalidPoints: array of invalid points.
 * 
 * @author Justin Lewis Salmon
 */
function TrendView(id, csv, invalidPoints, xlabel, ylabel) {

  this.id = id;
  this.data = this.parseCSV(csv);

  this.invalidPoints = invalidPoints;
  this.showInvalidPoints = true;

  this.xlabel = xlabel;
  this.ylabel = ylabel;
  this.chart = this.createChart();

  this.init();
}

/**
 * Initialise the trend view chart. Register necessary click handlers, event
 * handlers and components. Also resize the chart to fit the page as
 * appropriate.
 */
TrendView.prototype.init = function() {
  console.log('init() called');

  // Register a click handler for the reset zoom button
  $('#reset-zoom').prop('disabled', true);
  $('#reset-zoom').click(function() {
    this.chart.zoomOut();
    $('#reset-zoom').prop('disabled', true);
  }.bind(this));

  // Enable double-click to zoom out
  $(this.chart.container).bind('dblclick', function() {
    this.chart.zoom();
    $('#reset-zoom').prop('disabled', true);
  }.bind(this));

  // Show invalid points by default
  this.toggleInvalidPoints();

  // Register a click handler for the invalid points toggle
  $('#toggle-invalid').click(function() {
    this.toggleInvalidPoints();
  }.bind(this));

  // Check if we should hide the menu/title. If the URL contains the parameters
  // MENU=false and TITLE=false, then the toolbar and page title will be hidden
  // respectively. This is to enable embedding a chart in another page.
  if (this.shouldHideMenu()) {
    $('#chart-toolbar').hide();
  }
  if (this.shouldHideTitle()) {
    $('#page-title').hide();
    $('#page-body').css('margin-top', '20px');
    $('.container-fluid').css('padding-left', '35px');
    $('.container-fluid').css('padding-right', '35px');
  }

  // Resize the chart to fit the viewport
  this.resizeChart();

  // Register an event handler to resize the chart when the
  // browser window is resized
  $(window).resize(function() {
    this.resizeChart();
  }.bind(this));

  // Initialises the "Help" popover
  $(function() {
    $('[data-toggle="popover"]').popover({
      html : true,
      content : function() {
        return $('#popover-help').html();
      }
    })
  });
}

/**
 * Parse the CSV returned by the server into objects that Highcharts can
 * understand and plot. The current format is the following:
 * 
 * timestamp,value,valueDescription,quality
 * 
 * @param csv the history CSV returned by the server
 */
TrendView.prototype.parseCSV = function(csv) {
  var array = csv.split("\n")
  array.pop();
  var values = new Array();

  for (var i = 0; i < array.length; i++) {
    var value = array[i].split(',');
    value = {
      x : Date.parse(value[0]),
      y : parseFloat(value[1]),
      valueDescription : value[2],
      quality : value[3]
    };
    values[i] = value;
  }

  return values;
}

/**
 * Create and return a Highcharts chart.
 */
TrendView.prototype.createChart = function() {

  // Global options for Highcharts
  Highcharts.setOptions({
    global : {
      useUTC : false
    }
  });

  // Highcharts options
  var options = {
    chart : {
      renderTo : 'chart',
      type : 'spline',
      animation : Highcharts.svg, // don't animate in old IE
      zoomType : "xy",
      panning : true,
      panKey : 'shift',
      resetZoomButton : {
        theme : {
          display : 'none'
        }
      },
      events : {
        load : function() {
          this.myTooltip = new Highcharts.Tooltip(this, this.options.tooltip);
        }
      }
    // Experimental: auto-updating chart
    /*
     * events: { load: function () { // set up the updating of the chart each
     * second var series = this.series[0]; setInterval(function () { var x =
     * (new Date()).getTime(), // current time y = Math.random();
     * series.addPoint([x, y], true, true); }, 1000); } }
     */
    },
    title : {
      text : null
    },
    credits : false,
    xAxis : {
      type : 'datetime',
      gridLineWidth : 1,
      tickPixelInterval : 150,
      title : {
        text : 'Date'
      },
      events : {
        setExtremes : function() {
          $('#reset-zoom').prop('disabled', false);
        }
      },
    },
    yAxis : [
        {
          title : {
            text : this.ylabel
          }
        },
    ],
    tooltip : {
      enabled : false,
      crosshairs : true,
      useHTML : true,
      formatter : this.formatTooltip
    },
    legend : {
      enabled : false
    },
    exporting : {
      enabled : false
    },
    plotOptions : {
      series : {
        turboThreshold : 0,
        allowPointSelect : true,
        stickyTracking : false,
        events : {
          // Show the tooltip when a point is clicked, not automatically
          click : function(evt) {
            this.chart.myTooltip.refresh(evt.point, evt);
          },
          mouseOut : function() {
            this.chart.myTooltip.hide();
          }
        }
      }
    },
    series : [
        {
          name : 'History for tag ID: ' + this.id,
          data : this.data
        },
    ],
  };

  return new Highcharts.Chart(options);
}

/**
 * Return a HTML snippet that is used by Highcharts to create a tooltip.
 */
TrendView.prototype.formatTooltip = function() {
  var timestamp = Highcharts.dateFormat('%Y-%m-%d %H:%M:%S.%L', this.x);
  var value = Highcharts.numberFormat(this.y, 2);
  var s = '<table class="table"><thead>' + '<tr><th colspan="2">'
      + this.series.name + '</th></tr>' + '</thead><tbody>'
      + '<tr><th>Timestamp</th><td>' + timestamp + '</td></tr>'
      + '<tr><th>Value</th><td>' + value;

  if (this.point.valueDescription != "") {
    s += '<br/><small>' + this.point.valueDescription + '</small>';
  }

  s += '</td></tr>' + '<tr><th>Quality</th><td>' + this.point.quality
      + '</td></tr>';
  s += '<tbody></table>';
  return s;
}

/**
 * Handle the window resize event and resize the chart to fit properly inside
 * the browser window, taking into account whether or not the toolbar and title
 * should be hidden.
 */
TrendView.prototype.resizeChart = function() {
  var maxHeight = 400;
  var limit = 460;
  var offset = 60;

  if (!this.shouldHideMenu()) {
    limit = 510;
    offset = 110;
  }

  if ($(window).height() < limit) {
    $('#chart-container').height($(window).height() - offset);
    $('#chart').height($(window).height() - offset);
  } else {
    $('#chart-container').height(maxHeight);
    $('#chart').height(maxHeight);
  }

  this.chart.setSize($('#chart').width(), $('#chart').height(), false);
}

/**
 * Toggles the plot bands that indicate invalid points.
 */
TrendView.prototype.toggleInvalidPoints = function() {
  if (this.showInvalidPoints === true) {
    this.showInvalidPlotBands();
  } else {
    this.chart.xAxis[0].removePlotBand('plotband');
  }

  this.showInvalidPoints = !this.showInvalidPoints;
}

/**
 * Calculate the positions for and display the plot bands that indicate invalid
 * points.
 */
TrendView.prototype.showInvalidPlotBands = function() {
  if (this.invalidPoints.length == 0) {
    return;
  }

  var cpt = 0;

  // 1st value of the graph
  var min_data_x = this.chart.series[0].xData[0];
  var max_data_x = this.chart.series[0].xData[this.chart.series[0].xData.length - 1];

  // get the first invalid date and convert the string to a date
  var w = Date.parse(this.invalidPoints[cpt][0]);

  while (w < max_data_x) {
    // Save the index of the invalid date who is located in the all date table
    var savecpt;
    var end;
    var start_x_highlight = w;

    // i search the invalid date in the all date table, the next one is the end
    for (var j = 0; j < this.data.length; j++) {
      end = this.data[j].x;
      if (start_x_highlight <= end) {
        savecpt = j + 1;
        break;
      }
    }

    if (savecpt >= this.data.length) {
      savecpt = this.data.length - 1;
    }
    var end_x_highlight = this.data[savecpt].x;
    // make sure we don't try to plot outside the graph
    if (start_x_highlight < min_data_x) {
      start_x_highlight = min_data_x;
    }
    if (end_x_highlight > max_data_x) {
      end_x_highlight = max_data_x;
    }

    this.chart.xAxis[0].addPlotBand({
      from : start_x_highlight,
      to : end_x_highlight,
      color : '#D9EDF7', // 'rgba(102, 225, 235, 0.4)',
      id : 'plotband'
    });

    // get the next invalid point
    cpt++;
    if (cpt >= this.invalidPoints.length) {
      // cpt = invalid.length - 1;
      return;
    }

    nextDate = Date.parse(this.invalidPoints[cpt][0]);
    w = nextDate;
  }
}

/**
 * Inspect the URL parameters to see if we should hide the toolbar. If the
 * parameters contain MENU=false, then we hide the toolbar, otherwise not.
 */
TrendView.prototype.shouldHideMenu = function() {
  if (typeof this.getUrlVars()["MENU"] == 'undefined')
    return false;
  else {
    if (this.getUrlVars()["MENU"].toUpperCase() == "FALSE")
      return true;
    else
      return false;
  }
}

/**
 * Inspect the URL parameters to see if we should hide the page title. If the
 * parameters contain TITLE=false, then we hide the title, otherwise not.
 */
TrendView.prototype.shouldHideTitle = function() {
  if (typeof this.getUrlVars()["TITLE"] == 'undefined')
    return false;
  else {
    if (this.getUrlVars()["TITLE"].toUpperCase() == "FALSE")
      return true;
    else
      return false;
  }
}

/**
 * Grab all the parameters from the current page URL.
 */
TrendView.prototype.getUrlVars = function() {
  var vars = [], hash;
  var hashes = window.location.href
      .slice(window.location.href.indexOf('?') + 1).split('&');
  for (var i = 0; i < hashes.length; i++) {
    hash = hashes[i].split('=');
    vars.push(hash[0]);
    vars[hash[0]] = hash[1];
  }
  return vars;
}
