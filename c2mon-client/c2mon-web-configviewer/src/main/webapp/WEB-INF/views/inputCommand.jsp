<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<html>
<head>
<title>Configuration viewer</title>
<link rel="stylesheet" type="text/css" href="/css/tim.css"/>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/tim.css"/>" />  

</head>
<body>

<p><c:out value="${instruction}"></c:out></p>
<form action="" method="post"><input type="text"
	name="${objectName}" value="${objectValue}" size="10" /> <input
	type="submit" /></form>


<p>Selected id is: <c:out value="${objectValue}" /></p>

<c:if test="${not empty(err)}">
	<p><c:out value="${err}" /></p>
</c:if>
<c:if test="${not empty(name)}">
	<h1><c:out value="${name}" /> (<c:out value="${id}" /> )</h1>
</c:if>

<table class="inline">
	<th colspan="3">Current Value</th>
	<c:forEach var="entry" items="${value}" varStatus="status">
		<tr>
			<td>${entry.key}</td>
			<td>${entry.value}</td>
			<td>${entry.key.class}</td>
		</tr>
	</c:forEach>
</table>

<table class="inline">
	<th colspan="3"
		style="border: 1px solid #436976; background-color: #bdd1d7;">
	DataTag Configuration</th>
	<c:forEach var="entry" items="${config}">
		<tr>
			<td>${entry.key}</td>
			<td>${entry.value}</td>
			<td>${entry.key.class}</td>
		</tr>
	</c:forEach>
</table>

<table class="inline">
	<c:forEach var="entry" items="${address}">
		<tr>
			<td>${entry.key}</td>
			<td>${entry.value}</td>
			<td>${entry.key.class}</td>
		</tr>
	</c:forEach>
</table>


</body>
</html>
