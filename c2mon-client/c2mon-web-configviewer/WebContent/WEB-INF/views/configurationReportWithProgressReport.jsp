<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="<c:url value="../css/form.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="../css/tim.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="../css/webConfigViewer.css"/>" />

<link type="text/css" href="../css/ui-lightness/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery-spinner.js"></script>
<script type="text/javascript" src="../js/jquery-ui.js"></script>


<script type="text/javascript">


/**
 * Sets the default value of the progress bar to 0
 */
$(function(){

	// Reset the Progressbar
	$("#progressbar").progressbar({
		value: 0
	});
});


/**
 * Called when the submit button is pressed.
 */
function startProcess() {

	var submittedNumber = parseInt (document.theOnlyFormInThisPage.id.value);
	if (isNaN(submittedNumber) ) {
		return;
	}

	startConfigurationRequest(); // starts the apply configuration request
	getProgress(); // polls the server and updates the progress bar
	getProgressDescription(); // polls the server and updates the description info

	$("p").text("Starting...");

	document.theOnlyFormInThisPage.id.readOnly = true;
	document.getElementsByName("submitButton")[0].disabled = true;
	
	var $this = $("#progressbar");
	$this.spinner();
}

/**
 * Redirects the user to the final "ConfigurationReport"
 * once the Request has finished executing in the server. 
 */
 function progressFinished() {

		window.location = "../configloader/progress/finalReport/" 
			+ document.theOnlyFormInThisPage.id.value;
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
    		type: "POST",
        url: "../configloader/progress/start",
        data: { configurationId : parseInt (document.theOnlyFormInThisPage.id.value) },
				async: true,
				complete: progressFinished
	});
}

/**
 * Runs every "timeout" milliseconds and polls the server 
 * for information about the current progress of the Request.
 */
function getProgress() {
	
    $.ajax({ 
		type: "POST",
    url: "../configloader/progress/getProgress",
    data: { configurationId : document.theOnlyFormInThisPage.id.value },
			async: true,
    success: function(data){

    //Update progressbar
    $("#progressbar").progressbar({
    	value: data
			});
	    
	}, dataType: "json", complete: getProgress, timeout: 50 });
}


/**
 * Runs every "timeout" milliseconds and polls the server 
 * for a description of the current progress of the Request.
 */
function getProgressDescription() {
	
    $.ajax({ 
		type: "POST",
    url: "../configloader/progress/getProgressDescription",
    data: { configurationId : document.theOnlyFormInThisPage.id.value },
			async: true,
    success: function(data){

		var description = data;
		if (description == null) {
			description = "No response has been received yet from the server. Please wait..";
		}
		$("p").text(description);
	    
	}, dataType: "json", complete: getProgressDescription, timeout: 50 });
}


</script>
</head>

<body>
<h1>${title}</h1>

<c:url var="submitUrl" value="${formSubmitUrl}"/>

<form:form action="${submitUrl}" method="post" name="theOnlyFormInThisPage">
	<input id="config_id_input" type="text" name="id" value="${formTagValue}" size="10" /> 
</form:form>

<input name="submitButton" type="button" value="Submit" onclick="startProcess()">


<div class="ui-widget">
	<div class="ui-state-highlight ui-corner-all" style="margin-top: 20px; padding: 0 .7em;">
				<p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
				<strong>No configuration is running at the moment.</strong></p>
	</div>
</div>
<div id="progressbar"></div>

<div style="margin-top: 50px;">

<c:if test="${not empty reports}">
	<table class="inline" align="center">
		<tr>
			<th>Previously Applied Configurations</th>
		</tr>
	<c:forEach var="report" items="${reports}" >
		<tr>
			<td align="center">
			
				<a href="../configloader/progress/finalReport/${report.key}">
					${report.key}</a>
				
				(<a href="../configloader/progress/finalReport/xml/${report.key}" target="_blank">XML</a>)
				
			</td>
		</tr>
	</c:forEach>
	</table> 
</c:if>

</div> 

<br/>
<br/>

</body>
</html>


