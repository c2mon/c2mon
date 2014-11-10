<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<!DOCTYPE html>
<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="<c:url value="../css/form.css"/>" />
<%-- <link rel="stylesheet" type="text/css" href="<c:url value="../css/c2mon.css"/>" /> --%>
<link rel="stylesheet" type="text/css" href="<c:url value="../css/bootstrap/bootstrap.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="../css/web-config-viewer.css"/>" />

<style type="text/css">
body {
  padding-top: 50px;
  padding-bottom: 40px;
}

.sidebar-nav {
  padding: 9px 0;
}
</style>

<link type="text/css" href="../css/ui-lightness/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery-spinner.js"></script>
<script type="text/javascript" src="../js/jquery-ui.js"></script>

<script type="text/javascript">
  $(function() {

    // Reset the Progressbar
    $("#progressbar").progressbar({
      value : 0
    });

    $('#configLoaderForm').submit(function(event) {
      // prevent default browser behaviour
      event.preventDefault();
      startProcess();
    });
  });

  /**
   * Called when the submit button is pressed.
   */
  function startProcess() {

    var submittedNumber = parseInt(document.configLoaderForm.id.value);
    if (isNaN(submittedNumber)) {
      return;
    }

    startConfigurationRequest(); // starts the apply configuration request
    getProgress(); // polls the server and updates the progress bar
    getProgressDescription(); // polls the server and updates the description info

    $("#status").text("Starting...");

    document.configLoaderForm.id.readOnly = true;
    document.getElementsByName("submitButton")[0].disabled = true;

    var $this = $("#progressbar");
    $("#status").spinner();
  }

  /**
   * Redirects the user to the final "ConfigurationReport"
   * once the Request has finished executing in the server. 
   */
  function progressFinished() {

    window.location = "../configloader/progress/finalReport/"
        + document.configLoaderForm.id.value;
  }

  /**
   * When the submit button is pressed,
   * this call starts the ApplyConfiguration Request on the web server.
  
   * Calls "progressFinished()" when the "ApplyConfiguration" Request is completed.
   *
   * The configurationId is retrieved from the textfield
   */
  function startConfigurationRequest() {
    $.ajax({
      type : "POST",
      url : "../configloader/progress/start",
      data : {
        configurationId : parseInt(document.configLoaderForm.id.value)
      },
      async : true,
      complete : progressFinished
    });
  }

  /**
   * Runs every "timeout" milliseconds and polls the server 
   * for information about the current progress of the Request.
   */
  function getProgress() {

    $.ajax({
      type : "POST",
      url : "../configloader/progress/getProgress",
      data : {
        configurationId : document.configLoaderForm.id.value
      },
      async : true,
      success : function(data) {

        //Update progressbar
        $("#progressbar").progressbar({
          value : data
        });

      },
      dataType : "json",
      complete : getProgress,
      timeout : 50
    });
  }

  /**
   * Runs every "timeout" milliseconds and polls the server 
   * for a description of the current progress of the Request.
   */
  function getProgressDescription() {

    $
        .ajax({
          type : "POST",
          url : "../configloader/progress/getProgressDescription",
          data : {
            configurationId : document.configLoaderForm.id.value
          },
          async : true,
          success : function(data) {

            var description = data;
            if (description == null) {
              description = "No response has been received yet from the server. Please wait..";
            }
            $("#status").text(description);

          },
          dataType : "json",
          complete : getProgressDescription,
          timeout : 50
        });
  }
</script>
</head>

<body>

  <div class="container">
    <div class="row">

      <ul class="breadcrumb">
        <li><a href="../">Home</a> <span class="divider"></span></li>
        <li>${title}</li>

        <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
          <span class="pull-right">
            <a href="../j_spring_security_logout">Logout&nbsp;</a>
            <span class="glyphicon glyphicon-log-out"></span>
          </span>
        </sec:authorize>
      </ul>

      <div class="jumbotron">
        <h1>${title}</h1>
      </div>

      <div class="alert alert-info">
        <strong>${instruction}</strong>
      </div>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

      <form:form id="configLoaderForm" name="configLoaderForm" class="well form-inline" action="${submitUrl}" method="post">

        <div class="input-group">
          <div class="input-group-addon">Config ID</div>
          <input class="form-control" id="config_id_input" style="display: inline" type="text" name="id" value="${formTagValue}" />
        </div>

        <input id="submitButton" class="btn btn-large btn-primary" name="submitButton" type="submit" value="Submit">

        <div style="margin-left: 40px; display: inline;">
          <span id="status"></span>
        </div>

        <div id="progressbar" style="margin-top: 20px;"></div>
      </form:form>


      <div style="margin-top: 50px;">

        <c:if test="${not empty reports}">
          <table class="inline" align="center">
            <tr>
              <th>Previously Applied Configurations</th>
            </tr>
            <c:forEach var="report" items="${reports}">
              <tr>
                <td align="center"><a href="../configloader/progress/finalReport/${report.key}"> ${report.key}</a> (<a
                    href="../configloader/progress/finalReport/xml/${report.key}" target="_blank">XML</a>)</td>
              </tr>
            </c:forEach>
          </table>
        </c:if>

      </div>
    </div>
  </div>

  <br />
  <br />

</body>
</html>


