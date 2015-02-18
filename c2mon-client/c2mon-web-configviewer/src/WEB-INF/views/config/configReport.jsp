<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="c2mon"%>

<!-- JSP variables -->
<c:url var="home" value="../../../" />
<c:url var="confighistory" value="../../../confighistory" />
<c:url var="xml" value="xml/${report.id}" />

<c2mon:template title="${title}">

<style type="text/css">
tbody>tr>th {
  width: 25%;
}

tr {
  cursor: pointer;
}

.hiddenRow {
  padding: 0 !important;
  cursor: default;
}

.hiddenRow:hover {
  background-color: #fff;
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
          <a href="<c:url value="${confighistory}"/>"> Configuration History Viewer </a>
          <span class="divider"></span>
        </li>
        <li>Configuration Report: ${report.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="panel panel-default">
        <div class="panel-heading clearfix">
          <h3 class="panel-title pull-left" style="padding-top: 7.5px;">Configuration Report: ${report.name} (${report.id})</h3>
          <span class="pull-right">
            <a href="${xml}" class="btn btn-default btn-sm"><i class="fa fa-code fa-fw"></i> View as XML</a>
          </span>
        </div>

        <div class="panel-body">
          <h3>Overview</h3>

        </div>

        <table class="table table-striped table-bordered">
          <tbody>
            <tr>
              <th>Configuration ID</th>
              <td>${report.id}</td>
            </tr>
            <tr>
              <th class="bold">Name</th>
              <td>${report.name}</td>
            </tr>
            <tr>
              <th class="bold">Applied by</th>
              <td>${report.user}</td>
            </tr>
            <tr>
              <th class="bold">Applied on</th>
              <td>${report.timestamp}</td>
            </tr>
            <tr>
              <th class="bold">Status</th>
              <td class="cell-label">
                <span class="lead">
                  <c:choose>
                    <c:when test="${report.status == 'OK'}">
                      <span class="label label-success">${report.status}</span>
                    </c:when>
                    <c:when test="${report.status == 'RESTART'}">
                      <span class="label label-warning">${report.status}</span>
                    </c:when>
                    <c:otherwise>
                      <span class="label label-danger">${report.status}</span>
                    </c:otherwise>
                  </c:choose>
                </span>
              </td>
            </tr>
            <tr>
              <th class="highlight bold">Message</th>
              <td>${report.statusDescription}</td>
            </tr>
            <tr>
              <th class="highlight bold">DAQs to reboot</th>
              <td>${report.processesToReboot}</td>
            </tr>
          </tbody>
        </table>

        <div class="panel-body">
          <h3>Detailed Report</h3>
          <span>Click on a row to view further details, or use the controls below to filter the report.</span>
          
<!--           <form class="form-inline pull-left">
            <div class="input-group">
              <input type="text" class="form-control filter" placeholder="Search...">
              <span class="input-group-btn">
                <button class="btn btn-default" type="button"><i class="fa fa-search"></i>&nbsp;</button>
              </span>
            </div>
          </form> -->

          <div class="btn-group pull-right" data-toggle="buttons">
            <label class="btn btn-default active">
              <input id="toggle-successful" type="checkbox" autocomplete="off">
              <span class="glyphicon glyphicon-exclamation-sign text-success" aria-hidden="true"></span>
              Toggle Successful
            </label>
            <label class="btn btn-default active">
              <input id="toggle-warnings" type="checkbox" autocomplete="off">
              <span class="glyphicon glyphicon-exclamation-sign text-warning" aria-hidden="true"></span>
              Toggle Warnings
            </label>
            <label class="btn btn-default active">
              <input id="toggle-failures" type="checkbox" autocomplete="off">
              <span class="glyphicon glyphicon-exclamation-sign text-danger" aria-hidden="true"></span>
              Toggle Failures
            </label>
          </div>
        </div>

        <table id="detailed-report" class="table table-bordered table-hover" style="border-collapse: collapse;">
          <thead>
            <tr>
              <th class="col-sm-1"></th>
              <th class="col-sm-3">Action</th>
              <th class="col-sm-3">Entity</th>
              <th class="col-sm-3">Id</th>
              <th class="col-sm-3">Status</th>
            </tr>
          </thead>
          
          <tbody class="searchable">

            <c:forEach items="${report.elementReports}" var="subReport">

              <tr class="STATUS_${subReport.status}" data-toggle="collapse" data-target="#collapseme-${subReport.action}-${subReport.id}"
                class="accordion-toggle">
                <td class="button" style="text-align: center;">
                  <c:if test="${subReport.statusMessage != null || fn:length(subReport.subreports) > 0}">
                    <button class="btn btn-default btn-xs">
                      <span class="glyphicon glyphicon-eye-open"></span>
                      &nbsp;Expand
                    </button>
                  </c:if>
                </td>
                <td>${subReport.action}</td>
                <td>${subReport.entity}</td>
                <td>${subReport.id}</td>
                <td class="cell-label">
                  <c:choose>
                    <c:when test="${subReport.status == 'OK'}">
                      <span class="label label-success">${subReport.status}</span>
                    </c:when>
                    <c:when test="${subReport.status == 'WARNING' || subReport.status == 'RESTART'}">
                      <span class="label label-warning">${subReport.status}</span>
                    </c:when>
                    <c:otherwise>
                      <span class="label label-danger">${subReport.status}</span>
                    </c:otherwise>
                  </c:choose>
                </td>
              </tr>

              <tr class="STATUS_${subReport.status}">
                <td colspan="5" class="hiddenRow">
                  <div class="accordion-body collapse" id="collapseme-${subReport.action}-${subReport.id}">

                    <c:choose>
                      <c:when test="${subReport.statusMessage == null && fn:length(subReport.subreports) == 0}">
                        <!-- Show nothing -->
                      </c:when>

                      <c:otherwise>
                        <div class="col-md-12">
                          <c:if test="${fn:length(subReport.statusMessage) > 0}">
                            <h4>Message:</h4>
                            <pre style="white-space: pre-wrap;">${subReport.statusMessage}</pre>
                          </c:if>

                          <c:if test="${fn:length(subReport.subreports) > 0}">
                            <h4>Nested reports:</h4>
                            <c2mon:subReports list="${subReport.subreports}"></c2mon:subReports>
                          </c:if>
                        </div>
                      </c:otherwise>
                    </c:choose>

                  </div>
                </td>
              </tr>

            </c:forEach>

          </tbody>
        </table>
      </div>
    </div>
  </div>
</c2mon:template>


<script type="text/javascript">
  // Show/hide rows based on their category via the toggle buttons.
  $('#toggle-successful').change(function() {
    this.checked ? $('.STATUS_OK').show() : $('.STATUS_OK').hide();
  });

  $('#toggle-warnings').change(function() {
    this.checked ? $('.STATUS_WARNING').show() : $('.STATUS_WARNING').hide();
    this.checked ? $('.STATUS_RESTART').show() : $('.STATUS_RESTART').hide();
  });

  $('#toggle-failures').change(function() {
    this.checked ? $('.STATUS_FAILURE').show() : $('.STATUS_FAILURE').hide();
  });

  
  // Experimental: search feature
  
/*   $('input.filter').on('keyup', function() {
    var rex = new RegExp($(this).val(), 'i');
    $('.searchable tr').hide();
    $('.searchable tr').filter(function() {
      if (rex.test($(this).text())) {
        $(this).find('.collapse').collapse('toggle');
        return true;
      }
      
      return false;
    }).show();
  }); */
</script>
</body>
</html>