$(document).ready(function(){
	
 
  //If the div is empty that's mean there is no longer the data
	if ($('#trend_view').is(':empty')){
		$('#trend_view').append('<h3 style="margin-left:20%;">Records are only kept for the last 30 days</h3>');
	}
	
	
	// Remove the superimposition
	$(".dygraph-legend").css("margin-top","-4%");
	
	location();	
	resizeGraph();	
	
	//If the window is resize, the graph too
	$(window).resize(function() {
		location();
		resizeGraph();
	});
	
	//Function to get the parameter in an URL
	function getUrlVars()
	{
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	}
	
	// Hide and set the margin of the graph
	function location(){	
		if(TestUrlMenu() && TestUrlTitle()){
			$(".page-header").hide();
			$(".links").hide();
			$(".dygraph-legend").css("margin-top","0%");
		}	
		// hide title
		else if(TestUrlTitle()){
			$(".page-header").hide();
			if(window.innerHeight<600)
				$(".dygraph-legend").css("margin-top","-4%");
			else
				$(".dygraph-legend").css("margin-top","-2%");
			}
		// hide buttons
		else if(TestUrlMenu()){
			$(".links").hide();
			if(window.innerHeight<600)
				$(".dygraph-legend").css("margin-top","-4%");
			else
				$(".dygraph-legend").css("margin-top","-2%");
		}	
		else
		{
			if(window.innerHeight<600)
				$(".dygraph-legend").css("margin-top","-4%");
			else
				$(".dygraph-legend").css("margin-top","-2%");			
		}
		
	};
	
	//Resize the graph and set a minimum size
	function resizeGraph(){
		var w=window.innerWidth;
		var h=window.innerHeight;
		document.getElementById("row-fluid").style.height = window.innerHeight;
		if(w<680)
			w=680;
			
		if(h<400)
			h=400;
			
		if(TestUrlMenu() && TestUrlTitle()){
			trend.resize(w-130,h-80);
		}
		else if(TestUrlMenu() || TestUrlTitle()){
			trend.resize(w-130,h-140);}
		else{
			trend.resize(w-130,h-250);}
	}
	
	function TestUrlMenu()
	{
	  if(typeof getUrlVars()["MENU"] == 'undefined')
	    return false;
	  else{
  	  if(getUrlVars()["MENU"].toUpperCase()=="FALSE")
  	    return true;
  	  else
  	    return false;
	  }
	}
	
	 function TestUrlTitle()
	  {
	    if(typeof getUrlVars()["TITLE"] == 'undefined')
	      return false;
	    else{ 
	      if(getUrlVars()["TITLE"].toUpperCase()=="FALSE")
	        return true;
	      else
	        return false;
	    }
	  }

});