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
</head>

<body>
<h1>${title}</h1>

<div class="ui-widget">
	<div class="ui-state-highlight ui-corner-all" style="margin-top: 20px; padding: 0 .7em;">
				<p style="font-size : 1.1em;"><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
				${instruction}</p>
	</div>
</div>

<br/>

<div class="ui-widget">
			<div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
				<p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>
				<strong>id: ${err} NOT FOUND </strong> Sorry :( </p>
			</div>
</div>
<p>

<c:url var="submitUrl" value="${formSubmitUrl}"/>

<br/>
<form:form action="${submitUrl}" method="post">
	<input type="text" name="id" value="${formTagValue}" size="10" /> 
	<input type="submit" />
</form:form>
</p>

</body>
</html>


