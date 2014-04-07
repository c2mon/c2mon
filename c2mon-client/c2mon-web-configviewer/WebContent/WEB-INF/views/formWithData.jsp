

<html>
<head>
<title>${title}</title>

<link rel="stylesheet" type="text/css" href="../css/bootstrap.css" />
<link rel="stylesheet" type="text/css" href="../css/bootstrap-responsive.css" />

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
    <a href="../">Home</a> <span class="divider">/</span>
    </li>
    <li>${title}</li>
    </ul>
	
    <div class="hero-unit">
	<h1>${title}</h1>
    </div>
      
<div class="alert alert-info">
	<strong>${instruction}</strong>
</div>


<form class="well form-inline" action="" method="post">
		<input class="span2" style="display:inline"  type="text" name="id" value="${formTagValue}" /> 
		<input class="btn btn-large btn-primary" type="submit" value="Submit">
</form>

</div><!--/row-->

      <hr>

      <footer>
        <p>&copy; CERN 2012</p>
      </footer>
    </div><!--/.fluid-container-->
    
        <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="../js/jquery.js"></script>
    <script src="../js/bootstrap-transition.js"></script>
    <script src="../js/bootstrap-alert.js"></script>
    <script src="../js/bootstrap-modal.js"></script>
    <script src="../js/bootstrap-dropdown.js"></script>
    <script src="../js/bootstrap-scrollspy.js"></script>
    <script src="../js/bootstrap-tab.js"></script>
    <script src="../js/bootstrap-tooltip.js"></script>
    <script src="../js/bootstrap-popover.js"></script>
    <script src="../js/bootstrap-button.js"></script>
    <script src="../js/bootstrap-collapse.js"></script>
    <script src="../js/bootstrap-carousel.js"></script>
    <script src="../js/bootstrap-typeahead.js"></script>

</body>
</html>


