<%@ attribute name="list" required="true" type="java.util.ArrayList"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="c2mon"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${!empty list}">

  <table class="table table-striped table-bordered">
    <thead>
      <tr>
        <th class="col-sm-1"></th>
        <th class="col-sm-2">Action</th>
        <th class="col-sm-2">Entity</th>
        <th class="col-sm-2">Id</th>
        <th class="col-sm-5">Status</th>
      </tr>
    </thead>
    <tbody>


      <c:forEach items="${list}" var="subReport">

        <tr data-toggle="collapse" data-target="#collapseme-sub-${subReport.action}-${subReport.id}" class="accordion-toggle">
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

        <tr>
          <td colspan="5" class="hiddenRow">
            <div class="accordian-body collapse" id="collapseme-sub-${subReport.action}-${subReport.id}">

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
</c:if>