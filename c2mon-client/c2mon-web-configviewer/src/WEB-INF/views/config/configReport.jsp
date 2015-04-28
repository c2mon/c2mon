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

a.btn.btn-default.status {
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
        <li>Configuration Report: ${report.id}</li>
      </ul>

      <div class="page-header">
        <a href="xml/${report.id}" class="btn btn-default pull-right" style="margin-left: 10px; margin-top: 5px;">
          <i class="fa fa-code fa-fw"></i>
          View as XML
        </a>

        <span id="reports" class="btn-group pull-right" role="tablist" style="margin-top: 5px;">

          <c:forEach items="${allReports}" var="r">
            <c:choose>
              <c:when test="${r.timestamp == report.timestamp}">
                <a class="btn btn-default status active" href="?page=0&ts=${r.timestamp}">
              </c:when>
              <c:otherwise>
                <a class="btn btn-default status" href="?page=0&ts=${r.timestamp}">
              </c:otherwise>
            </c:choose>

              <c:choose>
                <c:when test="${r.status == 'OK'}">
                  <i class="fa fa-fw fa-check-circle-o text-success" data-toggle="tooltip" title="OK: ${r.timestamp}"></i>
                </c:when>
                <c:when test="${r.status == 'RESTART'}">
                  <i class="fa fa-fw fa-exclamation-circle text-warning"  data-toggle="tooltip" title="RESTART: ${r.timestamp}"></i>
                </c:when>
                <c:otherwise>
                  <i class="fa fa-fw fa-times-circle-o text-danger"  data-toggle="tooltip" title="FAILURE: ${r.timestamp}"></i>
                </c:otherwise>
              </c:choose>
            </a>
          </c:forEach>
        </span>

        <h1>${title}</h1>
      </div>

      <%--<div class="tab-content">--%>

        <%--<c:forEach items="${allReports}" var="report">--%>
          <c:set var="uniqueId" value="${report.id}-${report.timestamp.time}"></c:set>
          
          <%--<div role="tabpanel" class="tab-pane" id="${uniqueId}">--%>

            <div class="panel panel-default">
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
                  <label class="btn btn-default ${showSuccesses ? 'active' : ''}" data-toggle="tooltip" title="Toggle successful items" data-container="body">
                    <input id="toggle-success-${uniqueId}" 
                           type="checkbox" autocomplete="off" ${showSuccesses ? 'checked' : ''}>
                    <i class="fa fa-fw fa-check-circle-o text-success"></i>
                    Successful
                  </label>
                  <label class="btn btn-default ${showWarnings ? 'active' : ''}" data-toggle="tooltip" title="Toggle warning items" data-container="body">
                    <input id="toggle-warning-${uniqueId}" 
                           type="checkbox" autocomplete="off" ${showWarnings ? 'checked' : ''}>
                    <i class="fa fa-fw fa-exclamation-circle text-warning"></i>
                    Warnings
                  </label>
                  <label class="btn btn-default ${showFailures ? 'active' : ''}" data-toggle="tooltip" title="Toggle failure items" data-container="body">
                    <input id="toggle-failure-${uniqueId}" 
                           type="checkbox" autocomplete="off" ${showFailures ? 'checked' : ''}>
                    <i class="fa fa-fw fa-times-circle-o text-danger"></i>
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

                  <c:forEach items="${pagedListHolder.pageList}" var="subReport">

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

                <div class="text-center">
                    <nav>
                        <ul class="pagination">

                            <c:if test="${pagedListHolder.pageCount > 1}">
                                <c:if test="${!pagedListHolder.firstPage}">
                                    <li>
                                        <!-- link to previous page (pagedListHolder.getPage() - 1) -->
                                        <a href="?page=${pagedListHolder.getPage() - 1}&ts=${report.timestamp}">
                                            <span>&laquo;</span>
                                        </a>
                                    </li>
                                </c:if>
                                <c:if test="${pagedListHolder.firstLinkedPage > 0}">
                                    <!-- link to first page -->
                                    <li><a href="?page=0&ts=${report.timestamp}">1</a></li>
                                </c:if>
                                <c:if test="${pagedListHolder.firstLinkedPage > 1}">
                                    <li class="disabled"><span>...</span></li>
                                </c:if>
                                <c:forEach begin="${pagedListHolder.firstLinkedPage}" end="${pagedListHolder.lastLinkedPage}" var="i">
                                    <c:choose>
                                        <c:when test="${pagedListHolder.page == i}">
                                            <li class="active"><a href="#">${i+1}</a></li>
                                        </c:when>
                                        <c:otherwise>
                                            <!-- link to specific page (page i + 1) -->
                                            <li><a href="?page=${i}&ts=${report.timestamp}">${i+1}</a></li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                <c:if test="${pagedListHolder.lastLinkedPage < pagedListHolder.pageCount - 2}">
                                    <li class="disabled"><span>...</span></li>
                                </c:if>
                                <c:if test="${pagedListHolder.lastLinkedPage < pagedListHolder.pageCount - 1}">
                                    <!-- link to last page (pagedListHolder.getPageCount() - 1) -->
                                    <li><a href="?page=${pagedListHolder.getPageCount() - 1}&ts=${report.timestamp}">${pagedListHolder.pageCount}</a></li>
                                </c:if>
                                <c:if test="${!pagedListHolder.lastPage}">
                                    <!-- link to next page (pagedListHolder.getPage() + 1) -->
                                    <li><a href="?page=${pagedListHolder.getPage() + 1}&ts=${report.timestamp}">&raquo;</a></li>
                                </c:if>
                            </c:if>

                        </ul>
                    </nav>
                </div>
            </div>

    </div>
  </div>
</c2mon:template>


<script type="text/javascript">
  // Show/hide rows based on their category via the toggle buttons.
  $("[id|='toggle-success']").each(function() {
    $(this).change(function() {
      insertParam("s", this.checked ? "true" : "false");
    });
  });

  $("[id|='toggle-warning']").each(function() {
    $(this).change(function() {
      insertParam("w", this.checked ? "true" : "false");
    });
  });

  $("[id|='toggle-failure']").each(function() {
    $(this).change(function() {
      insertParam("f", this.checked ? "true" : "false");
    });
  });

  // Activate tooltips
  $(function () {
    $('[data-toggle="tooltip"]').tooltip()
  })

  /**
   * Add or modify a URL parameter
   */
  function insertParam(key, value) {
    key = escape(key);
    value = escape(value);

    var kvp = document.location.search.substr(1).split('&');
    if (kvp == '') {
      document.location.search = '?' + key + '=' + value;
    }
    else {

      var i = kvp.length;
      var x;
      while (i--) {
        x = kvp[i].split('=');
        if (x[0] == key) {
          x[1] = value;
          kvp[i] = x.join('=');
          break;
        }
      }

      if (i < 0) {
        kvp[kvp.length] = [key, value].join('=');
      }

      // reload the page
      window.location.search = kvp.join('&');
    }
  }

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