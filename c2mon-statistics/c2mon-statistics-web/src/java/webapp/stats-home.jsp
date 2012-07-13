<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>
  <f:view>
	  <head>
	    <title>TIM Statistics</title>
	  </head>
	  <body>
	    <h1>TIM Statistics</h1>
	    <p>These pages present some statistics on the operation of the TIM monitoring and control system.</p>
	    <p>The current graphs were generated on
	     <h:outputText value="#{chartManager.creationDate}"/>.
	    </p>
	    <h:form>
		    <p>
		     Details on the quality codes used in the charts can be found
		     <h:commandLink action="displayQualityCodes">
	        <h:outputText value="here"/>
	       </h:commandLink>
	       .	       	     
		    </p>
	    </h:form>
	    <p>
	    <h:form>
	     <h:selectOneMenu value="#{userRequests.chosenDirectory}">
	       <f:selectItems value="#{daqList.selectList}"/>
	       <h:commandButton value="view DAQ statistics" action="displayCharts"/>
	     </h:selectOneMenu>
	    </h:form> 
	    <h:form><h:commandButton value="view all statistics" action="allStatistics"/></h:form></p>
	    <p>The chart below displays the total number of updates to the applications server for each of the past 10 days.</p>
      <p>
       <img src="./chart-images/application-server/chart_5.png"/>
      </p>
	  </body>
  </f:view>
</html>