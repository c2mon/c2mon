$(document).ready(function(){
  
  // Display the error message
  if(typeof getUrlVars()["error"] != 'undefined')
  {
    $('#error-feedback').show();
    $('#username-field').addClass('has-error');
    $('#password-field').addClass('has-error');
  }
  
  // Function to get the parameter in an URL
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