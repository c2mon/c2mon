<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="cern.c2mon.web.configviewer.service.HistoryService"%>
<%@page import="cern.c2mon.client.ext.history.common.HistoryTagValueUpdate"%>
<%@page import="java.util.List"%>

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
  <script type="text/javascript" src="../js/bootstrap.js"></script>
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
    var invalid;
    
    var texte = ${CSV};
    //table with all dates
    var array = texte.split("\n");
    //Table without ',number' at the end
    var datefinal = new Array();
    
    /**
     * Table with the content of ${invalidPoint.time}
     */
    function populateArray() {  
      names = new Array();
      <c:forEach items="${invalidPoints}" var="invalidPoint" varStatus="status">  
      names[${status.index}] = "${invalidPoint.time}";  
      </c:forEach>  
      return names;  
    } 
     

    
    
    
    //Remove the unnecessary character
    for (var i=0;i<array.length;i++) {  
      datefinal[i] = array[i].substr(0,19);
    }
      
    // Table with all invalid point
    invalid = populateArray();
    
  	var trend = new Dygraph(

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
     ],
     xAxisLabelWidth: 70,
    
     underlayCallback: function(canvas, area, trend) {
       var cpt = 0;
       //Color of the area
       canvas.fillStyle = "rgba(102, 255, 255, 10.0)"
       
         /**
          * To draw the invalid area
          */
         function highlight_period(x_start, x_end) {
           var canvas_left_x = trend.toDomXCoord(x_start);
           var canvas_right_x = trend.toDomXCoord(x_end);
           var canvas_width = canvas_right_x - canvas_left_x;
           canvas.fillRect(canvas_left_x, area.y, canvas_width, area.h);
         }
       
       // 1st value of the graph
       var min_data_x = trend.getValue(0,0);
       var max_data_x = trend.getValue(trend.numRows()-1,0);

       // get the first invalid date and convert the string to a date
       var d = new Date(invalid[cpt]);
       // Convert this date to timestamp
       var w = d.getTime();

       while (w < max_data_x) {
         // Save the index of the invalid date who is located in the all date table
         var savecpt;
         var start_x_highlight = w;
         
        //i search the invalid date in the all date table, the next one is the end 
         for (var j=0;j<datefinal.length;j++)
         {  
           end = new Date(datefinal[j]).getTime();
           if(start_x_highlight == end)
             {
              savecpt = j + 1; 
             }        
         }

         var end_x_highlight = new Date(datefinal[savecpt]).getTime();
         // make sure we don't try to plot outside the graph
         if (start_x_highlight < min_data_x) {
           start_x_highlight = min_data_x;
         }
         if (end_x_highlight > max_data_x) {
           end_x_highlight = max_data_x;
         }
         highlight_period(start_x_highlight,end_x_highlight);
         // get the end date of the invalid area 
         cpt++;
         nextDate = new Date(invalid[cpt]).getTime();
         
         w = nextDate;
         //alert(w);
       }
       
     },
     axes: {
         x: {
           axisLabelFormatter: function(d) {
        		var month=new Array();
        		month[00]="Jan";
        		month[01]="Feb";
        		month[02]="Mar";
        		month[03]="Apr";
        		month[04]="May";
        		month[05]="Jun";
        		month[06]="Jul";
        		month[07]="Aug";
        		month[08]="Sep";
        		month[09]="Oct";
        		month[10]="Nov";
        		month[11]="Dec";
        		/* d.getMonth() return number and not Jan, Feb
        		*  uncomment d.getFullYear() if you want the year
        		*/
               return d.getDate()+"-"
               + month[(d.getMonth())]/*+"-"
               + Dygraph.zeropad(d.getFullYear())*/+"\n"
               + Dygraph.zeropad(d.getHours()) + ":"
               + Dygraph.zeropad(d.getMinutes());
           }
  
         }}
    }
  );
  	
   // Display the invalid notification (blue square)
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
