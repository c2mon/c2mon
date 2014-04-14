$(document).ready(function(){
	
	if ($('#trend_view').is(':empty')){
		$('#trend_view').append('<h3 style="margin-left:20%;">Records are only kept for the last 30 days</h3>');
	}
	
	
	
	$(".dygraph-legend").css("margin-top","-4%");
	
	location();	
	resizeGraph();	
		
	$(window).resize(function() {
		location();
		resizeGraph();
	});
	
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
	
	function location(){
				
		if(getUrlVars()["MENU"]=="false" && getUrlVars()["TITLE"]=="false"){
			$(".page-header").hide();
			$(".links").hide();
			$(".dygraph-legend").css("margin-top","0%");
		}	
		else if(getUrlVars()["TITLE"]=="false"){
			$(".page-header").hide();
			if(window.innerHeight<600)
				$(".dygraph-legend").css("margin-top","-4%");
			else
				$(".dygraph-legend").css("margin-top","-2%");
			}
		
		else if(getUrlVars()["MENU"]=="false"){
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
	
	
	function resizeGraph(){
		var w=window.innerWidth;
		var h=window.innerHeight;
		document.getElementById("row-fluid").style.height = window.innerHeight;
		if(w<680)
			w=680;
			
		if(h<400)
			h=400;
			
		if(getUrlVars()["MENU"]=="false" && getUrlVars()["TITLE"]=="false"){
			trend.resize(w-130,h-80);
		}
		else if(getUrlVars()["MENU"]=="false" || getUrlVars()["TITLE"]=="false")
			trend.resize(w-130,h-140);
		else{
			trend.resize(w-130,h-220);}
	}

});