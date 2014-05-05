<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
	<head>
		<title>Please Login</title>
<meta http-equiv="X-UA-Compatible" content="IE=9" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/form.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/c2mon.css"/>" />
<link rel="stylesheet" type="text/css" href="<c:url value="/css/web-config-viewer.css"/>" />
<script type="text/javascript" src="<c:url value="/js/jquery-1.7.min.js" />"></script>
<script type="text/javascript" src="<c:url value="/js/login-error.js" />"> ></script>

  </head>
	
	<body>
	<script>
	if(window.location.href.indexOf('?reloaded') === -1) 
	  window.location.href = window.location.href + "?reloaded=true";
	</script>
  <div id="container">
  <div id="error">
  
  </div>
  
  <form action="<c:url value='j_spring_security_check' />" method="post" class="niceform">
  	<fieldset>
      	<legend>Please Login</legend>
          <dl>
          	<dt><label for="j_username">Username:</label></dt>
              <dd><input type="text" name="j_username" id="j_username" size="32" maxlength="128" /></dd>
          </dl>
          <dl>
          	<dt><label for="password">Password:</label></dt>
              <dd><input type="password" name="j_password" id="j_password" size="32" maxlength="32" /></dd>
          </dl>
      </fieldset>
      <fieldset class="action">
      	<input type="submit" name="submit" id="submit" value="Submit" />
      </fieldset>
  </form>
  </div>

	</body>
</html>
