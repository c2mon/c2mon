#!/usr/bin/perl -wT
use strict;
use CGI qw(:standard);
use CGI::Carp qw(warningsToBrowser fatalsToBrowser);
use Config::Properties;

##
# Definition of global variables
#
my $jardir                    = "../lib";
my $appdir                    = "tim2-video-viewer/";
my $codebase                  = "http://timweb/javaws";
my $c2monClientPropertiesFile = "/user/timoper/rep/c2mon/client/c2mon-client.properties";
my $c2monClientPropertiesURL  = "http://timweb/conf/c2mon-client.properties";

##
# Reading version number from ../version.txt
#
open VFILE, "< ../version.txt"
  or die "Unable to open version file ../version.txt";
my $viewerVersion = <VFILE>;
chomp $viewerVersion; # removes new line character
close VFILE;

# Reading property file $c2monClientPropertiesFile #
open PROPS, "< $c2monClientPropertiesFile"
  or die "Unable to open configuration file $c2monClientPropertiesFile";
my $c2monProperties = new Config::Properties();
$c2monProperties->load(*PROPS);
my $jmsVideoRequestQueue = $c2monProperties->getProperty("c2mon.client.jms.video.request.queue");
close PROPS;


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
      if (/tim-video-viewer/) {
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
    <title>TIM Video Viewer (Version $viewerVersion)</title>
    <vendor>Technical Infrastructure Monitoring (TIM) Team</vendor>
    <homepage href=\"http://timweb.cern.ch\"/>
    <description>Used in CCC to monitor the access to PS and SPS the tunnels</description>
    <icon href=\"http://timweb.cern.ch/img/tim-video-splash.gif\" kind=\"splash\"/>
  </information>
  <security> 
    <all-permissions/> 
  </security> 
  <resources>
    <java version=\"1.6+\" initial-heap-size=\"64M\"  max-heap-size=\"64M\"/>" , "\n";

jarlist ("$jardir");

# Defines the version number that is shown in the TIM Viewer about dialog
print "    <property name=\"tim.version\" value=\"$viewerVersion\"/>\n";
# JMS configuration parameters needed by C2MON client API
print "    <property name=\"c2mon.client.conf.url\" value=\"$c2monClientPropertiesURL\"/>\n";
# JMS configuration parameters needed by TIM Video
print "    <property name=\"c2mon.client.jms.video.request.queue\" value=\"$jmsVideoRequestQueue\"/>\n";

if (param('configurl')) {
	print "    <property name=\"tim.conf.url\" value=\"", param('configurl'), "\"/>", "\n";
}

print "  </resources>
  <application-desc main-class=\"cern.c2mon.client.video.TimVideoViewer\"/>
</jnlp>" , "\n";
