<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Please Login</title>

<link rel="stylesheet" type="text/css" href="<c:url value="/css/bootstrap/bootstrap.css"/>" />

<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js" />"></script>
<script type="text/javascript" src="<c:url value="/js/login-error.js" />"></script>

<style type="text/css">

body {
  padding-top: 40px;
  padding-bottom: 40px;
  background-color: #eee;
}

.form-signin {
  max-width: 330px;
  padding: 15px;
  margin: 0 auto;
}

.form-signin .form-signin-heading, .form-signin .checkbox {
  margin-bottom: 10px;
}

.form-signin .checkbox {
  font-weight: normal;
}

.form-signin .form-control {
  position: relative;
  height: auto;
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  padding: 10px;
  font-size: 16px;
}

.form-signin .form-control:focus {
  z-index: 2;
}

.form-signin input[type="text"] {
  margin-bottom: -1px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}

.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}
</style>

</head>

<body>
  <script>
//  if(window.location.href.indexOf('?reloaded') === -1)
//    window.location.href = window.location.href + "?reloaded=true";
  </script>
  <div id="container">
    <div id="error"></div>

    <div class="container">

      <form class="form-signin" role="form" action="<c:url value='j_spring_security_check' />" method="post">
        <h2 class="form-signin-heading">Please sign in</h2>

        <div id="username" class="form-group has-feedback">
          <label id="error-feedback" class="control-label" style="display: none;" for="j_username">Invalid username or password</label>
          <input type="text" class="form-control" placeholder="Username" name="j_username" id="j_username" size="32" maxlength="128" required autofocus>
        </div>

        <div id="password" class="form-group has-feedback">
          <input type="password" class="form-control" placeholder="Password" name="j_password" id="j_password" size="32" maxlength="32" required>
        </div>

        <button class="btn btn-large btn-primary btn-block" type="submit" name="submit" id="submit">Sign in</button>

      </form>

    </div>
  </div>

</body>
</html>
