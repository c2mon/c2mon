$(function () { 
	/* set variables locally for increased performance */
	var scroll_timer;
	var displayed = false;
	var $message = $('.message');
	var $window = $(window);
	var top = $(document.body).children(0).position().top;
 
	/* react to scroll event on window */
	$window.scroll(function () {
		window.clearTimeout(scroll_timer);
		scroll_timer = window.setTimeout(function () { // use a timer for performance
			if($window.scrollTop() <= top) // hide if at the top of the page
			{
				displayed = false;
				$message.fadeOut(500);
			}
			else if(displayed == false) // show if scrolling down
			{
				displayed = true;
				$message.stop(true, true).show().click(function () { $message.fadeOut(500); });
			}
		}, 100);
	});
});