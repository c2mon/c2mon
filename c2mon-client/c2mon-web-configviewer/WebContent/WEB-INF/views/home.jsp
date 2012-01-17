<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" type="text/css" href="<c:url value="css/buttons.css"/>" />
<title>Online Viewer</title>
</head>
<body>

<sec:authorize ifAnyGranted="ROLE_ANONYMOUS"> 
	<h1>You are not logged in.</h1> 
</sec:authorize>

<sec:authorize ifNotGranted="ROLE_ANONYMOUS"> Hi 
	<h1><span style="color: #708090; font-size: 14pt">${username}!</span>  
	It's nice to see you again!</h1>
</sec:authorize>

<h4>Please select an option:</h4>
<ul>
	<li><a href="tagviewer/form">Tag Viewer</a></li>
	<li><a href="alarmviewer/form"> Alarm Viewer </a></li>
	<li><a href="commandviewer/form"> Command Viewer </a></li>
	<li><a href="configloader/form"> Config Loader </a></li>
	<li><a href="process/form"> DAQ XML Viewer </a></li>
</ul>

<sec:authorize ifNotGranted="ROLE_ANONYMOUS">
<p>You are currently logged in. <a href="/c2mon-web-configviewer/j_spring_security_logout">Logout</a>
</sec:authorize>

<p><sec:authorize url='/configloader/form'>You are currently authorised to access the 
<a href="configloader/form"> Config Loader </a>.
</sec:authorize></p>

<p><sec:authorize url='/process/form'>
You are currently authorised to access the <a href="process/form"> DAQ XML Viewer </a>.
</sec:authorize></p>

</body>
</html>