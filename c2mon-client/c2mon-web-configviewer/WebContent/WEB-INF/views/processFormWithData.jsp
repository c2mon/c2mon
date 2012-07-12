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

<p><a href="<c:url value="/j_spring_security_logout"/>">Logout</a>

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


