#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

##
# Default variable definition for production
#
my $configsubdir = "/javaws/tim2-jviews-viewer/conf";
my $configdir = "/user/timoper/dist/public/html/javaws/tim2-jviews-viewer/conf";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

my $title = "TIM2 Viewer (" . $viewerVersion . ") Configurations";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'TIM Viewer', -style=>"/css/tim.css");
print h1($title);
print p("To launch the TIM Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the TIM Viewer works correctly only on Technical Network)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "Start the TIM Viewer with configuration ";
  print a({-href=>"jnlpgenerator-pro.cgi?configurl=$configurl/$_"}, $fn);
  print "<br>";
  
  next;
}
print end_html;
