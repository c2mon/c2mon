<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<%@page import="cern.c2mon.web.configviewer.service.HistoryService"%>
<%@page import="cern.c2mon.client.ext.history.common.HistoryTagValueUpdate"%>
<%@page import="java.util.List"%>

<c:url var="home" value=".." />
<c:url var="trendviewer" value="../trendviewer/form" />

<c2mon:template title="${title}">

<style type="text/css">
#chart-toolbar {
  margin-bottom: 0;
  border-bottom-left-radius: 0px;
  -webkit-border-bottom-left-radius: 0px;
  -moz-border-bottom-left-radius: 0px;
  border-bottom-right-radius: 0px;
  -webkit-border-bottom-right-radius: 0px;
  -moz-border-bottom-right-radius: 0px;
  background-color: rgb(247, 247, 247);
}

#chart-container {
  border-top-left-radius: 0px;
  -webkit-border-top-left-radius: 0px;
  -moz-border-top-left-radius: 0px;
  border-top-right-radius: 0px;
  -webkit-border-top-right-radius: 0px;
  -moz-border-top-right-radius: 0px;
  border-top: 0px;
  background-color: #fff;
  padding-bottom: 0;
  padding-left: 0;
}

.btn-danger {
  margin-left: 8px;
}
</style>


  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="<c:url value="${home}"/>">
            Home
          </a>
          <span class="divider"></span>
        </li>
        <li>
          <a href="<c:url value="${trendviewer}"/>">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${id}</li>
      </ul>

      <div id="page-title">
        <div class="page-header">
          <h1>${title}</h1>
        </div>
      </div>

      <div id="page-body">
        <nav id="chart-toolbar" class="navbar navbar-default" role="navigation">
          <div>
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
              <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
              <span class="navbar-brand">
                ${view_title} <small>${view_description}</small>
              </span>
            </div>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
              <ul class="nav navbar-nav navbar-right">
                <li>
                  <div class="btn-group" data-toggle="buttons">
                    <button id="reset-zoom" class="btn btn-default navbar-btn">
                      <i class="glyphicon glyphicon-zoom-out"></i>
                      &nbsp;Reset Zoom
                    </button>
                    <label id="toggle-invalid" class="btn btn-default navbar-btn">
                      <input type="checkbox" autocomplete="off">
                      Toggle Invalid
                    </label>
                  </div>
                </li>

                <li>
                  <a data-toggle="popover" data-placement="bottom" data-container="body" title="Usage instructions">
                    <i class="glyphicon glyphicon-question-sign"></i>
                    &nbsp;Help
                  </a>

                  <div id="popover-help" style="display: none;">
                    Drag out a rectangle in the chart with your mouse to zoom into that area. Click the 'Reset Zoom' button or double click the chart to zoom
                    out again.
                    <br />
                    <br />
                    You can also hold down the Shift key and click to pan inside the chart.
                    <br />
                    <br />
                    Invalid tag indicators can be shown/hidden with the 'Toggle Invalid' button.
                  </div>
                </li>

                <li class="dropdown">
                  <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                    <i class="glyphicon glyphicon-cog"></i>
                    Tools
                    <span class="caret"></span>
                  </a>
                  <ul class="dropdown-menu" role="menu">
                    <li>
                      <a href="../historyviewer/${id}?${queryParameters}">View as Table &raquo;</a>
                    </li>
                    <li>
                      <a href="../tagviewer/${id}">View Tag &raquo;</a>
                    </li>

                    <!-- Only show the HelpAlarm button if the property is defined. -->
                    <c:if test="${fn:length(help_url) > 0}">
                      <li class="divider"></li>
                      <li>
                        <form action="${help_url}">
                          <input type="submit" class="btn btn-danger navbar-btn" style="margin-bottom: 0px;" value="View Help Alarm &raquo;">
                        </form>
                      </li>
                    </c:if>

                  </ul>
                </li>
              </ul>
            </div>
          </div>
        </nav>

        <div id="chart-container" class="well">
          <div id="chart" style="margin: 0 auto;"></div>
        </div>
      </div>
    </div>
  </div>
</c2mon:template>

<script type="text/javascript" src="../js/highcharts.js"></script>
<script type="text/javascript" src="../js/trend-view.js"></script>

<script type="text/javascript">

  /**
   * Called when the document is ready to be safely manipulated.
   */
  $(document).ready(function() {
    var data = ${CSV};

    // Parse the invalid points into an array
    // TODO: merge this into main CSV file via 'valid' column
    var invalid = new Array();
    <c:forEach items="${invalidPoints}" var="invalidPoint" varStatus="status">
      invalid[${status.index}] = ["${invalidPoint.time}", "${invalidPoint.invalidationReason}"];
    </c:forEach>

    // Create and show the chart
    var trendView = new TrendView(${id}, ${CSV}, invalid, "Date", "${ylabel}");
  });
</script>

