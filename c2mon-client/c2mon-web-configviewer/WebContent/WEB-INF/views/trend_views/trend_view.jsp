<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<head>    
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9; IE=EmulateIE10"> 
	<!--[if IE]><script src="/c2mon-web-configviewer/js/excanvas.js"></script><![endif]-->
   <title>TrendViewer</title>
	<script type="text/javascript" src="/c2mon-web-configviewer/js/dygraph-combined.js"></script>
	
	<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css"></link>
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/buttons.css"></link>
</head>

<body>

<style media="screen" type="text/css">
.invalidPoint {
    background-color: #66ffff; 
}
</style>

		<div id="title" style="width:1000px; height:30px;">
			<h2 style="width:100%;text-align:center;">${tagName} (Last ${records} records for [${id}])</h2>
		</div>

		<p style="width:1000px; height:50px;" class="links">
			<A href="{$base_url}{$history_url}{@id}/" 
				class="large blue awesome xml_button" target="_blank">History >>
			</A>	
			<A href="{$base_url}{$datatag_url}{/*/@id}/" 
				class="large blue awesome xml_button" target="_blank">View Tag >>
			</A>
			<A href="{$help_point_url}{@id}" 
				class="large red awesome xml_button" target="_blank">View Help Alarm >>
			</A>	

		</p>

		<div style="width:1000px; height:700px;" id="trend_view"></div>


<script type="text/javascript">
  trend = new Dygraph(

    // containing div
    document.getElementById("trend_view"),

    // CSV or path to a CSV file.
    ${CSV}
    
		,{
     // title: "History chart for: ${id}",
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
  
   trend.ready(function(g) {
    g.setAnnotations( [
    
   <c:set var="totalInvalidPoints" value="${fn:length(invalidPoints)}" />
   <c:forEach items="${invalidPoints}" var="invalidPoint" varStatus="invalidPointCounter">
    {
    	series:
     		<c:set var="totalLabels" value="${fn:length(labels)}" />
    		<c:forEach items="${labels}" var="label" varStatus="labelCounter">
    			<c:if test="${ totalLabels ==  labelCounter.count }">
    				"<c:out value="${label}"/>",
					</c:if>
				</c:forEach>
      x: "<c:out value="${invalidPoint}"/>",
      shortText: "?",
      cssClass: 'invalidPoint'
    }
    	<c:if test="${ totalInvalidPoints !=  invalidPointCounter.count }">
    		,
			</c:if>
		</c:forEach>
    ] );
  });
</script>

</body>
</html>
