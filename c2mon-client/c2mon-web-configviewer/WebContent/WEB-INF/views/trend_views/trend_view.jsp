<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<head>    
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9; IE=EmulateIE10"> 
	<!--[if IE]><script src="../js/excanvas.js"></script><![endif]-->
   <title>TrendViewer</title>
	<script type="text/javascript" src="../js/dygraph-combined.js"></script>
	
	<link rel="shortcut icon" href="../img/chart_icon.png">
	
	<link rel="stylesheet" type="text/css" href="../css/bootstrap.css" />
	<link rel="stylesheet" type="text/css" href="../css/bootstrap-responsive.css" />

	<script type="text/javascript" src="../js/jquery-1.7.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../css/c2mon.css"/>
	<link rel="stylesheet" type="text/css" href="../css/buttons.css"/>
	<script type="text/javascript" src="../js/hide-menu.js"></script>
</head>

<body >
	
	
	<style media="screen" type="text/css">
		.invalidPoint {
    		background-color: #66ffff; 
		}`
		
	</style>
	

    <div class="container-fluid">
      <div class="row-fluid" id="row-fluid">
	  
          <div class="page-header">
			<h2 style="margin-left:50px;display:inline; text-align:center;">${view_title}</h2>
			<div style="margin-left:50px;">
				${view_description}
			</div>
          </div>
		  
		<p style="margin-left:50px;width:90%; height:30px; min-width:550px; margin-bottom:5%;" class="links">
			<A href="../historyviewer/${id}?${queryParameters}" 
				class="large blue awesome xml_button" target="_blank">Table >>
			</A>	
			<A href="../tagviewer/${id}" 
				class="large blue awesome xml_button" target="_blank">View Tag >>
			</A>
			<A href="https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=${id}" 
				class="large red awesome xml_button" target="_blank">View Help Alarm >>
			</A>
			<A style="display:inline;float:left;" href="../" 
			class="blue awesome xml_button">		
				 <i class="icon-home"></i> Home
			</A>
		</p>

		<div style="width:100%; height:650px; ;" id="trend_view" ></div>
		
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
      x: "<c:out value="${invalidPoint.time}"/>",
      shortText: "?",
      text: "<c:out value="${invalidPoint.invalidationReason}"/>",
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
