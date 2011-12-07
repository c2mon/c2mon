<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="<c:url value="/css/tag.xsl"/>" ?>

<TagInfo>

<c:if test="${not empty(tagErr)}">
	<p class="notFound">${tagErr}</p>
</c:if> 
<c:if test="${not empty(configErr)}">
	<p class="notFound">${configErr}</p>
</c:if> 

<c:if test="${not empty(dataTagXml)}">
	${dataTagXml}
</c:if>

<c:if test="${not empty(configXml)}">
	${configXml}
</c:if>

<c:if test="${not empty(alarmXml)}">
	${alarmXml} 
</c:if>

<c:if test="${not empty(commandXml)}">
	${commandXml} 
</c:if>

<c:if test="${not empty(reportXml)}">
	${reportXml} 
</c:if>

<c:if test="${not empty(processXml)}">
	${processXml} 
</c:if>

<c:if test="${not empty(url)}">
	<p><a href="${url}">
	<c:if test="${not empty(urlText)}">
		${urlText}
	</c:if>
	</a>
	</p>
</c:if>


</TagInfo>