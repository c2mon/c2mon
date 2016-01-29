<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:set var="tag" value="${history[0]}" />
<c:url var="home" value=".." />
<c:url var="historyviewer" value="../historyviewer/form" />
<c:url var="tagviewer" value="../tagviewer/${tag.id}" />
<c:url var="trend" value="/trendviewer/${tag.id}" />

<c2mon:template title="${title}">

<style type="text/css">
.page-header {
  margin-top: -20px !important;
}

tr.invalid {
  color: #000000;
  background: #D9EDF7 !important;
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
          <a href="<c:url value="${historyviewer}"/>">Tag History</a>
          <span class="divider"></span>
        </li>
        <li>${tag.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>
          Tag History: ${tag.id} <small>${description}</small>
        </h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
        <a href="${tagviewer}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-tags"></span>
          &nbsp;View Tag
        </a>
        <a href="${trend}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-stats"></span>
          View Trend
        </a>
      </p>

      <!-- Only show the HelpAlarm button if the property is defined. -->
      <c:if test="${fn:length(help_url) > 0}">
        <p class="pull-right btn-toolbar">
          <a href="${help_url}" class="btn btn-default btn-large btn-danger">
            <span class="glyphicon glyphicon-question-sign"></span>
            View Help Alarm
          </a>
        </p>
      </c:if>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <thead>
          <tr>
            <th>Server Timestamp</th>
            <th>Value</th>
            <th>Quality description</th>
            <th>Value Description</th>
            <th>Source Timestamp</th>
            <th>Mode</th>
          </tr>
        </thead>

        <tbody>
          <c:forEach var="item" items="${history}">

            <!-- Used to display a light-blue line in case of a datatag with invalid quality -->
            <c:choose>
              <c:when test="${item.dataTagQuality.valid == true}">
                <c:set var="quality_status" value="ok" />
              </c:when>
              <c:otherwise>
                <c:set var="quality_status" value="invalid" />
              </c:otherwise>
            </c:choose>

            <tr class="${quality_status}">
              <td>${item.serverTimestamp}</td>
              <td>${item.value}</td>

              <td>
                <c:choose>
                  <c:when test="${item.dataTagQuality.valid == false}">
                    <c:forEach var="entry" items="${item.dataTagQuality.invalidQualityStates}">
                      <p>${entry.key}-${entry.value}</p>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <p>OK</p>
                  </c:otherwise>
                </c:choose>
              </td>

              <td>${item.description}</td>
              <td>${item.sourceTimestamp}</td>
              <td>${item.mode}</td>
            </tr>
          </c:forEach>

        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>