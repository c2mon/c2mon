<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c:url var="home" value="../" />
<c:url var="commandviewer" value="../commandviewer/form" />

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
          <a href="<c:url value="${commandviewer}"/>">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${tag.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>CommandTag: ${tag.name} ( ${tag.id} )</h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <tbody>
          <tr>
            <th>Command ID</th>
            <td>${tag.id}</td>
          </tr>
          <tr>
            <th>Name</th>
            <td>${tag.name}</td>
          </tr>
          <tr>
            <th>Description</th>
            <td>${tag.description}</td>
          </tr>
          <tr>
            <th>Value Type</th>
            <td>${tag.valueType}</td>
          </tr>
          <tr>
            <th>Client Timeout</th>
            <td>${tag.clientTimeout}</td>
          </tr>
          <tr>
            <th>Process ID</th>
            <td>${tag.processId}</td>
          </tr>
          <tr>
            <th>Equipment ID</th>
            <td>${tag.equipmentId}</td>
          </tr>
          <tr>
            <th>Min Value</th>
            <td>${tag.minValue}</td>
          </tr>
          <tr>
            <th>Max Value</th>
            <td>${tag.maxValue}</td>
          </tr>
          <tr>
            <th>Hardware Address</th>
            <td>
              <xmp>${fn:trim(fn:replace(tag.hardwareAddress, '        ', ''))}</xmp>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>