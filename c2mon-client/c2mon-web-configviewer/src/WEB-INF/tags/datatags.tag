<%@ attribute name="equipment" required="true" type="cern.c2mon.shared.common.process.EquipmentConfiguration"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="tagviewer" value="/tagviewer" />

<style type="text/css">
.hiddenRow tr {
  padding: 0 !important;
  cursor: default;
}

.hiddenRow:hover {
  background-color: #fff;
}
</style>

<c:choose>
  <c:when test="${fn:length(equipment.sourceDataTags) == 0}">
    <div class="panel-body">
      <p>This Equipment has no DataTags configured.</p>
    </div>
  </c:when>

  <c:otherwise>

    <div class="panel-body">
      <h3>DataTags</h3>
      <p>
        Click on a DataTag to view its configuration.
        <!-- You can use the search box below to search within DataTag configurations. You can also
        expand/collapse all configurations with the button below. -->
      </p>

      <!--       <form class="form-inline pull-left">
        <div class="input-group">
          <input type="text" class="form-control filter" placeholder="Search...">
          <span class="input-group-btn">
            <button class="btn btn-default" type="button">
              &nbsp;
              <i class="fa fa-search"></i>
              &nbsp;
            </button>
          </span>
        </div>
      </form> -->

      <div class="btn-group" role="group">
        <button id="expand-all-datatags" class="btn btn-default">
          <i class="fa fa-expand"></i>
          Expand All
        </button>
        <button id="collapse-all-datatags" class="btn btn-default">
          <i class="fa fa-compress"></i>
          Collapse All
        </button>
      </div>

    </div>

    <div class="table-responsive">
      <table class="table table-bordered table-hover" style="border-collapse: collapse;">
        <thead>
          <tr>
            <th class="col-md-1">ID</th>
            <th class="col-md-5">Name</th>
            <th class="col-md-3">Equipment</th>
            <th class="col-md-1">Data Type</th>
            <th class="col-md-1">Control</th>
            <th class="col-md-1">Mode</th>
            <th class="col-md-1"></th>
          </tr>
        </thead>

        <tbody class="searchable">

          <c:forEach items="${equipment.sourceDataTags}" var="entry">
            <c:set var="tag" value="${entry.value}"></c:set>

            <tr data-toggle="collapse" data-target="#collapse-tag-${tag.id}" class="accordion-toggle clickable">
              <td>${tag.id}</td>
              <td>${tag.name}</td>
              <td>${equipment.name}</td>
              <td>${tag.dataType}</td>
              <td>${tag.controlTag}</td>
              <td>
                <c:choose>
                  <c:when test="${tag.inOperation}">OPERATIONAL</c:when>
                  <c:when test="${tag.inTest}">TEST</c:when>
                  <c:when test="${tag.inMaintenance}">MAINTENANCE</c:when>
                </c:choose>
              </td>
              <td>
                <a href="${tagviewer}/${tag.id}" class="view-tag btn btn-default btn-sm">
                  <i class="fa fa-external-link"></i>
                  View Tag
                </a>
              </td>
            </tr>

            <tr>
              <td colspan="7" class="hiddenRow">
                <div class="accordion-body collapse datatag-accordion" id="collapse-tag-${tag.id}">

                  <table class="table table-striped table-bordered" style="margin-bottom: 0px;">
                    <thead>
                    </thead>
                    <tbody>
                      <%--
                    <c:if test="${not empty tag.minValue}">
                      <tr>
                        <th>Min Value</th>
                        <td class="col-sm-10">${tag.minValue}</td>
                      </tr>
                    </c:if>
                    <c:if test="${not empty tag.maxValue}">
                      <tr>
                        <th>Max Value</th>
                        <td class="col-sm-10">${tag.maxValue}</td>
                      </tr>
                    </c:if>
 --%>
                      <tr>
                        <td colspan="5">
                          <div class="col-md-12">
                            <h4>Address</h4>

                            <table class="table table-striped table-bordered">
                              <thead>
                              </thead>
                              <tbody>
                                <tr>
                                  <th>Priority</th>
                                  <td>${tag.address.priority}</td>
                                </tr>
                                <tr>
                                  <th>Guaranteed Delivery</th>
                                  <td>${tag.address.guaranteedDelivery}</td>
                                </tr>
                                <c:if test="${not empty tag.address.timeToLive}">
                                  <tr>
                                    <th>Time To Live</th>
                                    <td>${tag.address.timeToLive}</td>
                                  </tr>
                                </c:if>
                                <c:if test="${not empty tag.address.valueDeadband}">
                                  <tr>
                                    <th>Value Deadband</th>
                                    <td>${tag.address.valueDeadband}</td>
                                  </tr>
                                </c:if>
                                <c:if test="${not empty tag.address.valueDeadbandType}">
                                  <tr>
                                    <th>Value Deadband Type</th>
                                    <td>${tag.address.valueDeadbandType}</td>
                                  </tr>
                                </c:if>
                                <c:if test="${not empty tag.address.timeDeadband}">
                                  <tr>
                                    <th>Time Deadband</th>
                                    <td>${tag.address.timeDeadband}</td>
                                  </tr>
                                </c:if>
                                <tr>
                                  <th>Hardware Address</th>
                                  <td>
                                    <xmp>${fn:trim(fn:replace(tag.address.hardwareAddress, '        ', ''))}</xmp>
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:otherwise>
</c:choose>
