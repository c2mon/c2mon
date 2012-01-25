#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/~diamondev/dmn2-viewer/conf";
my $configdir = "/user/diamondev/public_html/dmn2-viewer/conf/dev";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

##
# Reading version number from ../version.txt
#
open VFILE, "<../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

print header;
print start_html(-title=>'DMN2 Viewer [DEV]', -style=>"/css/tim.css");
print h1("DMN2 Viewer [DEV] version: $viewerVersion connects to the DMN2 DEV server");
print p("To launch the DMN2 Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the DMN2 Viewer works correctly only on Technical Network)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "Start the DMN2 Viewer with configuration ";
  print a({-href=>"jnlpgenerator-dev.cgi?configurl=$configurl/$_"}, $fn);
  print "<br>";
  
  next;
}
print end_html;
