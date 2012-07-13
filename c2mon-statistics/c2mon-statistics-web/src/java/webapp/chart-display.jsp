<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<html>
  <body>
    <f:view>
      <c:forEach items="${userRequests.fragmentFiles}" var="current">
         <jsp:include page="${current}"/>       
      </c:forEach>
    </f:view>        
  </body>
</html>