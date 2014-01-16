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
    <link rel="stylesheet" type="text/css" href="<c:url value="css/bootstrap.css"/>" />
		<link rel="stylesheet" type="text/css" href="<c:url value="css/buttons.css"/>" />
    <link rel="stylesheet" type="text/css" href=" <c:url value="css/bootstrap-responsive.css"/>" />
		
    <style type="text/css">
      body {
        padding-top: 50px;
        padding-bottom: 40px;
				padding-left: 50px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
  </head>

  <body>
  

    <div class="container-fluid">
      <div class="row-fluid">
			
	  
        <div class="span9">
		
	<ul class="breadcrumb">
    <li> Home</li>
    </ul>
		
	<div class="hero-unit">
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
			
          <div class="row-fluid">
            <div class="span4">
              <h2>Tag Viewer</h2>
              <p><a class="btn" href="tagviewer/form">Go! &raquo;</a></p>
            </div><!--/span-->
            <div class="span4">
              <h2>History Viewer</h2>
              <p>
				<a class="btn" href="historyviewer/form">Table &raquo;</a>
				<a class="btn" href="trendviewer/form">Trend &raquo;</a>
			 </p>
            </div><!--/span-->
            <div class="span4">
              <h2>Alarm Viewer</h2>
              <p><a class="btn" href="alarmviewer/form">Go! &raquo;</a></p>
            </div><!--/span-->
          </div><!--/row-->
          <div class="row-fluid">
            <div class="span4">
              <h2>Command Viewer</h2>
              <p><a class="btn" href="commandviewer/form">Go! &raquo;</a></p>
            </div><!--/span-->
            <div class="span4">
              <h2>Config Loader</h2>
              <p><a class="btn" href="configloader/progress">Go! &raquo;</a></p>
            </div><!--/span-->
            <div class="span4">
              <h2>DAQ XML Viewer</h2>
              <p><a class="btn" href="process/form">Go! &raquo;</a></p>
            </div><!--/span-->
          </div><!--/row-->
        </div><!--/span-->
      </div><!--/row-->
	  
			<sec:authorize ifNotGranted="ROLE_ANONYMOUS">
				<div class="alert alert-success">
					<p>You are currently logged in. 
						<a href="/c2mon-web-configviewer/j_spring_security_logout">Logout</a>
    		</div>
   		</sec:authorize>

      <hr>

      <footer>
        <p>&copy; CERN 2012</p>
      </footer>
    </div><!--/.fluid-container-->

</body>
</html>