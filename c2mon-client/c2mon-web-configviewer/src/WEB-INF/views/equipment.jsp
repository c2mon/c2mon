<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>


<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="processviewer" value="../../form" />
<c:url var="tagviewer" value="../tagviewer" />

<c2mon:template title="${title}">

  <style type="text/css">
th {
  width: 25%;
}

.page-header {
  margin-top: 20px !important;
}

.hiddenRow {
  padding: 0 !important;
}

.hiddenRow tr {
  cursor: default;
}

.hiddenRow:hover {
  background-color: #fff;
}
</style>

  <div class="row">

    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li><a href="<c:url value="${home}"/>"> Home </a> <span class="divider"></span></li>
        <li><a href="<c:url value="${processviewer}"/>">DAQ Process Viewer</a> <span class="divider"></span></li>
        <li><a href="<c:url value="/process/${process.processName}"/>"> ${process.processName}</a> <span class="divider"></span></li>
        <li>Equipment Viewer <span class="divider"></span>
        </li>
        <li>${equipment.name}<span class="divider"></span>
        </li>
      </ul>

      <div class="row">
        <div class="col-lg-12">
          <div class="page-header">
            <h1>${title}
              <small>${equipment.name}</small>
            </h1>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-lg-12">

          <div class="panel panel-default">
            <div class="panel-heading clearfix">
              <h1 class="panel-title pull-left" style="padding-top: 7.5px;">
                Equipment Configuration: <strong>${equipment.name}</strong>
              </h1>
              <div class="btn-group pull-right" role="group">
                <button class="btn btn-default" disabled>
                  <i class="fa fa-tags"></i> ${fn:length(equipment.sourceDataTags)}
                </button>
                <button class="btn btn-default" disabled>
                  <i class="fa fa-code-fork"></i> ${fn:length(equipment.subEquipmentConfigurations)}
                </button>
                <button class="btn btn-default" disabled>
                  <i class="fa fa-terminal"></i> ${fn:length(equipment.sourceCommandTags)}
                </button>
              </div>
            </div>

            <div class="panel-body">

              <p>Use the tabs below to view the configuration of the SubEquipment, DataTags and Commands of this Equipment.</p>
            </div>

            <table class="table table-striped table-bordered">
              <tbody>
                <tr>
                  <th>Process ID</th>
                  <td>${process.processID} <a id="view-process" href="<c:url value="/process/${process.processName}" />"
                    class="btn btn-default btn-sm pull-right"> <i class="fa fa-external-link fa-fw"></i> View Process
                  </a>

                  </td>
                </tr>
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

            <div class="panel-body">
              <div role="tabpanel">

                <!-- Nav tabs -->
                <ul class="nav nav-tabs" role="tablist" id="tabs">
                  <li role="presentation" class="active"><a href="#datatags" aria-controls="datatags" role="tab" data-toggle="tab">DataTags</a></li>
                  <li role="presentation"><a href="#subequipment" aria-controls="subequipment" role="tab" data-toggle="tab">SubEquipment</a></li>
                  <li role="presentation"><a href="#commands" aria-controls="commands" role="tab" data-toggle="tab">Commands</a></li>
                </ul>

                <!-- Tab panes -->
                <div class="tab-content">
                  <div role="tabpanel" class="tab-pane active" id="datatags">
                    <c2mon:datatags equipment="${equipment}"></c2mon:datatags>
                  </div>
                  
                  <div role="tabpanel" class="tab-pane" id="subequipment">
                    <c2mon:subequipment equipment="${equipment}"></c2mon:subequipment>
                  </div>

                  <div role="tabpanel" class="tab-pane" id="commands">
                    <c2mon:commands equipment="${equipment}"></c2mon:commands>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</c2mon:template>

<script type="text/javascript">
  // Expand/collapse buttons
  $('#expand-all-subequipment').click(function(e) {
    e.preventDefault();
    $('.subequipment-accordion').collapse('show');
  });
  $('#collapse-all-subequipment').click(function(e) {
    e.preventDefault();
    $('.subequipment-accordion').collapse('hide');
  });

  $('#expand-all-datatags').click(function(e) {
    e.preventDefault();
    $('.datatag-accordion').collapse('show');
  });
  $('#collapse-all-datatags').click(function(e) {
    e.preventDefault();
    $('.datatag-accordion').collapse('hide');
  });

  // Tab click handler
  $('#tabs a').click(function(e) {
    e.preventDefault();
    $(this).tab('show');
  });

  // Store the currently selected tab in the hash value
  $("ul.nav-tabs > li > a").on("shown.bs.tab", function(e) {
    var id = $(e.target).attr("href").substr(1);
    window.location.hash = id;
  });

  // Make sure the accordion isn't opened when the buttons are clicked
  $('.view-tag').click(function(e) {
    e.stopPropagation();
  });
  
  // on load of the page: switch to the currently selected tab
  var hash = window.location.hash;
  $('#tabs a[href="' + hash + '"]').tab('show');
</script>
