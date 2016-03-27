<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>


<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="processviewer" value="../process/form" />
<c:url var="tagviewer" value="../tagviewer" />
<c:url var="xml" value="../process/xml" />

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
        <li>
          <a href="<c:url value="${home}"/>"> Home </a>
          <span class="divider"></span>
        </li>
        <li>
          <a href="<c:url value="${processviewer}"/>">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${process.processName}</li>
      </ul>

      <div class="row">
        <div class="col-lg-12">
          <div class="page-header">
            <h1>${title}
              <small>${process.processName}</small>
            </h1>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col-lg-12">

          <div class="panel panel-default">
            <div class="panel-heading clearfix">
              <h1 class="panel-title pull-left" style="padding-top: 7.5px;">
                Process Configuration: <strong>${process.processName}</strong>
              </h1>
              <span class="pull-right">
                <a href="${xml}/${process.processName}" class="btn btn-default btn-sm">
                  <i class="fa fa-user fa-code"></i>
                  Download XML
                </a>
              </span>
            </div>

            <div class="panel-body">
              <p>Use the tabs below to view the configuration of the Equipment, SubEquipment, DataTags and Commands of this DAQ Process.</p>
            </div>

            <table class="table table-striped table-bordered">

              <tbody>
                <tr>
                  <th>Process ID</th>
                  <td>${process.processID}</td>
                </tr>
                <tr>
                  <th>Alive Tag ID</th>
                  <td>
                    <a href="${tagviewer}/${process.aliveTagID}">${process.aliveTagID}</a>
                  </td>
                </tr>
                <tr>
                  <th>Alive Interval</th>
                  <td>${process.aliveInterval}</td>
                </tr>
                <tr>
                  <th>Max Message Size</th>
                  <td>${process.maxMessageSize}</td>
                </tr>
                <tr>
                  <th>Max Message Delay</th>
                  <td>${process.maxMessageDelay}</td>
                </tr>
              </tbody>
            </table>

            <div class="panel-body">
              <h3>Equipment</h3>
              <p>
                Click on an Equipment to view its configuration.
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

              <div class="btn-group" role="group" style="margin-bottom: 20px;">
                <button id="expand-all-equipment" class="btn btn-default">
                  <i class="fa fa-expand"></i>
                  Expand All
                </button>
                <button id="collapse-all-equipment" class="btn btn-default">
                  <i class="fa fa-compress"></i>
                  Collapse All
                </button>
              </div>


              <div class="table-responsive">
                <table class="table table-bordered table-hover" style="border-collapse: collapse;">
                  <thead>
                    <tr>
                      <th class="col-md-1">ID</th>
                      <th class="col-md-10">Name</th>
                      <th class="col-md-1"></th>
                    </tr>
                  </thead>

                  <tbody class="searchable">

                    <c:forEach items="${process.equipmentConfigurations}" var="entry">
                      <c:set var="equipment" value="${entry.value}"></c:set>

                      <tr data-toggle="collapse" data-target="#collapseme-${equipment.id}" class="accordion-toggle clickable">
                        <td>${equipment.id}</td>
                        <td>${equipment.name}</td>
                        <td>
                          <a href="<c:url value="../process/${process.processName}/equipment/${equipment.id}" />"
                            class="view-equipment btn btn-default btn-sm">
                            <i class="fa fa-external-link"></i>
                            View Equipment
                          </a>
                        </td>
                      </tr>

                      <tr>
                        <td colspan="3" class="hiddenRow">
                          <div class="accordion-body collapse equipment-accordion" id="collapseme-${equipment.id}">

                            <table class="table table-striped table-bordered" style="margin-bottom: 0px;">
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
                          </div>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
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
  $('#expand-all-equipment').click(function(e) {
    e.preventDefault();
    $('.equipment-accordion').collapse('show');
  });
  $('#collapse-all-equipment').click(function(e) {
    e.preventDefault();
    $('.equipment-accordion').collapse('hide');
  });
  
  // Make sure the accordion isn't opened when the buttons are clicked
  $('.view-equipment').click(function(e) {
    e.stopPropagation();
  });

  // Experimental: search feature
  /*   $('input.filter').on('keyup', function() {
   var rex = new RegExp($(this).val(), 'i');
   $('.searchable tr').hide();
   $('.searchable tr').filter(function() {
   if (rex.test($(this).text())) {
   $(this).find('.accordion-body').collapse('show');
   return true;
   }

   return false;
   }).show();
   }); */
</script>