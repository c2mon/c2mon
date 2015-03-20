<%@tag description="C2MON page template" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<%@attribute name="title" required="true"%>


<!-- JSP variables -->
<c:url var="home" value="/" />
<c:url var="login" value="/login" />
<c:url var="logout" value="/j_spring_security_logout" />
<c:url var="tagviewer" value="/tagviewer/form" />
<c:url var="alarmviewer" value="/alarmviewer/form" />
<c:url var="commandviewer" value="/commandviewer/form" />
<c:url var="processviewer" value="/process/form" />
<c:url var="trendviewer" value="/trendviewer/form" />
<c:url var="historyviewer" value="/historyviewer/form" />
<c:url var="configloader" value="/configloader/progress" />
<c:url var="confighistory" value="/confighistory/" />


<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>${title}</title>

<meta name="description" content="">
<meta name="author" content="">

<!-- Bootstrap Core CSS -->
<link href="<c:url value="/css/bootstrap/bootstrap.css"/>" rel="stylesheet" type="text/css" />

<!-- MetisMenu CSS -->
<link href="<c:url value="/css/metis/metisMenu.css"/>" rel="stylesheet" />

<!-- Custom CSS -->
<link href="<c:url value="/css/sb-admin-2.css"/>" rel="stylesheet" />

<!-- Common CSS -->
<link href="<c:url value="/css/common.css"/>" rel="stylesheet" />

<!-- Custom Fonts -->
<link href="<c:url value="/css/fonts/font-awesome/css/font-awesome.css"/>" rel="stylesheet" type="text/css" />

</head>

<body>
  <div id="pageheader">
    <jsp:invoke fragment="header" />
  </div>

  <div id="wrapper">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0; background-color: #F7F7F7;">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#navbar-main">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="index.html">
          C<sup>2</sup>MON Web Interface
        </a>
      </div>
      <!-- /.navbar-header -->

      <ul class="nav navbar-top-links navbar-right">
        <sec:authorize ifNotGranted="ROLE_ANONYMOUS">
          <li class="dropdown">
            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
              <i class="fa fa-user fa-fw"></i>

              ${username}

              <i class="fa fa-caret-down"></i>

            </a>
            <ul class="dropdown-menu dropdown-user" role="menu">
              <li>
                <a href="${logout}">
                  <i class="fa fa-sign-out fa-fw"></i>
                  Logout
                </a>
              </li>
            </ul>
            <!-- /.dropdown-user -->
          </li>
          <!-- /.dropdown -->
        </sec:authorize>
        <sec:authorize ifAllGranted="ROLE_ANONYMOUS">
          <li>
            <a href="${login}">
              <i class="fa fa-sign-out fa-fw"></i>
              Sign In
            </a>
          </li>
        </sec:authorize>
      </ul>
      <!-- /.navbar-top-links -->

      <div class="navbar-default sidebar" role="navigation">
        <div id="navbar-main" class="sidebar-nav navbar-collapse">
          <ul class="nav" id="side-menu">
            <li>
              <a href="${home}">
                <i class="fa fa-dashboard fa-fw"></i>
                Dashboard
              </a>
            </li>
            <li>
              <a href="${tagviewer}">
                <i class="fa fa-tags fa-fw"></i>
                Tag Viewer
              </a>
            </li>
            <li>
              <a href="${alarmviewer}">
                <i class="fa fa-bell fa-fw"></i>
                Alarm Viewer
              </a>
            </li>
            <li>
              <a href="${commandviewer}">
                <i class="fa fa-terminal fa-fw"></i>
                Command Viewer
              </a>
            </li>
            <li>
              <a href="${processviewer}">
                <i class="fa fa-binoculars fa-fw"></i>
                DAQ Process Viewer
              </a>
            </li>
            <li>
              <a href="${trendviewer}">
                <i class="fa fa-line-chart fa-fw"></i>
                Trend Viewer
              </a>
            </li>
            <li>
              <a href="${historyviewer}">
                <i class="fa fa-list fa-fw"></i>
                History Table Viewer
              </a>
            </li>
            <li>
              <a href="${configloader}">
                <i class="fa fa-gears fa-fw"></i>
                Configuration Loader
              </a>
            </li>
            <li>
              <a href="${confighistory}">
                <i class="fa fa-tasks fa-fw"></i>
                Configuration History
              </a>
            </li>
          </ul>
        </div>
        <!-- /.sidebar-collapse -->
      </div>
      <!-- /.navbar-static-side -->
    </nav>

    <div id="page-wrapper">
      <div id="body">

        <!-- Page content goes here -->
        <jsp:doBody />
        <!-- Page content goes here -->

        <div class="row">
          <div class="col-lg-12">
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
      </div>

    </div>
    <!-- /#page-wrapper -->
  </div>
  <!-- /#wrapper -->



  <!-- jQuery -->
  <script src="<c:url value="/js/jquery/jquery.js"/>"></script>

  <!-- Bootstrap Core JavaScript -->
  <script src="<c:url value="/js/bootstrap/bootstrap.js"/>"></script>

  <!-- Metis Menu Plugin JavaScript -->
  <script src="<c:url value="/js/metis/metisMenu.js"/>"></script>

  <!-- Custom Theme JavaScript -->
  <script src="<c:url value="/js/sb-admin-2.js"/>"></script>

  <div id="pagefooter">
    <jsp:invoke fragment="footer" />
  </div>
</body>
</html>