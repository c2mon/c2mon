#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

##
# Default variable definition for production
#
my $configsubdir = "/test/javaws/tim2-jviews-viewer-stable/conf";
my $configdir = "/user/timtest/dist/public/test/html/javaws/tim2-jviews-viewer-stable/conf";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

##
# In case of a SNAPSHOT we have to change the directory
#
if ($viewerVersion =~ /-SNAPSHOT/) {
  $configsubdir = "/test/javaws/tim2-jviews-viewer/conf";
  $configdir = "/user/timtest/dist/public/test/html/javaws/tim2-jviews-viewer/conf"; 
}

my $title = "TIM Viewer Test (" . $viewerVersion . "): Connecting to the TIM production server";
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'TIM Viewer', -style=>"/css/tim.css");
print h1($title);
print p("<br>To launch the TIM Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: the TIM Viewer works correctly only on <b>Technical Network</b>)"),":"); 
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;

print "<ul>";
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  
  print "<li>Use ";
  print a({-href=>"../bin/tim-viewer-test-$fn.jnlpx"}, "CERN JWS (recommended)");
  print " or ";
  print a({-href=>"../bin/tim-viewer-test-$fn.jnlp"}, "Java Web Start");
  print " to launch TIM Viewer (<b>$fn</b>)</li>";
  
  next;
}
print "</ul>";

print p("<br><br>");
print "To learn more about <b>JWS</b> - a CERN replacement for Java Web Start - please go to the ";
print "<a href=\"https://wikis.cern.ch/display/DVTLS/jws+-+a+replacement+for+javaws\" target=\"_blank\">JMS Wiki page</a>.";

print p("");
print "To launch the TIM Viewer via <b>Java Web Start</b> you have to first add 'http://timweb' to the ";
print "<a href=\"https://www.java.com/en/download/faq/exception_sitelist.xml\" target=\"_blank\">Security Exception List of the Java Control Panel</a>.";

print end_html;
