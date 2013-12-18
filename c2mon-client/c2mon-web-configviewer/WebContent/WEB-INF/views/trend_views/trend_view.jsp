<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<head>    
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9; IE=EmulateIE10"> 
	<!--[if IE]><script src="/c2mon-web-configviewer/js/excanvas.js"></script><![endif]-->
	<script type="text/javascript" src="/c2mon-web-configviewer/js/dygraph-combined.js"></script>
</head>

<body>
<div style="width:1000px; height:700px;" id="trend_view"></div>
<script type="text/javascript">
  g = new Dygraph(

    // containing div
    document.getElementById("trend_view"),

    // CSV or path to a CSV file.
    ${CSV}
    
		,{
     title: "History chart for: ${id}",
     legend: 'always',
     stepPlot: ${is_boolean},
     fillGraph: ${fill_graph},
     ylabel: '${ylabel}',
     	<c:if test="${is_boolean}"> 
     		valueRange: [-1, 2],  
     	</c:if>
     labels: [ 
     		<c:set var="totalLabels" value="${fn:length(labels)}" />
    		<c:forEach items="${labels}" var="label" varStatus="labelCounter">
    			"<c:out value="${label}"/>"
    			<c:if test="${ totalLabels !=  labelCounter.count }">
    				,
					</c:if>
				</c:forEach>
     ]
    }
  );
</script>

</body>
</html>
