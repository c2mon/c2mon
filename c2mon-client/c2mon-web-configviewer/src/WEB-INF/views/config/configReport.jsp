<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="c2mon"%>

<!-- JSP variables -->
<c:url var="home" value="../../../" />
<c:url var="confighistory" value="../../../confighistory" />

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

label.btn.btn-default.status {
  font-size: 18px;
  padding: 3px 6px;
}
</style>

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="<c:url value="${home}"/>"> Home </a>
          <span class="divider"></span>
        </li>
        <li>
          <a href="<c:url value="${confighistory}"/>"> Configuration History Viewer </a>
          <span class="divider"></span>
        </li>
        <li>Configuration Report: ${reports[0].id}</li>
      </ul>

      <div class="page-header">
        <a href="xml/${reports[0].id}" class="btn btn-default pull-right" style="margin-left: 10px; margin-top: 5px;">
          <i class="fa fa-code fa-fw"></i>
          View as XML
        </a>

        <span id="reports" class="btn-group pull-right" role="tablist" style="margin-top: 5px;">

          <c:forEach items="${reports}" var="report">
            <label class="btn btn-default status" data-toggle="tab" data-target="#${report.id}-${report.timestamp.time}">
              <c:choose>
                <c:when test="${report.status == 'OK'}">
                  <i class="fa fa-fw fa-check-circle-o text-success" data-toggle="tooltip" title="OK: ${report.timestamp}"></i>
                </c:when>
                <c:when test="${report.status == 'RESTART'}">
                  <i class="fa fa-fw fa-exclamation-circle text-warning"  data-toggle="tooltip" title="RESTART: ${report.timestamp}"></i>
                </c:when>
                <c:otherwise>
                  <i class="fa fa-fw fa-times-circle-o text-danger"  data-toggle="tooltip" title="FAILURE: ${report.timestamp}"></i>
                </c:otherwise>
              </c:choose>

            </label>
          </c:forEach>
        </span>

        <h1>${title}</h1>
      </div>

      <div class="tab-content">

        <c:forEach items="${reports}" var="report">
          <c:set var="uniqueId" value="${report.id}-${report.timestamp.time}"></c:set>
          
          <div role="tabpanel" class="tab-pane" id="${uniqueId}">

            <div class="panel panel-default">
<%--               <div class="panel-heading clearfix">
                <h3 class="panel-title pull-left" style="padding-top: 7.5px;">Configuration Report: ${report.name} (${report.id})</h3>
              </div>

              <div class="panel-body">
                <h3>Overview</h3>

              </div>
 --%>
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
                <span>Click on a row to view further details, or use the toggles to the right to filter the report.</span>

                <!-- <form class="form-inline pull-left">
                  <div class="input-group">
                    <input type="text" class="form-control filter" placeholder="Search...">
                    <span class="input-group-btn">
                      <button class="btn btn-default" type="button"><i class="fa fa-search"></i>&nbsp;</button>
                    </span>
                  </div>
                </form> -->

                <div class="btn-group pull-right" data-toggle="buttons">
                  <label class="btn btn-default active" data-toggle="tooltip" title="Toggle successful items" data-container="body">
                    <input id="toggle-success-${uniqueId}" 
                           type="checkbox" autocomplete="off">
                    <i class="fa fa-fw fa-check-circle-o text-success"></i>
                    Successful
                  </label>
                  <label class="btn btn-default active" data-toggle="tooltip" title="Toggle warning items" data-container="body">
                    <input id="toggle-warning-${uniqueId}" 
                           type="checkbox" autocomplete="off">
                    <i class="fa fa-fw fa-exclamation-circle text-warning"></i>
                    Warnings
                  </label>
                  <label class="btn btn-default active" data-toggle="tooltip" title="Toggle failure items" data-container="body">
                    <input id="toggle-failure-${uniqueId}" 
                           type="checkbox" autocomplete="off">
                    <i class="fa fa-fw fa-times-circle-o text-danger""></i>
                    Failures
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

                    <tr class="STATUS_${subReport.status}-${uniqueId} accordion-toggle" data-toggle="collapse" 
                        data-target="#collapse-${uniqueId}-${subReport.action}-${subReport.id}">
                        
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

                    <tr class="STATUS_${subReport.status}-${uniqueId}">
                      <td colspan="5" class="hiddenRow">
                        <div class="accordion-body collapse" id="collapse-${uniqueId}-${subReport.action}-${subReport.id}">

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
                                  <c2mon:subReports list="${subReport.subreports}" uniqueId="${uniqueId}"></c2mon:subReports>
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
        </c:forEach>
      </div>

    </div>
  </div>
</c2mon:template>


<script type="text/javascript">

  // Show the latest report by default
  $("#${reports[0].id}-${reports[0].timestamp.time}").addClass('active');
  $('#reports label:last').tab('show').addClass('active');
  
  $('#reports label').click(function (e) {
    e.preventDefault();
    $('#reports label').each(function() {$(this).removeClass('active')});
    $(this).addClass('active');
  })

  // Show/hide rows based on their category via the toggle buttons.
  $("[id|='toggle-success']").each(function() {
    $(this).change(function() {
      var ok = '.STATUS_OK-' + this.id.substr(15, this.id.length);
      this.checked ? $(ok).show() : $(ok).hide();
    });
  });

  $("[id|='toggle-warning']").each(function() {
    $(this).change(function() {
      var warning = '.STATUS_WARNING-' + this.id.substr(15, this.id.length);
      var restart = '.STATUS_RESTART-' + this.id.substr(15, this.id.length);
      this.checked ? $(warning).show() : $(warning).hide();
      this.checked ? $(restart).show() : $(restart).hide();
    });
  });

  $("[id|='toggle-failure']").each(function() {
    $(this).change(function() {
      var failure = '.STATUS_FAILURE-' + this.id.substr(15, this.id.length);
      this.checked ? $(failure).show() : $(failure).hide();
    });
  });

  // Activate tooltips
  $(function () {
    $('[data-toggle="tooltip"]').tooltip()
  })

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