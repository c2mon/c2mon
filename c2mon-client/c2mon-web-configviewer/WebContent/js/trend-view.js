$(document).ready(function(){
  $("#popup").hide();
  
  $(".navigation_popup").hover(function(){
    if($(".links").is(":visible")){
      $("#popup").css({
        "left":$(".navigation_popup").position().left,
        "top":$(".navigation_popup").position().top+35
      });
    }
    else
      {
      $("#popup").css({
        "left":$("#hide_links_show_menu").position().left+$(".Phone_Menu").width(),
        "top":$("#hide_links_show_menu").position().top
      });
      }
    $("#popup").show();
  }, 
  function () {
    $("#popup").hide();
  });

});