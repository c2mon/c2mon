$(document).ready(function(){
  
  if(typeof getUrlVars()["error"] != 'undefined')
  {
    $('#error').append("<p style='color:red;'>Invalid username or password !</p>")
  }
  
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
  
});