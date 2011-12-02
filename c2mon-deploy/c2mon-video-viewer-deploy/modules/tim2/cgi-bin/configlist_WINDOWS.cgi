#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/javaws/tim2-video-viewer/conf/windows";
my $configdir = $ENV{DOCUMENT_ROOT} . $configsubdir;
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'TIM Video Viewer', -style=>"/css/tim.css");
print h1('TIM Video Viewer Configurations (Windows)');
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
print p("To launch the TIM Video Viewer, choose one of the configurations below ",font({-color=>"#FF0000"},"(NOTE: For security reasons, the TIM Video Viewer only works on the Technical Network)"),":");  
print "<UL>";
foreach (@files) {
  my $fn = $_;
  $fn =~ s/.xml//g;
  print "<LI>";
  print a({-href=>"jnlpgenerator.cgi?configurl=$configurl/$_"}, $fn);
  print "</LI>";
  
  next;
}
print "</UL>";
print p("If you need an additional configuration, please contact ", 
       a({-href=>"mailto:tim.support\@cern.ch"}, "tim.support\@cern.ch"),
       ".");
print end_html;
