<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9; IE=EmulateIE10"> 
	<title>${title}</title>

	<link rel="shortcut icon" href="/c2mon-web-configviewer/img/chart_icon.png">

	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap.css" />
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/bootstrap-responsive.css" />
	<link rel="stylesheet" type="text/css" href="/c2mon-web-configviewer/css/datepicker.css" >

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

			<div class="alert alert-info">
				<strong>${instruction}</strong>
			</div>

			<c:if test="${error != null}"> 
				<div class="alert alert-error">
					id: <strong>${error}</strong> NOT FOUND....  Sorry : /
				</div>
			</c:if>

			<ul id="tabs" class="nav nav-tabs">
				<li><a href="#records" data-toggle="tab">Last Records</a></li>
				<li><a href="#days" data-toggle="tab">Last Days</a></li>
				<li><a href="#date" data-toggle="tab">From date to date</a></li>
			</ul>
			<div id="tab-content" class="tab-content">
				<div class="tab-pane fade in active" id="records">
					<form class="well form-inline" action="" method="post">
						<div class="input-prepend">
							<span class="add-on">Id</span>
							<input class="span3" style="display:inline" type="text" name="id" value="${formTagValue}" /> 
							<span class="add-on">Records</span>
							<input class="span3" style="display:inline"  type="text" name="records" value="100"/> 
						</div>
						<input class="btn btn-large btn-primary" type="submit" value="Submit">
					</form>
				</div>
				<div class="tab-pane fade" id="days">
					<form class="well form-inline" action="" method="post">
						<div class="input-prepend">
							<span class="add-on">Id</span>
							<input class="span3" style="display:inline" type="text" name="id" value="${formTagValue}" /> 
							<span class="add-on">Days</span>
							<select style="display:inline" name="days">
								<option>1</option>
								<option>2</option>
								<option>3</option>
								<option>4</option>
								<option>5</option>
								<option>6</option>
								<option>7</option>
								<option>8</option>
								<option>9</option>
								<option>10</option>
								<option>11</option>
								<option>12</option>
								<option>13</option>
								<option>14</option>
								<option>15</option>
								<option>16</option>
								<option>17</option>
								<option>18</option>
								<option>19</option>
								<option>20</option>
								<option>21</option>
								<option>22</option>
								<option>23</option>
								<option>24</option>
								<option>25</option>
								<option>26</option>
								<option>27</option>
								<option>28</option>
								<option>29</option>
								<option>30</option>
							</select>
						</div>
						<input class="btn btn-large btn-primary" type="submit" value="Submit">
					</form>
				</div>
				<div class="tab-pane fade" id="date">
					<form class="well form-inline" action="" method="post">
						<div class="input-prepend">
							<span class="add-on">Id</span>
							<input class="span1" style="display:inline" type="text" name="id" value="${formTagValue}" /> 

							<span class="input-daterange" id="datepickerFrom" style="margin-left:10px; margin-right:-10px;">
								<span class="add-on">From</span>
								<input type="text" class="input-small span2" name="start" value="${defaultFromDate}" />
							</span>
							<input type="text" class="input-small span1" name="startTime" value="${defaultFromTime}"/>

							<span class="input-daterange" id="datepickerTo" style="margin-left:10px; margin-right:-10px;">
								<span class="add-on ">To</span>
								<input type="text" class="input-small span2" name="end" value="${defaultToDate}" />
							</span>
							<input type="text" class="input-small span1" name="endTime" value="${defaultToTime}"/>
						</div>
						<input class="btn btn-large btn-primary" type="submit" value="Submit">
					</form>
				</div>
			</div>

		</div><!--/row-->

		<hr>

		<footer>
			<p>&copy; CERN 2012</p>
		</footer>
	</div><!--/.fluid-container-->

        <!-- Le javascript
        ================================================== -->
        <!-- Placed at the end of the document so the pages load faster -->

        <script src="/c2mon-web-configviewer/js/jquery.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-transition.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-alert.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-modal.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-dropdown.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-scrollspy.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-tab.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-tooltip.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-popover.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-button.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-collapse.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-carousel.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-typeahead.js"></script>
        <script src="/c2mon-web-configviewer/js/bootstrap-datepicker.js"></script>

        <script type="text/javascript">

		$('#tabs a:first').tab('show'); // Marks the first tab as selected in the interface 

		// Activates the datepicker plugin
		// https://github.com/eternicode/bootstrap-datepicker
		$('.input-daterange').datepicker({
			todayBtn: true,
			forceParse: false,
			autoclose: true,
			format: "dd/mm/yyyy", // Format should match the format expected in the TrendViewController class
			todayHighlight: true,
			beforeShowDay: function (date){
				if (date.getMonth() == (new Date()).getMonth())
					switch (date.getDate()){
						case 8:
						return false;
						case 12:
						return "green";
					}
				}
			});
		</script>

	</body>
	</html>



