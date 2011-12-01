#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);

my $configsubdir = "/javaws/tim-video-viewer-linux/configFiles";
my $configdir = $ENV{DOCUMENT_ROOT} . $configsubdir;
my $configurl = "http://" . $ENV{'HTTP_HOST'} . $configsubdir;

print header;
print start_html(-title=>'TIM Video Viewer', -style=>"/css/tim.css");
print h1('TIM Video Viewer Configurations (Linux) for VLC version 1.1.4');
opendir DIR, $configdir;
my @files = sort grep !/^\.\.?$/, readdir DIR;
closedir DIR;
print p("To launch the TIM Video Viewer for Linux, choose one of the configurations below. Please note, that you have to install first VLC v1.1.4 on your client machine and be a trusted client host.",font({-color=>"#FF0000"},"(For security reasons, the TIM Video Viewer only works on the Technical Network)"),":");  
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
