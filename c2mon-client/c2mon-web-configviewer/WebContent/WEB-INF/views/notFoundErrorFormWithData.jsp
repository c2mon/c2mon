<html>
<head>
<title>${title}</title>

<link rel="stylesheet" type="text/css" href="../../css/bootstrap/bootstrap.css" />

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
        <li><a href="../../">Home</a> <span class="divider"></span></li>
        <li>${title}</li>

        <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
          <span class="pull-right">
            <a href="../j_spring_security_logout">Logout&nbsp;</a>
            <span class="glyphicon glyphicon-log-out"></span>
          </span>
        </sec:authorize>
      </ul>

      <div class="jumbotron">
        <h1>${title}</h1>
      </div>

      <div class="alert alert-info">
        <strong>${instruction}</strong>
      </div>

      <div class="alert alert-danger">
        id: <strong> ${err} </strong> could not be found.
      </div>

      <form class="well form-inline" action="" method="post">
        <input class="span2" style="display: inline" type="text" name="id" value="${formTagValue}" />
        <input class="btn btn-large btn-primary" type="submit" value="Submit">
      </form>

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

  <!-- Le javascript
    ================================================== -->
  <!-- Placed at the end of the document so the pages load faster -->
  <script src="../../js/jquery/jquery.js"></script>
  <script src="../../js/bootstrap/bootstrap.js"></script>

</body>
</html>


