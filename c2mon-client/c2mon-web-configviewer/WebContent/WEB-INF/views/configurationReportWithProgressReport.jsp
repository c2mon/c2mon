<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/form.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/tim.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/webConfigViewer.css"/>" />

<link type="text/css" href="/c2mon-web-configviewer/css/ui-lightness/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery.js"></script>
<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-ui.js"></script>


<script type="text/javascript">

/**
 * Sets the default value of the progress bar to 0
 */
$(function(){

	// Progressbar
	$("#progressbar").progressbar({
		value: 0
	});
});


/**
 * Called when the submit button is pressed.
 */
function startProcess() {

	startConfigurationRequest(); // starts the apply configuration request
	getProgress(); // polls the server and updates the progress bar
	getProgressDescription(); // polls the server and updates the description info

	$("p").text("Started...");

	document.theOnlyFormInThisPage.id.readOnly = true;
	document.getElementsByName("submitButton")[0].disabled = true;
}

/**
 * Redirects the user to the final "ConfigurationReport"
 * once the Request has finished executing in the server. 
 */
 function progressFinished() {

		window.location = "/c2mon-web-configviewer/configloader/progress/finalReport/" 
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
        url: "/c2mon-web-configviewer/configloader/progress/start",
        data: { configurationId : document.theOnlyFormInThisPage.id.value },
				async: true,
        success: function(data){

        //Update progressbar
        $("#progressbar").progressbar({
						value: data
				});
    }, complete: progressFinished
});
}

/**
 * Runs every "timeout" milliseconds and polls the server 
 * for information about the current progress of the Request.
 */
function getProgress() {
	
    $.ajax({ 
		type: "POST",
    url: "/c2mon-web-configviewer/configloader/progress/getProgress",
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
    url: "/c2mon-web-configviewer/configloader/progress/getProgressDescription",
    data: { configurationId : document.theOnlyFormInThisPage.id.value },
			async: true,
    success: function(data){

		var description = data;
		$("p").text(description);
	    
	}, dataType: "json", complete: getProgressDescription, timeout: 50 });
}


</script>
</head>

<body>
<h1>${title}</h1>

<c:url var="submitUrl" value="${formSubmitUrl}"/>

<form:form action="${submitUrl}" method="post" name="theOnlyFormInThisPage">
	<input type="text" name="id" value="${formTagValue}" size="10" /> 
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
<table class="inline" align="center">
<tr>
<th>Previously Applied Configurations</th>
</tr>
<tr>
<c:forEach var="report" items="${reports}" >
	<td align="center"><a href="/c2mon-web-configviewer/configloader/progress/finalReport/${report.key}">
		${report.key}</a></td>
</c:forEach>
</tr>
</table> 
</div> 

<br/>
<br/>

</body>
</html>


