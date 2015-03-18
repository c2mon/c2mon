<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">

  <style>
.input-group .form-control {
  margin-bottom: 0;
  width: 100%;
  display:block !important;
}
</style>

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li><a href="#">Home</a></li>
      </ul>

      <div class="page-header">
        <h1>Dashboard</h1>
      </div>
    </div>

    <div class="col-lg-3 col-md-6">
      <div class="panel panel-success">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-3 auto-resize">
              <i class="fa fa-check fa-5x"></i>
            </div>
            <div class="col-xs-9 text-right">
              <div class="huge">
                <span id="server-uptime-total">-</span><small>%</small>
              </div>
              <div>Server availability this year</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="col-lg-3 col-md-6">
      <div class="panel panel-default">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-3 auto-resize">
              <i class="fa fa-tags fa-5x"></i>
            </div>
            <div class="col-xs-9 text-right">
              <div class="huge">
                <span id="num-updates-yesterday">-</span>
              </div>
              <div>Tag updates yesterday</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="col-lg-3 col-md-6">
      <div class="panel panel-info">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-3 auto-resize">
              <i class="fa fa-repeat fa-5x"></i>
            </div>
            <div class="col-xs-9 text-right">
              <div class="huge">
                <span id="num-rolling-restarts">-</span>
              </div>
              <div>Transparent restarts this year</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="col-lg-3 col-md-6">
      <div class="panel panel-danger">
        <div class="panel-heading">
          <div class="row">
            <div class="col-xs-3 auto-resize">
              <i class="fa fa-warning fa-5x"></i>
            </div>
            <div class="col-xs-9 text-right">
              <div class="huge">
                <span id="num-service-outages">-</span>
              </div>
              <div>Service outages this year</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="col-lg-12">
      <p class="lead">Welcome to the C2MON Web Interface!</p>

      <!--       <p>DAQ uptime, server uptime, server restarts, DAQ restarts, Equipment disconnections, etc - current amount of invalid tags in the server... or per
        DAQ - Current amount of configured tags, devices, classes,</p> -->
    </div>
  </div>


  <div class="row">

    <div class="col-lg-12">
      <div role="tabpanel">

        <!-- Nav tabs -->
        <ul class="nav nav-tabs" role="tablist">
          <li role="presentation" class="active"><a href="#application-server" aria-controls="home" role="tab" data-toggle="tab">Server Statistics</a></li>
          <li role="presentation"><a href="#process" aria-controls="profile" role="tab" data-toggle="tab">DAQ Statistics</a></li>
        </ul>

        <!-- Tab panes -->
        <div class="tab-content">


          <!-- Application server pane -->
          <div role="tabpanel" class="tab-pane active" id="application-server">

            <div class="row">
              <div class="col-lg-12" style="padding-bottom: 20px;">
                <h3>
                  C2MON Server Statistics <br> <small> <span> <i class="fa fa-tags fa-fw"></i> <strong><span
                        id="num-tags-configured"><i class="fa fa-refresh fa-spin"></i></span></strong> tags configured
                  </span> <span> <i class="fa fa-warning fa-fw"></i> <strong><span id="num-invalid-tags"><i class="fa fa-refresh fa-spin"></i></span></strong>
                      invalid tags
                  </span>
                  </small>

                </h3>
                <h3>Charts</h3>
                <p>The following charts show availability and throughput statistics for the C2MON server.</p>
              </div>
            </div>

            <div class="row">
              <div class="col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">

                      <form class="form-inline pull-right" style="margin-top: -7px;">
                        <div class="form-group">
                          <label for="server-availability-select">Select Year: </label> <select id="server-availability-year" class="form-control">
                          </select>
                        </div>
                      </form>

                    <i class="fa fa-bar-chart-o fa-fw"></i> Server Availability
                  </div>
                  <!-- /.panel-heading -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="server-availability-monthly" style="width: 100%; margin: 0 auto"></div>
                    </div>
                  </div>
                  <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> Total Updates to Application Server
                  </div>
                  <!-- /.panel-heading -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="server-updates" style="width: 100%; margin: 0 auto"></div>
                    </div>
                  </div>
                  <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> Updates per DAQ (including filtered)
                  </div>
                  <!-- /.panel-heading -->
                  <div class="panel-body">
                    <div class="flot-chart" style="height: 800px;">
                      <div class="flot-chart-content" id="server-updates-per-daq-filtered"></div>
                    </div>
                  </div>
                  <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> Updates per DAQ
                  </div>
                  <!-- /.panel-heading -->
                  <div class="panel-body">
                    <div class="flot-chart" style="height: 800px;">
                      <div class="flot-chart-content" id="server-updates-per-daq"></div>
                    </div>
                  </div>
                  <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
              </div>
              <!-- /.col-lg-6 -->

            </div>
          </div>



          <!-- DAQ pane -->
          <div role="tabpanel" class="tab-pane" id="process">

            <div class="row">
              <div class="col-lg-6" style="padding-bottom: 20px; padding-top: 20px;">
                <p>Select a DAQ process from the dropdown box below to view statistics for that DAQ.</p>
                <form class="form-inline">
                  <div class="form-group">
                    <label for="process-names">Select DAQ: </label> <select id="process-names" class="form-control"></select>
                  </div>
                </form>

                <h3>
                  DAQ Statistics <small><span id="process-name"></span> <br> <span> <i class="fa fa-tags fa-fw"></i> <strong><span
                        id="num-process-tags-configured"><i class="fa fa-refresh fa-spin"></i></span></strong> tags configured
                  </span> <span> <i class="fa fa-warning fa-fw"></i> <strong><span id="num-process-invalid-tags"><i
                          class="fa fa-refresh fa-spin"></i></span></strong> invalid tags
                  </span> </small>
                </h3>
                <h3>Charts</h3>
                <p>The following charts show availability and throughput statistics for the selected DAQ process.</p>
              </div>
            </div>

            <div class="row">
              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ Availability
                  </div>
                  <!-- /.panel-heading -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-availability-monthly" style="width: 100%; margin: 0 auto"></div>
                    </div>
                  </div>
                  <!-- /.panel-body -->
                </div>
                <!-- /.panel -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ updates
                  </div>
                  <!-- /.panel-heading  -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-updates"></div>
                    </div>
                  </div>
                  <!-- /.panel-body  -->
                </div>
                <!-- /.panel  -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ filtering reasons
                  </div>
                  <!-- /.panel-heading  -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-filtered-reasons"></div>
                    </div>
                  </div>
                  <!-- /.panel-body  -->
                </div>
                <!-- /.panel  -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ filtered update qualities
                  </div>
                  <!-- /.panel-heading  -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-filtered-qualities"></div>
                    </div>
                  </div>
                  <!-- /.panel-body  -->
                </div>
                <!-- /.panel  -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ invalid updates
                  </div>
                  <!-- /.panel-heading  -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-updates-invalid"></div>
                    </div>
                  </div>
                  <!-- /.panel-body  -->
                </div>
                <!-- /.panel  -->
              </div>
              <!-- /.col-lg-6 -->

              <div class="chart-container col-lg-6">
                <div class="panel panel-default">
                  <div class="panel-heading">
                    <i class="fa fa-bar-chart-o fa-fw"></i> DAQ invalid update qualities
                  </div>
                  <!-- /.panel-heading  -->
                  <div class="panel-body">
                    <div class="flot-chart">
                      <div class="flot-chart-content" id="daq-updates-invalid-qualities"></div>
                    </div>
                  </div>
                  <!-- /.panel-body  -->
                </div>
                <!-- /.panel  -->
              </div>
              <!-- /.col-lg-6 -->
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>

</c2mon:template>

<script src="<c:url value="/js/highcharts.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/dashboard.js"/>" type="text/javascript"></script>
<script src="<c:url value="/js/jquery/jquery-fittext.js"/>" type="text/javascript"></script>
<script>
  jQuery(".huge").fitText(0.56, {
    minFontSize : '20px',
    maxFontSize : '40px'
  });
  jQuery(".auto-resize").fitText(0.3, {
    minFontSize : '8px',
    maxFontSize : '15px'
  });
</script>
