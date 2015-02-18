<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="/" />
<c:url var="full_report" value="/configloader/progress/finalReport" />

<c2mon:template title="${title}">

<style type="text/css">
.table-hover tbody tr:hover td, .table-hover tbody tr:hover th {
  background-color: #eeeeea;
}

tr {
  cursor: pointer;
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
          ${title}
          <span class="divider"></span>
        </li>
      </ul>

      <div class="page-header">
        <h1>${title}</h1>
      </div>

      <div class="panel panel-default">
        <div class="panel-heading">
          <h3 class="panel-title">All previously applied configurations</h3>
        </div>
        <div class="panel-body">Click on a row in the table to view the full report for that configuration.</div>

        <table class="table table-striped table-hover">
          <thead>
            <tr>
              <th>Configuration ID</th>
              <th>Configuration Name</th>
              <th>Applied On</th>
              <th>Applied By</th>
              <th>Status</th>
            </tr>
          </thead>

          <tbody>
            <c:forEach items="${cache}" var="entry">
              <c:set var="id" value="${entry.key}" />
              <c:set var="report" value="${entry.value}" />

              <tr>
                <td class="id">${id}</td>
                <td>${report.name}</td>
                <td>${report.timestamp}</td>
                <td>${report.user}</td>
                <td class="cell-label">
                  <c:choose>
                    <c:when test="${report.status == 'FAILURE'}">
                      <span class="label label-danger">FAILURE</span>
                    </c:when>
                    <c:when test="${report.status == 'RESTART'}">
                      <span class="label label-warning">RESTART</span>
                    </c:when>
                    <c:otherwise>
                      <span class="label label-success">OK</span>
                    </c:otherwise>
                  </c:choose>
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
  $('.table > tbody > tr').click(function() {
    // Table row was clicked
    var id = $(this).find('td.id').text();
    window.location.href = "${full_report}/" + id;
  });
</script>
