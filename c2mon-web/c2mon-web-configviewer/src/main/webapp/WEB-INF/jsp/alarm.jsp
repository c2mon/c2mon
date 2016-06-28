<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value=".." />
<c:url var="alarmviewer" value="../alarmviewer/form" />
<c:url var="history" value="../historyviewer/${alarm.id}" />
<c:url var="trend" value="../trendviewer/${alarm.id}" />

<c2mon:template title="${title}">

<style type="text/css">
.page-header {
  margin-top: -20px !important;
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
          <a href="<c:url value="${alarmviewer}"/>">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${alarm.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>Alarm: ${alarm.faultFamily} : ${alarm.faultMember} : ${alarm.faultCode}</h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
        <a href="<c:url value="${history}"/>" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-list"></span>
          View History
        </a>
        <a href="<c:url value="${trend}"/>" class="btn btn-default btn-large">
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
        <tbody>
          <tr>
            <th>Alarm ID</th>
            <td>${alarm.id}</td>
          </tr>
          <tr>
            <th>Class</th>
            <td>${alarm['class'].name}</td>
          </tr>
          <tr>
            <th>DataTag</th>
            <td>
              <a href="<c:url value="/tagviewer/${alarm.tagId}"/>">${alarm.tagId}</a>
            </td>
          </tr>
          <tr>
            <th>State</th>
            <td>
              <c:choose>
                <c:when test="${alarm.active == false}">
                    TERMINATED
                  </c:when>
                <c:when test="${alarm.active == true}">
                    ACTIVE
                  </c:when>
              </c:choose>
            </td>
          </tr>
          <tr>
            <th>Fault Family</th>
            <td>${alarm.faultFamily}</td>
          </tr>
          <tr>
            <th>Fault Member</th>
            <td>${alarm.faultMember}</td>
          </tr>
          <tr>
            <th>Fault Code</th>
            <td>${alarm.faultCode}</td>
          </tr>
          <tr>
            <th>Info</th>
            <td>${alarm.info}</td>
          </tr>
          <tr>
            <th>Timestamp</th>
            <td>${alarm.timestamp}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>