<%@page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>


<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>Access Denied</title>

<link rel="stylesheet" type="text/css" href="<c:url value="css/bootstrap/bootstrap.css"/>" />
</head>

<body>
  <h2 style="margin-top: 100px; text-align: center;">Access Denied!</h2>

  <div class="container">
    <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
      <p style="text-align: center;">Sorry, your account is not authorised to access this page.</p>

      <p style="text-align: center;">
        If you need access to this application, please contact
        <a href="mailto:tim.support@cern.ch">TIM Support</a>.
      </p>

      <p style="text-align: center;">
        <a href="../">Click here to go back</a>.
      </p>
    </sec:authorize>
  </div>

  <script>
      if (window.location.href.indexOf('?reloaded') === -1)
        window.location.href = window.location.href + "?reloaded=true";
    </script>

</body>
</html>
