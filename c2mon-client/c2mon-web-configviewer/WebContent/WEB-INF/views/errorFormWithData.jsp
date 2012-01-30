<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/form.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/tim.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/webConfigViewer.css"/>" />
</head>

<body>
<h1>${title}</h1>

<p class="instruction">${instruction}</p>
<p>

<c:url var="submitUrl" value="${formSubmitUrl}"/>

<form:form action="${submitUrl}" method="post">
	<input type="text" name="id" value="${formTagValue}" size="10" /> 
	<input type="submit" />
</form:form>
</p>
<br/>
<br/>

<p class="notFound">

<br>

id: ${err} NOT FOUND <br> <br>

Sorry :( 

</p>

</body>
</html>


