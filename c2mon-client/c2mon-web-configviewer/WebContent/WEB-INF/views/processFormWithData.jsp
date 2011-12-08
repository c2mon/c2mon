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
	<select name="id">
		<c:forEach items="${processNames}" var="processName">  
  			<option>${processName}</option>
    	</c:forEach>  
	</select>
	<input type="submit" />
</form:form>
</p>
<br/>
<br/>


<c:if test="${not empty(err)}">
	<p class="notFound">${err}</p>
</c:if> 

<c:if test="${not empty(tagDataUrl)}">
	<iframe name="xmlIFrame" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes" 
			src="<c:url value="${tagDataUrl}"/>">
	</iframe>
</c:if>
</body>
</html>


