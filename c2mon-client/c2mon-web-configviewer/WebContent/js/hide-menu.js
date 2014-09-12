$(document).ready(function(){
  
	//Hide navigation info
  $(".Phone_Menu").hide();
  $("#hide_links_show_menu").hide();
  //If the div is empty that's mean there is no longer the data
	if ($('#trend_view').is(':empty')){
		$('#trend_view').append('<h3 style="margin-left:20%;">Records are only kept for the last 30 days</h3>');
	}
		
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
      $(".dygraph-legend").css("margin-top","-30px");
      $("#trend_view").css("margin-top","30px");
	
    }
			
		// hide title
		else if(TestUrlTitle()){
			$(".page-header").hide();
      if(window.innerHeight<600)
        $(".dygraph-legend").css("margin-top","-40px");
      else
        $(".dygraph-legend").css("margin-top","-30px"); 
			}
		// hide buttons
		else if(TestUrlMenu()){
			$(".links").hide();
	     if(window.innerHeight<600)
	        $(".dygraph-legend").css("margin-top","-40px");
	      else
	        $(".dygraph-legend").css("margin-top","-30px"); 
		}	
		else
		{
		  if(window.innerHeight<600)
				$(".dygraph-legend").css("margin-top","-40px");
			else
				$(".dygraph-legend").css("margin-top","-30px");			
		}
		
	};
	
	//Resize the graph and set a minimum size
	function resizeGraph(){
		var w=window.innerWidth;
		var h=window.innerHeight;
		document.getElementById("row-fluid").style.height = window.innerHeight;
		if(!TestUrlMenu()){
			if(w<770){
			  //Hide the main menu and display the phone menu
			  $(".links").hide();
			  $("#hide_links_show_menu").show();
			  $("#hide_links_show_menu").toggle(function(){	    
			    $(".Phone_Menu").css({
			      "top":$("#hide_links_show_menu").position().top+35
			    });
			    $(".Phone_Menu").fadeIn();
			  },function(){
			    $(".Phone_Menu").fadeOut();
			  });
		  }
			else
			  {
	         $(".links").show();
	         $(".Phone_Menu").hide();
	         $("#hide_links_show_menu").hide();
			  }
		}
    if(h<200)
      h=200;
		//Resizing the graph
		if(TestUrlMenu() && TestUrlTitle()){
			trend.resize(w-70,h-100);
		}
		else if(TestUrlMenu()){
			trend.resize(w-130,h-180);}
		
    else if(TestUrlTitle()){
      trend.resize(w-100,h-100);}
		else{
		  if($(".links").is(":visible"))
		    { 
		      trend.resize(w-130,h-250);
		    }
		  else{
		    trend.resize(w-130,h-200);
		  }
		}
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