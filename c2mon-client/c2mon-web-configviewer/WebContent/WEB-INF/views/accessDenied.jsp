<%@page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<html>
<title>Access Denied</title>
<body>
<h2 style="margin-top:100px; text-align:center;">Access Denied!</h2>

<div>

<sec:authorize ifNotGranted="ROLE_ANONYMOUS"> 

	<p style="text-align:center;">
	Sorry, 
	your account is not authorised to access this page.
	</p>
	
	<p style="text-align:center;">If you need access to this application, please contact 
	<a href="mailto:tim.support@cern.ch">TIM Support</A>.</p>

	<p style="text-align:center;"><a href="../">Click here to go back</a>.</p>
	
</sec:authorize>

</div>

<script>
if(window.location.href.indexOf('?reloaded') === -1) 
	window.location.href = window.location.href + "?reloaded=true";
</script>

</body>
</html>
