<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<head>    
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9; IE=EmulateIE10"> 
	<!--[if IE]><script src="/c2mon-web-configviewer/js/excanvas.js"></script><![endif]-->
   <title>TrendViewer</title>
	<script type="text/javascript" src="/c2mon-web-configviewer/js/dygraph-combined.js"></script>
	
	<link rel="shortcut icon" href="/c2mon-web-configviewer/img/chart_icon.png">
	
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap.css" />
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap-responsive.css" />

	<script type="text/javascript" src="/c2mon-web-configviewer/js/jquery-1.7.min.js"></script>
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/tim.css"/>
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/buttons.css"/>
</head>

<body>


<style media="screen" type="text/css">
.invalidPoint {
    background-color: #66ffff; 
}
</style>


    <div class="container-fluid">
      <div class="row-fluid">
	  
          <div class="page-header">
			<h2 style="margin-left:50px;display:inline; text-align:center;">${view_title}</h2>
			<div style="margin-left:50px;">
				${view_description}
			</div>
          </div>
		  
		<p style="margin-left:50px;width:900px; height:30px;" class="links">
			<A href="/c2mon-web-configviewer/historyviewer/${id}/" 
				class="large blue awesome xml_button" target="_blank">History >>
			</A>	
			<A href="/c2mon-web-configviewer/tagviewer/${id}/" 
				class="large blue awesome xml_button" target="_blank">View Tag >>
			</A>
			<A href="https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=${id}" 
				class="large red awesome xml_button" target="_blank">View Help Alarm >>
			</A>
			<A style="display:inline;float:left;" href="/c2mon-web-configviewer/" 
			class="blue awesome xml_button">		
				 <i class="icon-home"></i> Home
			</A>
		</p>

		<div style="width:1000px; height:650px;" id="trend_view"></div>
		
	</div>
    </div><!--/.fluid-container-->


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
	 	 colors: ['#2e8b57'],
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
