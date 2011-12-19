<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="<c:url value="css/buttons.css"/>" />
<title>Home</title>
</head>
<body>
<h1>Welcome to the Online Config Viewer</h1>

<ul>
	<li><a href="tagviewer/form">Tag Viewer</a></li>
	<li><a href="alarmviewer/form"> Alarm
	Viewer </a></li>
	<li><a href="commandviewer/form"> Command
	Viewer </a></li>
	<li><a href="configloader/form"> Config
	Loader </a></li>
	<li><a href="process/form"> DAQ XML
	Viewer </a></li>
</ul>

<sec:authorize ifAnyGranted="ROLE_ADMIN">
You are currently authorised

<p><a href="<c:url value="/j_spring_security_logout"/>">Logout</a>
</sec:authorize>

<sec:authorize ifNotGranted="ROLE_ADMIN">
You are not authorised
</sec:authorize>

<p><sec:authorize url='/configloader/form'>
					You are currently authorised to access the 
					<a href="configloader/form"> Config Loader </a>.
				</sec:authorize></p>

<p><sec:authorize url='/process/form'>
					You are currently authorised to access the DAQ XML Viewer.
				</sec:authorize></p>
</body>
</html>