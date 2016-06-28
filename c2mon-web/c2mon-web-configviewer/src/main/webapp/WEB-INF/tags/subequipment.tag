<%@ attribute name="equipment" required="true" type="cern.c2mon.shared.common.process.EquipmentConfiguration"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:choose>
  <c:when test="${fn:length(equipment.subEquipmentConfigurations) == 0}">
    <div class="panel-body">
      <p>This Equipment has no SubEquipment configured.</p>
    </div>
  </c:when>

  <c:otherwise>
    <div class="panel-body">
      <h3>SubEquipments</h3>
      <span>Click on a SubEquipment to view its configuration.</span>
    </div>

    <table class="table table-bordered table-hover" style="border-collapse: collapse;">
      <thead>
        <tr>
          <th class="col-sm-2">ID</th>
          <th class="col-sm-10">Name</th>
        </tr>
      </thead>

      <tbody>

        <c:forEach items="${equipment.subEquipmentConfigurations}" var="entry">
          <c:set var="subEquipment" value="${entry.value}"></c:set>

          <tr data-toggle="collapse" data-target="#collapse-subeq-${subEquipment.id}" class="accordion-toggle clickable">
            <td>${subEquipment.id}</td>
            <td>${subEquipment.name}</td>
          </tr>

          <tr>
            <td colspan="5" class="hiddenRow">
              <div class="accordion-body collapse" id="collapse-subeq-${subEquipment.id}">

                <table class="table table-striped table-bordered">
                  <thead>
                  </thead>
                  <tbody>
                    <tr>
                      <th class="col-sm-2">CommFault Tag ID</th>
                      <td>${subEquipment.commFaultTagId}</td>
                    </tr>
                    <tr>
                      <th class="col-sm-2">CommFault Tag Value</th>
                      <td>${subEquipment.commFaultTagValue}</td>
                    </tr>
                    <tr>
                      <th class="col-sm-2">Alive Tag ID</th>
                      <td>${subEquipment.aliveTagId}</td>
                    </tr>
                    <tr>
                      <th class="col-sm-2">Alive Tag Interval</th>
                      <td>${subEquipment.aliveInterval}</td>
                    </tr>
                  </tbody>
                </table>

              </div>
            </td>
          </tr>

        </c:forEach>

      </tbody>
    </table>
  </c:otherwise>
</c:choose>
