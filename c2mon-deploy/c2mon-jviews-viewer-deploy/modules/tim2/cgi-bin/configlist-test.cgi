#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/test/javaws/tim2-jviews-viewer/conf";
my $configdir = "/user/timtest/dist/public/test/html/javaws/tim2-jviews-viewer/conf";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'TIM Viewer', -style=>"/css/tim.css");
print h1('TIM2 Viewer Test: Using the latest SNAPSHOT from BAMBOO and connecting to the C2MON(TIM2) test server');
print p("To launch the TIM Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the TIM Viewer works correctly only on Technical Network)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "Start the TIM Viewer with configuration ";
  print a({-href=>"jnlpgenerator-test.cgi?configurl=$configurl/$_"}, $fn);
  print "<br>";
  
  next;
}
print end_html;
