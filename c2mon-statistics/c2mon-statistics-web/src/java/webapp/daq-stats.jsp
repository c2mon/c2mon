<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>
  <body> 
    <p></p>
    <f:view>    
      <h:form>
        <h:selectOneMenu value="#{userRequests.chosenDirectory}">
            <f:selectItems value="#{daqList.daqList}"/>
            <h:commandButton value="Display charts" action="displayCharts"/>
        </h:selectOneMenu>
      </h:form>
    </f:view>
  </body>
</html>