<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
<title>${title}</title>
<link rel="stylesheet" type="text/css" href="../css/bootstrap/bootstrap.css" />

<style type="text/css">
body {
  padding-top: 50px;
  padding-bottom: 40px;
}

.sidebar-nav {
  padding: 9px 0;
}
</style>

</head>

<body>

  <div class="container">
    <div class="row">

      <ul class="breadcrumb">
        <li><a href="../">Home</a> <span class="divider"></span></li>
        <li>${title}</li>
      </ul>

      <div class="jumbotron">
        <h1>${title}</h1>
      </div>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

      <form class="well form-inline" action="${submitUrl}" method="post">

        <select name="id" class="form-control">
          <c:forEach items="${processNames}" var="processName">
            <option>${processName}</option>
          </c:forEach>
        </select>
        <input class="btn btn-large btn-primary" type="submit" value="Submit">
      </form>

      <a href="../j_spring_security_logout">Logout</a>
    </div>
    <!--/row-->

    <div class="row">
      <footer>
        <hr>
        <p>
          &copy; CERN
          <script type="text/javascript">
                      document.write(new Date().getFullYear())
                    </script>
        </p>
      </footer>
    </div>
  </div>
  <!--/.fluid-container-->

  <!-- Placed at the end of the document so the pages load faster -->
  <script src="../js/jquery/jquery.js"></script>
  <script src="../js/bootstrap/bootstrap.js"></script>

</body>
</html>


