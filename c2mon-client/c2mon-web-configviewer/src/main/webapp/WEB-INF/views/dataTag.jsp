<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/tim.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/webConfigViewer.css"/>" />
<!--[if lte IE 8]>
<style type="text/css">
	.column {
		display: inline;
		zoom: 1;
	}
</style>
<![endif]-->
</head>

<body>
<div id="header" class="configViewer_header">
	C2MON Online configuration viewer	
</div>
<br/>

<p class="instruction">${instruction}</p>
<p>
<form action="" method="post">
	<input type="text"name="${objectName}" value="${objectValue}" size="10" /> 
	<input type="submit" />
</form>
</p>
<br/>
<br/>

<c:if test="${not empty(err)}">
	<p class="notFound">${err}</p>
</c:if> <c:if test="${not empty(name)}">
	<p class="tagName">${name} (${id})</p>
</c:if>

<div class="column">
	<c:if test="${not empty(value)}">
	
		<table class="inline">
			<th colspan="2">Current Value</th>
			<c:forEach var="entry" items="${value}">
				<tr>
					<td class="bold">${entry.key}</td>
					<td>${entry.value}</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
	<br/>
	<c:if test="${not empty(address)}">
		<table class="inline">
			<th colspan="2">Address Configuration</th>
			<c:forEach var="entry" items="${address}">
				<tr>
					<td class="bold">${entry.key}</td>
					<td>${entry.value}</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>

<div class="column">
	<c:if test="${not empty(config)}">
		<table class="inline">
			<th colspan="2">DataTag configuration</th>
			<c:forEach var="entry" items="${config}">
				<tr>
					<td class="bold">${entry.key}</td>
					<td>${entry.value}</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>
<p>
	<a href="${url}">${urlText}</a>
</p>

</body>
</html>
