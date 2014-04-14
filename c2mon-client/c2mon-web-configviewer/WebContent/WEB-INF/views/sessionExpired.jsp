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
	your session expired, you're going to be redirect in few seconds.
	</p>
	

	<p style="text-align:center;"><a href="../">Click here to go back</a>.</p>
	
</sec:authorize>

</div>

<script>

window.location.replace('../j_spring_security_logout');

</script>

</body>
</html>
