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

</script>
</head>

<body>
<h1>${title}</h1>

<c:url var="submitUrl" value="${formSubmitUrl}"/>

<form:form action="${submitUrl}" method="post" name="theOnlyFormInThisPage">
	<input type="text" name="id" value="${formTagValue}" size="10" /> 
</form:form>

<input type="button" value="Submit" onclick="startProcess()">

<div id="progressbar"></div>

<br/>
<br/>

</body>
</html>


