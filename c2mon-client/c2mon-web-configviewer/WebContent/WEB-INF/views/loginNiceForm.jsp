<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
	<head>
		<title>Please Login</title>
<link rel="stylesheet" type="text/css" href="<c:url value="/css/form.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/c2mon.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/web-config-viewer.css"/>" />
	</head>
	
	<body>
	
<div id="container">
<form action="xxx.xxx" method="post" class="niceform">
	<fieldset>
    	<legend>Please Login</legend>
        <dl>
        	<dt><label for="email">Username:</label></dt>
            <dd><input type="text" name="email" id="email" size="32" maxlength="128" /></dd>
        </dl>
        <dl>
        	<dt><label for="password">Password:</label></dt>
            <dd><input type="password" name="password" id="password" size="32" maxlength="32" /></dd>
        </dl>
    </fieldset>
    <fieldset class="action">
    	<input type="submit" name="submit" id="submit" value="Submit" />
    </fieldset>
</form>
</div>

	</body>
</html>
