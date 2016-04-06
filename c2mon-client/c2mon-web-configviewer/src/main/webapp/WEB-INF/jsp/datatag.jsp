<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>


<!-- JSP variables -->
<c:url var="home" value=".." />
<c:url var="tagviewer" value="../tagviewer/form" />
<c:url var="history" value="/historyviewer/${tag.id}" />
<c:url var="trend" value="/trendviewer/${tag.id}" />

<c2mon:template title="${title}">

<style type="text/css">
th {
  width: 25%;
}

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
          <a href="<c:url value="${tagviewer}"/>">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${tag.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>DataTag: ${tag.name} (${tag.id})</h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
        <a href="${history}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-list"></span>
          View History
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
        <tbody>
          <tr>
            <th>Tag ID</th>
            <td>${tag.id}</td>
          </tr>
          <tr>
            <th>Tag Name</th>
            <td>${tag.name}</td>
          </tr>
          <tr>
            <th>Description</th>
            <td>${tag.description}</td>
          </tr>
          <tr>
            <th>Tag Value</th>
            <td>${tag.value}</td>
          </tr>
          <c:if test="${tag.unit != null}">
          <tr>
            <th>Unit</th>
            <td>${tag.unit}</td>
          </tr>
          </c:if>
          <tr>
            <th>Value Description</th>
            <td>${tag.valueDescription}</td>
          </tr>
          <tr>
            <th>Source Timestamp</th>
            <td>${tag.timestamp}</td>
          </tr>
          <tr>
            <th>DAQ Timestamp</th>
            <td>${tag.daqTimestamp}</td>
          </tr>
          <tr>
            <th>Server Timestamp</th>
            <td>${tag.serverTimestamp}</td>
          </tr>
          <tr>
            <th>Tag Quality</th>
            <td>
              <c:choose>
                <c:when test="${tag.dataTagQuality.valid == true}">OK</c:when>
                <c:otherwise>INVALID</c:otherwise>
              </c:choose>
            </td>
          </tr>
          <tr>
            <th>Mode</th>
            <td>${tag.mode}</td>
          </tr>
          <tr>
            <th>Simulated</th>
            <td>${tag.simulated}</td>
          </tr>
          <tr>
            <th>Topic Name</th>
            <td>${tag.topicName}</td>
          </tr>
          <tr>
            <th>Data Type</th>
            <td>${tag.value['class'].name}</td>
          </tr>
          <tr>
            <th>Metadata</th>
            <td>${tag.metadata}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <c:if test="${tag.dataTagQuality.valid == false}">
        <h3>Reason for tag invalidation</h3>
        <table class="table table-striped table-bordered">
          <thead>
          </thead>

          <tbody>
            <c:forEach var="state" items="${tag.dataTagQuality.invalidQualityStates}">
              <tr>
                <th>Quality Status</th>
                <td>${state.key}</td>
              </tr>
              <tr>
                <th>Description</th>
                <td>${state.value}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:if>
    </div>
  </div>

  <c:forEach var="alarm" items="${tag.alarms}">
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
          <a href="<c:url value="/historyviewer/${alarm.id}"/>" class="btn btn-default btn-large">
            <span class="glyphicon glyphicon-list"></span>
            View History
          </a>
          <a href="<c:url value="/trendviewer/${alarm.id}"/>" class="btn btn-default btn-large">
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
  </c:forEach>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>DataTag Configuration</h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <tbody>
          <tr>
            <th>Value Deadband</th>
            <td>${tagConfig.valueDeadband}</td>
          </tr>
          <tr>
            <th>Value Deadband Label</th>
            <td>${tagConfig.valueDeadbandType}</td>
          </tr>
          <tr>
            <th>Time Deadband</th>
            <td>${tagConfig.timeDeadband}</td>
          </tr>
          <tr>
            <th>Priority</th>
            <c:choose>
              <c:when test="${tagConfig.priority == 2}">
                <td>LOW</td>
              </c:when>
              <c:otherwise>
                <td>HIGH</td>
              </c:otherwise>
            </c:choose>
          </tr>
          <tr>
            <th>Guaranteed Delivery</th>
            <td>${tagConfig.guaranteedDelivery}</td>
          </tr>
          <c:if test="${fn:length(tagConfig.ruleIds) > 0}">
            <tr>
              <th>Rule IDs</th>
              <td>${tagConfig.ruleIds}</td>
            </tr>
          </c:if>
          <c:if test="${fn:length(tagConfig.ruleExpression) > 0}">
            <tr>
              <th>Rule Expression</th>
              <td>${tagConfig.ruleExpression}</td>
            </tr>
          </c:if>
          <c:if test="${tagConfig.minValue != null}">
            <tr>
              <th>Min Value</th>
              <td>${tagConfig.minValue}</td>
            </tr>
          </c:if>
          <c:if test="${tagConfig.maxValue != null}">
            <tr>
              <th>Max Value</th>
              <td>${tagConfig.maxValue}</td>
            </tr>
          </c:if>
          <tr>
            <th>Control Tag</th>
            <td>${tagConfig.controlTag}</td>
          </tr>
          <tr>
            <th>Logged</th>
            <td>${tagConfig.logged}</td>
          </tr>
          <tr>
            <th>Publications</th>
            <td>
              <c:if test="${fn:length(tagConfig.japcPublication) > 0}">
                <p>
                  <b>JAPC: </b>${tagConfig.japcPublication}</p>
              </c:if>
              <c:if test="${fn:length(tagConfig.dipPublication) > 0}">
                <b>DIP: </b>${tagConfig.dipPublication}
            </c:if>
            </td>
          </tr>
          <tr>
            <th>Process Names</th>
            <td>${tagConfig.processNames}</td>
          </tr>
          <tr>
            <th>Alarms</th>
            <td>
              <c:forEach var="alarmId" items="${tagConfig.alarmIds}">
                <a href="<c:url value="/alarmviewer/${alarmId}"/>"> ${alarmId} </a>
              </c:forEach>
            </td>
          </tr>
          <tr>
            <th>Hardware Address</th>
            <td>
              <c:if test="${fn:length(tagConfig.hardwareAddress) > 0}">
                <!-- Trim a bit of whitespace -->
                <xmp>${fn:trim(fn:replace(tagConfig.hardwareAddress, '        ', ''))}</xmp>
              </c:if>
            </td>
          </tr>

        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>
