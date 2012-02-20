<%@page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<html>
<title>Access Denied</title>
<body>
<h2>Access Denied!</h2>

<div><sec:authorize ifNotGranted="ROLE_ANONYMOUS"> Hi!
	It seems you are not authorised to view this page..
</sec:authorize></div>

 <p><a href="/c2mon-web-configviewer">Click here to go back</a>.</p>

</body>
</html>
