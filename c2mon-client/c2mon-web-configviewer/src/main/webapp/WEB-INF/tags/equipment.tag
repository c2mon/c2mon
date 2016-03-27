<%@ attribute name="equipment" required="true" type="cern.c2mon.shared.common.process.EquipmentConfiguration"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:url var="tagviewer" value="../tagviewer" />
<c:url var="commandviewer" value="../commandviewer" />

<div class="panel-body">
  <h3>
    Equipment:
    <a href="#${equipment.name}">${equipment.name}</a>
    (${equipment.id})
  </h3>
</div>

<table class="table table-striped table-bordered">
  <thead></thead>
  <tbody>
    <tr>
      <th>Equipment ID</th>
      <td>${equipment.id}</td>
    </tr>
    <tr>
      <th>Equipment Name</th>
      <td>${equipment.name}</td>
    </tr>
    <tr>
      <th>CommFault Tag ID</th>
      <td>${equipment.commFaultTagId}</td>
    </tr>
    <tr>
      <th>CommFault Tag Value</th>
      <td>${equipment.commFaultTagValue}</td>
    </tr>
    <tr>
      <th>Alive Tag ID</th>
      <td>${equipment.aliveTagId}</td>
    </tr>
    <tr>
      <th>Alive Tag Interval</th>
      <td>${equipment.aliveTagInterval}</td>
    </tr>
    <tr>
      <th>Handler Class Name</th>
      <td>${equipment.handlerClassName}</td>
    </tr>
    <tr>
      <th>Address</th>
      <td>${equipment.address}</td>
    </tr>
  </tbody>
</table>
