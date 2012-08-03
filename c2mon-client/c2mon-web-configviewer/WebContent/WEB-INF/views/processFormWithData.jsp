<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<title>${title}</title>
<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap.css" />
<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap-responsive.css" />

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
      
    <ul class="breadcrumb">
    <li>
    <a href="/c2mon-web-configviewer">Home</a> <span class="divider">/</span>
    </li>
    <li>${title}</li>
    </ul>
	
    <div class="hero-unit">
	<h1>${title}</h1>
    </div>
      
<c:url var="submitUrl" value="${formSubmitUrl}"/>

<form class="well" action="${submitUrl}" method="post">

         <select name="id">
			<c:forEach items="${processNames}" var="processName">  
				<option>${processName}</option>
			</c:forEach>  
        </select>
	<input class="btn btn-large btn-primary" type="submit" value="Submit">
</form>

<a href="/c2mon-web-configviewer/j_spring_security_logout">Logout</a>
</div><!--/row-->

      <hr>

      <footer>
        <p>&copy; CERN 2012</p>
      </footer>
    </div><!--/.fluid-container-->
    
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="/c2mon-web-configviewer/js/jquery.js"></script>
    <script src="/c2mon-web-configviewer/js/bootstrap-alert.js"></script>
    <script src="/c2mon-web-configviewer/js/bootstrap-dropdown.js"></script>

</body>
</html>


