<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>
  <body> 
    <p>Please select the statistics you would like to view.</p>
	  <f:view>	  
	    <h:form>
	      <h:selectOneMenu value="#{userRequests.chosenDirectory}">
			      <f:selectItems value="#{graphCategories.categories}"/>
			      <h:commandButton value="Display charts" action="displayCharts"/>
		    </h:selectOneMenu>
	    </h:form>
	  </f:view>
  </body>
</html>
