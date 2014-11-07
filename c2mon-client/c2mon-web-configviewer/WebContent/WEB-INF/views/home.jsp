<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Online Viewer Home</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<!-- Le styles -->
<link rel="stylesheet" type="text/css" href="<c:url value="css/bootstrap/bootstrap.css"/>" />
<%-- <link rel="stylesheet" type="text/css" href="<c:url value="css/buttons.css"/>" /> --%>
<%-- <link rel="stylesheet" type="text/css" href="<c:url value="css/bootstrap-responsive.css"/>" /> --%>

<style type="text/css">
body {
  padding-top: 50px;
  padding-bottom: 40px;
  /*   padding-left: 50px; */
}

.sidebar-nav {
  padding: 9px 0;
}
</style>
</head>

<body>

  <div class="container">
    <div class="row">

      <div class="">

        <ul class="breadcrumb">
          <li>Home</li>
        </ul>

        <div class="jumbotron">
          <h1>Online Viewer Home</h1>
        </div>

        <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
          <div class="alert alert-info">
            Hi
            <span style="color: #708090; font-size: 14pt">${username}!</span>
            It's nice to see you again! Please select an option:
          </div>
        </sec:authorize>
        <sec:authorize ifAllGranted="ROLE_ANONYMOUS">
          <div class="alert alert-info">Welcome! Please select an option:</div>
        </sec:authorize>

        <div class="row">
          <div class="col-md-4">
            <h3>Tag Viewer</h3>
            <p>
              <a class="btn btn-default" href="tagviewer/form">Go! &raquo;</a>
            </p>
          </div>
          <!--/span-->
          <div class="col-md-4">
            <h3>History Viewer</h3>
            <p>
              <a class="btn btn-default" href="historyviewer/form">Table &raquo;</a>
              <a class="btn btn-default" href="trendviewer/form">Trend &raquo;</a>
            </p>
          </div>
          <!--/span-->
          <div class="col-md-4">
            <h3>Alarm Viewer</h3>
            <p>
              <a class="btn btn-default" href="alarmviewer/form">Go! &raquo;</a>
            </p>
          </div>
          <!--/span-->
        </div>
        <!--/row-->
        <div class="row">
          <div class="col-md-4">
            <h3>Command Viewer</h3>
            <p>
              <a class="btn btn-default" href="commandviewer/form">Go! &raquo;</a>
            </p>
          </div>
          <!--/span-->
          <div class="col-md-4">
            <h3>Config Loader</h3>
            <p>
              <a class="btn btn-default" href="configloader/progress">Go! &raquo;</a>
            </p>
          </div>
          <!--/span-->
          <div class="col-md-4">
            <h3>DAQ XML Viewer</h3>
            <p>
              <a class="btn btn-default" href="process/form">Go! &raquo;</a>
            </p>
          </div>
          <!--/span-->
        </div>
        <!--/row-->


        <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
          <p>
          <div class="alert alert-success">
            <span>
              You are currently logged in.
              <a href="j_spring_security_logout">Logout</a>
            </span>
          </div>
          </p>
        </sec:authorize>

      </div>
      <!--/span-->
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

</body>
</html>