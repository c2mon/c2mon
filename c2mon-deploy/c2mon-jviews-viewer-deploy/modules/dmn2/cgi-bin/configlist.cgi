#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/~alaser/dmn2-jviews-viewer/conf";
my $configdir = "/user/alaser/public_html/dmn2-jviews-viewer/conf";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'DMN2 Viewer', -style=>"/css/tim.css");
print h1('DMN2 Viewer Test: Using the latest SNAPSHOT from BAMBOO and connecting to the C2MON(DMN2) test server');
print p("To launch the DMN2 Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the DMN2 Viewer works correctly only on Technical Network)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "Start the DMN2 Viewer with configuration ";
  print a({-href=>"jnlpgenerator.cgi?configurl=$configurl/$_"}, $fn);
  print "<br>";
  
  next;
}
print end_html;
