#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
#
my $jardir                    = "../lib";
my $appdir                    = "tim2-psaccess-control-stable/";
my $codebase                  = "http://timweb.cern.ch/test/javaws";
my $c2monClientPropertiesURL  = "http://timweb/conf/test/c2mon-client.properties";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

##
# In case of a SNAPSHOT
#
if ($viewerVersion =~ /-SNAPSHOT/) {
  $appdir = "tim2-psaccess-control/";
}

##
# Procedure to generate for each library defined in the ../lib directory
# an entry in the jnlp file.
#
sub jarlist {
  my $dir = shift;
  opendir DIR, $dir or return;
  my @contents = 
        map "$dir/$_", 
        sort grep !/^\.\.?$/,
        readdir DIR;
    closedir DIR;
    foreach (@contents) {
              my $htmldir = $appdir.substr($_, 3, length($_));
        if (!-l && -d) {
      &jarlist($_);
    }
    else {
      if (/tim-psaccess-control/) {
            print "    <jar href=\"", $htmldir, "\" main=\"true\" download=\"eager\"/>\n";
      }
      elsif (/jar$/) {
            print "    <jar href=\"", $htmldir, "\" main=\"false\" download=\"eager\"/>\n";
      }
    }
        next
    }
}





##########################################
#         Generating JNLP file           #
##########################################

print "Content-type: application/x-java-jnlp-file" , "\n\n";
print "<?xml version = '1.0' encoding = 'utf-8'?>
  <jnlp spec=\"1.0+\" codebase=\"$codebase\">
  <information>
    <title>TIM PS Access Control TEST (v$viewerVersion)</title>
    <vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
    <homepage href=\"http://timweb.cern.ch/test\"/>
    <description>This application allows controlling the access keys in the different zones of the PS access complex</description>
    <icon href=\"http://timweb.cern.ch/img/tim-animated-320x200.gif\" kind=\"splash\"/>             
  </information>
  <security> 
    <all-permissions/> 
  </security> 
  <resources>
    <java version=\"1.6+\" initial-heap-size=\"128M\"  max-heap-size=\"128M\"/>" , "\n";

jarlist ("$jardir");

# Defines the version number that is shown in the TIM Viewer about dialog
print "    <property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# Configuration parameters needed by C2MON client API
print "    <property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";
print "  </resources>
  <resources os=\"Windows\" >
    <property name=\"tim.log.file\" value=\"c:\\temp\\tim-psaccess-control-test.log\"/>
  </resources>
  <application-desc main-class=\"cern.tim.client.psaccess.Main\"/>
</jnlp>" , "\n";
