<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="confighistory" value="/confighistory" />

<c2mon:template title="${title}">

<style type="text/css">
.progress {
  margin-top: 20px;
  height: 30px;
  margin-bottom: 0px;
}
</style>

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li><a href="../">Home</a> <span class="divider"></span></li>
        <li>${title}</li>
      </ul>

      <div class="page-header">
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

        <div class="progress">
          <div id="progressbar" class="progress-bar" aria-valuemin="0" aria-valuemax="100"></div>
        </div>
      </form:form>

      <span> Note: To view previously applied configurations, please go <a href="${confighistory}">here</a> .
      </span>
    </div>
  </div>
</c2mon:template>

<script type="text/javascript" src="../js/jquery/jquery-spinner.js"></script>

<script type="text/javascript">
  $(function() {
    // Reset the Progressbar
    $("#progressbar").css('width', '0%');

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
    window.location = "../configloader/progress/finalReport/" + document.configLoaderForm.id.value;
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

        // Update progressbar
        $("#progressbar").css('width', data + '%');

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
    $.ajax({
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


